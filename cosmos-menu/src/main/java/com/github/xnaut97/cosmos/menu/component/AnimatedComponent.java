package com.github.xnaut97.cosmos.menu.component;

import com.github.xnaut97.cosmos.menu.Menu;
import com.github.xnaut97.cosmos.menu.animation.Animation;

import java.util.Objects;

public class AnimatedComponent implements MenuComponent {

    private final Animation animation;

    public AnimatedComponent(Animation animation) {
        this.animation = Objects.requireNonNull(animation, "animation");
    }

    @Override
    public void onAttach(Menu menu) {
        menu.registerAnimation(animation);
    }

    @Override
    public void onDetach(Menu menu) {
        menu.stopAnimation(animation.getId());
    }
}
