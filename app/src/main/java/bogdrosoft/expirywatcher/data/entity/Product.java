/*
 * Copyright (C) 2026 Bogdan Drozdowski, bogdro (at) users . sourceforge . net
 * License: GNU General Public License, v3+
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package bogdrosoft.expirywatcher.data.entity;

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

    // Free-text, user-entered notes. Optional and empty by default.
    @Nullable
    public String notes;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;
}
