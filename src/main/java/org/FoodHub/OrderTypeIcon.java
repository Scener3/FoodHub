package org.FoodHub;

/**
 * Enum for order type icons
 * Maps to image file in path
 * used to display order type icons with TableView in OrderTrackerController
 */
public enum OrderTypeIcon {
    Delivery_Icon("/icons/DELIVERY_ICON.png"),
    Pick_Up_Icon("/icons/PICKUP_ICON.png"),
    To_Go_Icon("/icons/TO_GO_ICON.png");

    private final String imagePath;
    /**
     * @param imagePath the classpath resource path to the icon PNG file
     */
    OrderTypeIcon(String imagePath) {
        this.imagePath = imagePath;
    }
    /**
     * Gets the image path for this icon type.
     *
     * @return the classpath path string
     */
    public String getImagePath() {
        return imagePath;
    }
}
