package com.bookstore.model;

public class Position {
    private int id;
    private String positionName;
    private String description;

    public Position() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPositionName() { return positionName; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override public String toString() { return positionName; }
}
