package com.github.xnaut97.cosmos.utilities.java;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class RandomID {

    private final int length;
    private final List<IDRule> rules = new ArrayList<>();
    private final List<Range> ranges = new ArrayList<>();

    public static RandomID of(int length, IDRule... rules) {
        return of(length).rule(rules);
    }

    public static RandomID of(int length) {
        return new RandomID(length);
    }

    private RandomID(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("ID length must be > 0");
        }
        this.length = length;

        // default rule
        rule(IDRule.LOWERCASE);
    }

    public RandomID rule(IDRule... rules) {
        this.rules.clear();
        this.ranges.clear();

        this.rules.addAll(Arrays.asList(rules));

        for (IDRule rule : rules) {
            switch (rule) {
                case NUMBER:
                    ranges.add(Range.NUMBERS);
                    break;

                case LOWERCASE:
                    ranges.add(Range.LOWERCASE);
                    break;

                case UPPERCASE:
                    ranges.add(Range.UPPERCASE);
                    break;
            }
        }

        return this;
    }

    public String generate() {
        StringBuilder builder = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < length; i++) {
            Range range = ranges.get(random.nextInt(ranges.size()));
            builder.append((char) range.random(random));
        }

        return builder.toString();
    }

    public enum IDRule {
        LOWERCASE,
        UPPERCASE,
        NUMBER
    }

    /**
     * Replacement for IntRange (clean + faster + no Apache dependency)
     */
    private enum Range {

        NUMBERS(48, 57),
        UPPERCASE(65, 90),
        LOWERCASE(97, 122);

        private final int min;
        private final int max;

        Range(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public int random(ThreadLocalRandom random) {
            return random.nextInt(min, max + 1);
        }
    }
}