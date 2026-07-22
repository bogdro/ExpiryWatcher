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
    // hideExhausted is bound as 0/1, so "hideExhausted = 0" short-circuits the quantity check
    // when the setting is off.
    //
    // sortMode's values match bogdrosoft.expirymanager.util.SortOrder's ordinals. Each ORDER BY
    // term is a CASE that only produces a real value for the active sortMode; for every other
    // row/mode combination it evaluates to NULL for all rows alike, which is a no-op tiebreaker
    // rather than an actual sort key, so only the active mode's term(s) actually affect ordering.
    // Mode 0 (DEFAULT) reproduces the original "exhausted products sink to the bottom, then by
    // expiry date" behavior; the other modes sort purely by the chosen field, with expiry_date
    // as a final tiebreaker for equal values.
    @Query("SELECT * FROM products WHERE (:query = '' OR name LIKE '%' || :query || '%') "
            + "AND (:hideExhausted = 0 OR quantity != 0) "
            + "ORDER BY "
            + "CASE WHEN :sortMode = 0 THEN (quantity = 0) END ASC, "
            + "CASE WHEN :sortMode = 0 THEN expiry_date END ASC, "
            + "CASE WHEN :sortMode = 1 THEN expiry_date END ASC, "
            + "CASE WHEN :sortMode = 2 THEN expiry_date END DESC, "
            + "CASE WHEN :sortMode = 3 THEN name END COLLATE NOCASE ASC, "
            + "CASE WHEN :sortMode = 4 THEN name END COLLATE NOCASE DESC, "
            + "CASE WHEN :sortMode = 5 THEN type END COLLATE NOCASE ASC, "
            + "CASE WHEN :sortMode = 6 THEN type END COLLATE NOCASE DESC, "
            + "CASE WHEN :sortMode = 7 THEN container END COLLATE NOCASE ASC, "
            + "CASE WHEN :sortMode = 8 THEN container END COLLATE NOCASE DESC, "
            + "expiry_date ASC")
    LiveData<List<Product>> searchByName(String query, boolean hideExhausted, int sortMode);

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

    @Query("DELETE FROM products")
    void deleteAll();
}
