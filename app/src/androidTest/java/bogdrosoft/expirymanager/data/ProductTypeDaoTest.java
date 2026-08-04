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

package bogdrosoft.expirywatcher.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import bogdrosoft.expirywatcher.data.dao.ProductTypeDao;
import bogdrosoft.expirywatcher.data.entity.ProductType;

@RunWith(AndroidJUnit4.class)
public class ProductTypeDaoTest {

    private AppDatabase db;
    private ProductTypeDao dao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.productTypeDao();

        // AppDatabase seeds a handful of default type names via a RoomDatabase.Callback that
        // fires for any freshly created database, including this in-memory one. Clear them so
        // each test starts from a known-empty table regardless of what the seed list contains.
        for (ProductType seeded : dao.getAllSync()) {
            dao.delete(seeded);
        }
    }

    @After
    public void closeDb() {
        db.close();
    }

    private ProductType newType(String name, Integer leadTimeDays) {
        ProductType type = new ProductType();
        type.name = name;
        type.leadTimeDays = leadTimeDays;
        return type;
    }

    @Test
    public void getAllSync_returnsInsertedTypes() {
        dao.insert(newType("Medicine", 7));
        dao.insert(newType("Grocery", null));

        List<ProductType> result = dao.getAllSync();

        assertEquals(2, result.size());
    }

    @Test
    public void insert_duplicateName_throwsConstraintException() {
        dao.insert(newType("Grocery", null));
        try {
            dao.insert(newType("Grocery", 5));
            fail("Expected SQLiteConstraintException for a duplicate primary key");
        } catch (SQLiteConstraintException expected) {
            // expected: name is the primary key
        }
    }

    @Test
    public void update_changesLeadTimeDays() {
        dao.insert(newType("Medicine", null));

        ProductType updated = newType("Medicine", 10);
        dao.update(updated);

        ProductType result = dao.getAllSync().get(0);
        assertEquals(Integer.valueOf(10), result.leadTimeDays);
    }

    @Test
    public void delete_removesType() {
        ProductType type = newType("Household", null);
        dao.insert(type);

        dao.delete(type);

        assertEquals(0, dao.getAllSync().size());
    }

    @Test
    public void leadTimeDays_defaultsToNull() {
        dao.insert(newType("Other", null));
        assertNull(dao.getAllSync().get(0).leadTimeDays);
    }
}
