package com.github.xnaut97.cosmos.input;

import org.bukkit.entity.Player;

import java.util.function.BiPredicate;

public final class Validators {

    public static BiPredicate<Player, Integer> noNegative() {
        return (player, value) -> value > 0;
    }

    public static BiPredicate<Player, Integer> greaterThan(int min) {
        return (player, value) -> value > min;
    }

    public static BiPredicate<Player, Integer> lowerThan(int max) {
        return (player, value) -> value > max;
    }

    public static BiPredicate<Player, Integer> range(int min, int max) {
        return (player, value) -> value >= min && value <= max;
    }

    public static BiPredicate<Player, Double> range(double min, double max) {
        return (player, value) -> value >= min && value <= max;
    }

    public static BiPredicate<Player, Float> range(float min, float max) {
        return (player, value) -> value >= min && value <= max;
    }

    public static BiPredicate<Player, String> length(int min, int max) {
        return (player, s) -> s.length() >= min && s.length() <= max;
    }

    public static InputValidator<String> notEmpty() {
        return (player, value) -> !value.trim().isEmpty();
    }

    public static BiPredicate<Player, String> noWhiteSpace() {
        return (player, value) -> value != null && !value.matches(".*\\s.*");
    }

    public static InputValidator<String> noSpecialChars() {
        return (player, value) -> value != null && value.matches("^[a-zA-Z0-9_]+$");
    }

    public static <T> InputValidator<T> alwaysTrue() {
        return (p, v) -> true;
    }
}