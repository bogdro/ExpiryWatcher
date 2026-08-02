package bogdrosoft.expirywatcher.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A user-managed storage location (e.g. "Fridge", "Pantry"), used to populate the container
 * dropdown on the add/edit screen. Unlike {@link ProductType}, it has no other attributes.
 */
@Entity(tableName = "containers")
public class Container {

    @PrimaryKey
    @NonNull
    public String name = "";
}
