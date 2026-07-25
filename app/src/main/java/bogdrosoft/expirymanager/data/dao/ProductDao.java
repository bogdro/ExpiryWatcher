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
    // when the setting is off; it's also short-circuited whenever statusFilter explicitly asks
    // for exhausted products (3), so that filter can't be silently emptied by the setting.
    // containerFilter/typeFilter empty-string means "no filter", same convention as query.
    //
    // statusFilter's values match bogdrosoft.expirymanager.util.ProductStatusFilter's ordinals,
    // with -1 meaning "no filter". Effective lead time is the product's type's own override
    // (product_types.lead_time_days) falling back to the global default, the same rule the
    // expiry reminder notifications use.
    //
    // sortMode's values match bogdrosoft.expirymanager.util.SortOrder's ordinals. Each ORDER BY
    // term is a CASE that only produces a real value for the active sortMode; for every other
    // row/mode combination it evaluates to NULL for all rows alike, which is a no-op tiebreaker
    // rather than an actual sort key, so only the active mode's term(s) actually affect ordering.
    // Modes 0 and 1 (EXPIRY_ASC/EXPIRY_DESC) both sink exhausted products to the bottom first,
    // then order by expiry date; the other modes sort purely by the chosen field, with
    // expiry_date as a final tiebreaker for equal values.
    @Query("SELECT p.* FROM products p LEFT JOIN product_types pt ON pt.name = p.type "
            + "WHERE (:query = '' OR p.name LIKE '%' || :query || '%') "
            + "AND (:hideExhausted = 0 OR p.quantity != 0 OR :statusFilter = 3) "
            + "AND (:containerFilter = '' OR p.container = :containerFilter) "
            + "AND (:typeFilter = '' OR p.type = :typeFilter) "
            + "AND ("
            + "  :statusFilter = -1"
            + "  OR (:statusFilter = 3 AND p.quantity = 0)"
            + "  OR (:statusFilter != 3 AND p.quantity != 0 AND ("
            + "    (:statusFilter = 0 AND (p.expiry_date - :todayEpochDay) < 0)"
            + "    OR (:statusFilter = 1 AND (p.expiry_date - :todayEpochDay) >= 0 "
            + "        AND (p.expiry_date - :todayEpochDay) <= COALESCE(pt.lead_time_days, :defaultLeadTimeDays))"
            + "    OR (:statusFilter = 2 AND (p.expiry_date - :todayEpochDay) > COALESCE(pt.lead_time_days, :defaultLeadTimeDays))"
            + "  ))"
            + ") "
            + "ORDER BY "
            + "CASE WHEN :sortMode IN (0, 1) THEN (p.quantity = 0) END ASC, "
            + "CASE WHEN :sortMode = 0 THEN p.expiry_date END ASC, "
            + "CASE WHEN :sortMode = 1 THEN p.expiry_date END DESC, "
            + "CASE WHEN :sortMode = 2 THEN p.name END COLLATE NOCASE ASC, "
            + "CASE WHEN :sortMode = 3 THEN p.name END COLLATE NOCASE DESC, "
            + "CASE WHEN :sortMode = 4 THEN p.type END COLLATE NOCASE ASC, "
            + "CASE WHEN :sortMode = 5 THEN p.type END COLLATE NOCASE DESC, "
            + "CASE WHEN :sortMode = 6 THEN p.container END COLLATE NOCASE ASC, "
            + "CASE WHEN :sortMode = 7 THEN p.container END COLLATE NOCASE DESC, "
            + "p.expiry_date ASC")
    LiveData<List<Product>> searchByName(String query, boolean hideExhausted, int sortMode,
            String containerFilter, String typeFilter, int statusFilter, long todayEpochDay, int defaultLeadTimeDays);

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

    // Used when a container is renamed, so existing products keep pointing at it rather than
    // being left referencing a name that no longer exists in the containers table.
    @Query("UPDATE products SET container = :newName WHERE container = :oldName")
    void updateContainerName(String oldName, String newName);

    // Used when a type is renamed, so existing products keep pointing at it rather than being
    // left referencing a name that no longer exists in the product_types table.
    @Query("UPDATE products SET type = :newName WHERE type = :oldName")
    void updateTypeName(String oldName, String newName);

    @Insert
    long insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    @Query("DELETE FROM products")
    void deleteAll();
}
