package com.bookstore.model;

/** Phương thức thanh toán: code khớp ENUM trong bảng Payments. */
public enum PaymentMethod {
    CASH("cash", "Tiền mặt"),
    CREDIT_CARD("credit_card", "Thẻ tín dụng"),
    E_WALLET("e_wallet", "Ví điện tử"),
    BANK_TRANSFER("bank_transfer", "Chuyển khoản");

    private final String code, label;
    PaymentMethod(String code, String label) { this.code = code; this.label = label; }
    public String getCode() { return code; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }

    public static String labelOf(String code) {
        for (PaymentMethod m : values()) if (m.code.equals(code)) return m.label;
        return code == null ? "" : code;
    }
}
