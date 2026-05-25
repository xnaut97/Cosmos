package com.github.xnaut97.cosmos.menu.type;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.Animation;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.BiConsumer;

public class SkillTreeMenu extends Menu {

    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final Set<String> unlocked = new LinkedHashSet<>();
    private BiConsumer<Player, Node> unlockHandler = (player, node) -> {};
    private ItemStack dependencyItem = createButton(Material.GRAY_STAINED_GLASS_PANE, "&8Dependency", null, false);

    public SkillTreeMenu(Plugin plugin, int rows, String title) {
        super(plugin, rows, title);
    }

    @Override
    protected void setup() {
        renderNodes();
    }

    public SkillTreeMenu node(Node node) {
        if (node != null) {
            nodes.put(node.getId(), node);
        }
        return this;
    }

    public SkillTreeMenu unlocked(String id) {
        if (id != null) {
            unlocked.add(id);
        }
        return this;
    }

    public SkillTreeMenu dependencyItem(ItemStack dependencyItem) {
        this.dependencyItem = dependencyItem;
        return this;
    }

    public SkillTreeMenu onUnlock(BiConsumer<Player, Node> unlockHandler) {
        this.unlockHandler = unlockHandler == null ? (player, node) -> {} : unlockHandler;
        return this;
    }

    public SkillTreeMenu progressionAnimation(Animation animation) {
        if (animation != null) {
            registerAnimation(animation);
        }
        return this;
    }

    public boolean isUnlocked(String id) {
        return unlocked.contains(id);
    }

    private void renderNodes() {
        for (Node node : nodes.values()) {
            renderDependencyHints(node);
            setItem(node.getSlot(), itemFor(node))
                    .onClick(event -> {
                        event.setCancelled(true);
                        if (!canUnlock(node)) {
                            return;
                        }
                        unlocked.add(node.getId());
                        unlockHandler.accept((Player) event.getWhoClicked(), node);
                        renderComponents();
                    });
        }
    }

    private void renderDependencyHints(Node node) {
        for (Integer slot : node.getDependencySlots()) {
            if (slot >= 0 && slot < getSize()) {
                setItem(slot, dependencyItem).onClick(event -> event.setCancelled(true));
            }
        }
    }

    private ItemStack itemFor(Node node) {
        if (unlocked.contains(node.getId())) {
            return node.getUnlockedItem();
        }
        if (canUnlock(node)) {
            return node.getAvailableItem();
        }
        return node.getLockedItem();
    }

    private boolean canUnlock(Node node) {
        if (unlocked.contains(node.getId())) {
            return false;
        }
        for (String dependency : node.getDependencies()) {
            if (!unlocked.contains(dependency)) {
                return false;
            }
        }
        return true;
    }

    @Getter
    public static class Node {
        private final String id;
        private final int slot;
        private final ItemStack lockedItem;
        private final ItemStack availableItem;
        private final ItemStack unlockedItem;
        private final List<String> dependencies = new ArrayList<>();
        private final List<Integer> dependencySlots = new ArrayList<>();

        public Node(String id, int slot, ItemStack lockedItem, ItemStack availableItem, ItemStack unlockedItem) {
            this.id = id;
            this.slot = slot;
            this.lockedItem = lockedItem;
            this.availableItem = availableItem;
            this.unlockedItem = unlockedItem;
        }

        public Node dependsOn(String id) {
            if (id != null) {
                dependencies.add(id);
            }
            return this;
        }

        public Node dependencySlot(int slot) {
            dependencySlots.add(slot);
            return this;
        }
    }
}
