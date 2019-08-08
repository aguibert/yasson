package org.eclipse.yasson.jmh.model;

import java.util.Map;

public class MapsData {

    private Map<Integer, String> integerStringMap;

    private Map<Country, String> countryStringMap;

    public Map<Integer, String> getIntegerStringMap() {
        return integerStringMap;
    }

    public void setIntegerStringMap(Map<Integer, String> integerStringMap) {
        this.integerStringMap = integerStringMap;
    }

    public Map<Country, String> getCountryStringMap() {
        return countryStringMap;
    }

    public void setCountryStringMap(Map<Country, String> countryStringMap) {
        this.countryStringMap = countryStringMap;
    }

}
