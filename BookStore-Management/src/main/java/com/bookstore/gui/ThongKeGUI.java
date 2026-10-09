package com.bookstore.gui;

import com.bookstore.bus.BookBUS;
import com.bookstore.bus.SalesOrderBUS;
import com.bookstore.bus.SalesReportBUS;
import com.bookstore.model.Book;
import com.bookstore.model.Employee;
import com.bookstore.model.PaymentMethod;
import com.bookstore.model.SalesOrder;
import com.bookstore.model.SalesReport;
import com.bookstore.util.AppException;
import com.bookstore.util.Validate;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

/** Thống kê doanh thu (chỉ tính đơn hoàn tất), lịch sử đơn hàng, hủy đơn, lưu báo cáo vào Sales_Reports. */
public class ThongKeGUI extends JPanel implements Refreshable {
    private final SalesOrderBUS bus = new SalesOrderBUS();
    private final SalesReportBUS reportBUS = new SalesReportBUS();
    private final Employee user;
    private final JTextField txtFrom = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 9);
    private final JTextField txtTo = new JTextField(LocalDate.now().toString(), 9);
    private final JLabel lblCount = new JLabel("0"), lblRevenue = new JLabel("0 đ"), lblAvg = new JLabel("0 đ");
    private double lastRevenue = 0;

    private final DefaultTableModel dayModel = ro("Ngày", "Số đơn hoàn tất", "Doanh thu");
    private final DefaultTableModel topModel = ro("Mã", "Tên sách", "Đã bán", "Doanh thu");
    private final DefaultTableModel orderModel = ro("Số HĐ", "Thời gian", "Thu ngân", "Khách hàng", "Thanh toán", "Trạng thái", "Thành tiền");
    private final DefaultTableModel lowModel = ro("Mã", "Tên sách", "Tồn kho");
    private final DefaultTableModel reportModel = ro("Mã BC", "Ngày lập", "Người lập", "Tổng doanh thu");
    private final JTable orderTable = new JTable(orderModel);

    public ThongKeGUI(Employee user) {
        super(new BorderLayout(8, 8));
        this.user = user;
        setBorder(new EmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        JButton btn = UIUtil.button("Thống kê", UIUtil.PRIMARY);
        JButton btnSave = UIUtil.button("Lưu báo cáo doanh thu", UIUtil.SUCCESS);
        top.add(new JLabel("Từ ngày (yyyy-MM-dd):")); top.add(txtFrom);
        top.add(new JLabel("Đến ngày:")); top.add(txtTo); top.add(btn); top.add(btnSave);
        btn.addActionListener(e -> reload());
        btnSave.addActionListener(e -> UIUtil.run(this, () -> {
            reload();
            reportBUS.save(user.getId(), lastRevenue);
            reload();
            UIUtil.info(this, "Đã lưu báo cáo doanh thu " + UIUtil.money(lastRevenue) + " vào bảng Sales_Reports.");
        }));
        add(top, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 3, 10, 0));
        cards.add(card("Số đơn hoàn tất", lblCount));
        cards.add(card("Tổng doanh thu", lblRevenue));
        cards.add(card("Giá trị TB / đơn", lblAvg));

        JTable dayT = styled(new JTable(dayModel)), topT = styled(new JTable(topModel)),
                lowT = styled(new JTable(lowModel)), repT = styled(new JTable(reportModel));
        UIUtil.styleTable(orderTable);
        UIUtil.moneyColumns(dayT, 2); UIUtil.alignRight(dayT, 1);
        UIUtil.alignRight(topT, 2); UIUtil.moneyColumns(topT, 3);
        UIUtil.moneyColumns(orderTable, 6);
        UIUtil.alignRight(lowT, 2);
        UIUtil.moneyColumns(repT, 3);

        JPanel orders = new JPanel(new BorderLayout(0, 6));
        JPanel orderBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        JButton btnView = UIUtil.button("Xem hóa đơn", UIUtil.PRIMARY);
        JButton btnCancel = UIUtil.button("Hủy đơn đã chọn", UIUtil.DANGER);
        orderBtns.add(btnView); orderBtns.add(btnCancel);
        orders.add(orderBtns, BorderLayout.NORTH);
        orders.add(new JScrollPane(orderTable), BorderLayout.CENTER);
        btnView.addActionListener(e -> UIUtil.run(this, this::viewOrder));
        btnCancel.addActionListener(e -> UIUtil.run(this, this::cancelOrder));
        orderTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) UIUtil.run(ThongKeGUI.this, ThongKeGUI.this::viewOrder); }
        });

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Doanh thu theo ngày", new JScrollPane(dayT));
        tabs.addTab("Sách bán chạy", new JScrollPane(topT));
        tabs.addTab("Lịch sử đơn hàng", orders);
        tabs.addTab("Sách sắp hết (tồn ≤ 10)", new JScrollPane(lowT));
        tabs.addTab("Báo cáo đã lưu", new JScrollPane(repT));

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.add(cards, BorderLayout.NORTH);
        center.add(tabs, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
        reload();
    }

    private static DefaultTableModel ro(String... cols) {
        return new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
    }

    private static JTable styled(JTable t) { UIUtil.styleTable(t); return t; }

    private static JPanel card(String title, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xD0D7DE)), new EmptyBorder(10, 14, 10, 14)));
        value.setFont(value.getFont().deriveFont(Font.BOLD, 22f));
        value.setForeground(UIUtil.PRIMARY);
        p.add(new JLabel(title), BorderLayout.NORTH);
        p.add(value, BorderLayout.CENTER);
        return p;
    }

    private int selectedOrderId() {
        int r = orderTable.getSelectedRow();
        if (r < 0) throw new AppException("Vui lòng chọn một đơn hàng trong bảng.");
        String code = String.valueOf(orderModel.getValueAt(orderTable.convertRowIndexToModel(r), 0));
        return Integer.parseInt(code.substring(2));
    }

    private void viewOrder() {
        SalesOrder o = bus.getById(selectedOrderId());
        if (o == null) throw new AppException("Không tìm thấy đơn hàng.");
        UIUtil.showReceipt(this, bus.buildReceipt(o));
    }

    private void cancelOrder() {
        int id = selectedOrderId();
        if (!UIUtil.confirm(this, "Hủy đơn " + SalesOrderBUS.orderCode(id) + "? Sách sẽ được hoàn lại kho và thanh toán chuyển sang 'đã hoàn tiền'.")) return;
        bus.cancelOrder(id);
        reload();
        UIUtil.info(this, "Đã hủy đơn " + SalesOrderBUS.orderCode(id) + ".");
    }

    @Override public void reload() {
        UIUtil.run(this, () -> {
            LocalDate from = Validate.date(txtFrom.getText(), "Từ ngày"), to = Validate.date(txtTo.getText(), "Đến ngày");
            if (from == null || to == null) throw new AppException("Vui lòng nhập đủ khoảng ngày (yyyy-MM-dd).");
            if (to.isBefore(from)) throw new AppException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");

            double[] s = bus.summary(from, to);
            lastRevenue = s[1];
            lblCount.setText(String.valueOf((int) s[0]));
            lblRevenue.setText(UIUtil.money(s[1]));
            lblAvg.setText(UIUtil.money(s[0] == 0 ? 0 : s[1] / s[0]));

            dayModel.setRowCount(0);
            for (Object[] r : bus.revenueByDay(from, to)) dayModel.addRow(new Object[]{r[0], r[1], r[2]});
            topModel.setRowCount(0);
            for (Object[] r : bus.topBooks(from, to, 20)) topModel.addRow(r);
            orderModel.setRowCount(0);
            SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (SalesOrder o : bus.orders(from, to)) orderModel.addRow(new Object[]{SalesOrderBUS.orderCode(o.getOrderId()),
                    f.format(o.getOrderDate()), o.getEmployeeName(), o.getCustomerName() == null ? "Khách vãng lai" : o.getCustomerName(),
                    PaymentMethod.labelOf(o.getPaymentMethod()), SalesOrderBUS.statusLabel(o.getOrderStatus()), o.getTotalAmount()});
            lowModel.setRowCount(0);
            BookBUS.getInstance().load();
            for (Book b : BookBUS.getInstance().lowStock(10)) lowModel.addRow(new Object[]{b.getBookId(), b.getTitle(), b.getStockQuantity()});
            reportModel.setRowCount(0);
            for (SalesReport r : reportBUS.getAll()) reportModel.addRow(new Object[]{r.getReportId(), r.getReportDate(), r.getEmployeeName(), r.getTotalSales()});
        });
    }
}
