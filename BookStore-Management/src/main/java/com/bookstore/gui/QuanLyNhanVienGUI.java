package com.bookstore.gui;

import com.bookstore.bus.EmployeeBUS;
import com.bookstore.bus.PositionBUS;
import com.bookstore.model.Employee;
import com.bookstore.model.Position;
import com.bookstore.util.Validate;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/** Quản lý nhân viên (Employee) và tài khoản đăng nhập (Account). "Xóa" = cho thôi việc (isDeleted = 1). */
public class QuanLyNhanVienGUI extends CrudPanel {
    private static final String[] GENDER_LABEL = {"(không chọn)", "Nam", "Nữ"};
    private static final String[] GENDER_CODE = {null, "male", "female"};
    private static final String[] WORK_LABEL = {"(không chọn)", "Toàn thời gian", "Bán thời gian", "Thực tập", "Tại văn phòng", "Làm việc từ xa"};
    private static final String[] WORK_CODE = {null, "full-time", "part-time", "internship", "on_board", "work_from_home"};

    private final EmployeeBUS bus = new EmployeeBUS();
    private final Employee current;
    private final JTextField txtId = new JTextField(), txtName = new JTextField(), txtDob = new JTextField(),
            txtPhone = new JTextField(), txtEmail = new JTextField(), txtAddress = new JTextField(), txtHire = new JTextField(),
            txtSalary = new JTextField(), txtUser = new JTextField();
    private final JPasswordField txtPass = new JPasswordField();
    private final JComboBox<UIUtil.Item> cboPosition = new JComboBox<>();
    private final JComboBox<String> cboGender = new JComboBox<>(GENDER_LABEL), cboWork = new JComboBox<>(WORK_LABEL),
            cboStatus = new JComboBox<>(new String[]{"Đang làm việc", "Đã thôi việc"});

    public QuanLyNhanVienGUI(Employee current) {
        super(new String[]{"Mã NV", "Họ tên", "Chức vụ", "Điện thoại", "Tên đăng nhập", "Trạng thái"});
        this.current = current;
        txtId.setEditable(false);
        addField("Mã NV:", txtId);
        addField("Họ tên:", txtName);
        addField("Chức vụ:", cboPosition);
        addField("Ngày sinh:", txtDob);
        addField("Giới tính:", cboGender);
        addField("Điện thoại:", txtPhone);
        addField("Email:", txtEmail);
        addField("Địa chỉ:", txtAddress);
        addField("Ngày vào làm:", txtHire);
        addField("Lương cơ bản (đ):", txtSalary);
        addField("Hình thức làm việc:", cboWork);
        addField("Tên đăng nhập:", txtUser);
        addField("Mật khẩu:", txtPass);
        addField("Trạng thái:", cboStatus);
        txtDob.setToolTipText("Định dạng: năm-tháng-ngày, ví dụ 1999-08-12");
        txtHire.setToolTipText("Định dạng: năm-tháng-ngày, ví dụ 2026-01-05");
        loadLookups();
        clearForm();
        setDeleteLabel("Thôi việc");
        init();
    }

    private void loadLookups() {
        List<UIUtil.Item> pos = new ArrayList<>();
        for (Position p : new PositionBUS().getAll()) pos.add(new UIUtil.Item(p.getId(), p.getPositionName()));
        UIUtil.fillCombo(cboPosition, pos, null);
    }

    private static int indexOf(String[] codes, String code) {
        for (int i = 0; i < codes.length; i++) if (codes[i] == null ? code == null : codes[i].equals(code)) return i;
        return 0;
    }

    private static String s(String v) { return v == null ? "" : v; }

    private Employee fromForm(Integer id) {
        Employee e = new Employee();
        Validate.required(txtName.getText(), "Họ tên nhân viên");   // báo lỗi theo thứ tự các ô trên form
        if (id != null) e.setId(id);
        e.setName(txtName.getText());
        e.setPositionId(UIUtil.selectedId(cboPosition));
        e.setDateOfBirth(Validate.date(txtDob.getText(), "Ngày sinh"));
        e.setGender(GENDER_CODE[cboGender.getSelectedIndex()]);
        e.setPhoneNumber(txtPhone.getText().trim()); e.setEmail(txtEmail.getText()); e.setAddress(txtAddress.getText());
        e.setHireDate(Validate.date(txtHire.getText(), "Ngày vào làm"));
        e.setSalary(UIUtil.parseMoneyOrNull(txtSalary.getText(), "Lương cơ bản"));
        e.setWorkType(WORK_CODE[cboWork.getSelectedIndex()]);
        e.setUsername(txtUser.getText());
        e.setDeleted(cboStatus.getSelectedIndex() == 1);
        return e;
    }

    @Override protected List<Object[]> loadRows(String k) {
        List<Object[]> rows = new ArrayList<>();
        for (Employee e : bus.search(k)) rows.add(new Object[]{e.getId(), e.getName(), s(e.getPositionName()),
                s(e.getPhoneNumber()), s(e.getUsername()), e.isDeleted() ? "Đã thôi việc" : "Đang làm việc"});
        return rows;
    }

    @Override protected void fillForm(String id) {
        Employee e = bus.getById(Integer.parseInt(id));
        if (e == null) return;
        txtId.setText(String.valueOf(e.getId())); txtName.setText(e.getName());
        UIUtil.selectId(cboPosition, e.getPositionId());
        txtDob.setText(e.getDateOfBirth() == null ? "" : e.getDateOfBirth().toString());
        cboGender.setSelectedIndex(indexOf(GENDER_CODE, e.getGender()));
        txtPhone.setText(s(e.getPhoneNumber())); txtEmail.setText(s(e.getEmail())); txtAddress.setText(s(e.getAddress()));
        txtHire.setText(e.getHireDate() == null ? "" : e.getHireDate().toString());
        txtSalary.setText(e.getSalary() == null ? "" : String.valueOf(e.getSalary().longValue()));
        cboWork.setSelectedIndex(indexOf(WORK_CODE, e.getWorkType()));
        txtUser.setText(s(e.getUsername())); txtPass.setText("");
        cboStatus.setSelectedIndex(e.isDeleted() ? 1 : 0);
    }

    @Override protected void clearForm() {
        txtId.setText("(tự động)");
        for (JTextField t : List.of(txtName, txtDob, txtPhone, txtEmail, txtAddress, txtHire, txtSalary, txtUser)) t.setText("");
        txtPass.setText("");
        cboGender.setSelectedIndex(0); cboWork.setSelectedIndex(0); cboStatus.setSelectedIndex(0);
        if (cboPosition.getItemCount() > 0) cboPosition.setSelectedIndex(0);
    }

    @Override protected void onAdd() { bus.add(fromForm(null), new String(txtPass.getPassword())); }

    /** Để trống ô mật khẩu = giữ nguyên mật khẩu cũ. */
    @Override protected void onUpdate(String id) {
        bus.update(fromForm(Integer.parseInt(id)), new String(txtPass.getPassword()), current.getId());
    }

    @Override protected void onDelete(String id) { bus.delete(Integer.parseInt(id), current.getId()); }
    @Override protected String deleteMessage(String id) { return "Cho nhân viên mã " + id + " thôi việc? (Tài khoản sẽ bị khóa, lịch sử đơn hàng vẫn được giữ; có thể khôi phục bằng nút Sửa)"; }
    @Override public void reload() { UIUtil.run(this, () -> { loadLookups(); refresh(); }); }
}
