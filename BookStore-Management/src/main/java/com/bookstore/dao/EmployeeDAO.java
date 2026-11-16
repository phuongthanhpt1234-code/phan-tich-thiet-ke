package com.bookstore.dao;

import com.bookstore.config.MySQLConnect;
import com.bookstore.model.Employee;
import com.bookstore.util.AppException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.bookstore.dao.DaoSupport.nz;

/**
 * Nhân viên = bảng Employee (thông tin) + Account (đăng nhập). Chỉ SELECT các cột cần dùng,
 * không đọc số tài khoản ngân hàng / mã số thuế / số BHXH.
 */
public class EmployeeDAO {
    private static final String SELECT =
            "SELECT e.id, e.name, e.position_id, e.date_of_birth, e.gender, e.phone_number, e.address, e.email, " +
            "e.hire_date, e.salary, e.work_type, e.isDeleted, p.position_name, a.username, a.password " +
            "FROM Employee e LEFT JOIN `Position` p ON e.position_id = p.id " +
            "LEFT JOIN Account a ON a.employee_id = e.id ";

    private static final DaoSupport.Mapper<Employee> MAP = rs -> {
        Employee e = new Employee();
        e.setId(rs.getInt("id")); e.setName(rs.getString("name"));
        e.setPositionId(DaoSupport.intOrNull(rs, "position_id"));
        e.setDateOfBirth(DaoSupport.date(rs, "date_of_birth")); e.setGender(rs.getString("gender"));
        e.setPhoneNumber(rs.getString("phone_number")); e.setAddress(rs.getString("address")); e.setEmail(rs.getString("email"));
        e.setHireDate(DaoSupport.date(rs, "hire_date")); e.setSalary(DaoSupport.doubleOrNull(rs, "salary"));
        e.setWorkType(rs.getString("work_type"));
        e.setDeleted(rs.getInt("isDeleted") == 1);
        e.setPositionName(rs.getString("position_name"));
        e.setUsername(rs.getString("username"));
        return e;
    };

    public List<Employee> selectAll() { return DaoSupport.query(SELECT + "ORDER BY e.id", MAP); }
    public Employee selectById(int id) { return DaoSupport.queryOne(SELECT + "WHERE e.id=?", MAP, id); }

    /** Trả về {Employee, mã băm mật khẩu} theo tên đăng nhập, hoặc null. */
    public Object[] selectForLogin(String username) {
        return DaoSupport.queryOne(SELECT + "WHERE a.username=?",
                rs -> new Object[]{MAP.map(rs), rs.getString("password")}, username);
    }

    public boolean usernameExists(String username, int exceptEmployeeId) {
        return DaoSupport.queryOne("SELECT COUNT(*) FROM Account WHERE username=? AND employee_id<>?",
                rs -> rs.getInt(1), username, exceptEmployeeId) > 0;
    }

    private Object[] employeeArgs(Employee e) {
        return new Object[]{e.getName(), e.getPositionId(), e.getDateOfBirth(), nz(e.getGender()),
                nz(e.getPhoneNumber()), nz(e.getAddress()), nz(e.getEmail()), e.getHireDate(), e.getSalary(),
                nz(e.getWorkType()), e.isDeleted() ? 1 : 0};
    }

    /** Thêm Employee và Account trong cùng một giao dịch. Trả về mã nhân viên mới. */
    public int insertWithAccount(Employee e, String passwordHash) {
        try (Connection c = MySQLConnect.getConnection()) {
            c.setAutoCommit(false);
            try {
                int id = DaoSupport.insert(c, "INSERT INTO Employee(name, position_id, date_of_birth, gender, phone_number, address, email, hire_date, salary, work_type, isDeleted) VALUES (?,?,?,?,?,?,?,?,?,?,?)", employeeArgs(e));
                DaoSupport.update(c, "INSERT INTO Account(employee_id, username, password) VALUES (?,?,?)", id, e.getUsername(), passwordHash);
                c.commit();
                return id;
            } catch (SQLException ex) { c.rollback(); throw AppException.of(ex); }
        } catch (SQLException ex) { throw AppException.of(ex); }
    }

    /** Cập nhật Employee + Account; passwordHash = null thì giữ nguyên mật khẩu cũ. */
    public void updateWithAccount(Employee e, String passwordHash) {
        try (Connection c = MySQLConnect.getConnection()) {
            c.setAutoCommit(false);
            try {
                Object[] a = employeeArgs(e);
                Object[] args = java.util.Arrays.copyOf(a, a.length + 1);
                args[a.length] = e.getId();
                DaoSupport.update(c, "UPDATE Employee SET name=?, position_id=?, date_of_birth=?, gender=?, phone_number=?, address=?, email=?, hire_date=?, salary=?, work_type=?, isDeleted=? WHERE id=?", args);
                int n = passwordHash == null
                        ? DaoSupport.update(c, "UPDATE Account SET username=? WHERE employee_id=?", e.getUsername(), e.getId())
                        : DaoSupport.update(c, "UPDATE Account SET username=?, password=? WHERE employee_id=?", e.getUsername(), passwordHash, e.getId());
                if (n == 0 && passwordHash != null)
                    DaoSupport.update(c, "INSERT INTO Account(employee_id, username, password) VALUES (?,?,?)", e.getId(), e.getUsername(), passwordHash);
                c.commit();
            } catch (SQLException ex) { c.rollback(); throw AppException.of(ex); }
        } catch (SQLException ex) { throw AppException.of(ex); }
    }

    /** Xóa mềm: isDeleted = 1 (nhân viên thôi việc, vẫn giữ lịch sử hóa đơn). */
    public int softDelete(int id) { return DaoSupport.update("UPDATE Employee SET isDeleted=1 WHERE id=?", id); }
}
