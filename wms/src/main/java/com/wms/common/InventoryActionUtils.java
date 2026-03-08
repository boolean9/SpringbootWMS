package com.wms.common;

public final class InventoryActionUtils {

    private InventoryActionUtils() {
    }

    public static String normalize(String actionType, String legacyAction) {
        String candidate = blankToNull(actionType);
        if (candidate == null) {
            candidate = blankToNull(legacyAction);
        }
        if (candidate == null) {
            return "INBOUND";
        }

        String value = candidate.trim().toUpperCase();
        return switch (value) {
            case "1", "IN", "INBOUND" -> "INBOUND";
            case "2", "OUT", "OUTBOUND" -> "OUTBOUND";
            case "TRANSFERIN", "TRANSFER_IN" -> "TRANSFER_IN";
            case "TRANSFEROUT", "TRANSFER_OUT" -> "TRANSFER_OUT";
            default -> value;
        };
    }

    public static boolean isInbound(String actionType) {
        return "INBOUND".equals(actionType) || "TRANSFER_IN".equals(actionType);
    }

    public static boolean isOutbound(String actionType) {
        return "OUTBOUND".equals(actionType) || "TRANSFER_OUT".equals(actionType);
    }

    public static int signedDelta(String actionType, int quantity) {
        return isOutbound(actionType) ? -quantity : quantity;
    }

    private static String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value;
    }
}
