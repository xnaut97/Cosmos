package com.github.xnaut97.cosmos.input;

import org.bukkit.entity.Player;

public interface InputValidator<T> {

    /**
     * @return true if valid, false otherwise
     */
    boolean test(Player player, T value);

    /**
     * Called when validation fails
     */
    default void onFail(Player player, T value) {}
}