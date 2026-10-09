package com.bookstore.bus;

import com.bookstore.dao.CategoryDAO;
import com.bookstore.model.Category;
import com.bookstore.util.Validate;

import java.util.List;
import java.util.stream.Collectors;

public class CategoryBUS {
    private final CategoryDAO dao = new CategoryDAO();

    public List<Category> getAll() { return dao.selectAll(); }
    public Category getById(int id) { return dao.selectById(id); }

    public List<Category> search(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        return getAll().stream().filter(c -> k.isEmpty() || c.getCategoryName().toLowerCase().contains(k)
                || String.valueOf(c.getCategoryId()).equals(k)).collect(Collectors.toList());
    }

    private void validate(Category c) {
        Validate.required(c.getCategoryName(), "Tên thể loại");
        Validate.maxLen(c.getCategoryName(), 100, "Tên thể loại");
        c.setCategoryName(c.getCategoryName().trim());
    }

    public void add(Category c) { validate(c); dao.insert(c); }
    public void update(Category c) { validate(c); dao.update(c); }
    public void delete(int id) { dao.delete(id); }

    /** Dùng khi nhập CSV: trả về mã thể loại theo tên, chưa có thì tạo mới. */
    public Integer findOrCreate(String name) {
        if (Validate.blank(name)) return null;
        Category c = dao.selectByName(name.trim());
        if (c != null) return c.getCategoryId();
        Category n = new Category();
        n.setCategoryName(name.trim());
        return dao.insert(n);
    }
}
