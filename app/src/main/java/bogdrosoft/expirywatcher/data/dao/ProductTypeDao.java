package bogdrosoft.expirywatcher.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bogdrosoft.expirywatcher.data.entity.ProductType;

@Dao
public interface ProductTypeDao {

    @Query("SELECT * FROM product_types ORDER BY name")
    LiveData<List<ProductType>> getAllSorted();

    @Query("SELECT * FROM product_types ORDER BY name")
    List<ProductType> getAllSync();

    @Insert
    void insert(ProductType type);

    @Update
    void update(ProductType type);

    // A plain @Update won't do for a name change: it matches the row to update by the entity's
    // own primary key (name), so passing a ProductType with an already-changed name would look
    // for a row that doesn't exist yet rather than renaming the old one.
    @Query("UPDATE product_types SET name = :newName, lead_time_days = :leadTimeDays WHERE name = :oldName")
    void rename(String oldName, String newName, Integer leadTimeDays);

    @Delete
    void delete(ProductType type);

    @Query("DELETE FROM product_types")
    void deleteAll();
}
