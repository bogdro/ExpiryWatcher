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
