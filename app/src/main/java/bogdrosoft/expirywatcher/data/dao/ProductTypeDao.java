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

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import bogdrosoft.expirywatcher.data.entity.ProductType;

@Dao
public interface ProductTypeDao {

    @Query("SELECT * FROM product_types ORDER BY name")
    LiveData<List<ProductType>> getAllSorted();

    @Query("SELECT * FROM product_types ORDER BY name")
    List<ProductType> getAllSync();

    @Insert
    void insert(ProductType type);

    @Update
    void update(ProductType type);

    // A plain @Update won't do for a name change: it matches the row to update by the entity's
    // own primary key (name), so passing a ProductType with an already-changed name would look
    // for a row that doesn't exist yet rather than renaming the old one.
    @Query("UPDATE product_types SET name = :newName, lead_time_days = :leadTimeDays WHERE name = :oldName")
    void rename(String oldName, String newName, Integer leadTimeDays);

    @Delete
    void delete(ProductType type);

    @Query("DELETE FROM product_types")
    void deleteAll();
}
