package com.bookstore.bus;

import com.bookstore.dao.BookDAO;
import com.bookstore.model.Book;
import com.bookstore.util.AppException;
import com.bookstore.util.CsvUtil;
import com.bookstore.util.Validate;

import java.io.File;
import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Nghiệp vụ sách: cache ArrayList<Book> trong bộ nhớ, kiểm tra hợp lệ, lọc & tìm kiếm, nhập/xuất CSV. */
public class BookBUS {
    private static final BookBUS INSTANCE = new BookBUS();
    public static BookBUS getInstance() { return INSTANCE; }

    private final BookDAO dao = new BookDAO();
    private ArrayList<Book> cache;

    private BookBUS() {}

    /** Nạp lại toàn bộ dữ liệu sách từ CSDL vào cache. */
    public synchronized void load() { cache = new ArrayList<>(dao.selectAll()); }

    public synchronized List<Book> getAll() {
        if (cache == null) load();
        return new ArrayList<>(cache);
    }

    public Book getById(int id) { return getAll().stream().filter(b -> b.getBookId() == id).findFirst().orElse(null); }

    /** Lọc theo thể loại (null = tất cả) và tìm theo tên sách, tác giả, ISBN hoặc mã. */
    public List<Book> search(String keyword, Integer categoryId) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        return getAll().stream()
                .filter(b -> categoryId == null || categoryId.equals(b.getCategoryId()))
                .filter(b -> k.isEmpty()
                        || b.getTitle().toLowerCase().contains(k)
                        || (b.getAuthorName() != null && b.getAuthorName().toLowerCase().contains(k))
                        || (b.getIsbn() != null && b.getIsbn().toLowerCase().contains(k))
                        || String.valueOf(b.getBookId()).equals(k))
                .collect(Collectors.toList());
    }

    public List<Book> lowStock(int threshold) {
        return getAll().stream().filter(b -> b.getStockQuantity() <= threshold).collect(Collectors.toList());
    }

    private void validate(Book b) {
        Validate.required(b.getTitle(), "Tên sách");
        Validate.maxLen(b.getTitle(), 150, "Tên sách");
        Validate.maxLen(b.getIsbn(), 20, "ISBN");
        if (b.getPrice() <= 0) throw new AppException("Giá bán phải lớn hơn 0.");
        if (b.getPrice() >= 100_000_000) throw new AppException("Giá bán quá lớn (tối đa dưới 100.000.000 đ).");
        if (b.getStockQuantity() < 0) throw new AppException("Số lượng tồn không được âm.");
        Integer y = b.getPublicationYear();
        if (y != null && (y < 1000 || y > Year.now().getValue() + 1))
            throw new AppException("Năm xuất bản không hợp lệ (1000 - " + (Year.now().getValue() + 1) + ").");
        if (!Validate.blank(b.getIsbn())) {
            String isbn = b.getIsbn().trim();
            for (Book o : getAll())
                if (o.getBookId() != b.getBookId() && isbn.equalsIgnoreCase(o.getIsbn()))
                    throw new AppException("ISBN '" + isbn + "' đã thuộc về sách \"" + o.getTitle() + "\".");
        }
        b.setTitle(b.getTitle().trim());
    }

    public void add(Book b) { validate(b); dao.insert(b); load(); }

    public void update(Book b) {
        validate(b);
        if (dao.update(b) == 0 && getById(b.getBookId()) == null) throw new AppException("Không tìm thấy sách cần cập nhật.");
        load();
    }

    public void delete(int bookId) {
        try { dao.delete(bookId); }
        catch (AppException e) { throw new AppException("Sách đã phát sinh trong đơn hàng nên không thể xóa. Hãy đặt số lượng tồn về 0."); }
        load();
    }

    /**
     * Nhập từ CSV, các cột: Tên sách, ISBN, Tác giả, Thể loại, Nhà xuất bản, Năm xuất bản, Giá, Số lượng.
     * Tác giả / thể loại / nhà xuất bản chưa có sẽ được tạo mới. Trả về báo cáo kết quả.
     */
    public String importCsv(File file) {
        List<String[]> rows;
        try { rows = CsvUtil.read(file); }
        catch (IOException e) { throw new AppException("Không đọc được file: " + e.getMessage()); }
        AuthorBUS authors = new AuthorBUS();
        CategoryBUS categories = new CategoryBUS();
        PublisherBUS publishers = new PublisherBUS();
        int ok = 0, fail = 0;
        StringBuilder err = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);
            if (i == 0 && r.length > 7 && !r[7].trim().matches("\\d+")) continue; // dòng tiêu đề
            try {
                if (r.length < 8) throw new AppException("thiếu cột (cần đủ 8 cột)");
                Book b = new Book();
                b.setTitle(r[0]); b.setIsbn(r[1]);
                // đọc các số trước, để dòng lỗi không làm phát sinh tác giả/thể loại/NXB rác
                b.setPublicationYear(Validate.blank(r[5]) ? null : Integer.parseInt(r[5].trim()));
                b.setPrice(Double.parseDouble(r[6].replace(".", "").replace(",", "").trim()));
                b.setStockQuantity(Integer.parseInt(r[7].trim()));
                validate(b);
                b.setAuthorId(authors.findOrCreate(r[2]));
                b.setCategoryId(categories.findOrCreate(r[3]));
                b.setPublisherId(publishers.findOrCreate(r[4]));
                add(b);
                ok++;
            } catch (NumberFormatException ex) {
                fail++;
                if (fail <= 5) err.append("\n - Dòng ").append(i + 1).append(": số không hợp lệ (năm, giá hoặc số lượng)");
            } catch (RuntimeException ex) {
                fail++;
                if (fail <= 5) err.append("\n - Dòng ").append(i + 1).append(": ").append(ex.getMessage());
            }
        }
        return "Nhập thành công " + ok + " sách, lỗi " + fail + " dòng." + err;
    }

    public void exportCsv(File file, List<Book> books) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Tên sách", "ISBN", "Tác giả", "Thể loại", "Nhà xuất bản", "Năm xuất bản", "Giá", "Số lượng"});
        for (Book b : books) rows.add(new String[]{b.getTitle(), b.getIsbn(), b.getAuthorName(), b.getCategoryName(),
                b.getPublisherName(), b.getPublicationYear() == null ? "" : String.valueOf(b.getPublicationYear()),
                String.valueOf((long) b.getPrice()), String.valueOf(b.getStockQuantity())});
        try { CsvUtil.write(file, rows); }
        catch (IOException e) { throw new AppException("Không ghi được file: " + e.getMessage()); }
    }
}
