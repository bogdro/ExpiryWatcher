package bogdrosoft.expirymanager.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Remembers the last-entered name/type/quantity/unit for a given barcode so the
 * add-product form can pre-fill itself next time that barcode is scanned.
 * Deliberately excludes the expiry date, which is always per-batch and entered fresh.
 */
@Entity(tableName = "barcode_defaults")
public class BarcodeDefaults {

    @PrimaryKey
    @NonNull
    public String barcode = "";

    @NonNull
    public String name = "";

    @NonNull
    public String type = "";

    public int quantity;

    @NonNull
    public String unit = "";

    @ColumnInfo(name = "updated_at")
    public long updatedAt;
}
