package com.bookstore.bus;

import com.bookstore.dao.PositionDAO;
import com.bookstore.model.Position;
import com.bookstore.util.AppException;
import com.bookstore.util.Validate;

import java.util.List;
import java.util.stream.Collectors;

public class PositionBUS {
    private final PositionDAO dao = new PositionDAO();

    public List<Position> getAll() { return dao.selectAll(); }
    public Position getById(int id) { return dao.selectById(id); }

    public List<Position> search(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        return getAll().stream().filter(p -> k.isEmpty() || p.getPositionName().toLowerCase().contains(k)
                || String.valueOf(p.getId()).equals(k)).collect(Collectors.toList());
    }

    private void validate(Position p) {
        Validate.required(p.getPositionName(), "Tên chức vụ");
        Validate.maxLen(p.getPositionName(), 50, "Tên chức vụ");
        p.setPositionName(p.getPositionName().trim());
    }

    public void add(Position p) { validate(p); dao.insert(p); }

    public void update(Position p) {
        validate(p);
        Position old = dao.selectById(p.getId());
        if (old != null && old.getPositionName().equalsIgnoreCase(com.bookstore.model.Employee.ADMIN_POSITION)
                && !p.getPositionName().equalsIgnoreCase(com.bookstore.model.Employee.ADMIN_POSITION))
            throw new AppException("Không được đổi tên chức vụ \"" + com.bookstore.model.Employee.ADMIN_POSITION
                    + "\" vì đây là chức vụ cấp quyền quản trị.");
        dao.update(p);
    }

    public void delete(int id) {
        Position old = dao.selectById(id);
        if (old != null && old.getPositionName().equalsIgnoreCase(com.bookstore.model.Employee.ADMIN_POSITION))
            throw new AppException("Không được xóa chức vụ cấp quyền quản trị.");
        dao.delete(id);
    }
}
