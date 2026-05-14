package com.github.xnaut97.cosmos.menu.state;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
public class MenuState {

    private boolean loading;
    private boolean readonly;
    private boolean valid = true;
    private String validationMessage;
    private ItemStack loadingPlaceholder;

    private final Set<Integer> selectedSlots = new LinkedHashSet<>();
    private final List<ItemStack> selectedItems = new ArrayList<>();
    private final Map<String, Object> attributes = new HashMap<>();

    public MenuState loading(boolean loading) {
        this.loading = loading;
        return this;
    }

    public MenuState readonly(boolean readonly) {
        this.readonly = readonly;
        return this;
    }

    public MenuState valid(boolean valid) {
        this.valid = valid;
        return this;
    }

    public MenuState validationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
        return this;
    }

    public MenuState loadingPlaceholder(ItemStack loadingPlaceholder) {
        this.loadingPlaceholder = loadingPlaceholder;
        return this;
    }

}
