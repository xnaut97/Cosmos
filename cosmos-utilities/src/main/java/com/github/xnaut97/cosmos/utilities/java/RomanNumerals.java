package com.github.xnaut97.cosmos.utilities.java;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

public final class RomanNumerals {

    private RomanNumerals() {
    }

    @Getter
    private enum RomanNumber {

        M("M", 1000),
        CM("CM", 900),
        D("D", 500),
        CD("CD", 400),
        C("C", 100),
        XC("XC", 90),
        L("L", 50),
        XL("XL", 40),
        X("X", 10),
        IX("IX", 9),
        V("V", 5),
        IV("IV", 4),
        I("I", 1);

        private final String symbol;
        private final int value;

        RomanNumber(String symbol, int value) {
            this.symbol = symbol;
            this.value = value;
        }
    }

    public static String toRomanNumeral(int value) {
        Validate.isTrue(value > 0,
                "Roman numerals cannot represent zero or negative numbers");

        StringBuilder builder = new StringBuilder();

        for (RomanNumber numeral : RomanNumber.values()) {
            while (value >= numeral.value) {
                value -= numeral.value;
                builder.append(numeral.symbol);
            }
        }

        return builder.toString();
    }

    public static int fromRomanNumeral(String roman) {
        if (roman == null || roman.isEmpty()) {
            return 0;
        }

        roman = roman.toUpperCase();

        int result = 0;
        int previous = 0;

        for (int i = roman.length() - 1; i >= 0; i--) {
            int current = getNumeralValue(roman.charAt(i));

            if (current == 0) {
                return 0;
            }

            if (current < previous) {
                result -= current;
            } else {
                result += current;
            }

            previous = current;
        }

        return result;
    }

    private static int getNumeralValue(char c) {
        switch (c) {
            case 'I':
                return 1;

            case 'V':
                return 5;

            case 'X':
                return 10;

            case 'L':
                return 50;

            case 'C':
                return 100;

            case 'D':
                return 500;

            case 'M':
                return 1000;

            default:
                return 0;
        }
    }
}