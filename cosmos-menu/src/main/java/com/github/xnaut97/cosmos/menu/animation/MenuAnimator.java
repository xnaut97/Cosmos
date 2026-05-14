package com.github.xnaut97.cosmos.menu.animation;

import com.github.xnaut97.cosmos.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class MenuAnimator {

    private final Menu<?> menu;
    private final Map<String, Animation> animations = new LinkedHashMap<>();
    private final Map<String, BukkitTask> tasks = new HashMap<>();
    private final Map<String, Long> frames = new HashMap<>();

    public MenuAnimator(Menu<?> menu) {
        this.menu = menu;
    }

    public void registerAnimation(Animation animation) {
        Objects.requireNonNull(animation, "animation");
        String id = Objects.requireNonNull(animation.getId(), "animation id");

        stopAnimation(id);
        animations.put(id, animation);

        if (menu.isViewing()) {
            startAnimation(animation);
        }
    }

    public void stopAnimation(String id) {
        BukkitTask task = tasks.remove(id);
        if (task != null) {
            task.cancel();
        }

        Animation animation = animations.remove(id);
        frames.remove(id);
        if (animation != null) {
            if (!animation.isComplete()) {
                animation.stop();
            }
            animation.onStop(menu);
        }
    }

    public void pauseAnimation(String id) {
        Animation animation = animations.get(id);
        if (animation != null) {
            animation.pause();
        }
    }

    public void resumeAnimation(String id) {
        Animation animation = animations.get(id);
        if (animation != null) {
            animation.resume();
        }
    }

    public void stopAllAnimations() {
        for (String id : animations.keySet().toArray(new String[0])) {
            stopAnimation(id);
        }
    }

    public void startAll() {
        for (Animation animation : animations.values()) {
            startAnimation(animation);
        }
    }

    public void stopRunningTasks() {
        for (Map.Entry<String, BukkitTask> entry : tasks.entrySet()) {
            entry.getValue().cancel();
            Animation animation = animations.get(entry.getKey());
            if (animation != null) {
                animation.onStop(menu);
            }
        }
        tasks.clear();
    }

    private void startAnimation(Animation animation) {
        String id = animation.getId();
        if (tasks.containsKey(id)) {
            return;
        }

        frames.putIfAbsent(id, 0L);
        animation.onStart(menu);

        long delay = Math.max(0L, animation.getDelayTicks());
        long period = Math.max(1L, animation.getPeriodTicks());
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(menu.getPlugin(), () -> {
            if (!menu.isViewing()) {
                stopRunningTask(id);
                return;
            }

            if (animation.getState() == AnimationState.PAUSED) {
                return;
            }

            if (animation.isComplete()) {
                stopAnimation(id);
                return;
            }

            long frame = frames.getOrDefault(id, 0L);
            animation.tick(menu, frame);
            if (!animation.isComplete()) {
                frames.put(id, frame + 1L);
            }

            if (animation.isComplete()) {
                stopAnimation(id);
            }
        }, delay, period);
        tasks.put(id, task);
    }

    private void stopRunningTask(String id) {
        BukkitTask task = tasks.remove(id);
        if (task != null) {
            task.cancel();
        }

        Animation animation = animations.get(id);
        if (animation != null) {
            animation.onStop(menu);
        }
    }
}
