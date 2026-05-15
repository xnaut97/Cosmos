package com.github.xnaut97.cosmos.input;

import org.bukkit.entity.Player;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class SimpleValidator<T> implements InputValidator<T> {

    private final BiPredicate<Player, T> predicate;
    private final BiConsumer<Player, T> onFail;

    public SimpleValidator(BiPredicate<Player, T> predicate,
                           BiConsumer<Player, T> onFail) {
        this.predicate = predicate;
        this.onFail = onFail;
    }

    @Override
    public boolean test(Player player, T value) {
        return predicate.test(player, value);
    }

    @Override
    public void onFail(Player player, T value) {
        if (onFail != null)
            onFail.accept(player, value);
    }
}