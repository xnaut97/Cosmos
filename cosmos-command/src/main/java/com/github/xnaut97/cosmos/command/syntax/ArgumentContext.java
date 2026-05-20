package com.github.xnaut97.cosmos.command.syntax;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ArgumentContext {

    private final Map<String, Object> values;
    private final Map<String, String> rawValues;

    ArgumentContext(Map<String, Object> values, Map<String, String> rawValues) {
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
        this.rawValues = Collections.unmodifiableMap(new LinkedHashMap<>(rawValues));
    }

    public boolean has(String name) {
        return values.containsKey(name);
    }

    public Object get(String name) {
        return values.get(name);
    }

    public <T> T get(String name, Class<T> type) {
        Object value = values.get(name);
        if (!type.isInstance(value)) {
            return null;
        }
        return type.cast(value);
    }

    public String getString(String name) {
        Object value = values.get(name);
        return value == null ? null : String.valueOf(value);
    }

    public Integer getInt(String name) {
        return get(name, Integer.class);
    }

    public Double getDouble(String name) {
        return get(name, Double.class);
    }

    public Float getFloat(String name) {
        return get(name, Float.class);
    }

    public Boolean getBoolean(String name) {
        return get(name, Boolean.class);
    }

    public String getRaw(String name) {
        return rawValues.get(name);
    }

    public Map<String, Object> asMap() {
        return values;
    }

    public Map<String, String> rawMap() {
        return rawValues;
    }
}
