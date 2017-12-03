/*******************************************************************************
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v. 1.0
 * which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * Roman Grigoriadi
 ******************************************************************************/

package org.eclipse.yasson.internal.serializer;

import org.eclipse.yasson.internal.model.JsonBindingModel;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;

/**
 * Deserializer for {@link Date} type.
 *
 * @author David Kral
 */
public class DateTypeDeserializer extends AbstractDateTimeDeserializer<Date> {

    private static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME.withZone(UTC);
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ISO_DATE.withZone(UTC);

    /**
     * Creates a new instance.
     *
     * @param model Binding model.
     */
    public DateTypeDeserializer(JsonBindingModel model) {
        super(Date.class, model);
    }

    @Override
    protected Date fromInstant(Instant instant) {
        return new Date(instant.toEpochMilli());
    }

    @Override
    protected Date parseDefault(String jsonValue, Locale locale) {
    	if(jsonValue.contains("T") || jsonValue.contains("t")) {
    		// If time information is present, parse as ISO_DATE_TIME
    		final TemporalAccessor parsed = DEFAULT_DATE_TIME_FORMATTER.withLocale(locale).parse(jsonValue);
    		return new Date(Instant.from(parsed).toEpochMilli());
    	} else {
    		// If no time info is present, parse as ISO_DATE
    		final LocalDateTime ldt = LocalDate.parse(jsonValue, DEFAULT_DATE_FORMATTER.withLocale(locale)).atStartOfDay();
    		return new Date(ldt.toInstant(ZoneOffset.UTC).toEpochMilli());
    	}
    }

    @Override
    protected Date parseWithFormatter(String jsonValue, DateTimeFormatter formatter) {
        final TemporalAccessor parsed = getZonedFormatter(formatter).parse(jsonValue);
        return new Date(Instant.from(parsed).toEpochMilli());
    }
}
