-- =====================================================================
-- CSDL Quản lý Nhà sách (BookStore Management)
-- =====================================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS bookstore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bookstore;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS Sales_Reports, Payments, Order_Items, Sales_Orders, Customers,
    Books, Publishers, Authors, Categories, Account, Employee, `Position`;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================ NHÂN VIÊN & PHÂN QUYỀN ============================
CREATE TABLE `Position` (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    position_name VARCHAR(50) NOT NULL UNIQUE,
    description   TEXT
);

CREATE TABLE Employee (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(50) NOT NULL,
    position_id         INT NULL,
    date_of_birth       DATE,
    gender              ENUM('male','female'),
    phone_number        CHAR(10),
    address             VARCHAR(100),
    email               VARCHAR(100),
    hire_date           DATE,
    salary              DECIMAL(10,2),
    work_type           ENUM('full-time','part-time','internship','on_board','work_from_home'),
    isDeleted           TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (position_id) REFERENCES `Position`(id) ON DELETE SET NULL
);

CREATE TABLE Account (
    employee_id INT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,          -- SHA-256 (hex)
    FOREIGN KEY (employee_id) REFERENCES Employee(id) ON DELETE CASCADE
);

-- ============================ NHÀ SÁCH & BÁN HÀNG ============================
CREATE TABLE Categories (
    category_id   INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE Authors (
    author_id   INT AUTO_INCREMENT PRIMARY KEY,
    author_name VARCHAR(100) NOT NULL,
    bio         TEXT
);

CREATE TABLE Publishers (
    publisher_id   INT AUTO_INCREMENT PRIMARY KEY,
    publisher_name VARCHAR(100) NOT NULL UNIQUE,
    phone          CHAR(10),
    email          VARCHAR(100),
    address        VARCHAR(150)
);

CREATE TABLE Books (
    book_id          INT AUTO_INCREMENT PRIMARY KEY,
    title            VARCHAR(150) NOT NULL,
    isbn             VARCHAR(20) UNIQUE,
    author_id        INT NULL,
    category_id      INT NULL,
    publisher_id     INT NULL,
    publication_year INT,
    price            DECIMAL(10,2) NOT NULL CHECK (price > 0),
    stock_quantity   INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    description      TEXT,
    FOREIGN KEY (author_id)    REFERENCES Authors(author_id)       ON DELETE SET NULL,
    FOREIGN KEY (category_id)  REFERENCES Categories(category_id)  ON DELETE SET NULL,
    FOREIGN KEY (publisher_id) REFERENCES Publishers(publisher_id) ON DELETE SET NULL
);

CREATE TABLE Customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100),
    phone       CHAR(10),
    address     VARCHAR(150),
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Sales_Orders (
    order_id     INT AUTO_INCREMENT PRIMARY KEY,
    customer_id  INT NULL,
    employee_id  INT NOT NULL,
    order_date   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12,2) NOT NULL,
    order_status ENUM('pending','completed','cancelled') NOT NULL DEFAULT 'pending',
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id) ON DELETE SET NULL,
    FOREIGN KEY (employee_id) REFERENCES Employee(id)
);

CREATE TABLE Order_Items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id      INT NOT NULL,
    book_id       INT NOT NULL,
    quantity      INT NOT NULL CHECK (quantity > 0),
    unit_price    DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Sales_Orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id)  REFERENCES Books(book_id)
);

CREATE TABLE Payments (
    payment_id     INT AUTO_INCREMENT PRIMARY KEY,
    order_id       INT NOT NULL,
    payment_date   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    amount         DECIMAL(12,2) NOT NULL,
    payment_method ENUM('cash','credit_card','e_wallet','bank_transfer') NOT NULL,
    payment_status ENUM('completed','failed','refunded') NOT NULL DEFAULT 'completed',
    FOREIGN KEY (order_id) REFERENCES Sales_Orders(order_id) ON DELETE CASCADE
);

CREATE TABLE Sales_Reports (
    report_id   INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    total_sales DECIMAL(15,2) NOT NULL,
    report_date DATE NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES Employee(id)
);

-- ============================ DỮ LIỆU MẪU ============================
INSERT INTO `Position`(id, position_name, description) VALUES
(1, 'Quản lý', 'Quản lý cửa hàng, toàn quyền hệ thống'),
(2, 'Nhân viên bán hàng', 'Bán hàng, quản lý sách và khách hàng');

INSERT INTO Employee(id, name, position_id, date_of_birth, gender, phone_number, address, email, hire_date, salary, work_type, isDeleted) VALUES
(1, 'Nguyễn Quản Lý', 1, '1990-05-20', 'male',   '0901111111', 'TP. Hồ Chí Minh', 'quanly@nhasach.vn',  '2020-01-01', 15000000, 'full-time', 0),
(2, 'Trần Thị Bán Hàng', 2, '1999-08-12', 'female', '0902222222', 'TP. Hồ Chí Minh', 'banhang@nhasach.vn', '2023-03-15', 8000000, 'full-time', 0);

