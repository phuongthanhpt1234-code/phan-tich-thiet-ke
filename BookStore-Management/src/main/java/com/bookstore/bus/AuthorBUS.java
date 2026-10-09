package com.bookstore.bus;

import com.bookstore.dao.AuthorDAO;
import com.bookstore.model.Author;
import com.bookstore.util.Validate;

import java.util.List;
import java.util.stream.Collectors;

public class AuthorBUS {
    private final AuthorDAO dao = new AuthorDAO();

    public List<Author> getAll() { return dao.selectAll(); }
    public Author getById(int id) { return dao.selectById(id); }

    public List<Author> search(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        return getAll().stream().filter(a -> k.isEmpty() || a.getAuthorName().toLowerCase().contains(k)
                || String.valueOf(a.getAuthorId()).equals(k)).collect(Collectors.toList());
    }

    private void validate(Author a) {
        Validate.required(a.getAuthorName(), "Tên tác giả");
        Validate.maxLen(a.getAuthorName(), 100, "Tên tác giả");
        a.setAuthorName(a.getAuthorName().trim());
    }

    public void add(Author a) { validate(a); dao.insert(a); }
    public void update(Author a) { validate(a); dao.update(a); }
    public void delete(int id) { dao.delete(id); }

    public Integer findOrCreate(String name) {
        if (Validate.blank(name)) return null;
        Author a = dao.selectByName(name.trim());
        if (a != null) return a.getAuthorId();
        Author n = new Author();
        n.setAuthorName(name.trim());
        return dao.insert(n);
    }
}
