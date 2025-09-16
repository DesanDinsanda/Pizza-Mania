package com.example.pizza_mania.menu;

public class MenuItem {

    private boolean availability;
    private String category;
    private String itemImage;
    private String itemName;
    private int smallPrice;
    private int mediumPrice;
    private int largePrice;
    private String size;

    public MenuItem() {}

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getSmallPrice() {
        return smallPrice;
    }

    public void setSmallPrice(int smallPrice) {
        this.smallPrice = smallPrice;
    }

    public int getMediumPrice() {
        return mediumPrice;
    }

    public void setMediumPrice(int mediumPrice) {
        this.mediumPrice = mediumPrice;
    }

    public int getLargePrice() {
        return largePrice;
    }

    public void setLargePrice(int largePrice) {
        this.largePrice = largePrice;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
