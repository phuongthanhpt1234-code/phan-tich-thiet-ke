package com.bookstore.model;

public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int bookId;
    private int quantity;
    private double unitPrice;
    private String bookTitle;
    public int getOrderItemId() { return orderItemId; }
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public OrderItem() {}
    public OrderItem(int bookId, String bookTitle, int quantity, double unitPrice) {
        this.bookId = bookId; this.bookTitle = bookTitle; this.quantity = quantity; this.unitPrice = unitPrice;
    }
    public double getAmount() { return quantity * unitPrice; }
}
