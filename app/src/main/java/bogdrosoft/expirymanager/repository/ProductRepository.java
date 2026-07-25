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
import bogdrosoft.expirymanager.data.dao.ContainerDao;
import bogdrosoft.expirymanager.data.dao.ProductDao;
import bogdrosoft.expirymanager.data.dao.ProductTypeDao;
import bogdrosoft.expirymanager.data.entity.BarcodeDefaults;
import bogdrosoft.expirymanager.data.entity.Container;
import bogdrosoft.expirymanager.data.entity.Product;
import bogdrosoft.expirymanager.data.entity.ProductType;
import bogdrosoft.expirymanager.util.SortOrder;

/**
 * Single source of truth for product data used by the UI layer. Wraps Room DAO access
 * on a background executor, since plain Java Room has no coroutines/Flow to lean on.
 */
public class ProductRepository {

    public interface Callback<T> {
        void onResult(@Nullable T result);
    }

    private final AppDatabase db;
    private final ProductDao productDao;
    private final BarcodeDefaultsDao barcodeDefaultsDao;
    private final ProductTypeDao productTypeDao;
    private final ContainerDao containerDao;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ProductRepository(@NonNull Context context) {
        this.db = AppDatabase.getInstance(context);
        this.productDao = db.productDao();
        this.barcodeDefaultsDao = db.barcodeDefaultsDao();
        this.productTypeDao = db.productTypeDao();
        this.containerDao = db.containerDao();
    }

    public LiveData<List<Product>> searchProducts(String query, boolean hideExhausted, SortOrder sortOrder) {
        return productDao.searchByName(query == null ? "" : query, hideExhausted, sortOrder.ordinal());
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

    /**
     * Deletes all products, but keeps barcode scan history (so re-scanning a barcode still
     * prefills its remembered details), custom types, containers, and non-database settings.
     */
    public void deleteAllProducts(Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.deleteAll();
            mainHandler.post(onComplete);
        });
    }

    /**
     * Deletes everything in the database: products, barcode scan history, custom types and
     * containers, then re-seeds types/containers back to the same defaults a fresh install
     * gets ({@link AppDatabase#DEFAULT_TYPE_NAMES}/{@link AppDatabase#DEFAULT_CONTAINER_NAMES}).
     * Non-database settings (e.g. reminder time) are untouched, since those live in
     * SharedPreferences rather than this database.
     */
    public void deleteAllData(Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productDao.deleteAll();
            barcodeDefaultsDao.deleteAll();
            productTypeDao.deleteAll();
            containerDao.deleteAll();
            for (String name : AppDatabase.DEFAULT_TYPE_NAMES) {
                ProductType type = new ProductType();
                type.name = name;
                productTypeDao.insert(type);
            }
            for (String name : AppDatabase.DEFAULT_CONTAINER_NAMES) {
                Container container = new Container();
                container.name = name;
                containerDao.insert(container);
            }
            mainHandler.post(onComplete);
        });
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

    public LiveData<List<Container>> getAllContainers() {
        return containerDao.getAllSorted();
    }

    /**
     * @param callback delivered {@code true} on success, {@code false} if a container with
     *                 this name already exists (name is the primary key).
     */
    public void addContainer(Container container, Callback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean success;
            try {
                containerDao.insert(container);
                success = true;
            } catch (SQLiteConstraintException e) {
                success = false;
            }
            boolean result = success;
            mainHandler.post(() -> callback.onResult(result));
        });
    }

    public void deleteContainer(Container container) {
        AppDatabase.databaseWriteExecutor.execute(() -> containerDao.delete(container));
    }

    /**
     * Renames a container and repoints every product that referenced the old name, as one
     * transaction so the two tables can't end up disagreeing if something fails in between.
     *
     * @param callback delivered {@code true} on success, {@code false} if a container with
     *                 {@code newName} already exists (name is the primary key).
     */
    public void renameContainer(String oldName, String newName, Callback<Boolean> callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean success;
            try {
                db.runInTransaction(() -> {
                    containerDao.rename(oldName, newName);
                    productDao.updateContainerName(oldName, newName);
                });
                success = true;
            } catch (SQLiteConstraintException e) {
                success = false;
            }
            boolean result = success;
            mainHandler.post(() -> callback.onResult(result));
        });
    }
}
