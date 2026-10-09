package com.bookstore.dao;

import com.bookstore.model.Category;

import java.util.List;

public class CategoryDAO {
    private static final DaoSupport.Mapper<Category> MAP =
            rs -> { Category c = new Category(); c.setCategoryId(rs.getInt("category_id")); c.setCategoryName(rs.getString("category_name")); return c; };

    public List<Category> selectAll() { return DaoSupport.query("SELECT * FROM Categories ORDER BY category_id", MAP); }
    public Category selectById(int id) { return DaoSupport.queryOne("SELECT * FROM Categories WHERE category_id=?", MAP, id); }
    public Category selectByName(String name) { return DaoSupport.queryOne("SELECT * FROM Categories WHERE category_name=?", MAP, name); }
    public int insert(Category c) { return DaoSupport.insert("INSERT INTO Categories(category_name) VALUES (?)", c.getCategoryName()); }
    public int update(Category c) { return DaoSupport.update("UPDATE Categories SET category_name=? WHERE category_id=?", c.getCategoryName(), c.getCategoryId()); }
    public int delete(int id) { return DaoSupport.update("DELETE FROM Categories WHERE category_id=?", id); }
}
