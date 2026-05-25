package com.github.xnaut97.cosmos.utilities.java;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@UtilityClass
public class JsonUtil {

    /*
     * =========================
     * Gson Instances
     * =========================
     */

    private final Gson GSON = new Gson();

    private final Gson PRETTY_GSON = new GsonBuilder()
            .setPrettyPrinting().create();

    /*
     * =========================
     * Serialization
     * =========================
     */

    /**
     * Serialize object to compact JSON.
     */
    public String toJson(Object object) {

        return GSON.toJson(object);
    }

    /**
     * Serialize object to pretty JSON.
     */
    public String toPrettyJson(Object object) {

        return PRETTY_GSON.toJson(object);
    }

    /*
     * =========================
     * Deserialization
     * =========================
     */

    /**
     * Deserialize JSON to class.
     */
    @Nullable
    public <T> T fromJson(String json,
                          Class<T> type) {

        if (json == null || type == null) {
            return null;
        }

        try {

            return GSON.fromJson(json, type);

        } catch (JsonSyntaxException ignored) {
            return null;
        }
    }

    /**
     * Deserialize JSON using Type.
     */
    @Nullable
    public <T> T fromJson(String json,
                          Type type) {

        if (json == null || type == null) {
            return null;
        }

        try {

            return GSON.fromJson(json, type);

        } catch (JsonSyntaxException ignored) {
            return null;
        }
    }

    /*
     * =========================
     * Collections
     * =========================
     */

    @Nullable
    public <T> List<T> list(String json,
                            Class<T> type) {

        if (json == null || type == null) {
            return null;
        }

        try {

            Type listType = TypeToken
                    .getParameterized(List.class, type)
                    .getType();

            return GSON.fromJson(json, listType);

        } catch (Exception ignored) {
            return null;
        }
    }

    @Nullable
    public <K, V> Map<K, V> map(String json,
                                Class<K> keyType,
                                Class<V> valueType) {

        if (json == null
                || keyType == null
                || valueType == null) {

            return null;
        }

        try {

            Type mapType = TypeToken
                    .getParameterized(
                            Map.class,
                            keyType,
                            valueType
                    )
                    .getType();

            return GSON.fromJson(json, mapType);

        } catch (Exception ignored) {
            return null;
        }
    }

    /*
     * =========================
     * Validation
     * =========================
     */

    public boolean isJson(String json) {

        if (json == null || json.trim().isEmpty()) {
            return false;
        }

        try {

            GSON.fromJson(json, Object.class);

            return true;

        } catch (Exception ignored) {
            return false;
        }
    }

    /*
     * =========================
     * Access Gson
     * =========================
     */

    public Gson gson() {

        return GSON;
    }

    public Gson prettyGson() {

        return PRETTY_GSON;
    }

}