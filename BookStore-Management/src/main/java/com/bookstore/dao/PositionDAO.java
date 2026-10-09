package com.bookstore.dao;

import com.bookstore.model.Position;

import java.util.List;

import static com.bookstore.dao.DaoSupport.nz;

public class PositionDAO {
    private static final DaoSupport.Mapper<Position> MAP = rs -> {
        Position p = new Position();
        p.setId(rs.getInt("id")); p.setPositionName(rs.getString("position_name")); p.setDescription(rs.getString("description"));
        return p;
    };

    public List<Position> selectAll() { return DaoSupport.query("SELECT * FROM `Position` ORDER BY id", MAP); }
    public Position selectById(int id) { return DaoSupport.queryOne("SELECT * FROM `Position` WHERE id=?", MAP, id); }
    public int insert(Position p) { return DaoSupport.insert("INSERT INTO `Position`(position_name, description) VALUES (?,?)", p.getPositionName(), nz(p.getDescription())); }
    public int update(Position p) { return DaoSupport.update("UPDATE `Position` SET position_name=?, description=? WHERE id=?", p.getPositionName(), nz(p.getDescription()), p.getId()); }
    public int delete(int id) { return DaoSupport.update("DELETE FROM `Position` WHERE id=?", id); }
}
