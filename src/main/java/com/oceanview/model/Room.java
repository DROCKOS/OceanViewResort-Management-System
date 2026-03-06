package com.oceanview.model;

public class Room {
    private int roomId;
    private String roomNumber;
    private String roomType;
    private double pricePerNight;
    private boolean isAvailable;
    private String description;
    private String roomSize;
    private int numBeds;
    private int maxPersons;
    private String imageUrl;

    public Room() {}

    public Room(int roomId, String roomNumber, String roomType, double pricePerNight,
                boolean isAvailable, String description, String roomSize,
                int numBeds, int maxPersons, String imageUrl) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.isAvailable = isAvailable;
        this.description = description;
        this.roomSize = roomSize;
        this.numBeds = numBeds;
        this.maxPersons = maxPersons;
        this.imageUrl = imageUrl;
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRoomSize() { return roomSize; }
    public void setRoomSize(String roomSize) { this.roomSize = roomSize; }

    public int getNumBeds() { return numBeds; }
    public void setNumBeds(int numBeds) { this.numBeds = numBeds; }

    public int getMaxPersons() { return maxPersons; }
    public void setMaxPersons(int maxPersons) { this.maxPersons = maxPersons; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}