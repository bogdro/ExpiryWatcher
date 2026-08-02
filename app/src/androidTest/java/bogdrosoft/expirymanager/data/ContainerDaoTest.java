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
