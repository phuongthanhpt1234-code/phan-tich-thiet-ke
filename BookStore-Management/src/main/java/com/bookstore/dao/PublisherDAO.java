package com.bookstore.dao;

import com.bookstore.model.Publisher;

import java.util.List;

import static com.bookstore.dao.DaoSupport.nz;

public class PublisherDAO {
    private static final DaoSupport.Mapper<Publisher> MAP = rs -> {
        Publisher p = new Publisher();
        p.setPublisherId(rs.getInt("publisher_id")); p.setPublisherName(rs.getString("publisher_name"));
        p.setPhone(rs.getString("phone")); p.setEmail(rs.getString("email")); p.setAddress(rs.getString("address"));
        return p;
    };

    public List<Publisher> selectAll() { return DaoSupport.query("SELECT * FROM Publishers ORDER BY publisher_id", MAP); }
    public Publisher selectById(int id) { return DaoSupport.queryOne("SELECT * FROM Publishers WHERE publisher_id=?", MAP, id); }
    public Publisher selectByName(String name) { return DaoSupport.queryOne("SELECT * FROM Publishers WHERE publisher_name=?", MAP, name); }
    public int insert(Publisher p) {
        return DaoSupport.insert("INSERT INTO Publishers(publisher_name, phone, email, address) VALUES (?,?,?,?)",
                p.getPublisherName(), nz(p.getPhone()), nz(p.getEmail()), nz(p.getAddress()));
    }
    public int update(Publisher p) {
        return DaoSupport.update("UPDATE Publishers SET publisher_name=?, phone=?, email=?, address=? WHERE publisher_id=?",
                p.getPublisherName(), nz(p.getPhone()), nz(p.getEmail()), nz(p.getAddress()), p.getPublisherId());
    }
    public int delete(int id) { return DaoSupport.update("DELETE FROM Publishers WHERE publisher_id=?", id); }
}
