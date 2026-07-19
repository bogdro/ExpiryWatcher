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

    @Query("SELECT * FROM products ORDER BY expiry_date ASC")
    LiveData<List<Product>> getAllSortedByExpiry();

    // Synchronous twin of getAllSortedByExpiry(), used by DAO tests where asserting on a
    // LiveData emission would otherwise need extra test-only infrastructure.
    @Query("SELECT * FROM products ORDER BY expiry_date ASC")
    List<Product> getAllSortedByExpirySync();

    @Query("SELECT * FROM products WHERE id = :id")
    LiveData<Product> getById(long id);

    @Query("SELECT * FROM products WHERE expiry_date <= :thresholdEpochDay ORDER BY expiry_date ASC")
    List<Product> getExpiringByThresholdSync(long thresholdEpochDay);

    @Query("SELECT COUNT(*) FROM products")
    int countSync();

    @Insert
    long insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);
}
