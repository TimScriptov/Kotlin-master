package com.mcal.kotlin.data;

public final class ScreenMode {
    public static Mode getCurrentMode() {
        return Preferences.isInFullscreenMode() ? Mode.FULLSCREEN : Mode.DEFAULT;
    }

    public enum Mode {
        DEFAULT, FULLSCREEN
    }
}
