package com.github.xnaut97.cosmos.utilities.title;

public enum AnimationType {

    STATIC {
        @Override
        public String render(String text, int tick) {
            return text;
        }
    },

    TYPEWRITER {
        @Override
        public String render(String text, int tick) {

            if (text == null || text.isEmpty()) {
                return "";
            }

            int length =
                    Math.min(text.length(), tick + 1);

            return text.substring(0, length);
        }
    },

    BLINK {
        @Override
        public String render(String text, int tick) {

            return tick % 20 < 10
                    ? text
                    : "";
        }
    },

    SCROLL {
        @Override
        public String render(String text, int tick) {

            if (text == null || text.isEmpty()) {
                return "";
            }

            int index =
                    tick % text.length();

            return text.substring(index)
                    + " "
                    + text.substring(0, index);
        }
    },

    WAVE {
        @Override
        public String render(String text, int tick) {

            if (text == null) {
                return "";
            }

            StringBuilder builder =
                    new StringBuilder();

            for (int i = 0; i < text.length(); i++) {

                char c = text.charAt(i);

                if ((i + tick) % 2 == 0) {
                    builder.append(Character.toUpperCase(c));
                } else {
                    builder.append(Character.toLowerCase(c));
                }
            }

            return builder.toString();
        }
    };

    public abstract String render(String text,
                                  int tick);

}