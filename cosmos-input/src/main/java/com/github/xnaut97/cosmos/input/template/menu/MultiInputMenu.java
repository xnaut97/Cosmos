package com.github.xnaut97.cosmos.input.template.menu;

import com.cryptomorin.xseries.XMaterial;
import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.utilities.item.ItemCreator;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Setter
@Accessors(fluent = true)
public class MultiInputMenu extends Menu {

    private int rows = 6;

    private int exitSlot = 45;
    private int previousPageSlot = 48;
    private int infoSlot = 49;
    private int confirmSlot = 50;
    private int nextPageSlot = 53;

    private List<Integer> inputSlots = new ArrayList<>();

    private ItemStack backgroundItem = createPane(Material.GRAY_STAINED_GLASS_PANE, " ");
    private ItemStack activeBackgroundItem = createPane(Material.GREEN_STAINED_GLASS_PANE, " ");

    private Consumer<List<ItemStack>> onAutoSave = items -> {};

    private BiConsumer<Player, List<ItemStack>> onConfirm = (player, items) -> {};
    private BiConsumer<Player, List<ItemStack>> onInputChange = (player, items) -> {};
    private BiConsumer<Player, Integer> onPageChange = (player, page) -> {};
    private BiConsumer<Player, ItemStack> onInvalidItem = (player, item) -> {};

    private Predicate<ItemStack> validator = item -> true;

    private int maxItems = Integer.MAX_VALUE;

    private boolean readonly = false;

    private final List<ItemStack> submittedItems = new ArrayList<>();

    private int currentPage = 0;

    public MultiInputMenu(Plugin plugin, String title) {
        super(plugin, 6, title);
    }

    @Override
    protected void setup() {
        configureInputFilters();
        refresh();
    }

    @Override
    protected void afterAllowedDrag(InventoryDragEvent event) {

        for (Integer rawSlot : event.getRawSlots()) {

            if (!inputSlots.contains(rawSlot)) {
                continue;
            }

            Bukkit.getScheduler().runTask(getPlugin(), () -> {

                ItemStack item = getInventory().getItem(rawSlot);

                updateSubmittedItem(rawSlot, item);
            });
        }
    }

    public MultiInputMenu fillInputSlots(int startRow,
                                         int endRow,
                                         int startColumn,
                                         int endColumn) {

        inputSlots.clear();

        for (int row = startRow; row <= endRow; row++) {

            for (int column = startColumn; column <= endColumn; column++) {

                int slot = row * 9 + column;

                inputSlots.add(slot);
            }
        }

        return this;
    }

    public MultiInputMenu fillInputRows(int startRow, int endRow) {

        inputSlots.clear();

        for (int row = startRow; row <= endRow; row++) {

            for (int column = 0; column < 9; column++) {

                inputSlots.add(row * 9 + column);
            }
        }

        return this;
    }

    public MultiInputMenu fillInputBorderless(int startRow, int endRow) {

        inputSlots.clear();

        for (int row = startRow; row <= endRow; row++) {

            for (int column = 1; column <= 7; column++) {

                inputSlots.add(row * 9 + column);
            }
        }

        return this;
    }

    public MultiInputMenu fillInputRectangle(int startSlot,
                                             int width,
                                             int height) {

        inputSlots.clear();

        int startRow = startSlot / 9;
        int startColumn = startSlot % 9;

        for (int row = 0; row < height; row++) {

            for (int column = 0; column < width; column++) {

                int slot = (startRow + row) * 9 + (startColumn + column);

                inputSlots.add(slot);
            }
        }

        return this;
    }

    private void refresh() {

        clearItems();

        setBackground();

        renderInputs();

        renderControls();
    }

    private void renderInputs() {

        int start = currentPage * inputSlots.size();

        for (int i = 0; i < inputSlots.size(); i++) {

            int inventorySlot = inputSlots.get(i);
            int itemIndex = start + i;

            ItemStack item = itemIndex < submittedItems.size()
                    ? submittedItems.get(itemIndex)
                    : null;

            setItem(inventorySlot, item == null
                    ? new ItemStack(Material.AIR)
                    : item)
                    .onClick(event -> handleInputClick(event, inventorySlot));
        }

        updateBackgroundState();
    }

    private void handleInputClick(InventoryClickEvent event,
                                  int inventorySlot) {

        if (readonly) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(false);

        Bukkit.getScheduler().runTask(getPlugin(), () -> {

            ItemStack current = getInventory().getItem(inventorySlot);

            updateSubmittedItem(inventorySlot, current);
        });
    }

