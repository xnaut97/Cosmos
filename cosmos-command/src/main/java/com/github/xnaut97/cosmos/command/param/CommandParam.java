package com.github.xnaut97.cosmos.command.param;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Accessors(chain = true)
public abstract class CommandParam {

    private final String name;

    private final String description;

    private final ParamType type;

    private ParamPriority priority = ParamPriority.PRIMARY;

    private List<String> placeholders = new ArrayList<>();

    public CommandParam(String name, String description, ParamType type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    public CommandParam setPlaceholders(String... placeholders) {
        this.placeholders = Arrays.asList(placeholders);
        return this;
    }

    protected abstract List<String> test(String value);

    public List<String> apply(String value) {
        if (value == null || value.isEmpty()) {
            return placeholders.stream()
                    .map(s -> s.replace("&", "§"))
                    .collect(Collectors.toList());
        }

        List<String> testResult = test(value);
        if (testResult != null) {
            return testResult.stream()
                    .map(s -> s.replace("&", "§"))
                    .collect(Collectors.toList());
        }


        return placeholders.stream()
                .filter(s -> s.startsWith(value))
                .collect(Collectors.toList());
    }

}
