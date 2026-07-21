package bogdrosoft.expirymanager.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

// Indexed on expiry_date (the main list's sort column) and name (the search column).
@Entity(tableName = "products", indices = {@Index("expiry_date"), @Index("name")})
public class Product {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name = "";

    @NonNull
    public String type = "";

    public int quantity;

    @NonNull
    public String unit = "";

    @ColumnInfo(name = "expiry_date")
    @NonNull
    public LocalDate expiryDate = LocalDate.now();

    // When the product was opened, e.g. for tracking a shorter shelf life once unsealed.
    // Optional and empty by default, unlike expiryDate.
    @ColumnInfo(name = "open_date")
    @Nullable
    public LocalDate openDate;

    // Where the product is stored (e.g. "Fridge", "Pantry"), from the user-managed Container
    // list. Optional and empty by default.
    @Nullable
    public String container;

    @Nullable
    public String barcode;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;
}
