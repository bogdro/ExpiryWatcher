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

import bogdrosoft.expirywatcher.data.dao.ContainerDao;
import bogdrosoft.expirywatcher.data.entity.Container;

@RunWith(AndroidJUnit4.class)
public class ContainerDaoTest {

    private AppDatabase db;
    private ContainerDao dao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.containerDao();

        // AppDatabase seeds a handful of default container names via a RoomDatabase.Callback
        // that fires for any freshly created database, including this in-memory one. Clear them
        // so each test starts from a known-empty table regardless of what the seed list contains.
        for (Container seeded : dao.getAllSync()) {
            dao.delete(seeded);
        }
    }

    @After
    public void closeDb() {
        db.close();
    }

    private Container newContainer(String name) {
        Container container = new Container();
        container.name = name;
        return container;
    }

    @Test
    public void getAllSync_returnsInsertedContainers() {
        dao.insert(newContainer("Fridge"));
        dao.insert(newContainer("Pantry"));

        List<Container> result = dao.getAllSync();

        assertEquals(2, result.size());
    }

    @Test
    public void insert_duplicateName_throwsConstraintException() {
        dao.insert(newContainer("Fridge"));
        try {
            dao.insert(newContainer("Fridge"));
            fail("Expected SQLiteConstraintException for a duplicate primary key");
        } catch (SQLiteConstraintException expected) {
            // expected: name is the primary key
        }
    }

    @Test
    public void delete_removesContainer() {
        Container container = newContainer("Freezer");
        dao.insert(container);

        dao.delete(container);

        assertEquals(0, dao.getAllSync().size());
    }
}
