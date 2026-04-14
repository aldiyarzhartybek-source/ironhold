package com.ironhold.core;

import com.badlogic.gdx.Game;

public class IronHoldGame extends Game {
    @Override
    public void create() {
        setScreen(new PlayScreen());
    }
}
