package com.github.xnaut97.cosmos.command.syntax;

import com.github.xnaut97.cosmos.command.param.CommandParam;
import com.github.xnaut97.cosmos.command.param.ParamPriority;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.*;

public class CommandSyntax {

    private final List<SyntaxElement> elements = new ArrayList<>();
    private final List<SyntaxElement> elementView = Collections.unmodifiableList(elements);
    private final List<CommandParam> params = new ArrayList<>();
    private final List<CommandParam> paramView = Collections.unmodifiableList(params);
    private final Map<Class<? extends CommandParam>, CommandParameterResolver<? extends CommandParam>> resolvers = new LinkedHashMap<>();
    private final Map<Class<? extends CommandParam>, CommandParameterResolver<? extends CommandParam>> resolverView = Collections.unmodifiableMap(resolvers);

    private String description = "";
    private String permission;
    private String permissionDescription = "";
    private PermissionDefault permissionDefault = PermissionDefault.OP;
    private SyntaxExecutor executor;
    private PlayerRequirement requirement;
    private String usage;

    public CommandSyntax literal(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Literal cannot be empty");
        }
        ensureNoGreedyParam();
        elements.add(new SyntaxLiteral(value.toLowerCase(Locale.ROOT)));
        usage = null;
        return this;
    }

    public CommandSyntax param(CommandParam param) {
        return param(param, param.getPriority() == ParamPriority.OPTIONAL, false);
    }

    public CommandSyntax optionalParam(CommandParam param) {
        return param(param, true, false);
    }

    public CommandSyntax greedyParam(CommandParam param) {
        return param(param, param.getPriority() == ParamPriority.OPTIONAL, true);
    }

    public CommandSyntax param(CommandParam param, boolean optional, boolean greedy) {
        if (param == null) {
            throw new IllegalArgumentException("Param cannot be null");
        }
        ensureNoGreedyParam();
        elements.add(new SyntaxParameter(param, optional, greedy));
        params.add(param);
        usage = null;
        return this;
    }

    public <P extends CommandParam> CommandSyntax resolver(Class<P> paramType, CommandParameterResolver<P> resolver) {
        if (paramType == null || resolver == null) {
            throw new IllegalArgumentException("Resolver type and resolver cannot be null");
        }
        resolvers.put(paramType, resolver);
        return this;
    }

    public CommandSyntax executor(SyntaxExecutor executor) {
        this.executor = executor;
        return this;
    }

    public CommandSyntax description(String description) {
        this.description = description == null ? "" : description;
        return this;
    }

    public CommandSyntax permission(String permission) {
        this.permission = permission;
        return this;
    }

    public CommandSyntax permission(String permission, String description, PermissionDefault permissionDefault) {
        this.permission = permission;
        this.permissionDescription = description == null ? "" : description;
        this.permissionDefault = permissionDefault == null ? PermissionDefault.OP : permissionDefault;
        return this;
    }

    public CommandSyntax require(PlayerRequirement requirement) {
        this.requirement = requirement;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public String getPermission() {
        return permission;
    }

    public String getPermissionDescription() {
        return permissionDescription;
    }

    public PermissionDefault getPermissionDefault() {
        return permissionDefault;
    }

    public SyntaxExecutor getExecutor() {
        return executor;
    }

    public PlayerRequirement getRequirement() {
        return requirement;
    }

    List<SyntaxElement> getElements() {
        return elementView;
    }

    Map<Class<? extends CommandParam>, CommandParameterResolver<? extends CommandParam>> getResolvers() {
        return resolverView;
    }

    public List<CommandParam> getParams() {
        return paramView;
    }

    public String getUsage() {
        if (usage != null) {
            return usage;
        }
        List<String> parts = new ArrayList<>();
        for (SyntaxElement element : elements) {
            if (element instanceof SyntaxLiteral) {
                parts.add(((SyntaxLiteral) element).getValue());
            } else if (element instanceof SyntaxParameter) {
                SyntaxParameter parameter = (SyntaxParameter) element;
                CommandParam param = parameter.getParam();
                if (param.getName() == null || param.getName().isEmpty()) {
                    continue;
                }
                String left = parameter.isOptional() ? "{" : "[";
                String right = parameter.isOptional() ? "}" : "]";
                String suffix = parameter.isGreedy() ? "..." : "";
                parts.add(left + param.getName() + suffix + right);
            }
        }
        usage = String.join(" ", parts);
        return usage;
    }

    private void ensureNoGreedyParam() {
        if (elements.isEmpty()) {
            return;
        }
        SyntaxElement last = elements.get(elements.size() - 1);
        if (last instanceof SyntaxParameter && ((SyntaxParameter) last).isGreedy()) {
            throw new IllegalStateException("Greedy parameters must be the final syntax element");
        }
    }

    public interface PlayerRequirement {
        boolean test(Player player);
    }
}
