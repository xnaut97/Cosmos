package com.github.xnaut97.cosmos.command.syntax;

import com.github.xnaut97.cosmos.command.param.CommandParam;

public final class SyntaxMatch {

    private final CommandSyntax syntax;
    private final ArgumentContext context;
    private final CommandParam activeParam;
    private final String activeInput;
    private final boolean matched;

    private SyntaxMatch(CommandSyntax syntax, ArgumentContext context, CommandParam activeParam, String activeInput, boolean matched) {
        this.syntax = syntax;
        this.context = context;
        this.activeParam = activeParam;
        this.activeInput = activeInput;
        this.matched = matched;
    }

    static SyntaxMatch matched(CommandSyntax syntax, ArgumentContext context) {
        return new SyntaxMatch(syntax, context, null, null, true);
    }

    static SyntaxMatch partial(CommandSyntax syntax, CommandParam activeParam, String activeInput) {
        return new SyntaxMatch(syntax, null, activeParam, activeInput, false);
    }

    public CommandSyntax getSyntax() {
        return syntax;
    }

    public ArgumentContext getContext() {
        return context;
    }

    public CommandParam getActiveParam() {
        return activeParam;
    }

    public String getActiveInput() {
        return activeInput;
    }

    public boolean isMatched() {
        return matched;
    }
}
