package com.github.xnaut97.cosmos.database.consumer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface ResultSetConsumer {
    void accept(ResultSet statement) throws Exception;

}
