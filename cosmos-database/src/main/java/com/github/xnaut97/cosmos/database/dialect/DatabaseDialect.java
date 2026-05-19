package com.github.xnaut97.cosmos.database.dialect;

import com.github.xnaut97.cosmos.database.ColumnType;
import com.github.xnaut97.cosmos.database.credentials.DatabaseCredentials;

public interface DatabaseDialect {

    String buildJdbcUrl(DatabaseCredentials credentials);

    String getType(ColumnType type);
}