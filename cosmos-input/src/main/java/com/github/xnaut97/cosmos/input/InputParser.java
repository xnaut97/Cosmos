package com.github.xnaut97.cosmos.input;

public interface InputParser<T> {
    T parse(String input) throws Exception;
}