package com.mcal.kotlin.data;

public final class ListMode {
    public static Mode getCurrentMode() {
        return com.mcal.kotlin.data.Preferences.isInGridMode() ? Mode.GRID : Mode.DEFAULT;
    }

    public enum Mode {
        DEFAULT, GRID
    }
}