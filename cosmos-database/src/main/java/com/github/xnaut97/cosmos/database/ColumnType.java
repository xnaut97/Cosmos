package com.github.xnaut97.cosmos.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ColumnType {

    CHAR("CHAR(1)"),
    VARCHAR("VARCHAR(255)"),
    TEXT("TEXT"),
    LONG_TEXT("LONGTEXT"),

    BOOLEAN("BOOLEAN"),

    INT("INT"),
    BIG_INT("BIGINT"),

    FLOAT("FLOAT"),
    DOUBLE("DOUBLE"),
    DECIMAL("DECIMAL(10,2)");

    private final String sql;
}