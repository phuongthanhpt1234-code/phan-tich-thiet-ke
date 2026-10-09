package com.bookstore.dao;

import com.bookstore.model.InventoryTask;

import java.util.List;

import static com.bookstore.dao.DaoSupport.nz;

public class InventoryTaskDAO {
    private static final String SELECT = "SELECT t.*, e.name AS emp_name FROM BookInventory_Tasks t JOIN Employee e ON t.employee_id = e.id ";
    private static final DaoSupport.Mapper<InventoryTask> MAP = rs -> {
        InventoryTask t = new InventoryTask();
        t.setTaskId(rs.getInt("task_id")); t.setEmployeeId(rs.getInt("employee_id"));
        t.setCheckDate(DaoSupport.date(rs, "check_date")); t.setNotes(rs.getString("notes"));
        t.setEmployeeName(rs.getString("emp_name"));
        return t;
    };

    public List<InventoryTask> selectAll() { return DaoSupport.query(SELECT + "ORDER BY t.check_date DESC, t.task_id DESC", MAP); }
    public InventoryTask selectById(int id) { return DaoSupport.queryOne(SELECT + "WHERE t.task_id=?", MAP, id); }
    public int insert(InventoryTask t) { return DaoSupport.insert("INSERT INTO BookInventory_Tasks(employee_id, check_date, notes) VALUES (?,?,?)", t.getEmployeeId(), t.getCheckDate(), nz(t.getNotes())); }
    public int update(InventoryTask t) { return DaoSupport.update("UPDATE BookInventory_Tasks SET employee_id=?, check_date=?, notes=? WHERE task_id=?", t.getEmployeeId(), t.getCheckDate(), nz(t.getNotes()), t.getTaskId()); }
    public int delete(int id) { return DaoSupport.update("DELETE FROM BookInventory_Tasks WHERE task_id=?", id); }
}
