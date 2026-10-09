package com.bookstore.gui;

import com.bookstore.bus.BookBUS;
import com.bookstore.bus.CustomerBUS;
import com.bookstore.bus.SalesOrderBUS;
import com.bookstore.model.*;
import com.bookstore.util.AppException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/** Giao diện POS: chọn sách -> giỏ hàng -> chọn khách + phương thức thanh toán -> lập đơn và in hóa đơn. */
public class QuanLyBanHangGUI extends JPanel implements Refreshable {
    private final BookBUS bookBUS = BookBUS.getInstance();
    private final CustomerBUS customerBUS = new CustomerBUS();
    private final SalesOrderBUS orderBUS = new SalesOrderBUS();
    private final Employee employee;
    private final List<OrderItem> cart = new ArrayList<>();

    private final JTextField txtSearch = new JTextField(16);
    private final DefaultTableModel bookModel = new DefaultTableModel(new String[]{"Mã", "Tên sách", "Tác giả", "Giá", "Tồn"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable bookTable = new JTable(bookModel);
    private final DefaultTableModel cartModel = new DefaultTableModel(new String[]{"Mã", "Tên sách", "SL", "Đơn giá", "Thành tiền"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable cartTable = new JTable(cartModel);
    private final JSpinner spnQty = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
    private final JComboBox<UIUtil.Item> cboCustomer = new JComboBox<>();
    private final JComboBox<PaymentMethod> cboMethod = new JComboBox<>(PaymentMethod.values());
    private final JLabel lblTotal = new JLabel("0 đ");

    public QuanLyBanHangGUI(Employee employee) {
        super(new BorderLayout(10, 10));
        this.employee = employee;
        setBorder(new EmptyBorder(8, 8, 8, 8));
        UIUtil.styleTable(bookTable);
        UIUtil.styleTable(cartTable);
        UIUtil.moneyColumns(bookTable, 3);
        UIUtil.alignRight(bookTable, 4);
        UIUtil.moneyColumns(cartTable, 3, 4);
        UIUtil.alignRight(cartTable, 2);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildLeft(), buildRight());
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);
        reload();
    }

    private JPanel buildLeft() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createTitledBorder("Chọn sách"));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton btnFind = UIUtil.button("Tìm", UIUtil.PRIMARY);
        top.add(new JLabel("Tên / tác giả / ISBN / mã:"));
        top.add(txtSearch);
        top.add(btnFind);
        btnFind.addActionListener(e -> loadBooks());
        txtSearch.addActionListener(e -> loadBooks());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton btnAdd = UIUtil.button("Thêm vào giỏ", UIUtil.SUCCESS);
        bottom.add(new JLabel("Số lượng:"));
        bottom.add(spnQty);
        bottom.add(btnAdd);
        btnAdd.addActionListener(e -> UIUtil.run(this, () -> addToCart((int) spnQty.getValue())));
        bookTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) UIUtil.run(QuanLyBanHangGUI.this, () -> addToCart(1));
            }
        });

        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(bookTable), BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildRight() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createTitledBorder("Giỏ hàng & thanh toán"));

        JPanel cartBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton btnQty = UIUtil.button("Đổi số lượng", UIUtil.PRIMARY);
        JButton btnDel = UIUtil.button("Xóa dòng", UIUtil.DANGER);
        JButton btnClear = UIUtil.button("Xóa giỏ", UIUtil.NEUTRAL);
        cartBtns.add(btnQty); cartBtns.add(btnDel); cartBtns.add(btnClear);
        btnQty.addActionListener(e -> UIUtil.run(this, this::changeQty));
        btnDel.addActionListener(e -> {
            int r = cartTable.getSelectedRow();
            if (r >= 0) { cart.remove(cartTable.convertRowIndexToModel(r)); refreshCart(); }
        });
        btnClear.addActionListener(e -> { cart.clear(); refreshCart(); });

        JPanel pay = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0; pay.add(new JLabel("Khách hàng:"), g);
        g.gridx = 1; g.weightx = 1; pay.add(cboCustomer, g);
        g.gridx = 0; g.gridy = 1; g.weightx = 0; pay.add(new JLabel("Thanh toán:"), g);
        g.gridx = 1; g.weightx = 1; pay.add(cboMethod, g);
        g.gridy = 2; g.gridx = 0; g.weightx = 0;
        JLabel t = new JLabel("THÀNH TIỀN:"); t.setFont(t.getFont().deriveFont(Font.BOLD, 16f)); pay.add(t, g);
        g.gridx = 1; lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 20f)); lblTotal.setForeground(UIUtil.DANGER); pay.add(lblTotal, g);
        JButton btnPay = UIUtil.button("THANH TOÁN & IN HÓA ĐƠN", UIUtil.SUCCESS);
        btnPay.setFont(btnPay.getFont().deriveFont(Font.BOLD, 14f));
        g.gridy = 3; g.gridx = 0; g.gridwidth = 2; pay.add(btnPay, g);
        btnPay.addActionListener(e -> UIUtil.run(this, this::pay));

        p.add(cartBtns, BorderLayout.NORTH);
        p.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        p.add(pay, BorderLayout.SOUTH);
        return p;
    }

    private void loadBooks() {
        UIUtil.run(this, () -> {
            bookModel.setRowCount(0);
            for (Book b : bookBUS.search(txtSearch.getText(), null))
                bookModel.addRow(new Object[]{b.getBookId(), b.getTitle(), b.getAuthorName() == null ? "" : b.getAuthorName(), b.getPrice(), b.getStockQuantity()});
        });
    }

    private void loadCustomers() {
        List<UIUtil.Item> items = new ArrayList<>();
        for (Customer c : customerBUS.getAll()) items.add(new UIUtil.Item(c.getCustomerId(), c.toString()));
        UIUtil.fillCombo(cboCustomer, items, "Khách vãng lai");
    }

    private Customer selectedCustomer() {
        Integer id = UIUtil.selectedId(cboCustomer);
        return id == null ? null : customerBUS.getById(id);
    }

    private void addToCart(int qty) {
        int r = bookTable.getSelectedRow();
        if (r < 0) throw new AppException("Vui lòng chọn một cuốn sách trong danh sách.");
        int id = (int) bookModel.getValueAt(bookTable.convertRowIndexToModel(r), 0);
        Book b = bookBUS.getById(id);
        OrderItem line = cart.stream().filter(d -> d.getBookId() == id).findFirst().orElse(null);
        int newQty = (line == null ? 0 : line.getQuantity()) + qty;
        orderBUS.checkStock(id, newQty);
        if (line == null) cart.add(new OrderItem(id, b.getTitle(), qty, b.getPrice()));
        else line.setQuantity(newQty);
        refreshCart();
    }

    private void changeQty() {
        int r = cartTable.getSelectedRow();
        if (r < 0) throw new AppException("Vui lòng chọn dòng trong giỏ hàng.");
        OrderItem d = cart.get(cartTable.convertRowIndexToModel(r));
        String s = JOptionPane.showInputDialog(this, "Số lượng mới cho '" + d.getBookTitle() + "':", d.getQuantity());
        if (s == null) return;
        int q = UIUtil.parseInt(s, "Số lượng");
        if (q <= 0) throw new AppException("Số lượng phải lớn hơn 0.");
        orderBUS.checkStock(d.getBookId(), q);
        d.setQuantity(q);
        refreshCart();
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        for (OrderItem d : cart) cartModel.addRow(new Object[]{d.getBookId(), d.getBookTitle(), d.getQuantity(), d.getUnitPrice(), d.getAmount()});
        lblTotal.setText(UIUtil.money(orderBUS.total(cart)));
    }

    private void pay() {
        if (cart.isEmpty()) throw new AppException("Giỏ hàng đang trống.");
        if (!UIUtil.confirm(this, "Xác nhận thanh toán " + lblTotal.getText() + " bằng " + cboMethod.getSelectedItem() + "?")) return;
        SalesOrder o = orderBUS.checkout(employee, selectedCustomer(), new ArrayList<>(cart), (PaymentMethod) cboMethod.getSelectedItem());
        cart.clear();
        reload();
        UIUtil.showReceipt(this, orderBUS.buildReceipt(o));
    }

    @Override public void reload() {
        UIUtil.run(this, () -> {
            bookBUS.load();
            loadBooks();
            loadCustomers();
            refreshCart();
        });
    }
}