-- Tài khoản: admin / admin123 (Quản lý) ; staff / staff123 (Bán hàng)
INSERT INTO Account(employee_id, username, password) VALUES
(1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9'),
(2, 'staff', '10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6');

INSERT INTO Categories(category_id, category_name) VALUES
(1, 'Văn học'), (2, 'Kinh tế'), (3, 'Thiếu nhi'), (4, 'Kỹ năng sống'), (5, 'Công nghệ thông tin'), (6, 'Ngoại ngữ');

INSERT INTO Authors(author_id, author_name, bio) VALUES
(1, 'Tô Hoài', 'Nhà văn Việt Nam, tác giả Dế Mèn phiêu lưu ký'),
(2, 'Paulo Coelho', 'Nhà văn người Brazil'),
(3, 'Dale Carnegie', 'Nhà văn và nhà diễn thuyết người Mỹ'),
(4, 'Robert Kiyosaki', 'Doanh nhân, tác giả sách tài chính cá nhân'),
(5, 'Robert C. Martin', 'Kỹ sư phần mềm, tác giả Clean Code'),
(6, 'Nguyễn Văn A', 'Giảng viên công nghệ thông tin'),
(7, 'Lê Thị B', 'Giáo viên ngoại ngữ'),
(8, 'Nguyễn Nhật Ánh', 'Nhà văn Việt Nam'),
(9, 'Yuval Noah Harari', 'Nhà sử học người Israel'),
(10, 'Fujiko F. Fujio', 'Tác giả truyện tranh Doraemon');

INSERT INTO Publishers(publisher_id, publisher_name, phone, email, address) VALUES
(1, 'NXB Kim Đồng', '0243942030', 'info@nxbkimdong.com.vn', 'Hà Nội'),
(2, 'NXB Hội Nhà Văn', '0243822234', 'info@nxbhnv.vn', 'Hà Nội'),
(3, 'NXB Tổng hợp TP.HCM', '0283822534', 'info@nxbhcm.vn', 'TP. Hồ Chí Minh'),
(4, 'NXB Trẻ', '0283931619', 'hopthubandoc@nxbtre.com.vn', 'TP. Hồ Chí Minh'),
(5, 'NXB Prentice Hall', '0000000000', 'contact@prenhall.com', 'Hoa Kỳ'),
(6, 'NXB Giáo dục', '0243822136', 'info@nxbgd.vn', 'Hà Nội'),
(7, 'NXB Tri Thức', '0243945572', 'info@nxbtrithuc.com.vn', 'Hà Nội');

INSERT INTO Books(book_id, title, isbn, author_id, category_id, publisher_id, publication_year, price, stock_quantity, description) VALUES
(1,  'Dế Mèn phiêu lưu ký', '9786042000011', 1, 3, 1, 2019, 45000, 50, 'Truyện thiếu nhi kinh điển'),
(2,  'Nhà giả kim', '9786042000028', 2, 1, 2, 2020, 79000, 40, 'Hành trình đi tìm kho báu của chàng chăn cừu'),
(3,  'Đắc nhân tâm', '9786042000035', 3, 4, 3, 2018, 86000, 60, 'Nghệ thuật đối nhân xử thế'),
(4,  'Cha giàu cha nghèo', '9786042000042', 4, 2, 4, 2017, 98000, 35, 'Bài học về tài chính cá nhân'),
(5,  'Clean Code', '9786042000059', 5, 5, 5, 2008, 350000, 15, 'Cẩm nang viết mã sạch'),
(6,  'Lập trình Java cơ bản', '9786042000066', 6, 5, 6, 2021, 120000, 25, 'Giáo trình Java cho người mới'),
(7,  'Tiếng Anh giao tiếp', '9786042000073', 7, 6, 6, 2022, 95000, 8, 'Mẫu câu giao tiếp hằng ngày'),
(8,  'Tôi thấy hoa vàng trên cỏ xanh', '9786042000080', 8, 1, 4, 2015, 125000, 70, 'Tiểu thuyết tuổi thơ'),
(9,  'Sapiens: Lược sử loài người', '9786042000097', 9, 2, 7, 2016, 189000, 20, 'Lịch sử loài người'),
(10, 'Doraemon tập 1', '9786042000103', 10, 3, 1, 2020, 22000, 100, 'Truyện tranh thiếu nhi');

INSERT INTO Customers(customer_id, full_name, email, phone, address) VALUES
(1, 'Nguyễn Văn An', 'an@example.com', '0901234567', 'Quận 1, TP. Hồ Chí Minh'),
(2, 'Trần Thị Bình', 'binh@example.com', '0912345678', 'Quận 3, TP. Hồ Chí Minh'),
(3, 'Lê Hoàng Cường', 'cuong@example.com', '0987654321', 'Thủ Đức, TP. Hồ Chí Minh');
