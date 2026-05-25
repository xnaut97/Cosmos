package com.github.xnaut97.cosmos.utilities.java;

import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

@UtilityClass
public class ParserUtil {

    /*
     * =========================
     * String
     * =========================
     */

    @Nullable
    public String string(Object value) {

        return value == null
                ? null
                : String.valueOf(value);
    }

    public String string(Object value,
                         String defaultValue) {

        String parsed = string(value);

        return parsed == null
                ? defaultValue
                : parsed;
    }

    /*
     * =========================
     * Integer
     * =========================
     */

    @Nullable
    public Integer integer(String value) {

        try {

            return value == null
                    ? null
                    : Integer.parseInt(value);

        } catch (Exception ignored) {
            return null;
        }
    }

    public int integer(String value,
                       int defaultValue) {

        Integer parsed = integer(value);

        return parsed == null
                ? defaultValue
                : parsed;
    }

    /*
     * =========================
     * Long
     * =========================
     */

    @Nullable
    public Long longValue(String value) {

        try {

            return value == null
                    ? null
                    : Long.parseLong(value);

        } catch (Exception ignored) {
            return null;
        }
    }

    public long longValue(String value,
                          long defaultValue) {

        Long parsed = longValue(value);

        return parsed == null
                ? defaultValue
                : parsed;
    }

    /*
     * =========================
     * Double
     * =========================
     */

    @Nullable
    public Double doubleValue(String value) {

        try {

            return value == null
                    ? null
                    : Double.parseDouble(value);

        } catch (Exception ignored) {
            return null;
        }
    }

    public double doubleValue(String value,
                              double defaultValue) {

        Double parsed = doubleValue(value);

        return parsed == null
                ? defaultValue
                : parsed;
    }

    /*
     * =========================
     * Float
     * =========================
     */

    @Nullable
    public Float floatValue(String value) {

        try {

            return value == null
                    ? null
                    : Float.parseFloat(value);

        } catch (Exception ignored) {
            return null;
        }
    }

    public float floatValue(String value,
                            float defaultValue) {

        Float parsed = floatValue(value);

        return parsed == null
                ? defaultValue
                : parsed;
    }

    /*
     * =========================
     * Short
     * =========================
     */

    @Nullable
    public Short shortValue(String value) {

        try {

            return value == null
                    ? null
                    : Short.parseShort(value);

        } catch (Exception ignored) {
            return null;
        }
    }

    /*
     * =========================
     * Byte
     * =========================
     */

    @Nullable
    public Byte byteValue(String value) {

        try {

            return value == null
                    ? null
                    : Byte.parseByte(value);

        } catch (Exception ignored) {
            return null;
        }
    }

    /*
     * =========================
     * Boolean
     * =========================
     */

    @Nullable
    public Boolean bool(String value) {

        if (value == null) {
            return null;
        }

        switch (value.toLowerCase()) {

            case "true":
            case "yes":
            case "y":
            case "on":
            case "1":
                return true;

            case "false":
            case "no":
            case "n":
            case "off":
            case "0":
                return false;

            default:
                return null;
        }
    }

    public boolean bool(String value,
                        boolean defaultValue) {

        Boolean parsed = bool(value);

        return parsed == null
                ? defaultValue
                : parsed;
    }

    /*
     * =========================
     * BigDecimal
     * =========================
     */

    @Nullable
    public BigDecimal decimal(String value) {

        try {

            return value == null
                    ? null
                    : new BigDecimal(value);

        } catch (Exception ignored) {
            return null;
        }
    }

    /*
     * =========================
     * BigInteger
     * =========================
     */

    @Nullable
    public BigInteger bigInteger(String value) {

        try {

            return value == null
                    ? null
                    : new BigInteger(value);

        } catch (Exception ignored) {
            return null;
        }
    }

    /*
     * =========================
     * UUID
     * =========================
     */

    @Nullable
    public UUID uuid(String value) {

        try {

            return value == null
                    ? null
                    : UUID.fromString(value);

        } catch (Exception ignored) {
            return null;
        }
    }

    /*
     * =========================
     * Enum
     * =========================
     */

    @Nullable
    public <T extends Enum<T>> T enumValue(Class<T> type,
                                           String value) {

        if (type == null || value == null) {
            return null;
        }

        try {

            return Enum.valueOf(
                    type,
                    value.toUpperCase()
            );

        } catch (Exception ignored) {
            return null;
        }
    }

    public <T extends Enum<T>> T enumValue(Class<T> type,
                                           String value,
                                           T defaultValue) {

        T parsed = enumValue(type, value);

        return parsed == null
                ? defaultValue
                : parsed;
    }

    /*
     * =========================
     * Validation
     * =========================
     */

    public boolean isInteger(String value) {

        return integer(value) != null;
    }

    public boolean isDouble(String value) {

        return doubleValue(value) != null;
    }

    public boolean isBoolean(String value) {

        return bool(value) != null;
    }

    public boolean isUUID(String value) {

        return uuid(value) != null;
    }

}