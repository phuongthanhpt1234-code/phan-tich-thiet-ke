package com.bookstore.gui;

import com.bookstore.util.AppException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Khung CRUD dùng chung: thanh tìm kiếm + bảng dữ liệu + form nhập (cuộn được) + nút Thêm/Sửa/Xóa/Làm mới. */
public abstract class CrudPanel extends JPanel implements Refreshable {
    protected final DefaultTableModel model;
    protected final JTable table;
    protected final JTextField txtSearch = new JTextField(20);
    protected final JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
    private final JPanel form = new JPanel(new GridBagLayout());
    private final JButton btnDelete = UIUtil.button("Xóa", UIUtil.DANGER);
    private int formRow = 0;

    protected CrudPanel(String[] columns) {
        super(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UIUtil.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            String id = selectedId();
            if (!e.getValueIsAdjusting() && id != null) fillForm(id);
        });

        JButton btnSearch = UIUtil.button("Tìm kiếm", UIUtil.PRIMARY);
        btnSearch.addActionListener(e -> refresh());
        txtSearch.addActionListener(e -> refresh());
        toolbar.add(new JLabel("Từ khóa:"));
        toolbar.add(txtSearch);
        toolbar.add(btnSearch);
        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    /** Subclass gọi ở cuối constructor sau khi đã addField(...). */
    protected void init() {
        JPanel buttons = new JPanel(new GridLayout(0, 2, 6, 6));
        buttons.setBorder(new EmptyBorder(8, 8, 8, 8));
        JButton add = UIUtil.button("Thêm", UIUtil.SUCCESS);
        JButton upd = UIUtil.button("Sửa", UIUtil.PRIMARY);
        JButton clr = UIUtil.button("Làm mới", UIUtil.NEUTRAL);
        add.addActionListener(e -> UIUtil.run(this, () -> { onAdd(); refresh(); clearForm(); }));
        upd.addActionListener(e -> UIUtil.run(this, () -> {
            String id = requireSelection();
            onUpdate(id); refresh(); clearForm();
        }));
        btnDelete.addActionListener(e -> UIUtil.run(this, () -> {
            String id = requireSelection();
            if (UIUtil.confirm(this, deleteMessage(id))) { onDelete(id); refresh(); clearForm(); }
        }));
        clr.addActionListener(e -> { table.clearSelection(); txtSearch.setText(""); clearForm(); refresh(); });
        buttons.add(add); buttons.add(upd); buttons.add(btnDelete); buttons.add(clr);

        // Form cuộn dọc, luôn co theo chiều rộng khung (không bao giờ xuất hiện thanh cuộn ngang)
        JPanel formWrap = new ScrollableForm();
        formWrap.add(form, BorderLayout.NORTH);
        JScrollPane formScroll = new JScrollPane(formWrap, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroll.setBorder(null);

        JPanel east = new JPanel(new BorderLayout());
        east.setPreferredSize(new Dimension(400, 0));
        east.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết"));
        east.add(formScroll, BorderLayout.CENTER);
        east.add(buttons, BorderLayout.SOUTH);
        add(east, BorderLayout.EAST);
        refresh();
    }

    protected void setDeleteLabel(String text) { btnDelete.setText(text); }
    protected String deleteMessage(String id) { return "Bạn có chắc muốn xóa mục có mã '" + id + "'?"; }

    protected void addField(String label, JComponent c) {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 8, 4, 8);
        g.anchor = GridBagConstraints.NORTHWEST;
        g.gridy = formRow++;
        g.gridx = 0; g.weightx = 0;
        form.add(new JLabel(label), g);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        if (c instanceof JTextField) ((JTextField) c).setColumns(10);
        form.add(c, g);
    }

    protected String selectedId() {
        int r = table.getSelectedRow();
        return r < 0 ? null : String.valueOf(model.getValueAt(table.convertRowIndexToModel(r), 0));
    }

    protected int selectedIntId() { return Integer.parseInt(selectedId()); }

    private String requireSelection() {
        String id = selectedId();
        if (id == null) throw new AppException("Vui lòng chọn một dòng trong bảng.");
        return id;
    }

    protected void refresh() {
        UIUtil.run(this, () -> {
            model.setRowCount(0);
            for (Object[] row : loadRows(txtSearch.getText())) model.addRow(row);
        });
    }

    @Override public void reload() { refresh(); }

    protected abstract List<Object[]> loadRows(String keyword);
    protected abstract void fillForm(String id);
    protected abstract void clearForm();
    protected abstract void onAdd();
    protected abstract void onUpdate(String id);
    protected abstract void onDelete(String id);

    /** Panel đặt trong JScrollPane: bám theo chiều rộng khung nhìn, chỉ cuộn dọc. */
    private static class ScrollableForm extends JPanel implements Scrollable {
        ScrollableForm() { super(new BorderLayout()); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 64; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
