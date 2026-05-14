package com.github.xnaut97.cosmos.command.param;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Accessors(chain = true)
public class StringParam extends CommandParam {

    private boolean allowSpecialCharacter;
    private final Set<String> allowedCharacters = new HashSet<>();
    private TextStyle style = TextStyle.NONE;

    public StringParam(String name, String description) {
        super(name, description, ParamType.STRING);
    }

    /**
     * Add allowed characters: e.g. addAllowedChars("-", "_")
     */
    public StringParam addAllowedChars(String... chars) {
        for (String c : chars) {
            if (c != null && c.length() == 1) {
                allowedCharacters.add(c);
            }
        }
        return this;
    }

    @Override
    protected List<String> test(String value) {

        // No special char allowed at all
        if (!allowSpecialCharacter) {
            if (value.matches(".*[^a-zA-Z0-9 ].*"))
                return Lists.newArrayList("&cMust not contain special characters.");
            return null;
        }

        List<String> blackList = new ArrayList<>();

        for (String c : value.split("")) {
            if (c.isEmpty()) continue;

            boolean isSpecial = c.matches(".*[^a-zA-Z0-9 ].*");
            if (isSpecial && !allowedCharacters.contains(c)) {
                blackList.add(c);
            }
        }

        if (!blackList.isEmpty()) {
            String chars = blackList.stream().distinct().collect(Collectors.joining(", "));
            return Lists.newArrayList("&cCharacter '&6" + chars + "&c' is not allowed.");
        }
        return null;
    }


    public enum TextStyle {
        NONE,
        UNDERLINE,
        ITALIC,
        STRIKETHROUGH,
        BOLD,
        MATRIX;

        public String getCode() {
            switch (this) {
                case UNDERLINE:
                    return "§n";
                case ITALIC:
                    return "§o";
                case STRIKETHROUGH:
                    return "§m";
                case BOLD:
                    return "§b";
                case MATRIX:
                    return "§k";
                default:
                    return "";
            }
        }
    }
}
