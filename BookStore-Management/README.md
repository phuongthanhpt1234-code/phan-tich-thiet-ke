# BookStore-Management – Phần mềm Quản lý Nhà sách

Ứng dụng desktop **Java Swing** theo kiến trúc **3 lớp** (GUI – BUS – DAO – Model), build bằng **Maven**, dữ liệu lưu trên **MySQL/MariaDB** qua JDBC.
CSDL theo tài liệu `csdl_demo.docx` (29 bảng) + 1 bảng bổ sung `Categories`.

## Yêu cầu
JDK 17+, Maven 3.8+, MySQL 8+ hoặc XAMPP (MariaDB 10.4+).

## Cài đặt & chạy
1. Bật MySQL (XAMPP Control Panel → Start MySQL).
2. Nạp CSDL (xóa và tạo lại toàn bộ bảng + dữ liệu mẫu):
   ```bash
   /c/xampp/mysql/bin/mysql.exe -u root --default-character-set=utf8mb4 < database/bookstore.sql
   ```
   (hoặc phpMyAdmin → Import, bảng mã utf-8)
3. Sửa `src/main/resources/db.properties` nếu MySQL có mật khẩu / cổng khác.
4. Chạy: `mvn compile exec:java` — hoặc đóng gói: `mvn clean package` rồi `java -jar target/bookstore-management-1.0.0.jar`.

## Tài khoản mẫu
| Chức vụ | Tên đăng nhập | Mật khẩu | Quyền |
|---|---|---|---|
| Quản lý | `admin` | `admin123` | Toàn bộ (thêm tab Nhân viên, Thống kê, Chức vụ, Phòng ban) |
| Nhân viên bán hàng | `staff` | `staff123` | Bán hàng, Sách, Khách hàng, Danh mục (Thể loại/Tác giả/NXB), Kiểm kê |
| Thủ kho | `kho` | `kho12345` | Như nhân viên |

> Bảng `Account` trong tài liệu không có cột `role`, nên **quyền quản trị = nhân viên có chức vụ tên "Quản lý"** (bảng `Position`). Không được xóa/đổi tên chức vụ này.

## Chức năng
- **Bán hàng (POS)**: chọn sách → giỏ hàng → khách (hoặc vãng lai) → phương thức thanh toán (tiền mặt / thẻ / ví điện tử / chuyển khoản) → hóa đơn xem & lưu `.txt`.
  Thanh toán chạy trong **một giao dịch**: `Sales_Orders` → `Order_Items` → trừ `stock_quantity` → `Payments`; lỗi ở bước nào thì rollback toàn bộ.
- **Quản lý sách**: CRUD, tìm theo tên/tác giả/ISBN/mã, lọc thể loại, Import/Export CSV (tự tạo tác giả/thể loại/NXB chưa có).
- **Khách hàng**, **Danh mục** (Thể loại, Tác giả, Nhà xuất bản; admin thêm Chức vụ, Phòng ban), **Kiểm kê kho** (`BookInventory_Tasks`).
- **Nhân viên** (admin): thêm/sửa nhân viên + tài khoản (`Employee` + `Account`); "Thôi việc" = xóa mềm `isDeleted = 1`, có thể khôi phục.
- **Thống kê** (admin): doanh thu theo ngày, sách bán chạy, lịch sử đơn (xem hóa đơn, **hủy đơn** → hoàn kho + `refunded`), sách sắp hết, **lưu báo cáo** vào `Sales_Reports`.

## Khác biệt so với tài liệu `csdl_demo.docx`
| Thay đổi | Lý do |
|---|---|
| Thêm bảng `Categories` | `Books.category_id` có nhưng tài liệu không có bảng thể loại |
| Thêm cột `Books.author_id` | Bảng `Authors` chưa được nối với `Books` |
| `Account.username` có `UNIQUE` | Đăng nhập cần tên duy nhất |
| `Books.isbn` có `UNIQUE` | Tránh trùng sách |
| `Sales_Orders.customer_id` cho phép NULL | Khách vãng lai |
| Sửa tên `Tasts`→`Tasks`, `Booklnventory_Tasks`→`BookInventory_Tasks` | Lỗi chính tả trong tài liệu |
| `int(3)`… viết thành `INT` | MySQL 8 bỏ qua độ rộng hiển thị |
| Không có điểm tích lũy / giảm giá | Tài liệu không có cột tương ứng |

17 bảng nhân sự khác (TimeKeeping, Salaries, Goals, Tasks, Interviews…) đã được tạo trong CSDL nhưng chưa có màn hình.

## Định dạng CSV nhập sách
`Tên sách, ISBN, Tác giả, Thể loại, Nhà xuất bản, Năm xuất bản, Giá, Số lượng` (dòng tiêu đề là tùy chọn).

## Cấu trúc
```
src/main/java/com/bookstore/
├── Main.java
├── config/MySQLConnect.java
├── model/  Book, Category, Author, Publisher, Customer, Employee, Position, Department,
│           SalesOrder, OrderItem, Payment, PaymentMethod, SalesReport, InventoryTask
├── dao/    DaoSupport + BookDAO, CategoryDAO, AuthorDAO, PublisherDAO, CustomerDAO, EmployeeDAO,
│           PositionDAO, DepartmentDAO, SalesOrderDAO, OrderItemDAO, PaymentDAO, SalesReportDAO, InventoryTaskDAO
├── bus/    BookBUS, CategoryBUS, AuthorBUS, PublisherBUS, CustomerBUS, EmployeeBUS, PositionBUS,
│           DepartmentBUS, SalesOrderBUS, SalesReportBUS, InventoryTaskBUS
├── gui/    LoginFrame, MainFrame, QuanLyBanHangGUI, QuanLySachGUI, QuanLyKhachHangGUI, CatalogTabs,
│           QuanLyTheLoaiGUI, QuanLyTacGiaGUI, QuanLyNhaXuatBanGUI, QuanLyChucVuGUI, QuanLyPhongBanGUI,
│           KiemKeKhoGUI, QuanLyNhanVienGUI, ThongKeGUI, CrudPanel, UIUtil, Refreshable
└── util/   AppException, CsvUtil, PasswordUtil, Validate
database/bookstore.sql
```
