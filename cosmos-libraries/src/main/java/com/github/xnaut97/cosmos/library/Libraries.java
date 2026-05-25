package com.github.xnaut97.cosmos.library;

import lombok.Getter;

@Getter
public enum Libraries implements LibraryObject {
    LOMBOK(
            "org.projectlombok",
            "lombok",
            "1.18.42",
            "lombok_library",
            null
    ),
    NBT_API(
            "de.tr7zw",
            "item-nbt-api-plugin",
            "2.15.7",
            "nbt_api_library",
            "https://repo.codemc.io/repository/maven-public/"
    ),

    HIKARI_CP(
            "com.zaxxer",
            "HikariCP",
            "7.0.2",
            "hikari_cp_library",
            null
    ),
    XSERIES(
            "com.github.cryptomorin",
            "XSeries",
            "13.6.0",
            "xseries_library",
            null
    ),
    MONGO_DB(
            "org.mongodb",
            "mongodb-driver-sync",
            "5.5.1",
            "mongodb_library",
            null
    ),
    COSMOS_CORE(
            "io.gitlab.xnaut97",
            "cosmos-core",
            "1.0.1c",
            "cosmos_core_library",
            null
    ),
    COSMOS_DATABASE(
            "io.gitlab.xnaut97",
            "cosmos-database",
            "1.0.1c",
            "cosmos_database_library",
            null
    ),
    COSMOS_MENU(
            "io.gitlab.xnaut97",
            "cosmos-menu",
            "1.0.1c",
            "cosmos_menu_library",
            null
    ),
    COSMOS_COMMAND(
            "io.gitlab.xnaut97",
            "cosmos-command",
            "1.0.1c",
            "cosmos_command_library",
            null
    ),
    COSMOS_INPUT(
            "io.gitlab.xnaut97",
            "cosmos-input",
            "1.0.1c",
            "cosmos_input_library",
            null
    ),
    COSMOS_UTILITIES(
            "io.gitlab.xnaut97",
            "cosmos-utilities",
            "1.0.1c",
            "cosmos_utilities_library",
            null
    ),
    ;

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String id;
    private final String repository;

    Libraries(String groupId, String artifactId, String version, String id, String repository) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.id = id;
        this.repository = repository;
    }

}

