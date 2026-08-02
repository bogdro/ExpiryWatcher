package bogdrosoft.expirywatcher.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import bogdrosoft.expirywatcher.data.dao.BarcodeDefaultsDao;
import bogdrosoft.expirywatcher.data.entity.BarcodeDefaults;

@RunWith(AndroidJUnit4.class)
public class BarcodeDefaultsDaoTest {

    private AppDatabase db;
    private BarcodeDefaultsDao dao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = db.barcodeDefaultsDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    @Test
    public void getByBarcode_returnsNullWhenNeverSeen() {
        assertNull(dao.getByBarcodeSync("0000000000000"));
    }

    @Test
    public void upsert_withSameBarcodeReplacesRatherThanDuplicates() {
        BarcodeDefaults first = new BarcodeDefaults();
        first.barcode = "1234567890128";
        first.name = "Milk";
        first.type = "Dairy";
        first.quantity = 1;
        first.unit = "carton";
        dao.upsert(first);

        BarcodeDefaults second = new BarcodeDefaults();
        second.barcode = "1234567890128";
        second.name = "Milk";
        second.type = "Dairy";
        second.quantity = 2;
        second.unit = "carton";
        dao.upsert(second);

        BarcodeDefaults result = dao.getByBarcodeSync("1234567890128");
        assertEquals(2, result.quantity);
    }
}
