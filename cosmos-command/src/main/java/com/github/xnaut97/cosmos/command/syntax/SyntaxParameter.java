package com.github.xnaut97.cosmos.command.syntax;

import com.github.xnaut97.cosmos.command.param.CommandParam;

final class SyntaxParameter extends SyntaxElement {

    private final CommandParam param;
    private final boolean optional;
    private final boolean greedy;

    SyntaxParameter(CommandParam param, boolean optional, boolean greedy) {
        this.param = param;
        this.optional = optional;
        this.greedy = greedy;
    }

    CommandParam getParam() {
        return param;
    }

    boolean isOptional() {
        return optional;
    }

    boolean isGreedy() {
        return greedy;
    }
}
