package com.bookstore.dao;

import com.bookstore.model.SalesReport;

import java.util.List;

public class SalesReportDAO {
    private static final DaoSupport.Mapper<SalesReport> MAP = rs -> {
        SalesReport r = new SalesReport();
        r.setReportId(rs.getInt("report_id")); r.setEmployeeId(rs.getInt("employee_id"));
        r.setTotalSales(rs.getDouble("total_sales")); r.setReportDate(DaoSupport.date(rs, "report_date"));
        r.setEmployeeName(rs.getString("emp_name"));
        return r;
    };

    public List<SalesReport> selectAll() {
        return DaoSupport.query("SELECT r.*, e.name AS emp_name FROM Sales_Reports r JOIN Employee e ON r.employee_id = e.id ORDER BY r.report_id DESC", MAP);
    }

    public int insert(SalesReport r) {
        return DaoSupport.insert("INSERT INTO Sales_Reports(employee_id, total_sales, report_date) VALUES (?,?,?)",
                r.getEmployeeId(), r.getTotalSales(), r.getReportDate());
    }
}
