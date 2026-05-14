package com.github.xnaut97.cosmos.menu.animation;


import com.github.xnaut97.cosmos.menu.Menu;

public interface Animation {

    String getId();

    default long getDelayTicks() {
        return 0L;
    }

    default long getPeriodTicks() {
        return 1L;
    }

    void tick(Menu<?> menu, long frame);

    default AnimationState getState() {
        return AnimationState.IDLE;
    }

    default void pause() {
    }

    default void resume() {
    }

    default void stop() {
    }

    default boolean isPaused() {
        return getState() == AnimationState.PAUSED;
    }

    default boolean isComplete() {
        AnimationState state = getState();
        return state == AnimationState.COMPLETED || state == AnimationState.CANCELLED;
    }

    default void onStart(Menu<?> menu) {
    }

    default void onStop(Menu<?> menu) {
    }
}
