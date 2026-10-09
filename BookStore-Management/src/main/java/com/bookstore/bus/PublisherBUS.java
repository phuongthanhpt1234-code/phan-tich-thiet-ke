package com.bookstore.bus;

import com.bookstore.dao.PublisherDAO;
import com.bookstore.model.Publisher;
import com.bookstore.util.Validate;

import java.util.List;
import java.util.stream.Collectors;

public class PublisherBUS {
    private final PublisherDAO dao = new PublisherDAO();

    public List<Publisher> getAll() { return dao.selectAll(); }
    public Publisher getById(int id) { return dao.selectById(id); }

    public List<Publisher> search(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        return getAll().stream().filter(p -> k.isEmpty() || p.getPublisherName().toLowerCase().contains(k)
                || (p.getPhone() != null && p.getPhone().contains(k))
                || String.valueOf(p.getPublisherId()).equals(k)).collect(Collectors.toList());
    }

    private void validate(Publisher p) {
        Validate.required(p.getPublisherName(), "Tên nhà xuất bản");
        Validate.maxLen(p.getPublisherName(), 100, "Tên nhà xuất bản");
        Validate.phone(p.getPhone(), "Số điện thoại");
        Validate.email(p.getEmail());
        Validate.maxLen(p.getAddress(), 150, "Địa chỉ");
        p.setPublisherName(p.getPublisherName().trim());
    }

    public void add(Publisher p) { validate(p); dao.insert(p); }
    public void update(Publisher p) { validate(p); dao.update(p); }
    public void delete(int id) { dao.delete(id); }

    public Integer findOrCreate(String name) {
        if (Validate.blank(name)) return null;
        Publisher p = dao.selectByName(name.trim());
        if (p != null) return p.getPublisherId();
        Publisher n = new Publisher();
        n.setPublisherName(name.trim());
        return dao.insert(n);
    }
}
