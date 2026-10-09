package com.bookstore.gui;

import com.bookstore.util.AppException;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class UIUtil {
    public static final Color PRIMARY = new Color(0x1F6FEB);
    public static final Color SUCCESS = new Color(0x2DA44E);
    public static final Color DANGER = new Color(0xD1242F);
    public static final Color NEUTRAL = new Color(0x57606A);
    private static final NumberFormat NF = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

    private UIUtil() {}

    /** Mục combobox gồm mã (có thể null = "không chọn"/"tất cả") + tên hiển thị. */
    public static class Item {
        public final Integer id;
        public final String name;
        public Item(Integer id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    /** Nạp lại combobox; noneLabel != null thì thêm mục đầu tiên có id = null. */
    public static void fillCombo(JComboBox<Item> cbo, List<Item> items, String noneLabel) {
        Integer old = selectedId(cbo);
        cbo.removeAllItems();
        if (noneLabel != null) cbo.addItem(new Item(null, noneLabel));
        for (Item i : items) cbo.addItem(i);
        selectId(cbo, old);
    }

    public static Integer selectedId(JComboBox<Item> cbo) {
        Object o = cbo.getSelectedItem();
        return o instanceof Item ? ((Item) o).id : null;
    }

    public static void selectId(JComboBox<Item> cbo, Integer id) {
        for (int i = 0; i < cbo.getItemCount(); i++) {
            Integer x = cbo.getItemAt(i).id;
            if (x == null ? id == null : x.equals(id)) { cbo.setSelectedIndex(i); return; }
        }
        if (cbo.getItemCount() > 0) cbo.setSelectedIndex(0);
    }

    public static String money(double v) { return NF.format(Math.round(v)) + " đ"; }

    /** Hiển thị số tiền (Double) dạng 45.000 đ nhưng vẫn sắp xếp theo giá trị số. */
    public static DefaultTableCellRenderer moneyRenderer() {
        return new DefaultTableCellRenderer() {
            { setHorizontalAlignment(SwingConstants.RIGHT); }
            @Override protected void setValue(Object v) { setText(v instanceof Number ? money(((Number) v).doubleValue()) : String.valueOf(v)); }
        };
    }

    public static void error(Component p, String msg) { JOptionPane.showMessageDialog(p, msg, "Lỗi", JOptionPane.ERROR_MESSAGE); }
    public static void info(Component p, String msg) { JOptionPane.showMessageDialog(p, msg, "Thông báo", JOptionPane.INFORMATION_MESSAGE); }
    public static boolean confirm(Component p, String msg) {
        return JOptionPane.showConfirmDialog(p, msg, "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    /** Chạy hành động và hiển thị lỗi nghiệp vụ thân thiện. */
    public static void run(Component parent, Runnable r) {
        try { r.run(); }
        catch (AppException ex) { error(parent, ex.getMessage()); }
        catch (Exception ex) { ex.printStackTrace(); error(parent, "Đã xảy ra lỗi: " + ex.getMessage()); }
    }

    public static double parseMoney(String s, String field) {
        String d = s == null ? "" : s.replaceAll("[^0-9]", "");
        if (d.isEmpty()) throw new AppException(field + " không hợp lệ.");
        return Double.parseDouble(d);
    }

    public static int parseInt(String s, String field) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { throw new AppException(field + " phải là số nguyên."); }
    }

    /** Ô để trống = null. */
    public static Integer parseIntOrNull(String s, String field) {
        return s == null || s.isBlank() ? null : parseInt(s, field);
    }

    public static Double parseMoneyOrNull(String s, String field) {
        return s == null || s.isBlank() ? null : parseMoney(s, field);
    }

    public static JTextArea textArea(int rows) {
        JTextArea t = new JTextArea(rows, 14);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        return t;
    }

    public static JButton button(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return b;
    }

    public static void styleTable(JTable t) {
        t.setRowHeight(26);
        t.setAutoCreateRowSorter(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFillsViewportHeight(true);
        t.setGridColor(new Color(0xE1E4E8));
        JTableHeader h = t.getTableHeader();
        h.setFont(h.getFont().deriveFont(Font.BOLD));
        h.setReorderingAllowed(false);
    }

    public static void alignRight(JTable t, int... cols) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int c : cols) t.getColumnModel().getColumn(c).setCellRenderer(r);
    }

    public static void moneyColumns(JTable t, int... cols) {
        for (int c : cols) t.getColumnModel().getColumn(c).setCellRenderer(moneyRenderer());
    }

    public static void showReceipt(Component parent, String text) {
        JTextArea area = new JTextArea(text, 22, 44);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        Object[] opts = {"Lưu file .txt", "Đóng"};
        int r = JOptionPane.showOptionDialog(parent, new JScrollPane(area), "Hóa đơn", JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, opts, opts[1]);
        if (r == 0) {
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("hoadon.txt"));
            if (fc.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                try { Files.write(fc.getSelectedFile().toPath(), text.getBytes(StandardCharsets.UTF_8)); info(parent, "Đã lưu hóa đơn."); }
                catch (IOException e) { error(parent, "Không lưu được file: " + e.getMessage()); }
            }
        }
    }
}
