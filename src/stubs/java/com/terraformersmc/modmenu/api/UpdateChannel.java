package com.terraformersmc.modmenu.api;

public enum UpdateChannel {
    ALPHA,
    BETA,
    RELEASE;

    public static UpdateChannel getUserPreference() {
        return RELEASE;
    }
}
