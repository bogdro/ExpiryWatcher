package bogdrosoft.expirywatcher.data;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.List;

import bogdrosoft.expirywatcher.data.dao.ProductDao;
import bogdrosoft.expirywatcher.data.entity.Product;

@RunWith(AndroidJUnit4.class)
public class ProductDaoTest {

    private AppDatabase db;
    private ProductDao productDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        productDao = db.productDao();
    }

    @After
    public void closeDb() {
        db.close();
    }

    private Product newProduct(String name, LocalDate expiryDate) {
        Product product = new Product();
        product.name = name;
        product.type = "Grocery";
        product.quantity = 1;
        product.unit = "pcs";
        product.expiryDate = expiryDate;
        return product;
    }

    @Test
    public void getAllSortedByExpiry_returnsAscendingOrder() {
        LocalDate today = LocalDate.now();
        productDao.insert(newProduct("Later", today.plusDays(10)));
        productDao.insert(newProduct("Soonest", today.plusDays(1)));
        productDao.insert(newProduct("Middle", today.plusDays(5)));

        List<Product> result = productDao.getAllSortedByExpirySync();

        assertEquals(3, result.size());
        assertEquals("Soonest", result.get(0).name);
        assertEquals("Middle", result.get(1).name);
        assertEquals("Later", result.get(2).name);
    }

    @Test
    public void update_changesExistingRowRatherThanInserting() {
        long id = productDao.insert(newProduct("Milk", LocalDate.now().plusDays(3)));

        Product loaded = productDao.getAllSortedByExpirySync().get(0);
        loaded.quantity = 5;
        productDao.update(loaded);

        List<Product> result = productDao.getAllSortedByExpirySync();
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).quantity);
        assertEquals(id, result.get(0).id);
    }

    @Test
    public void delete_removesRow() {
        Product product = newProduct("Yogurt", LocalDate.now().plusDays(1));
        long id = productDao.insert(product);
        product.id = id;

        productDao.delete(product);

        assertEquals(0, productDao.getAllSortedByExpirySync().size());
    }
}
