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

    // A plain @Update won't do here: it matches the row to update by the entity's own primary
    // key (name), so passing a Container with a already-changed name would look for a row that
    // doesn't exist yet rather than renaming the old one.
    @Query("UPDATE containers SET name = :newName WHERE name = :oldName")
    void rename(String oldName, String newName);

    @Delete
    void delete(Container container);

    @Query("DELETE FROM containers")
    void deleteAll();
}
