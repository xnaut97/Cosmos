package com.github.xnaut97.cosmos.command.syntax;

final class SyntaxLiteral extends SyntaxElement {

    private final String value;

    SyntaxLiteral(String value) {
        this.value = value;
    }

    String getValue() {
        return value;
    }
}
