-- =====================================================================
-- CSDL Quản lý nhà sách + nhân sự 

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS bookstore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bookstore;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS BookInventory_Tasks, Sales_Reports, Payments, Order_Items, Sales_Orders, Customers,
    Books, Publishers, Authors, Categories, JobOpenings, Applicants, Interviews, Interviewer,
    Trainings_Development, Train, Achievements, Reports, Account, AssignedTo, Tasks, Participants,
    Meeting_Schedule, Goals, SalaryChangeHistory, Salaries, TimeKeeping, Employee, Departments, `Position`;
SET FOREIGN_KEY_CHECKS = 1;

-- ============================ NHÂN SỰ ============================
CREATE TABLE `Position` (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    position_name VARCHAR(50) NOT NULL UNIQUE,
    description   TEXT
);

CREATE TABLE Departments (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    department_name VARCHAR(50) NOT NULL UNIQUE,
    manager_id      INT NULL          
);

CREATE TABLE Employee (
    id                      INT AUTO_INCREMENT PRIMARY KEY,
    name                    VARCHAR(50) NOT NULL,
    position_id             INT NULL,
    departments_id          INT NULL,
    previous_position       VARCHAR(50),
    date_of_birth           DATE,
    gender                  ENUM('male','female'),
    phone_number            CHAR(10),
    address                 VARCHAR(100),
    email                   VARCHAR(100),
    hire_date               DATE,
    salary                  DECIMAL(10,2),
    bank_account_number     VARCHAR(20),
    tax_code                VARCHAR(20),
    social_insurance_number VARCHAR(20),
    work_type               ENUM('full-time','part-time','internship','on_board','work_from_home'),
    `level`                 VARCHAR(10),
    image                   VARCHAR(255),
    isDeleted               TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (position_id)    REFERENCES `Position`(id)  ON DELETE SET NULL,
    FOREIGN KEY (departments_id) REFERENCES Departments(id) ON DELETE SET NULL
);
ALTER TABLE Departments ADD FOREIGN KEY (manager_id) REFERENCES Employee(id) ON DELETE SET NULL;

CREATE TABLE Account (
    employee_id INT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,          -- SHA-256 (hex)
    FOREIGN KEY (employee_id) REFERENCES Employee(id) ON DELETE CASCADE
);

CREATE TABLE TimeKeeping (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    check_in    DATETIME,
    check_out   DATETIME,
    work_date   DATE,
    status      ENUM('present','absent','late'),
    FOREIGN KEY (employee_id) REFERENCES Employee(id) ON DELETE CASCADE
);

CREATE TABLE Salaries (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    employee_id  INT NOT NULL,
    basic_salary DECIMAL(12,2),
    bonus        DECIMAL(12,2),
    deductions   DECIMAL(12,2),
    net_salary   DECIMAL(12,2),
    pay_date     DATE,
    FOREIGN KEY (employee_id) REFERENCES Employee(id) ON DELETE CASCADE
);

CREATE TABLE SalaryChangeHistory (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    old_salary  DECIMAL(12,2),
    new_salary  DECIMAL(12,2),
    change_date DATE,
    reason      TEXT,
    FOREIGN KEY (employee_id) REFERENCES Employee(id) ON DELETE CASCADE
);

CREATE TABLE Goals (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    title       VARCHAR(100),
    target_kpi  VARCHAR(50),
    start_date  DATE,
    end_date    DATE,
    status      ENUM('pending','in_progress','completed'),
    FOREIGN KEY (employee_id) REFERENCES Employee(id) ON DELETE CASCADE
);

CREATE TABLE Meeting_Schedule (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(100),
    meeting_date DATETIME,
    location     VARCHAR(100)
);

