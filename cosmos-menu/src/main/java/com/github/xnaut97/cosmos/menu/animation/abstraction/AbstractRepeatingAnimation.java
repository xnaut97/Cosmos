package com.github.xnaut97.cosmos.menu.animation.abstraction;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.Animation;
import com.github.xnaut97.cosmos.menu.animation.AnimationResponse;
import com.github.xnaut97.cosmos.menu.animation.AnimationResult;
import com.github.xnaut97.cosmos.menu.animation.AnimationState;
import com.github.xnaut97.cosmos.menu.animation.utility.AnimationSupport;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.logging.Level;

@Getter
@Accessors(fluent = true)
public abstract class AbstractRepeatingAnimation implements Animation {

    private final String id;
    private final long delayTicks;
    private final long periodTicks;
    private final long maxFrames;
    private final boolean loop;
    private final Map<Double, Consumer<AnimationResponse>> progressCallbacks = new TreeMap<>();
    private final Set<Double> triggeredProgressCallbacks = new HashSet<>();
    private AnimationState state = AnimationState.IDLE;
    private Consumer<AnimationResponse> completeCallback;
    private Consumer<AnimationResponse> cancelCallback;
    private Consumer<AnimationResponse> pauseCallback;
    private Consumer<AnimationResponse> resumeCallback;
    private Menu<?> currentMenu;
    private long lastFrame;

    protected AbstractRepeatingAnimation(String id, long delayTicks, long periodTicks, long maxFrames, boolean loop) {
        this.id = AnimationSupport.id(id, getClass().getSimpleName() + "-" + Integer.toHexString(System.identityHashCode(this)));
        this.delayTicks = Math.max(0L, delayTicks);
        this.periodTicks = Math.max(1L, periodTicks);
        this.maxFrames = Math.max(0L, maxFrames);
        this.loop = loop;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getDelayTicks() {
        return delayTicks;
    }

    @Override
    public long getPeriodTicks() {
        return periodTicks;
    }

    @Override
    public AnimationState getState() {
        return state;
    }

    @Override
    public final void tick(Menu<?> menu, long frame) {
        if (state == AnimationState.PAUSED || isComplete()) {
            return;
        }

        if (state == AnimationState.IDLE) {
            state = AnimationState.RUNNING;
        }

        currentMenu = menu;
        lastFrame = frame;
        renderFrame(menu, frame);

        double progress = progress(frame);
        triggerProgressCallbacks(menu, frame, progress);

        if (!loop && maxFrames > 0L && frame + 1L >= maxFrames) {
            markCompleted(menu, frame, progress);
        }
    }

    @Override
    public boolean isComplete() {
        return state == AnimationState.COMPLETED || state == AnimationState.CANCELLED;
    }

    @Override
    public void onStart(Menu<?> menu) {
        this.currentMenu = menu;
        this.lastFrame = 0L;
        this.state = AnimationState.RUNNING;
        this.triggeredProgressCallbacks.clear();
    }

    @Override
    public void pause() {
        if (state != AnimationState.RUNNING) {
            return;
        }

        state = AnimationState.PAUSED;
        invoke(pauseCallback, response(AnimationResult.PAUSED, currentMenu, lastFrame, progress(lastFrame), -1.0D));
    }

    @Override
    public void resume() {
        if (state != AnimationState.PAUSED) {
            return;
        }

        state = AnimationState.RUNNING;
        invoke(resumeCallback, response(AnimationResult.RESUMED, currentMenu, lastFrame, progress(lastFrame), -1.0D));
    }

    @Override
    public void stop() {
        if (isComplete()) {
            return;
        }

        state = AnimationState.CANCELLED;
        invoke(cancelCallback, response(AnimationResult.CANCELLED, currentMenu, lastFrame, progress(lastFrame), -1.0D));
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractRepeatingAnimation> T onComplete(Consumer<AnimationResponse> callback) {
        this.completeCallback = callback;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractRepeatingAnimation> T onCancel(Consumer<AnimationResponse> callback) {
        this.cancelCallback = callback;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractRepeatingAnimation> T onPause(Consumer<AnimationResponse> callback) {
        this.pauseCallback = callback;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractRepeatingAnimation> T onResume(Consumer<AnimationResponse> callback) {
        this.resumeCallback = callback;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractRepeatingAnimation> T onProgress(double threshold, Consumer<AnimationResponse> callback) {
        if (callback != null) {
            progressCallbacks.put(normalizeThreshold(threshold), callback);
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractRepeatingAnimation> T onProgressPercent(double percentage, Consumer<AnimationResponse> callback) {
        return (T) onProgress(percentage / 100.0D, callback);
    }

    protected final void markCompleted(Menu<?> menu, long frame) {
        markCompleted(menu, frame, progress(frame));
    }

    protected double progress(long frame) {
        if (maxFrames <= 0L) {
            return 0.0D;
        }
        return Math.max(0.0D, Math.min(1.0D, (frame + 1.0D) / maxFrames));
    }

    protected abstract void renderFrame(Menu<?> menu, long frame);

    private void markCompleted(Menu<?> menu, long frame, double progress) {
        if (isComplete()) {
            return;
        }

        state = AnimationState.COMPLETED;
        invoke(completeCallback, response(AnimationResult.COMPLETED, menu, frame, progress, 1.0D));
    }

    private void triggerProgressCallbacks(Menu<?> menu, long frame, double progress) {
        if (progressCallbacks.isEmpty()) {
            return;
        }

        for (Map.Entry<Double, Consumer<AnimationResponse>> entry : progressCallbacks.entrySet()) {
            double threshold = entry.getKey();
            if (triggeredProgressCallbacks.contains(threshold) || progress + 0.000001D < threshold) {
                continue;
            }

            triggeredProgressCallbacks.add(threshold);
            invoke(entry.getValue(), response(AnimationResult.PROGRESS, menu, frame, progress, threshold));
        }
    }

    private AnimationResponse response(AnimationResult result,
                                       Menu<?> menu,
                                       long frame,
                                       double progress,
                                       double threshold) {
        return new AnimationResponse(this, menu, state, result, frame, progress, threshold);
    }

    private void invoke(Consumer<AnimationResponse> callback, AnimationResponse response) {
        if (callback == null) {
            return;
        }

        try {
            callback.accept(response);
        } catch (RuntimeException exception) {
            Menu<?> responseMenu = response.getMenu();
            if (responseMenu != null) {
                responseMenu.getPlugin().getLogger().log(Level.SEVERE, "Animation callback failed for " + id, exception);
            } else {
                throw exception;
            }
        }
    }

    private double normalizeThreshold(double threshold) {
        double normalized = threshold > 1.0D ? threshold / 100.0D : threshold;
        return Math.max(0.0D, Math.min(1.0D, normalized));
    }
}
