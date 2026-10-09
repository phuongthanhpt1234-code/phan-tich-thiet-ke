package com.bookstore.gui;

import com.bookstore.bus.DepartmentBUS;
import com.bookstore.bus.EmployeeBUS;
import com.bookstore.model.Department;
import com.bookstore.model.Employee;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class QuanLyPhongBanGUI extends CrudPanel {
    private final DepartmentBUS bus = new DepartmentBUS();
    private final EmployeeBUS employeeBUS = new EmployeeBUS();
    private final JTextField txtId = new JTextField(), txtName = new JTextField();
    private final JComboBox<UIUtil.Item> cboManager = new JComboBox<>();

    public QuanLyPhongBanGUI() {
        super(new String[]{"Mã", "Tên phòng ban", "Trưởng phòng"});
        txtId.setEditable(false);
        loadManagers();
        clearForm();
        addField("Mã phòng ban:", txtId);
        addField("Tên phòng ban:", txtName);
        addField("Trưởng phòng:", cboManager);
        init();
    }

    private void loadManagers() {
        List<UIUtil.Item> items = new ArrayList<>();
        for (Employee e : employeeBUS.getActive()) items.add(new UIUtil.Item(e.getId(), e.getName()));
        UIUtil.fillCombo(cboManager, items, "(chưa có)");
    }

    private Department fromForm(Integer id) {
        Department d = new Department();
        if (id != null) d.setId(id);
        d.setDepartmentName(txtName.getText());
        d.setManagerId(UIUtil.selectedId(cboManager));
        return d;
    }

    @Override protected List<Object[]> loadRows(String k) {
        List<Object[]> rows = new ArrayList<>();
        for (Department d : bus.search(k)) rows.add(new Object[]{d.getId(), d.getDepartmentName(), d.getManagerName() == null ? "" : d.getManagerName()});
        return rows;
    }

    @Override protected void fillForm(String id) {
        Department d = bus.getById(Integer.parseInt(id));
        if (d == null) return;
        txtId.setText(String.valueOf(d.getId())); txtName.setText(d.getDepartmentName());
        UIUtil.selectId(cboManager, d.getManagerId());
    }

    @Override protected void clearForm() { txtId.setText("(tự động)"); txtName.setText(""); if (cboManager.getItemCount() > 0) cboManager.setSelectedIndex(0); }
    @Override protected void onAdd() { bus.add(fromForm(null)); }
    @Override protected void onUpdate(String id) { bus.update(fromForm(Integer.parseInt(id))); }
    @Override protected void onDelete(String id) { bus.delete(Integer.parseInt(id)); }
    @Override protected String deleteMessage(String id) { return "Xóa phòng ban có mã " + id + "? Nhân viên thuộc phòng này sẽ chuyển thành chưa có phòng ban."; }
    @Override public void reload() { UIUtil.run(this, () -> { loadManagers(); refresh(); }); }
}
