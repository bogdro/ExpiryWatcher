package bogdrosoft.expirymanager.repository;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.List;

import bogdrosoft.expirymanager.data.AppDatabase;
import bogdrosoft.expirymanager.data.dao.BarcodeDefaultsDao;
import bogdrosoft.expirymanager.data.dao.ProductDao;
import bogdrosoft.expirymanager.data.dao.ProductTypeDao;
import bogdrosoft.expirymanager.data.entity.BarcodeDefaults;
import bogdrosoft.expirymanager.data.entity.Product;
import bogdrosoft.expirymanager.data.entity.ProductType;

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
    private final ProductTypeDao productTypeDao;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ProductRepository(@NonNull Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.productDao = db.productDao();
        this.barcodeDefaultsDao = db.barcodeDefaultsDao();
        this.productTypeDao = db.productTypeDao();
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

    public LiveData<List<ProductType>> getAllTypes() {
        return productTypeDao.getAllSorted();
    }

    /**
     * @param callback delivered {@code true} on success, {@code false} if a type with this
     *                 name already exists (name is the primary key).
     */
    public void addType(ProductType type, Callback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean success;
            try {
                productTypeDao.insert(type);
                success = true;
            } catch (SQLiteConstraintException e) {
                success = false;
            }
            boolean result = success;
            mainHandler.post(() -> callback.onResult(result));
        });
    }

    public void updateType(ProductType type) {
        AppDatabase.databaseWriteExecutor.execute(() -> productTypeDao.update(type));
    }

    public void deleteType(ProductType type) {
        AppDatabase.databaseWriteExecutor.execute(() -> productTypeDao.delete(type));
    }
}
