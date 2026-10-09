package com.bookstore.gui;

import com.bookstore.bus.EmployeeBUS;
import com.bookstore.bus.InventoryTaskBUS;
import com.bookstore.model.Employee;
import com.bookstore.model.InventoryTask;
import com.bookstore.util.AppException;
import com.bookstore.util.Validate;

import javax.swing.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Nhiệm vụ kiểm kê kho sách (bảng BookInventory_Tasks). */
public class KiemKeKhoGUI extends CrudPanel {
    private final InventoryTaskBUS bus = new InventoryTaskBUS();
    private final EmployeeBUS employeeBUS = new EmployeeBUS();
    private final Employee current;
    private final JTextField txtId = new JTextField(), txtDate = new JTextField();
    private final JComboBox<UIUtil.Item> cboEmployee = new JComboBox<>();
    private final JTextArea txtNotes = UIUtil.textArea(6);

    public KiemKeKhoGUI(Employee current) {
        super(new String[]{"Mã", "Nhân viên phụ trách", "Ngày kiểm kê", "Ghi chú"});
        this.current = current;
        txtId.setEditable(false);
        addField("Mã nhiệm vụ:", txtId);
        addField("Nhân viên:", cboEmployee);
        addField("Ngày kiểm kê:", txtDate);
        addField("Ghi chú:", new JScrollPane(txtNotes));
        loadEmployees();
        clearForm();
        init();
    }

    private void loadEmployees() {
        List<UIUtil.Item> items = new ArrayList<>();
        for (Employee e : employeeBUS.getActive()) items.add(new UIUtil.Item(e.getId(), e.getName()));
        UIUtil.fillCombo(cboEmployee, items, null);
    }

    private InventoryTask fromForm(Integer id) {
        InventoryTask t = new InventoryTask();
        if (id != null) t.setTaskId(id);
        Integer emp = UIUtil.selectedId(cboEmployee);
        if (emp == null) throw new AppException("Vui lòng chọn nhân viên phụ trách.");
        t.setEmployeeId(emp);
        t.setCheckDate(Validate.date(txtDate.getText(), "Ngày kiểm kê"));
        t.setNotes(txtNotes.getText());
        return t;
    }

    @Override protected List<Object[]> loadRows(String k) {
        List<Object[]> rows = new ArrayList<>();
        for (InventoryTask t : bus.search(k)) rows.add(new Object[]{t.getTaskId(), t.getEmployeeName(), t.getCheckDate(), t.getNotes() == null ? "" : t.getNotes()});
        return rows;
    }

    @Override protected void fillForm(String id) {
        InventoryTask t = bus.getById(Integer.parseInt(id));
        if (t == null) return;
        txtId.setText(String.valueOf(t.getTaskId())); UIUtil.selectId(cboEmployee, t.getEmployeeId());
        txtDate.setText(String.valueOf(t.getCheckDate())); txtNotes.setText(t.getNotes() == null ? "" : t.getNotes());
    }

    @Override protected void clearForm() {
        txtId.setText("(tự động)"); txtDate.setText(LocalDate.now().toString()); txtNotes.setText("");
        UIUtil.selectId(cboEmployee, current.getId());
    }

    @Override protected void onAdd() { bus.add(fromForm(null)); }
    @Override protected void onUpdate(String id) { bus.update(fromForm(Integer.parseInt(id))); }
    @Override protected void onDelete(String id) { bus.delete(Integer.parseInt(id)); }
    @Override public void reload() { UIUtil.run(this, () -> { loadEmployees(); refresh(); }); }
}
