package com.github.xnaut97.cosmos.command.syntax;

import com.github.xnaut97.cosmos.command.param.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public final class SyntaxParser {

    private final Map<Class<? extends CommandParam>, CommandParameterResolver<? extends CommandParam>> defaultResolvers;

    public SyntaxParser() {
        Map<Class<? extends CommandParam>, CommandParameterResolver<? extends CommandParam>> resolvers = new LinkedHashMap<>();
        resolvers.put(StringParam.class, new CommandParameterResolver<StringParam>() {
            @Override
            public Object resolve(CommandSender sender, StringParam param, String input) throws SyntaxParseException {
                validate(param, input);
                return input;
            }
        });
        resolvers.put(ArrayParam.class, new CommandParameterResolver<ArrayParam>() {
            @Override
            public Object resolve(CommandSender sender, ArrayParam param, String input) throws SyntaxParseException {
                validate(param, input);
                return input;
            }
        });
        resolvers.put(IntParam.class, new CommandParameterResolver<IntParam>() {
            @Override
            public Object resolve(CommandSender sender, IntParam param, String input) throws SyntaxParseException {
                validate(param, input);
                try {
                    return Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    throw new SyntaxParseException("Cannot parse integer");
                }
            }
        });
        resolvers.put(DoubleParam.class, new CommandParameterResolver<DoubleParam>() {
            @Override
            public Object resolve(CommandSender sender, DoubleParam param, String input) throws SyntaxParseException {
                validate(param, input);
                return Double.parseDouble(input);
            }
        });
        resolvers.put(FloatParam.class, new CommandParameterResolver<FloatParam>() {
            @Override
            public Object resolve(CommandSender sender, FloatParam param, String input) throws SyntaxParseException {
                validate(param, input);
                return Float.parseFloat(input);
            }
        });
        resolvers.put(BooleanParam.class, new CommandParameterResolver<BooleanParam>() {
            @Override
            public Object resolve(CommandSender sender, BooleanParam param, String input) throws SyntaxParseException {
                if ("true".equalsIgnoreCase(input) || "false".equalsIgnoreCase(input)) {
                    return Boolean.parseBoolean(input);
                }
                throw new SyntaxParseException("Cannot parse boolean");
            }
        });
        resolvers.put(PlayerParam.class, new CommandParameterResolver<PlayerParam>() {
            @Override
            public Object resolve(CommandSender sender, PlayerParam param, String input) throws SyntaxParseException {
                Player player = Bukkit.getPlayerExact(input);
                if (player != null) {
                    return player;
                }
                if (param.isIncludeOffline()) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input);
                    if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
                        return offlinePlayer;
                    }
                }
                throw new SyntaxParseException("Player not found");
            }
        });
        this.defaultResolvers = Collections.unmodifiableMap(resolvers);
    }

    public SyntaxMatch match(CommandSender sender, CommandSyntax syntax, String[] args) {
        Map<String, Object> values = new LinkedHashMap<>();
        Map<String, String> rawValues = new LinkedHashMap<>();
        int argIndex = 0;

        for (SyntaxElement element : syntax.getElements()) {
            if (element instanceof SyntaxLiteral) {
                if (argIndex >= args.length) {
                    return null;
                }
                String literal = ((SyntaxLiteral) element).getValue();
                if (!literal.equalsIgnoreCase(args[argIndex])) {
                    return null;
                }
                argIndex++;
                continue;
            }

            SyntaxParameter parameter = (SyntaxParameter) element;
            if (argIndex >= args.length) {
                if (parameter.isOptional()) {
                    continue;
                }
                return null;
            }

            String input = parameter.isGreedy()
                    ? join(args, argIndex, args.length)
                    : args[argIndex];

            try {
                Object value = resolve(sender, syntax, parameter.getParam(), input);
                values.put(parameter.getParam().getName(), value);
                rawValues.put(parameter.getParam().getName(), input);
            } catch (SyntaxParseException e) {
                if (parameter.isOptional()) {
                    continue;
                }
                return null;
            }

            argIndex = parameter.isGreedy() ? args.length : argIndex + 1;
        }

        if (argIndex != args.length) {
            return null;
        }
        return SyntaxMatch.matched(syntax, new ArgumentContext(values, rawValues));
    }

    public SyntaxMatch partial(CommandSender sender, CommandSyntax syntax, String[] args) {
        int argIndex = 0;

        for (SyntaxElement element : syntax.getElements()) {
            if (element instanceof SyntaxLiteral) {
                String literal = ((SyntaxLiteral) element).getValue();
                if (argIndex >= args.length) {
                    return null;
                }
                if (argIndex == args.length - 1) {
                    return literal.startsWith(args[argIndex].toLowerCase(Locale.ROOT)) ? SyntaxMatch.partial(syntax, null, literal) : null;
                }
                if (!literal.equalsIgnoreCase(args[argIndex])) {
                    return null;
                }
                argIndex++;
                continue;
            }

            SyntaxParameter parameter = (SyntaxParameter) element;
            if (argIndex >= args.length) {
                return SyntaxMatch.partial(syntax, parameter.getParam(), "");
            }

            if (argIndex == args.length - 1 || parameter.isGreedy()) {
                String input = parameter.isGreedy()
                        ? join(args, argIndex, args.length)
                        : args[argIndex];
                return SyntaxMatch.partial(syntax, parameter.getParam(), input);
            }

            try {
                resolve(sender, syntax, parameter.getParam(), args[argIndex]);
            } catch (SyntaxParseException e) {
                if (!parameter.isOptional()) {
                    return null;
                }
            }
            argIndex++;
        }

        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object resolve(CommandSender sender, CommandSyntax syntax, CommandParam param, String input) throws SyntaxParseException {
        CommandParameterResolver resolver = findResolver(syntax.getResolvers(), param);
        if (resolver == null) {
            resolver = findResolver(defaultResolvers, param);
        }
        if (resolver == null) {
            validate(param, input);
            return input;
        }
        return resolver.resolve(sender, param, input);
    }

    private CommandParameterResolver<? extends CommandParam> findResolver(
            Map<Class<? extends CommandParam>, CommandParameterResolver<? extends CommandParam>> resolvers,
            CommandParam param) {
        CommandParameterResolver<? extends CommandParam> resolver = resolvers.get(param.getClass());
        if (resolver != null) {
            return resolver;
        }
        for (Map.Entry<Class<? extends CommandParam>, CommandParameterResolver<? extends CommandParam>> entry : resolvers.entrySet()) {
            if (entry.getKey().isAssignableFrom(param.getClass())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static void validate(CommandParam param, String input) throws SyntaxParseException {
        List<String> errors = param.validate(input);
        if (errors != null && !errors.isEmpty()) {
            throw new SyntaxParseException(errors.get(0));
        }
    }

    private static String join(String[] args, int start, int end) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; i++) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }
}
