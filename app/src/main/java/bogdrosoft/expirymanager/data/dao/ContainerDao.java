package bogdrosoft.expirymanager.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import bogdrosoft.expirymanager.data.entity.Container;

@Dao
public interface ContainerDao {

    @Query("SELECT * FROM containers ORDER BY name")
    LiveData<List<Container>> getAllSorted();

    @Query("SELECT * FROM containers ORDER BY name")
    List<Container> getAllSync();

    @Insert
    void insert(Container container);

    @Delete
    void delete(Container container);

    @Query("DELETE FROM containers")
    void deleteAll();
}
