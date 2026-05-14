package com.github.xnaut97.cosmos.command.param;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(fluent = true)
public class ArrayParam extends CommandParam{

    private int minLength, maxLength;

    private boolean allowSpecialCharacter;

    public ArrayParam(String placeholder, String description) {
        super(placeholder, description, ParamType.ARRAY);
    }

    @Override
    public List<String> test(String value) {
        String result = "";
        if(value.length() < minLength)
            result = "&cLength must greater or equal than " + minLength;
        else if(value.length() > maxLength)
            result = "&cLength must lower or equal than " + maxLength;
        else if(!allowSpecialCharacter && value.matches(".*[^a-zA-Z0-9 ].*"))
            result = "&cMust not contains special characters.";
        return Lists.newArrayList(result);
    }
}
