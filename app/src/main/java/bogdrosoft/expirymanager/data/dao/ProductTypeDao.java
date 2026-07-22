package bogdrosoft.expirymanager.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bogdrosoft.expirymanager.data.entity.ProductType;

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

    @Delete
    void delete(ProductType type);

    @Query("DELETE FROM product_types")
    void deleteAll();
}
