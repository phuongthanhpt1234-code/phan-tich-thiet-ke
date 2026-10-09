package com.bookstore.bus;

import com.bookstore.dao.SalesReportDAO;
import com.bookstore.model.SalesReport;
import com.bookstore.util.AppException;

import java.time.LocalDate;
import java.util.List;

/** Lưu báo cáo doanh thu vào bảng Sales_Reports. */
public class SalesReportBUS {
    private final SalesReportDAO dao = new SalesReportDAO();

    public List<SalesReport> getAll() { return dao.selectAll(); }

    public void save(int employeeId, double totalSales) {
        if (totalSales < 0) throw new AppException("Doanh thu không hợp lệ.");
        SalesReport r = new SalesReport();
        r.setEmployeeId(employeeId); r.setTotalSales(totalSales); r.setReportDate(LocalDate.now());
        dao.insert(r);
    }
}
