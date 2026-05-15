package com.github.xnaut97.cosmos.input;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class InputPipeline<T> {

    private final InputParser<T> parser;
    private final List<InputValidator<T>> validators = new ArrayList<>();

    private Consumer<Player> onError;

    public InputPipeline(InputParser<T> parser) {
        this.parser = parser;
    }

    public InputPipeline<T> validate(InputValidator<T> validator) {
        validators.add(validator);
        return this;
    }

    public InputPipeline<T> validate(
            BiPredicate<Player, T> predicate,
            BiConsumer<Player, T> onFail
    ) {
        validators.add(new SimpleValidator<>(predicate, onFail));
        return this;
    }

    public InputPipeline<T> onError(Consumer<Player> onFail) {
        this.onError = onFail;
        return this;
    }

    public T process(Player player, String input) {
        T value;

        try {
            value = parser.parse(input);
        } catch (Exception e) {
            if (onError != null)
                onError.accept(player);
            return null;
        }

        for (InputValidator<T> validator : validators) {
            if (!validator.test(player, value)) {
                validator.onFail(player, value);
                return null;
            }
        }

        return value;
    }
}