package bogdrosoft.expirywatcher.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A user-managed product type (e.g. "Grocery", "Medicine"), used to populate the type
 * dropdown on the add/edit screen and, optionally, to override the global expiry reminder
 * lead time for products of this type. {@link #leadTimeDays} null means "use the global
 * default lead time" rather than any specific number of days.
 */
@Entity(tableName = "product_types")
public class ProductType {

    @PrimaryKey
    @NonNull
    public String name = "";

    @ColumnInfo(name = "lead_time_days")
    @Nullable
    public Integer leadTimeDays;
}
