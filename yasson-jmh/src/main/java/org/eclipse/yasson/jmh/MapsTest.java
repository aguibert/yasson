package org.eclipse.yasson.jmh;

import org.eclipse.yasson.jmh.model.Country;
import org.eclipse.yasson.jmh.model.MapsData;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Timeout;
import org.openjdk.jmh.annotations.Warmup;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Timeout(time = 20)
@State(Scope.Benchmark)
@Warmup(iterations = 3)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class MapsTest {

    private MapsData data;

    private Jsonb jsonb;

    private String json;

    @Setup(Level.Trial)
    public void setUp() {
        data = new MapsData();
        data.setCountryStringMap(new HashMap<>());
        data.setIntegerStringMap(new HashMap<>());
        Country[] countries = Country.values();
        for (int i = 0; i < 50; i++) {
            data.getCountryStringMap().put(countries[i % countries.length], "strValue" + i);
            data.getIntegerStringMap().put(i, "strValue" + i);
        }
        jsonb = JsonbBuilder.create();
        json = "{\"countryStringMap\":{\"BI\":\"strValue36\",\"BR\":\"strValue31\",\"BT\":\"strValue25\",\"IO\":\"strValue32\",\"CO\":\"strValue48\",\"AU\":\"strValue13\",\"BJ\":\"strValue23\",\"BF\":\"strValue35\",\"AD\":\"strValue5\",\"BS\":\"strValue16\",\"CA\":\"strValue39\",\"BH\":\"strValue17\",\"BQ\":\"strValue27\",\"KY\":\"strValue41\",\"AL\":\"strValue2\",\"AI\":\"strValue7\",\"CC\":\"strValue47\",\"AM\":\"strValue11\",\"BA\":\"strValue28\",\"AF\":\"strValue0\",\"AG\":\"strValue9\",\"CV\":\"strValue40\",\"AO\":\"strValue6\",\"KH\":\"strValue37\",\"CM\":\"strValue38\",\"KM\":\"strValue49\",\"BD\":\"strValue18\",\"BZ\":\"strValue22\",\"AZ\":\"strValue15\",\"CX\":\"strValue46\",\"BG\":\"strValue34\",\"AT\":\"strValue14\",\"AS\":\"strValue4\",\"AR\":\"strValue10\",\"BW\":\"strValue29\",\"CF\":\"strValue42\",\"BN\":\"strValue33\",\"BO\":\"strValue26\",\"AQ\":\"strValue8\",\"BE\":\"strValue21\",\"DZ\":\"strValue3\",\"TD\":\"strValue43\",\"BV\":\"strValue30\",\"CN\":\"strValue45\",\"BY\":\"strValue20\",\"CL\":\"strValue44\",\"BB\":\"strValue19\",\"AW\":\"strValue12\",\"AX\":\"strValue1\",\"BM\":\"strValue24\"},\"integerStringMap\":{\"0\":\"strValue0\",\"1\":\"strValue1\",\"2\":\"strValue2\",\"3\":\"strValue3\",\"4\":\"strValue4\",\"5\":\"strValue5\",\"6\":\"strValue6\",\"7\":\"strValue7\",\"8\":\"strValue8\",\"9\":\"strValue9\",\"10\":\"strValue10\",\"11\":\"strValue11\",\"12\":\"strValue12\",\"13\":\"strValue13\",\"14\":\"strValue14\",\"15\":\"strValue15\",\"16\":\"strValue16\",\"17\":\"strValue17\",\"18\":\"strValue18\",\"19\":\"strValue19\",\"20\":\"strValue20\",\"21\":\"strValue21\",\"22\":\"strValue22\",\"23\":\"strValue23\",\"24\":\"strValue24\",\"25\":\"strValue25\",\"26\":\"strValue26\",\"27\":\"strValue27\",\"28\":\"strValue28\",\"29\":\"strValue29\",\"30\":\"strValue30\",\"31\":\"strValue31\",\"32\":\"strValue32\",\"33\":\"strValue33\",\"34\":\"strValue34\",\"35\":\"strValue35\",\"36\":\"strValue36\",\"37\":\"strValue37\",\"38\":\"strValue38\",\"39\":\"strValue39\",\"40\":\"strValue40\",\"41\":\"strValue41\",\"42\":\"strValue42\",\"43\":\"strValue43\",\"44\":\"strValue44\",\"45\":\"strValue45\",\"46\":\"strValue46\",\"47\":\"strValue47\",\"48\":\"strValue48\",\"49\":\"strValue49\"}}";
    }

    @Benchmark
    public String testSerialize() {
        return jsonb.toJson(data);
    }

    @Benchmark
    public MapsData testDeserialize() {
        return jsonb.fromJson(json, MapsData.class);
    }
}
