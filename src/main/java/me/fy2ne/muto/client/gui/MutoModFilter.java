package me.fy2ne.muto.client.gui;

public enum MutoModFilter {
    ALL("All"),
    ACTIVE("Active"),
    DISABLED("Disabled"),
    UNLOADED("Unloaded");

    private final String label;

    MutoModFilter(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public MutoModFilter next() {
        return switch (this) {
            case ALL -> ACTIVE;
            case ACTIVE -> DISABLED;
            case DISABLED -> UNLOADED;
            case UNLOADED -> ALL;
        };
    }

    public static MutoModFilter CURRENT = ALL;
}
