package com.bookstore.gui;

import com.bookstore.bus.AuthorBUS;
import com.bookstore.bus.BookBUS;
import com.bookstore.bus.CategoryBUS;
import com.bookstore.bus.PublisherBUS;
import com.bookstore.model.Author;
import com.bookstore.model.Book;
import com.bookstore.model.Category;
import com.bookstore.model.Publisher;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class QuanLySachGUI extends CrudPanel {
    private final BookBUS bus = BookBUS.getInstance();

    private final JTextField txtId = new JTextField(), txtTitle = new JTextField(), txtIsbn = new JTextField(),
            txtYear = new JTextField(), txtPrice = new JTextField(), txtQty = new JTextField("0");
    private final JTextArea txtDesc = UIUtil.textArea(3);
    private final JComboBox<UIUtil.Item> cboAuthor = new JComboBox<>(), cboCategory = new JComboBox<>(),
            cboPublisher = new JComboBox<>(), cboFilter = new JComboBox<>();

    public QuanLySachGUI() {
        super(new String[]{"Mã", "Tên sách", "ISBN", "Tác giả", "Thể loại", "Nhà xuất bản", "Năm XB", "Giá bán", "Tồn kho"});
        txtId.setEditable(false);
        addField("Mã sách:", txtId);
        addField("Tên sách:", txtTitle);
        addField("ISBN:", txtIsbn);
        addField("Tác giả:", cboAuthor);
        addField("Thể loại:", cboCategory);
        addField("Nhà xuất bản:", cboPublisher);
        addField("Năm xuất bản:", txtYear);
        addField("Giá bán (đ):", txtPrice);
        addField("Tồn kho:", txtQty);
        addField("Mô tả:", new JScrollPane(txtDesc));

        toolbar.add(cboFilter);
        cboFilter.addActionListener(e -> refresh());
        JButton imp = UIUtil.button("Import CSV", UIUtil.NEUTRAL);
        JButton exp = UIUtil.button("Export CSV", UIUtil.NEUTRAL);
        imp.addActionListener(e -> UIUtil.run(this, this::importCsv));
        exp.addActionListener(e -> UIUtil.run(this, this::exportCsv));
        toolbar.add(imp);
        toolbar.add(exp);

        loadLookups();
        clearForm();
        UIUtil.moneyColumns(table, 7);
        UIUtil.alignRight(table, 6, 8);
        init();
    }

    private void loadLookups() {
        List<UIUtil.Item> authors = new ArrayList<>(), cats = new ArrayList<>(), pubs = new ArrayList<>();
        for (Author a : new AuthorBUS().getAll()) authors.add(new UIUtil.Item(a.getAuthorId(), a.getAuthorName()));
        for (Category c : new CategoryBUS().getAll()) cats.add(new UIUtil.Item(c.getCategoryId(), c.getCategoryName()));
        for (Publisher p : new PublisherBUS().getAll()) pubs.add(new UIUtil.Item(p.getPublisherId(), p.getPublisherName()));
        UIUtil.fillCombo(cboAuthor, authors, "(chưa có)");
        UIUtil.fillCombo(cboCategory, cats, "(chưa phân loại)");
        UIUtil.fillCombo(cboPublisher, pubs, "(chưa có)");
        UIUtil.fillCombo(cboFilter, cats, "Tất cả thể loại");
    }

    private Book fromForm(Integer id) {
        Book b = new Book();
        com.bookstore.util.Validate.required(txtTitle.getText(), "Tên sách");   // báo lỗi theo thứ tự các ô trên form
        if (id != null) b.setBookId(id);
        b.setTitle(txtTitle.getText()); b.setIsbn(txtIsbn.getText());
        b.setAuthorId(UIUtil.selectedId(cboAuthor));
        b.setCategoryId(UIUtil.selectedId(cboCategory));
        b.setPublisherId(UIUtil.selectedId(cboPublisher));
        b.setPublicationYear(UIUtil.parseIntOrNull(txtYear.getText(), "Năm xuất bản"));
        b.setPrice(UIUtil.parseMoney(txtPrice.getText(), "Giá bán"));
        b.setStockQuantity(UIUtil.parseInt(txtQty.getText(), "Số lượng tồn"));
        b.setDescription(txtDesc.getText());
        return b;
    }

    @Override protected List<Object[]> loadRows(String keyword) {
        List<Object[]> rows = new ArrayList<>();
        for (Book b : bus.search(keyword, UIUtil.selectedId(cboFilter)))
            rows.add(new Object[]{b.getBookId(), b.getTitle(), b.getIsbn() == null ? "" : b.getIsbn(),
                    b.getAuthorName() == null ? "" : b.getAuthorName(), b.getCategoryName() == null ? "" : b.getCategoryName(),
                    b.getPublisherName() == null ? "" : b.getPublisherName(),
                    b.getPublicationYear() == null ? "" : b.getPublicationYear(), b.getPrice(), b.getStockQuantity()});
        return rows;
    }

    @Override protected void fillForm(String id) {
        Book b = bus.getById(Integer.parseInt(id));
        if (b == null) return;
        txtId.setText(String.valueOf(b.getBookId())); txtTitle.setText(b.getTitle());
        txtIsbn.setText(b.getIsbn() == null ? "" : b.getIsbn());
        UIUtil.selectId(cboAuthor, b.getAuthorId()); UIUtil.selectId(cboCategory, b.getCategoryId());
        UIUtil.selectId(cboPublisher, b.getPublisherId());
        txtYear.setText(b.getPublicationYear() == null ? "" : String.valueOf(b.getPublicationYear()));
        txtPrice.setText(String.valueOf((long) b.getPrice())); txtQty.setText(String.valueOf(b.getStockQuantity()));
        txtDesc.setText(b.getDescription() == null ? "" : b.getDescription());
    }

    @Override protected void clearForm() {
        txtId.setText("(tự động)"); txtTitle.setText(""); txtIsbn.setText(""); txtYear.setText("");
        txtPrice.setText(""); txtQty.setText("0"); txtDesc.setText("");
        for (JComboBox<UIUtil.Item> c : List.of(cboAuthor, cboCategory, cboPublisher)) if (c.getItemCount() > 0) c.setSelectedIndex(0);
    }

    @Override protected void onAdd() { bus.add(fromForm(null)); }
    @Override protected void onUpdate(String id) { bus.update(fromForm(Integer.parseInt(id))); }
    @Override protected void onDelete(String id) { bus.delete(Integer.parseInt(id)); }

    private void importCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Chọn file CSV: Tên sách, ISBN, Tác giả, Thể loại, NXB, Năm XB, Giá, Số lượng");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String report = bus.importCsv(fc.getSelectedFile());
        loadLookups();
        refresh();
        UIUtil.info(this, report);
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("danhsach_sach.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        bus.exportCsv(fc.getSelectedFile(), bus.search(txtSearch.getText(), UIUtil.selectedId(cboFilter)));
        UIUtil.info(this, "Đã xuất file: " + fc.getSelectedFile().getAbsolutePath());
    }

    @Override public void reload() {
        UIUtil.run(this, () -> { bus.load(); loadLookups(); refresh(); });
    }
}
