package com.github.xnaut97.cosmos.input;

public final class Parsers {

    public static final InputParser<String> STRING = input -> input;

    public static final InputParser<Integer> INTEGER = Integer::parseInt;

    public static final InputParser<Float> FLOAT = Float::parseFloat;

    public static final InputParser<Double> DOUBLE = Double::parseDouble;

    public static final InputParser<Boolean> BOOLEAN = input -> {
        if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("yes")) return true;
        if (input.equalsIgnoreCase("false") || input.equalsIgnoreCase("no")) return false;
        throw new IllegalArgumentException("Invalid boolean");
    };
}