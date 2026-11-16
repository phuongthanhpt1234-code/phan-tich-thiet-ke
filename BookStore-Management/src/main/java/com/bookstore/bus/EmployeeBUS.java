package com.bookstore.bus;

import com.bookstore.dao.EmployeeDAO;
import com.bookstore.dao.PositionDAO;
import com.bookstore.model.Employee;
import com.bookstore.model.Position;
import com.bookstore.util.AppException;
import com.bookstore.util.PasswordUtil;
import com.bookstore.util.Validate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/** Nghiệp vụ nhân viên + tài khoản đăng nhập (Employee + Account). */
public class EmployeeBUS {
    private final EmployeeDAO dao = new EmployeeDAO();
    private final PositionDAO positionDAO = new PositionDAO();

    /** Đăng nhập: đúng tài khoản + mật khẩu và nhân viên chưa nghỉ việc (isDeleted = 0). */
    public Employee login(String username, String password) {
        if (Validate.blank(username) || password == null || password.isEmpty())
            throw new AppException("Vui lòng nhập tên đăng nhập và mật khẩu.");
        Object[] row = dao.selectForLogin(username.trim());
        if (row == null || !((String) row[1]).equals(PasswordUtil.hash(password)))
            throw new AppException("Sai tên đăng nhập hoặc mật khẩu.");
        Employee e = (Employee) row[0];
        if (e.isDeleted()) throw new AppException("Tài khoản đã bị khóa (nhân viên đã thôi việc).");
        return e;
    }

    public List<Employee> getAll() { return dao.selectAll(); }
    public Employee getById(int id) { return dao.selectById(id); }

    /** Nhân viên đang làm việc (dùng cho combobox người phụ trách). */
    public List<Employee> getActive() { return getAll().stream().filter(e -> !e.isDeleted()).collect(Collectors.toList()); }

    public List<Employee> search(String keyword) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        return getAll().stream().filter(e -> k.isEmpty() || e.getName().toLowerCase().contains(k)
                || (e.getUsername() != null && e.getUsername().toLowerCase().contains(k))
                || (e.getPhoneNumber() != null && e.getPhoneNumber().contains(k))
                || String.valueOf(e.getId()).equals(k)).collect(Collectors.toList());
    }

    private boolean isAdminPosition(Integer positionId) {
        if (positionId == null) return false;
        Position p = positionDAO.selectById(positionId);
        return p != null && p.getPositionName().equalsIgnoreCase(Employee.ADMIN_POSITION);
    }

    private void validate(Employee e) {
        Validate.required(e.getName(), "Họ tên nhân viên");
        Validate.maxLen(e.getName(), 50, "Họ tên");
        if (e.getPositionId() == null) throw new AppException("Vui lòng chọn chức vụ.");
        if (e.getUsername() == null || !e.getUsername().trim().matches("[A-Za-z0-9_.]{3,30}"))
            throw new AppException("Tên đăng nhập gồm 3-30 ký tự chữ/số/_/. không dấu.");
        Validate.phone(e.getPhoneNumber(), "Số điện thoại");
        Validate.email(e.getEmail());
        Validate.maxLen(e.getAddress(), 100, "Địa chỉ");
        if (e.getSalary() != null && (e.getSalary() < 0 || e.getSalary() >= 100_000_000))
            throw new AppException("Lương cơ bản phải từ 0 đến dưới 100.000.000.");
        LocalDate today = LocalDate.now();
        if (e.getDateOfBirth() != null && !e.getDateOfBirth().isBefore(today))
            throw new AppException("Ngày sinh phải trước ngày hôm nay.");
        if (e.getDateOfBirth() != null && e.getHireDate() != null && !e.getHireDate().isAfter(e.getDateOfBirth()))
            throw new AppException("Ngày vào làm phải sau ngày sinh.");
        e.setName(e.getName().trim());
        e.setUsername(e.getUsername().trim());
    }

    public void add(Employee e, String rawPassword) {
        validate(e);
        if (rawPassword == null || rawPassword.length() < 6) throw new AppException("Mật khẩu phải có ít nhất 6 ký tự.");
        if (dao.usernameExists(e.getUsername(), 0)) throw new AppException("Tên đăng nhập '" + e.getUsername() + "' đã tồn tại.");
        e.setDeleted(false);
        dao.insertWithAccount(e, PasswordUtil.hash(rawPassword));
    }

    /** rawPassword rỗng = giữ nguyên mật khẩu cũ. */
    public void update(Employee e, String rawPassword, int currentUserId) {
        validate(e);
        if (e.getId() == currentUserId && (e.isDeleted() || !isAdminPosition(e.getPositionId())))
            throw new AppException("Không thể cho nghỉ việc hoặc hạ quyền chính tài khoản đang đăng nhập.");
        if (dao.usernameExists(e.getUsername(), e.getId())) throw new AppException("Tên đăng nhập '" + e.getUsername() + "' đã tồn tại.");
        String hash = null;
        if (rawPassword != null && !rawPassword.isEmpty()) {
            if (rawPassword.length() < 6) throw new AppException("Mật khẩu phải có ít nhất 6 ký tự.");
            hash = PasswordUtil.hash(rawPassword);
        }
        dao.updateWithAccount(e, hash);
    }

    /** "Xóa" = xóa mềm (isDeleted = 1), giữ nguyên lịch sử đơn hàng. Có thể khôi phục bằng nút Sửa. */
    public void delete(int id, int currentUserId) {
        if (id == currentUserId) throw new AppException("Không thể cho nghỉ việc chính tài khoản đang đăng nhập.");
        dao.softDelete(id);
    }
}
