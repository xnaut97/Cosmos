package com.github.xnaut97.cosmos.database;

import lombok.Getter;

@Getter
public class DatabaseColumn {

    private final String name;
    private final ColumnType type;

    private final boolean nullable;
    private final boolean primaryKey;

    public DatabaseColumn(
            String name,
            ColumnType type,
            boolean nullable,
            boolean primaryKey
    ) {
        this.name = name;
        this.type = type;
        this.nullable = nullable;
        this.primaryKey = primaryKey;
    }
}