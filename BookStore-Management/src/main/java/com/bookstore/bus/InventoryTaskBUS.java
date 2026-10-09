package com.bookstore.bus;

import com.bookstore.dao.InventoryTaskDAO;
import com.bookstore.model.InventoryTask;
import com.bookstore.util.AppException;

import java.util.List;
import java.util.stream.Collectors;

/** Nhiệm vụ kiểm kê kho sách (bảng BookInventory_Tasks). */
public class InventoryTaskBUS {
    private final InventoryTaskDAO dao = new InventoryTaskDAO();

    public List<InventoryTask> getAll() { return dao.selectAll(); }
    public InventoryTask getById(int id) { return dao.selectById(id); }

    public List<InventoryTask> search(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        return getAll().stream().filter(t -> k.isEmpty() || t.getEmployeeName().toLowerCase().contains(k)
                || (t.getNotes() != null && t.getNotes().toLowerCase().contains(k))
                || String.valueOf(t.getCheckDate()).contains(k)
                || String.valueOf(t.getTaskId()).equals(k)).collect(Collectors.toList());
    }

    private void validate(InventoryTask t) {
        if (t.getEmployeeId() <= 0) throw new AppException("Vui lòng chọn nhân viên phụ trách.");
        if (t.getCheckDate() == null) throw new AppException("Vui lòng nhập ngày kiểm kê (yyyy-MM-dd).");
    }

    public void add(InventoryTask t) { validate(t); dao.insert(t); }
    public void update(InventoryTask t) { validate(t); dao.update(t); }
    public void delete(int id) { dao.delete(id); }
}
