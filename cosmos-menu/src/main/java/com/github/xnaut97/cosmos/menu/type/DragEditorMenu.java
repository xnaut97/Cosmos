package com.github.xnaut97.cosmos.menu.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.component.InputComponent;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

public class DragEditorMenu extends Menu {

    private final Set<Integer> editSlots = new LinkedHashSet<>();
    private Predicate<ItemStack> itemValidator = item -> true;
    private BiPredicate<Integer, ItemStack> slotValidator = (slot, item) -> true;
    private IntPredicate draggableRule = slot -> true;
    private IntPredicate takeableRule = slot -> true;
    private BiConsumer<Integer, ItemStack> changeHandler = (slot, item) -> {};
    private boolean initialized;

    public DragEditorMenu(Plugin plugin, int rows, String title) {
        super(plugin, rows, title);
    }

    @Override
    protected void setup() {
        if (initialized) {
            return;
        }
        initialized = true;
        int[] slots = editSlots.stream().mapToInt(Integer::intValue).toArray();
        addComponent(new InputComponent(slots)
                .validator(item -> itemValidator.test(item))
                .onChange((slot, item) -> {
                    if (!slotValidator.test(slot, item)) {
                        renderSlot(slot, null);
                        return;
                    }
                    changeHandler.accept(slot, item);
                }));
    }

    public DragEditorMenu editableSlots(Collection<Integer> slots) {
        editSlots.clear();
        if (slots != null) {
            editSlots.addAll(slots);
        }
        return this;
    }

    public DragEditorMenu editableRectangle(int startSlot, int width, int height) {
        return editableSlots(rectangle(startSlot, width, height));
    }

    public DragEditorMenu itemValidator(Predicate<ItemStack> itemValidator) {
        this.itemValidator = itemValidator == null ? item -> true : itemValidator;
        return this;
    }

    public DragEditorMenu slotValidator(BiPredicate<Integer, ItemStack> slotValidator) {
        this.slotValidator = slotValidator == null ? (slot, item) -> true : slotValidator;
        return this;
    }

    public DragEditorMenu draggableRule(IntPredicate draggableRule) {
        this.draggableRule = draggableRule == null ? slot -> true : draggableRule;
        return this;
    }

    public DragEditorMenu takeableRule(IntPredicate takeableRule) {
        this.takeableRule = takeableRule == null ? slot -> true : takeableRule;
        return this;
    }

    public DragEditorMenu onChange(BiConsumer<Integer, ItemStack> changeHandler) {
        this.changeHandler = changeHandler == null ? (slot, item) -> {} : changeHandler;
        return this;
    }

    @Override
    public boolean canDragInto(int slot) {
        return editSlots.contains(slot) && draggableRule.test(slot) && super.canDragInto(slot);
    }

    @Override
    public boolean canPlace(int slot, ItemStack item) {
        return editSlots.contains(slot) && slotValidator.test(slot, item) && super.canPlace(slot, item);
    }

    @Override
    public boolean canTake(int slot) {
        return editSlots.contains(slot) && takeableRule.test(slot) && super.canTake(slot);
    }

    @Override
    protected void afterAllowedDrag(InventoryDragEvent event) {
        for (Integer slot : event.getRawSlots()) {
            if (editSlots.contains(slot)) {
                Bukkit.getScheduler().runTask(getPlugin(), () -> changeHandler.accept(slot, getInventory().getItem(slot)));
            }
        }
    }
}
