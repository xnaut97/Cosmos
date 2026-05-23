package com.github.xnaut97.cosmos.database.consumer;

import java.sql.PreparedStatement;

public interface StatementConsumer {
    void accept(PreparedStatement statement) throws Exception;
}