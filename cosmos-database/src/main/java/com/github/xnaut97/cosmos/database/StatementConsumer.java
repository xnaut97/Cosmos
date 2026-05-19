package com.github.xnaut97.cosmos.database;

import java.sql.PreparedStatement;

public interface StatementConsumer {
    void accept(PreparedStatement statement) throws Exception;
}