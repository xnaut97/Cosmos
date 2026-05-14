package com.github.xnaut97.cosmos.command.param;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(fluent = true)
public abstract class NumberParam extends CommandParam {

    private boolean allowNegative = true;

    public NumberParam(String name, String description, ParamType type) {
        super(name, description, type);
    }


    @Override
    public List<String> test(String value) {
        try {
            double parse = Double.parseDouble(value);
            if(!allowNegative() && parse < 0)
                return Lists.newArrayList("&cNumber must greater than 0");
            return null;
        }catch (Exception e) {
            return Lists.newArrayList("&cCannot parse number with value " + value);
        }
    }
}
