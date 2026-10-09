package com.bookstore.dao;

import com.bookstore.model.Author;

import java.util.List;

import static com.bookstore.dao.DaoSupport.nz;

public class AuthorDAO {
    private static final DaoSupport.Mapper<Author> MAP = rs -> {
        Author a = new Author();
        a.setAuthorId(rs.getInt("author_id")); a.setAuthorName(rs.getString("author_name")); a.setBio(rs.getString("bio"));
        return a;
    };

    public List<Author> selectAll() { return DaoSupport.query("SELECT * FROM Authors ORDER BY author_id", MAP); }
    public Author selectById(int id) { return DaoSupport.queryOne("SELECT * FROM Authors WHERE author_id=?", MAP, id); }
    public Author selectByName(String name) { return DaoSupport.queryOne("SELECT * FROM Authors WHERE author_name=? LIMIT 1", MAP, name); }
    public int insert(Author a) { return DaoSupport.insert("INSERT INTO Authors(author_name, bio) VALUES (?,?)", a.getAuthorName(), nz(a.getBio())); }
    public int update(Author a) { return DaoSupport.update("UPDATE Authors SET author_name=?, bio=? WHERE author_id=?", a.getAuthorName(), nz(a.getBio()), a.getAuthorId()); }
    public int delete(int id) { return DaoSupport.update("DELETE FROM Authors WHERE author_id=?", id); }
}