CREATE TABLE Participants (
    meeting_id  INT NOT NULL,
    employee_id INT NOT NULL,
    role        VARCHAR(50),
    PRIMARY KEY (meeting_id, employee_id),
    FOREIGN KEY (meeting_id)  REFERENCES Meeting_Schedule(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES Employee(id) ON DELETE CASCADE
);

CREATE TABLE Tasks (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    task_name   VARCHAR(100),
    description TEXT,
    deadline    DATETIME,
    status      ENUM('todo','doing','done')
);

CREATE TABLE AssignedTo (
    task_id       INT NOT NULL,
    employee_id   INT NOT NULL,
    assigned_date DATETIME,
    PRIMARY KEY (task_id, employee_id),
    FOREIGN KEY (task_id)     REFERENCES Tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES Employee(id) ON DELETE CASCADE
);

CREATE TABLE Reports (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    employee_id  INT NOT NULL,
    title        VARCHAR(100),
    created_date DATE,
    content      TEXT,
    FOREIGN KEY (employee_id) REFERENCES Employee(id) ON DELETE CASCADE
);

CREATE TABLE Achievements (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    employee_id  INT NOT NULL,
    title        VARCHAR(50),
    description  TEXT,
    date_awarded DATE,
    FOREIGN KEY (employee_id) REFERENCES Employee(id) ON DELETE CASCADE
);

CREATE TABLE Trainings_Development (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    course_name VARCHAR(100),
    description TEXT,
    start_date  DATE,
    end_date    DATE
);

CREATE TABLE Train (
    employee_id       INT NOT NULL,
    training_id        INT NOT NULL,
    completion_status VARCHAR(50),
    PRIMARY KEY (employee_id, training_id),
    FOREIGN KEY (employee_id) REFERENCES Employee(id) ON DELETE CASCADE,
    FOREIGN KEY (training_id) REFERENCES Trainings_Development(id) ON DELETE CASCADE
);

CREATE TABLE Applicants (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100),
    email       VARCHAR(100),
    phone       CHAR(10),
    resume_link VARCHAR(255)
);

CREATE TABLE JobOpenings (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    department_id INT NULL,
    title         VARCHAR(100),
    quantity      INT,
    status        ENUM('open','closed'),
    FOREIGN KEY (department_id) REFERENCES Departments(id) ON DELETE SET NULL
);

CREATE TABLE Interviews (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    job_opening_id INT NOT NULL,
    applicant_id   INT NOT NULL,
    interview_date DATETIME,
    result         ENUM('passed','failed','pending'),
    FOREIGN KEY (job_opening_id) REFERENCES JobOpenings(id) ON DELETE CASCADE,
    FOREIGN KEY (applicant_id)   REFERENCES Applicants(id)  ON DELETE CASCADE
);

CREATE TABLE Interviewer (
    interview_id INT NOT NULL,
    employee_id  INT NOT NULL,
    PRIMARY KEY (interview_id, employee_id),
    FOREIGN KEY (interview_id) REFERENCES Interviews(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id)  REFERENCES Employee(id)   ON DELETE CASCADE
);

-- ============================ NHÀ SÁCH ============================

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

-- customer_id cho phép NULL = khách vãng lai
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

CREATE TABLE BookInventory_Tasks (
    task_id     INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    check_date  DATE NOT NULL,
    notes       TEXT,
    FOREIGN KEY (employee_id) REFERENCES Employee(id)
);

-- ============================ DỮ LIỆU MẪU ============================
-- Quyền ADMIN = nhân viên có chức vụ tên "Quản lý"; các chức vụ khác là nhân viên thường.
INSERT INTO `Position`(id, position_name, description) VALUES
(1, 'Quản lý', 'Quản lý cửa hàng, toàn quyền hệ thống'),
(2, 'Nhân viên bán hàng', 'Bán hàng, quản lý sách và khách hàng'),
(3, 'Thủ kho', 'Kiểm kê và quản lý kho sách');

INSERT INTO Departments(id, department_name) VALUES (1, 'Kinh doanh'), (2, 'Kho');

INSERT INTO Employee(id, name, position_id, departments_id, date_of_birth, gender, phone_number, address, email, hire_date, salary, work_type, `level`, isDeleted) VALUES
(1, 'Nguyễn Quản Lý', 1, 1, '1990-05-20', 'male',   '0901111111', 'TP. Hồ Chí Minh', 'quanly@nhasach.vn',  '2020-01-01', 15000000, 'full-time', 'L3', 0),
(2, 'Trần Thị Bán Hàng', 2, 1, '1999-08-12', 'female', '0902222222', 'TP. Hồ Chí Minh', 'banhang@nhasach.vn', '2023-03-15', 8000000, 'full-time', 'L1', 0),
(3, 'Lê Văn Kho', 3, 2, '1996-11-02', 'male',   '0903333333', 'TP. Hồ Chí Minh', 'kho@nhasach.vn',     '2022-07-01', 9000000, 'part-time', 'L2', 0);
UPDATE Departments SET manager_id = 1 WHERE id = 1;
UPDATE Departments SET manager_id = 3 WHERE id = 2;

-- Tài khoản: admin / admin123 (Quản lý) ; staff / staff123 (Bán hàng) ; kho / kho12345 (Thủ kho)
INSERT INTO Account(employee_id, username, password) VALUES
(1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9'),
(2, 'staff', '10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6'),
(3, 'kho',   '3d7819184aeaf4a2a9283e70e6ff9027d664a322ceb8f4a4054b826268f8f362');

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