    private void renderControls() {

        setItem(exitSlot,
                new ItemCreator(Objects.requireNonNull(XMaterial.BARRIER.parseItem()))
                        .setDisplayName("&cExit")
                        .build())
                .onClick(event -> {
                    event.setCancelled(true);
                    getPlayer().closeInventory();
                });

        setItem(confirmSlot,
                new ItemCreator(Objects.requireNonNull(XMaterial.EMERALD.parseItem()))
                        .setDisplayName("&aConfirm")
                        .build())
                .onClick(event -> {
                    event.setCancelled(true);

                    onConfirm.accept(
                            getPlayer(),
                            Collections.unmodifiableList(submittedItems)
                    );
                });

        setItem(previousPageSlot,
                new ItemCreator(Objects.requireNonNull(XMaterial.ARROW.parseItem()))
                        .setDisplayName("&ePrevious Page")
                        .build())
                .onClick(event -> {
                    event.setCancelled(true);

                    if (currentPage <= 0) {
                        return;
                    }

                    currentPage--;

                    refresh();

                    onPageChange.accept(getPlayer(), currentPage);
                });

        setItem(nextPageSlot,
                new ItemCreator(Objects.requireNonNull(XMaterial.ARROW.parseItem()))
                        .setDisplayName("&eNext Page")
                        .build())
                .onClick(event -> {
                    event.setCancelled(true);

                    currentPage++;

                    refresh();

                    onPageChange.accept(getPlayer(), currentPage);
                });
    }

    private void updateBackgroundState() {

        boolean active = !submittedItems.isEmpty();

        ItemStack background = active
                ? activeBackgroundItem
                : backgroundItem;

        for (int i = 0; i < getInventory().getSize(); i++) {

            if (inputSlots.contains(i)
                    || i == exitSlot
                    || i == previousPageSlot
                    || i == confirmSlot
                    || i == nextPageSlot) {
                continue;
            }

            renderSlot(i, background);
        }
    }

    private void setBackground() {

        for (int i = 0; i < getInventory().getSize(); i++) {

            if (inputSlots.contains(i)
                    || i == exitSlot
                    || i == previousPageSlot
                    || i == confirmSlot
                    || i == nextPageSlot) {
                continue;
            }

            setItem(i, backgroundItem)
                    .onClick(event -> event.setCancelled(true));
        }
    }

    public List<ItemStack> getSubmittedItems() {
        return Collections.unmodifiableList(submittedItems);
    }

    @Override
    public boolean canDragInto(int slot) {
        return !readonly && super.canDragInto(slot);
    }

    @Override
    public boolean canPlace(int slot, ItemStack item) {
        return !readonly && super.canPlace(slot, item);
    }

    @Override
    public boolean canTake(int slot) {
        return !readonly && super.canTake(slot);
    }

    private ItemStack createPane(Material material, String name) {
        return new ItemCreator(new ItemStack(material))
                .setDisplayName(name)
                .build();
    }

    private int getMaxPage() {

        if (submittedItems.isEmpty()) {
            return 0;
        }

        return (submittedItems.size() - 1) / inputSlots.size();
    }

    private void compactItems() {

        submittedItems.removeIf(item ->
                item == null || item.getType() == Material.AIR);
    }

    private void updateSubmittedItem(int inventorySlot, ItemStack item) {

        int relativeIndex = inputSlots.indexOf(inventorySlot);

        if (relativeIndex == -1) {
            return;
        }

        int itemIndex = currentPage * inputSlots.size() + relativeIndex;

        if (item == null || item.getType() == Material.AIR) {

            if (itemIndex < submittedItems.size()) {
                submittedItems.remove(itemIndex);
            }

            compactItems();

            refresh();

            return;
        }

        if (!validator.test(item)) {

            renderSlot(inventorySlot, null);

            onInvalidItem.accept(getPlayer(), item);

            return;
        }

        if (itemIndex >= submittedItems.size()
                && submittedItems.size() >= maxItems) {

            renderSlot(inventorySlot, null);

            return;
        }

        while (submittedItems.size() <= itemIndex) {
            submittedItems.add(null);
        }

        submittedItems.set(itemIndex, item.clone());

        compactItems();

        updateBackgroundState();

        onInputChange.accept(getPlayer(),
                Collections.unmodifiableList(submittedItems));

        Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () ->
                onAutoSave.accept(
                        Collections.unmodifiableList(submittedItems)
                ));
    }

    private void validatePageBounds() {

        currentPage = Math.max(0,
                Math.min(currentPage, getMaxPage()));
    }

    private void nextPage() {

        int nextStart = (currentPage + 1) * inputSlots.size();

        if (nextStart > submittedItems.size()) {
            return;
        }

        currentPage++;

        validatePageBounds();

        refresh();

        onPageChange.accept(getPlayer(), currentPage);
    }

    private void previousPage() {

        if (currentPage <= 0) {
            return;
        }

        currentPage--;

        validatePageBounds();

        refresh();

        onPageChange.accept(getPlayer(), currentPage);
    }

    private void renderPageInfo() {

        setItem(infoSlot,
                new ItemCreator(new ItemStack(Material.BOOK))
                        .setDisplayName("&ePage " + (currentPage + 1))
                        .setLore(
                                "&7Items: &f" + submittedItems.size(),
                                "&7Capacity: &f" + maxItems
                        )
                        .build())
                .onClick(event -> event.setCancelled(true));
    }

    @Override
    protected void afterShiftClickPlaced(Collection<Integer> changedSlots) {
        for (Integer slot : changedSlots) {
            if (inputSlots.contains(slot)) {
                updateSubmittedItem(slot, getInventory().getItem(slot));
            }
        }
    }

    private void configureInputFilters() {
        for (Integer slot : inputSlots) {
            allowItemInput(slot);
        }
    }

}
