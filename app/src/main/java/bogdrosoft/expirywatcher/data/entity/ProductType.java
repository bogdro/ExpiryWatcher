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
