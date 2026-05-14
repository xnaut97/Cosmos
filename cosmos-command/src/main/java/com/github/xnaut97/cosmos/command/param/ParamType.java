package com.github.xnaut97.cosmos.command.param;

public enum ParamType {
    ARRAY,
    BOOLEAN,
    DOUBLE,
    FLOAT,
    INT,
    PLAYER,
    STRING;

    public String lowercase() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
