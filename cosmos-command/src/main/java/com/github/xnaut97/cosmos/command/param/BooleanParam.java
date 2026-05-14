package com.github.xnaut97.cosmos.command.param;

import com.google.common.collect.Lists;

import java.util.List;

public class BooleanParam extends CommandParam {
    public BooleanParam(String placeholder, String description) {
        super(placeholder, description, ParamType.BOOLEAN);
    }

    @Override
    public List<String> test(String value) {
       try {
           double bool = Double.parseDouble(value);
           return null;
       }catch (Exception e) {
           return Lists.newArrayList(value + " is not a boolean value");
       }
    }
}
