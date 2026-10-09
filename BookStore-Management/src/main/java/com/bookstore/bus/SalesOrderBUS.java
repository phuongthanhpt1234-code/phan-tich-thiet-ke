package com.bookstore.bus;

import com.bookstore.config.MySQLConnect;
import com.bookstore.dao.*;
import com.bookstore.model.*;
import com.bookstore.util.AppException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/** Nghiệp vụ bán hàng: kiểm tra tồn kho, tính tiền, lập đơn + thanh toán trong một giao dịch, hủy đơn, thống kê. */
public class SalesOrderBUS {
    private final SalesOrderDAO orderDAO = new SalesOrderDAO();
    private final OrderItemDAO itemDAO = new OrderItemDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final BookDAO bookDAO = new BookDAO();

    public double total(List<OrderItem> items) { return items.stream().mapToDouble(OrderItem::getAmount).sum(); }

    /** Kiểm tra tồn kho hiện tại (đọc mới từ CSDL) có đủ bán không. */
    public void checkStock(int bookId, int qty) {
        Book b = bookDAO.selectById(bookId);
        if (b == null) throw new AppException("Sách không còn tồn tại trong hệ thống.");
        if (qty > b.getStockQuantity())
            throw new AppException("Sách '" + b.getTitle() + "' chỉ còn " + b.getStockQuantity() + " cuốn trong kho.");
    }

    /**
     * Lập đơn hàng: Sales_Orders (completed) -> Order_Items -> trừ stock_quantity -> Payments (completed).
     * Tất cả trong một giao dịch: lỗi ở bước nào cũng rollback toàn bộ.
     */
    public SalesOrder checkout(Employee employee, Customer customer, List<OrderItem> items, PaymentMethod method) {
        if (items == null || items.isEmpty()) throw new AppException("Giỏ hàng đang trống.");
        if (method == null) throw new AppException("Vui lòng chọn phương thức thanh toán.");
        for (OrderItem d : items) checkStock(d.getBookId(), d.getQuantity());

        SalesOrder o = new SalesOrder();
        o.setCustomerId(customer == null ? null : customer.getCustomerId());
        o.setCustomerName(customer == null ? null : customer.getFullName());
        o.setEmployeeId(employee.getId());
        o.setEmployeeName(employee.getName());
        o.setOrderDate(new Timestamp(System.currentTimeMillis()));
        o.setTotalAmount(total(items));
        o.setOrderStatus("completed");
        o.setPaymentMethod(method.getCode());
        o.setItems(items);
        if (o.getTotalAmount() >= 10_000_000_000.0) throw new AppException("Giá trị đơn hàng vượt giới hạn.");

        Connection c = null;
        try {
            c = MySQLConnect.getConnection();
            c.setAutoCommit(false);
            o.setOrderId(orderDAO.insert(c, o));
            for (OrderItem d : items) {
                d.setOrderId(o.getOrderId());
                itemDAO.insert(c, d);
                if (bookDAO.decreaseStock(c, d.getBookId(), d.getQuantity()) == 0)
                    throw new AppException("Sách '" + d.getBookTitle() + "' không đủ số lượng tồn kho.");
            }
            Payment p = new Payment();
            p.setOrderId(o.getOrderId()); p.setPaymentDate(o.getOrderDate()); p.setAmount(o.getTotalAmount());
            p.setPaymentMethod(method.getCode()); p.setPaymentStatus("completed");
            paymentDAO.insert(c, p);
            c.commit();
        } catch (SQLException e) {
            rollback(c);
            throw AppException.of(e);
        } catch (RuntimeException e) {
            rollback(c);
            throw e;
        } finally {
            if (c != null) try { c.setAutoCommit(true); c.close(); } catch (SQLException ignored) { }
        }
        BookBUS.getInstance().load();
        return o;
    }

