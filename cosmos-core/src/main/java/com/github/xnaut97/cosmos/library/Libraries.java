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
    COSMOS_CORE(
            "com.github.xnaut97",
            "cosmos-core",
            "1.0.0",
            "cosmos_core_library",
            null
    ),
    COSMOS_MENU(
            "com.github.xnaut97",
            "cosmos-menu",
            "1.0.0",
            "cosmos_menu_library",
            null
    ),
    COSMOS_COMMAND(
            "com.github.xnaut97",
            "cosmos-command",
            "1.0.0",
            "cosmos_command_library",
            null
    ),
    COSMOS_INPUT(
            "com.github.xnaut97",
            "cosmos-input",
            "1.0.0",
            "cosmos_input_library",
            null
    ),
    COSMOS_UTILITIES(
            "com.github.xnaut97",
            "cosmos-utilities",
            "1.0.0",
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
