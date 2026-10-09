package com.bookstore.dao;

import com.bookstore.model.Department;

import java.util.List;

public class DepartmentDAO {
    private static final String SELECT =
            "SELECT d.*, e.name AS manager_name FROM Departments d LEFT JOIN Employee e ON d.manager_id = e.id ";
    private static final DaoSupport.Mapper<Department> MAP = rs -> {
        Department d = new Department();
        d.setId(rs.getInt("id")); d.setDepartmentName(rs.getString("department_name"));
        d.setManagerId(DaoSupport.intOrNull(rs, "manager_id")); d.setManagerName(rs.getString("manager_name"));
        return d;
    };

    public List<Department> selectAll() { return DaoSupport.query(SELECT + "ORDER BY d.id", MAP); }
    public Department selectById(int id) { return DaoSupport.queryOne(SELECT + "WHERE d.id=?", MAP, id); }
    public int insert(Department d) { return DaoSupport.insert("INSERT INTO Departments(department_name, manager_id) VALUES (?,?)", d.getDepartmentName(), d.getManagerId()); }
    public int update(Department d) { return DaoSupport.update("UPDATE Departments SET department_name=?, manager_id=? WHERE id=?", d.getDepartmentName(), d.getManagerId(), d.getId()); }
    public int delete(int id) { return DaoSupport.update("DELETE FROM Departments WHERE id=?", id); }
}