    /** Hủy đơn đã hoàn tất: order_status = cancelled, hoàn kho, payment_status = refunded (một giao dịch). */
    public void cancelOrder(int orderId) {
        Connection c = null;
        try {
            c = MySQLConnect.getConnection();
            c.setAutoCommit(false);
            SalesOrder o = orderDAO.selectByIdForUpdate(c, orderId);
            if (o == null) throw new AppException("Không tìm thấy đơn hàng.");
            if (!"completed".equals(o.getOrderStatus())) throw new AppException("Chỉ hủy được đơn hàng ở trạng thái đã hoàn tất.");
            orderDAO.updateStatus(c, orderId, "cancelled");
            for (OrderItem d : itemDAO.selectByOrder(c, orderId)) bookDAO.increaseStock(c, d.getBookId(), d.getQuantity());
            paymentDAO.updateStatusByOrder(c, orderId, "refunded");
            c.commit();
        } catch (SQLException e) {
            rollback(c);
            throw AppException.of(e);
        } catch (RuntimeException e) {
            rollback(c);
            throw e;
        } finally {
            if (c != null) try { c.setAutoCommit(true); c.close(); } catch (SQLException ignored) { }
        }
        BookBUS.getInstance().load();
    }

    private void rollback(Connection c) {
        if (c != null) try { c.rollback(); } catch (SQLException ignored) { }
    }

    // ---------------- Thống kê / lịch sử ----------------
    private static Timestamp start(LocalDate d) { return Timestamp.valueOf(d.atStartOfDay()); }
    private static Timestamp endExclusive(LocalDate d) { return Timestamp.valueOf(d.plusDays(1).atStartOfDay()); }

    public List<SalesOrder> orders(LocalDate from, LocalDate to) { return orderDAO.selectByRange(start(from), endExclusive(to)); }
    public SalesOrder getById(int id) { return orderDAO.selectById(id); }
    /** {số đơn hoàn tất, doanh thu}. */
    public double[] summary(LocalDate from, LocalDate to) { return orderDAO.summary(start(from), endExclusive(to)); }
    public List<Object[]> revenueByDay(LocalDate from, LocalDate to) { return orderDAO.revenueByDay(start(from), endExclusive(to)); }
    public List<Object[]> topBooks(LocalDate from, LocalDate to, int limit) { return itemDAO.topBooks(start(from), endExclusive(to), limit); }
    public List<OrderItem> itemsOf(int orderId) { return itemDAO.selectByOrder(orderId); }

    public static String orderCode(int orderId) { return String.format("HD%06d", orderId); }

    public static String statusLabel(String status) {
        if (status == null) return "";
        switch (status) {
            case "completed": return "Hoàn tất";
            case "cancelled": return "Đã hủy";
            case "pending": return "Chờ xử lý";
            default: return status;
        }
    }

    /** Dựng nội dung hóa đơn dạng văn bản để hiển thị / lưu file. */
    public String buildReceipt(SalesOrder o) {
        NumberFormat nf = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
        if (o.getItems().isEmpty()) o.setItems(itemsOf(o.getOrderId()));
        StringBuilder sb = new StringBuilder();
        sb.append("================ NHÀ SÁCH ================\n");
        sb.append("HÓA ĐƠN BÁN HÀNG").append("cancelled".equals(o.getOrderStatus()) ? "  (ĐÃ HỦY)" : "").append('\n');
        sb.append("Số HĐ    : ").append(orderCode(o.getOrderId())).append('\n');
        sb.append("Ngày     : ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(o.getOrderDate())).append('\n');
        sb.append("Thu ngân : ").append(o.getEmployeeName()).append('\n');
        sb.append("Khách    : ").append(o.getCustomerName() == null ? "Khách vãng lai" : o.getCustomerName()).append('\n');
        sb.append("Thanh toán: ").append(PaymentMethod.labelOf(o.getPaymentMethod())).append('\n');
        sb.append("------------------------------------------\n");
        int i = 1;
        for (OrderItem d : o.getItems()) {
            sb.append(i++).append(". ").append(d.getBookTitle()).append('\n');
            sb.append(String.format("     %d x %s = %s đ%n", d.getQuantity(), nf.format(d.getUnitPrice()), nf.format(d.getAmount())));
        }
        sb.append("------------------------------------------\n");
        sb.append(String.format("THÀNH TIỀN: %s đ%n", nf.format(o.getTotalAmount())));
        sb.append("------------------------------------------\n");
        sb.append("Cảm ơn quý khách, hẹn gặp lại!\n");
        return sb.toString();
    }
}
