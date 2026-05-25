package com.github.xnaut97.cosmos.utilities.bar;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class ProgressBossBar {

    /*
     * Runtime bossbar
     */
    private final BossBar bossBar;

    /*
     * Cached state
     */
    private String title;

    private BarColor color;

    private BarStyle style;

    private double progress;

    private boolean visible;

    private boolean deleted;

    public ProgressBossBar() {
        this(
                "",
                BarColor.WHITE,
                BarStyle.SOLID
        );
    }

    public ProgressBossBar(String title) {
        this(
                title,
                BarColor.WHITE,
                BarStyle.SOLID
        );
    }

    public ProgressBossBar(String title,
                           BarColor color,
                           BarStyle style,
                           BarFlag... flags) {

        this.title = color(title);

        this.color = color == null
                ? BarColor.WHITE
                : color;

        this.style = style == null
                ? BarStyle.SOLID
                : style;

        this.progress = 0D;

        this.visible = true;

        this.bossBar = Bukkit.createBossBar(
                this.title,
                this.color,
                this.style,
                flags == null
                        ? new BarFlag[0]
                        : flags
        );

        this.bossBar.setVisible(true);

        update();
    }

    /*
     * =========================
     * Realtime Update
     * =========================
     */

    public ProgressBossBar title(String title) {

        this.title = color(title);

        update();

        return this;
    }

    public ProgressBossBar color(BarColor color) {

        if (color == null) {
            return this;
        }

        this.color = color;

        update();

        return this;
    }

    public ProgressBossBar style(BarStyle style) {

        if (style == null) {
            return this;
        }

        this.style = style;

        update();

        return this;
    }

    public ProgressBossBar progress(double progress) {

        this.progress = clamp(progress);

        update();

        return this;
    }

    public ProgressBossBar visible(boolean visible) {

        this.visible = visible;

        update();

        return this;
    }

    /*
     * =========================
     * Progress Utilities
     * =========================
     */

    public ProgressBossBar addProgress(double percent) {

        return progress(
                this.progress +
                        (percent / 100D)
        );
    }

    public ProgressBossBar removeProgress(double percent) {

        return progress(
                this.progress -
                        (percent / 100D)
        );
    }

    public ProgressBossBar clearProgress() {

        return progress(0D);
    }

    public ProgressBossBar maxProgress() {

        return progress(1D);
    }

    public ProgressBossBar complete(Runnable runnable) {

        maxProgress();

        if (runnable != null) {
            runnable.run();
        }

        return this;
    }

    /*
     * =========================
     * Player Management
     * =========================
     */

    public ProgressBossBar show(Player player) {

        if (deleted || player == null) {
            return this;
        }

        if (!bossBar.getPlayers().contains(player)) {
            bossBar.addPlayer(player);
        }

        return this;
    }

    public ProgressBossBar hide(Player player) {

        if (deleted || player == null) {
            return this;
        }

        bossBar.removePlayer(player);

        return this;
    }

    public ProgressBossBar clearPlayers() {

        if (!deleted) {
            bossBar.removeAll();
        }

        return this;
    }

    public boolean isVisible(Player player) {

        return !deleted
                && visible
                && bossBar.getPlayers().contains(player);
    }

    /*
     * =========================
     * Flag Management
     * =========================
     */

    public ProgressBossBar addFlags(BarFlag... flags) {

        if (deleted || flags == null) {
            return this;
        }

        Arrays.stream(flags)
                .filter(flag -> !bossBar.hasFlag(flag))
                .forEach(bossBar::addFlag);

        return this;
    }

    public ProgressBossBar removeFlags(BarFlag... flags) {

        if (deleted || flags == null) {
            return this;
        }

        Arrays.stream(flags)
                .filter(bossBar::hasFlag)
                .forEach(bossBar::removeFlag);

        return this;
    }

    /*
     * =========================
     * Internal Update
     * =========================
     */

    private void update() {

        if (deleted) {
            return;
        }

        bossBar.setTitle(title);
        bossBar.setColor(color);
        bossBar.setStyle(style);
        bossBar.setProgress(progress);
        bossBar.setVisible(visible);
    }

    /*
     * =========================
     * Delete
     * =========================
     */

    public void delete() {

        if (deleted) {
            return;
        }

        bossBar.removeAll();
        bossBar.setVisible(false);

        deleted = true;
    }

    /*
     * =========================
     * Utility
     * =========================
     */

    private double clamp(double value) {

        return Math.max(
                0D,
                Math.min(1D, value)
        );
    }

    private String color(String text) {

        return text == null
                ? ""
                : text.replace('&', '§');
    }

}