/*******************************************************************************
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * Roman Grigoriadi
 * Sebastien Rius
 ******************************************************************************/
package org.eclipse.yasson.internal.serializer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.json.bind.JsonbException;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;

import org.eclipse.yasson.internal.JsonbParser;
import org.eclipse.yasson.internal.JsonbRiParser;
import org.eclipse.yasson.internal.ReflectionUtils;
import org.eclipse.yasson.internal.Unmarshaller;
import org.eclipse.yasson.internal.properties.MessageKeys;
import org.eclipse.yasson.internal.properties.Messages;

/**
 * Item implementation for {@link java.util.Map} fields.
 * According to JSON specification object can have only string keys, given that maps could only be parsed
 * from JSON objects, implementation is bound to String type.
 * We will also support Enum keys as well, which goes beyond the JSON-B 1.0 spec
 *
 * @author Roman Grigoriadi
 */
public class MapDeserializer<T extends Map<?,?>> extends AbstractContainerDeserializer<T> implements EmbeddedItem {

    private final Type mapKeyRuntimeType;
    private final Type mapValueRuntimeType;
    private final T instance;
    private static final Map<Class<?>, Optional<Method>> stringableMethodCache = new IdentityHashMap<>();

    /**
     * Create instance of current item with its builder.
     *
     * @param builder {@link DeserializerBuilder} used to build this instance
     */
    protected MapDeserializer(DeserializerBuilder builder) {
        super(builder);
        mapKeyRuntimeType = getRuntimeType() instanceof ParameterizedType ?
                ReflectionUtils.resolveType(this, ((ParameterizedType) getRuntimeType()).getActualTypeArguments()[0])
                : String.class;
        mapValueRuntimeType = getRuntimeType() instanceof ParameterizedType ?
                ReflectionUtils.resolveType(this, ((ParameterizedType) getRuntimeType()).getActualTypeArguments()[1])
                : Object.class;
        this.instance = createInstance(builder);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private T createInstance(DeserializerBuilder builder) {
        Class<?> rawType = ReflectionUtils.getRawType(getRuntimeType());
        if (rawType.isInterface()) {
            return (T) getMapImpl(rawType, builder);
        } else if (EnumMap.class.isAssignableFrom(rawType)) {
            return (T) new EnumMap<>((Class<Enum>) mapKeyRuntimeType);
        } else {
            return (T) builder.getJsonbContext().getInstanceCreator().createInstance(rawType);
        }
    }

    private Map<?,?> getMapImpl(Class<?> ifcType, DeserializerBuilder builder) {
        // SortedMap, NavigableMap
        if (SortedMap.class.isAssignableFrom(ifcType)) {
            Class<?> defaultMapImplType = builder.getJsonbContext().getConfigProperties().getDefaultMapImplType();
            return SortedMap.class.isAssignableFrom(defaultMapImplType) ?
                    (Map<?,?>) builder.getJsonbContext().getInstanceCreator().createInstance(defaultMapImplType) :
                    new TreeMap<>();
        }
        return new HashMap<>();
    }

    @Override
    public T getInstance(Unmarshaller unmarshaller) {
        return instance;
    }

    @Override
    public void appendResult(Object result) {
        appendCaptor(parserContext.getLastKeyName(), convertNullToOptionalEmpty(mapValueRuntimeType, result));
    }

    @SuppressWarnings({ "unchecked" })
    private <V> void appendCaptor(String key, V value) {
        // First check if the key is String/Object type
        if (mapKeyRuntimeType == String.class || mapKeyRuntimeType == Object.class) {
            ((Map<String, V>) getInstance(null)).put(key, value);
            return;
        }
        // If the map key is non-String, check to see if the class has a well known "stringable"
        // method such as X.valueOf(String s) or X.fromString(String s)
        Object valueAsObject = asStringable((Class<?>) mapKeyRuntimeType, key);
        if (valueAsObject != null) {
            ((Map<Object, V>) getInstance(null)).put(valueAsObject, value);
        } else {
            // If the key type is not a String and is not stringable, force it to be a String
            // In the future, we may support user-defined adapters here for more complex map keys
            ((Map<String, V>) getInstance(null)).put(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    private <K> K asStringable(Class<K> clazz, String value) {
        Optional<Method> method = stringableMethodCache.computeIfAbsent(clazz, c -> {
            Method valueOf = getStaticPublicMethod(c, "valueOf");
            if (valueOf != null) {
                return Optional.of(valueOf);
            }
            Method fromString = getStaticPublicMethod(c, "fromString");
            if (fromString != null) {
                return Optional.of(fromString);
            }
            return Optional.empty();
        });

        if (method.isPresent()) {
            try {
                return (K) method.get().invoke(null, value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new JsonbException(Messages.getMessage(MessageKeys.DESERIALIZE_VALUE_ERROR, value, clazz), e);
            }
        }
        return null;
    }

    private Method getStaticPublicMethod(Class<?> clazz, String name) {
        try {
            Method method = clazz.getMethod(name, String.class);
            if (Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {
                method.setAccessible(true);
                return method;
            }
        } catch (NoSuchMethodException ignore) {
        }
        return null;
    }

    @Override
    protected void deserializeNext(JsonParser parser, Unmarshaller context) {
        final JsonbDeserializer<?> deserializer = newCollectionOrMapItem(mapValueRuntimeType, context.getJsonbContext());
        appendResult(deserializer.deserialize(parser, context, mapValueRuntimeType));
    }

    @Override
    protected JsonbRiParser.LevelContext moveToFirst(JsonbParser parser) {
        parser.moveTo(JsonParser.Event.START_OBJECT);
        return parser.getCurrentLevel();
    }
}
