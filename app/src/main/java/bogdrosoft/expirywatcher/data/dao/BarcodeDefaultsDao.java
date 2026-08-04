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

package bogdrosoft.expirywatcher.data.dao;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import bogdrosoft.expirywatcher.data.entity.BarcodeDefaults;

@Dao
public interface BarcodeDefaultsDao {

    @Query("SELECT * FROM barcode_defaults WHERE barcode = :barcode")
    @Nullable
    BarcodeDefaults getByBarcodeSync(String barcode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(BarcodeDefaults defaults);

    @Query("DELETE FROM barcode_defaults")
    void deleteAll();
}
