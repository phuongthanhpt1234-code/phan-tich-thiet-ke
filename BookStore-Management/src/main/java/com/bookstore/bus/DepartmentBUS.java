package com.bookstore.bus;

import com.bookstore.dao.DepartmentDAO;
import com.bookstore.model.Department;
import com.bookstore.util.Validate;

import java.util.List;
import java.util.stream.Collectors;

public class DepartmentBUS {
    private final DepartmentDAO dao = new DepartmentDAO();

    public List<Department> getAll() { return dao.selectAll(); }
    public Department getById(int id) { return dao.selectById(id); }

    public List<Department> search(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        return getAll().stream().filter(d -> k.isEmpty() || d.getDepartmentName().toLowerCase().contains(k)
                || String.valueOf(d.getId()).equals(k)).collect(Collectors.toList());
    }

    private void validate(Department d) {
        Validate.required(d.getDepartmentName(), "Tên phòng ban");
        Validate.maxLen(d.getDepartmentName(), 50, "Tên phòng ban");
        d.setDepartmentName(d.getDepartmentName().trim());
    }

    public void add(Department d) { validate(d); dao.insert(d); }
    public void update(Department d) { validate(d); dao.update(d); }
    public void delete(int id) { dao.delete(id); }
}
