package com.bookstore.dao;

import com.bookstore.model.Book;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static com.bookstore.dao.DaoSupport.nz;

public class BookDAO {
    private static final String SELECT =
            "SELECT b.*, a.author_name, c.category_name, p.publisher_name FROM Books b " +
            "LEFT JOIN Authors a ON b.author_id = a.author_id " +
            "LEFT JOIN Categories c ON b.category_id = c.category_id " +
            "LEFT JOIN Publishers p ON b.publisher_id = p.publisher_id ";

    private static final DaoSupport.Mapper<Book> MAP = rs -> {
        Book b = new Book();
        b.setBookId(rs.getInt("book_id")); b.setTitle(rs.getString("title")); b.setIsbn(rs.getString("isbn"));
        b.setAuthorId(DaoSupport.intOrNull(rs, "author_id"));
        b.setCategoryId(DaoSupport.intOrNull(rs, "category_id"));
        b.setPublisherId(DaoSupport.intOrNull(rs, "publisher_id"));
        b.setPublicationYear(DaoSupport.intOrNull(rs, "publication_year"));
        b.setPrice(rs.getDouble("price")); b.setStockQuantity(rs.getInt("stock_quantity"));
        b.setDescription(rs.getString("description"));
        b.setAuthorName(rs.getString("author_name")); b.setCategoryName(rs.getString("category_name"));
        b.setPublisherName(rs.getString("publisher_name"));
        return b;
    };

    /** Lấy danh sách tất cả sách trong CSDL. */
    public List<Book> selectAll() { return DaoSupport.query(SELECT + "ORDER BY b.book_id", MAP); }
    public Book selectById(int id) { return DaoSupport.queryOne(SELECT + "WHERE b.book_id=?", MAP, id); }

    public int insert(Book b) {
        return DaoSupport.insert("INSERT INTO Books(title, isbn, author_id, category_id, publisher_id, publication_year, price, stock_quantity, description) VALUES (?,?,?,?,?,?,?,?,?)",
                b.getTitle(), nz(b.getIsbn()), b.getAuthorId(), b.getCategoryId(), b.getPublisherId(),
                b.getPublicationYear(), b.getPrice(), b.getStockQuantity(), nz(b.getDescription()));
    }

    public int update(Book b) {
        return DaoSupport.update("UPDATE Books SET title=?, isbn=?, author_id=?, category_id=?, publisher_id=?, publication_year=?, price=?, stock_quantity=?, description=? WHERE book_id=?",
                b.getTitle(), nz(b.getIsbn()), b.getAuthorId(), b.getCategoryId(), b.getPublisherId(),
                b.getPublicationYear(), b.getPrice(), b.getStockQuantity(), nz(b.getDescription()), b.getBookId());
    }

    public int delete(int id) { return DaoSupport.update("DELETE FROM Books WHERE book_id=?", id); }

    /** Trừ tồn kho trong giao dịch; trả về 0 nếu không đủ số lượng (không bao giờ âm kho). */
    public int decreaseStock(Connection c, int bookId, int qty) throws SQLException {
        return DaoSupport.update(c, "UPDATE Books SET stock_quantity = stock_quantity - ? WHERE book_id = ? AND stock_quantity >= ?", qty, bookId, qty);
    }

    /** Hoàn kho khi hủy đơn. */
    public int increaseStock(Connection c, int bookId, int qty) throws SQLException {
        return DaoSupport.update(c, "UPDATE Books SET stock_quantity = stock_quantity + ? WHERE book_id = ?", qty, bookId);
    }
}
