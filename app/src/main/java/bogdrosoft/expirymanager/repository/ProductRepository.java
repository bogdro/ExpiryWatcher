package bogdrosoft.expirymanager.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.List;

import bogdrosoft.expirymanager.data.AppDatabase;
import bogdrosoft.expirymanager.data.dao.BarcodeDefaultsDao;
import bogdrosoft.expirymanager.data.dao.ProductDao;
import bogdrosoft.expirymanager.data.entity.BarcodeDefaults;
import bogdrosoft.expirymanager.data.entity.Product;

/**
 * Single source of truth for product data used by the UI layer. Wraps Room DAO access
 * on a background executor, since plain Java Room has no coroutines/Flow to lean on.
 */
public class ProductRepository {

    public interface Callback<T> {
        void onResult(@Nullable T result);
    }

    private final ProductDao productDao;
    private final BarcodeDefaultsDao barcodeDefaultsDao;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ProductRepository(@NonNull Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.productDao = db.productDao();
        this.barcodeDefaultsDao = db.barcodeDefaultsDao();
    }

    public LiveData<List<Product>> getAllProducts() {
        return productDao.getAllSortedByExpiry();
    }

    public LiveData<Product> getProduct(long id) {
        return productDao.getById(id);
    }

    public void saveProduct(Product product, boolean isNew) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            product.updatedAt = now;
            if (isNew) {
                product.createdAt = now;
                productDao.insert(product);
            } else {
                productDao.update(product);
            }

            if (product.barcode != null && !product.barcode.isEmpty()) {
                BarcodeDefaults defaults = new BarcodeDefaults();
                defaults.barcode = product.barcode;
                defaults.name = product.name;
                defaults.type = product.type;
                defaults.quantity = product.quantity;
                defaults.unit = product.unit;
                defaults.updatedAt = now;
                barcodeDefaultsDao.upsert(defaults);
            }
        });
    }

    public void deleteProduct(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> productDao.delete(product));
    }

    public void lookupBarcodeDefaults(String barcode, Callback<BarcodeDefaults> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            BarcodeDefaults result = barcodeDefaultsDao.getByBarcodeSync(barcode);
            mainHandler.post(() -> callback.onResult(result));
        });
    }
}
