package com.irctc.model;

/**
 * Defines standard Indian Railway sleeper/3AC coach berth positions.
 */
public enum BerthType {
    LOWER("LB", "Lower Berth"),
    MIDDLE("MB", "Middle Berth"),
    UPPER("UB", "Upper Berth"),
    SIDE_LOWER("SL", "Side Lower"),
    SIDE_UPPER("SU", "Side Upper");

    private final String code;
    private final String description;

    BerthType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Determines the berth type based on standard 8-berth bay layout in Indian coaches.
     */
    public static BerthType getBerthForSeatNumber(int seatNumber) {
        int mod = seatNumber % 8;
        return switch (mod) {
            case 1, 4 -> LOWER;
            case 2, 5 -> MIDDLE;
            case 3, 6 -> UPPER;
            case 7 -> SIDE_LOWER;
            case 0 -> SIDE_UPPER;
            default -> UPPER;
        };
    }
}
