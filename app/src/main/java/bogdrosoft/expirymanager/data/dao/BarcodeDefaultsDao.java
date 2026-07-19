package bogdrosoft.expirymanager.data.dao;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import bogdrosoft.expirymanager.data.entity.BarcodeDefaults;

@Dao
public interface BarcodeDefaultsDao {

    @Query("SELECT * FROM barcode_defaults WHERE barcode = :barcode")
    @Nullable
    BarcodeDefaults getByBarcodeSync(String barcode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(BarcodeDefaults defaults);
}
