package com.github.xnaut97.cosmos.menu.animation;

import com.github.xnaut97.cosmos.menu.Menu;
import lombok.Getter;

@Getter
public final class AnimationResponse {

    private final Animation animation;
    private final Menu menu;
    private final AnimationState state;
    private final AnimationResult result;
    private final long frame;
    private final double progress;
    private final double threshold;

    public AnimationResponse(Animation animation,
                             Menu menu,
                             AnimationState state,
                             AnimationResult result,
                             long frame,
                             double progress,
                             double threshold) {
        this.animation = animation;
        this.menu = menu;
        this.state = state;
        this.result = result;
        this.frame = frame;
        this.progress = progress;
        this.threshold = threshold;
    }

    public String getAnimationId() {
        return animation == null ? null : animation.getId();
    }
}
