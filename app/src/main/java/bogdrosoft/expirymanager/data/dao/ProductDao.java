package bogdrosoft.expirymanager.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bogdrosoft.expirymanager.data.entity.Product;

@Dao
public interface ProductDao {

    // An empty query matches everything, so the main list screen can use this same method
    // whether or not the user has typed a search term. SQLite's LIKE is already
    // case-insensitive for ASCII, which covers the "case-insensitive" requirement here.
    // "quantity = 0" evaluates to 0/1 in SQLite, so ordering by it first pushes exhausted
    // products to the bottom regardless of expiry date, while both groups are still
    // sorted by expiry date within themselves.
    @Query("SELECT * FROM products WHERE (:query = '' OR name LIKE '%' || :query || '%') "
            + "ORDER BY (quantity = 0) ASC, expiry_date ASC")
    LiveData<List<Product>> searchByName(String query);

    // Synchronous twin of searchByName(""): used by ExpiryCheckWorker, which needs to
    // filter by each product's own effective (possibly type-overridden) lead time rather than
    // a single SQL threshold, and by DAO tests where asserting on a LiveData emission would
    // otherwise need extra test-only infrastructure.
    @Query("SELECT * FROM products ORDER BY expiry_date ASC")
    List<Product> getAllSortedByExpirySync();

    @Query("SELECT * FROM products WHERE id = :id")
    LiveData<Product> getById(long id);

    @Query("SELECT COUNT(*) FROM products")
    int countSync();

    @Insert
    long insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);
}
