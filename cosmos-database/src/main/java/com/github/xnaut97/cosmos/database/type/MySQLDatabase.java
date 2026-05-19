package com.github.xnaut97.cosmos.database.type;

import com.github.xnaut97.cosmos.database.Database;
import com.github.xnaut97.cosmos.database.credentials.DatabaseCredentials;
import com.github.xnaut97.cosmos.database.dialect.MySQLDialect;
import org.bukkit.plugin.Plugin;

public class MySQLDatabase extends Database {

    public MySQLDatabase(
            Plugin plugin,
            DatabaseCredentials credentials
    ) {
        super(plugin, credentials, new MySQLDialect());
    }
}