package bogdrosoft.expirywatcher.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bogdrosoft.expirywatcher.data.dao.BarcodeDefaultsDao;
import bogdrosoft.expirywatcher.data.dao.ContainerDao;
import bogdrosoft.expirywatcher.data.dao.ProductDao;
import bogdrosoft.expirywatcher.data.dao.ProductTypeDao;
import bogdrosoft.expirywatcher.data.entity.BarcodeDefaults;
import bogdrosoft.expirywatcher.data.entity.Container;
import bogdrosoft.expirywatcher.data.entity.Product;
import bogdrosoft.expirywatcher.data.entity.ProductType;
import bogdrosoft.expirywatcher.util.Constants;

@Database(entities = {Product.class, BarcodeDefaults.class, ProductType.class, Container.class}, version = 6, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    // Just a new nullable column, so an in-place migration (rather than the usual destructive
    // fallback) is both trivial and worthwhile here: unlike earlier schema bumps, this one has
    // real user data to lose (including a manually migrated import), and ALTER TABLE ADD COLUMN
    // is a cheap, well-supported SQLite operation for exactly this case.
    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE products ADD COLUMN notes TEXT");
        }
    };

    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(2);

    // Also reused by ProductRepository.deleteAllData() to re-seed types/containers after a
    // full data wipe, so this is the single source of truth for the default lists.
    public static final String[] DEFAULT_TYPE_NAMES = {"Grocery", "Medicine", "Dairy", "Household", "Documents", "Other"};
    public static final String[] DEFAULT_CONTAINER_NAMES = {"Fridge", "Freezer", "Pantry", "Medicine cabinet"};

    public abstract ProductDao productDao();

    public abstract BarcodeDefaultsDao barcodeDefaultsDao();

    public abstract ProductTypeDao productTypeDao();

    public abstract ContainerDao containerDao();

    public static AppDatabase getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = build(context);
                }
            }
        }
        return instance;
    }

    private static AppDatabase build(Context context) {
        // TRUNCATE (not the WAL default) keeps the on-disk file a complete, single-file
        // snapshot at all times, which is what makes straightforward file-copy export/import
        // safe without a manual WAL checkpoint step.
        return Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, Constants.DATABASE_NAME)
                .setJournalMode(JournalMode.TRUNCATE)
                .addMigrations(MIGRATION_5_6)
                // Falls back to recreating the db for any version jump without its own
                // migration above (there was no prior release to preserve data for until now).
                .fallbackToDestructiveMigration()
                .addCallback(new Callback() {
                    // Seeding here (rather than onCreate()) is deliberate: onCreate() only fires
                    // for a truly brand-new database file. fallbackToDestructiveMigration()'s
                    // upgrade path drops and recreates tables on an *existing* file without going
                    // through onCreate(), so an in-place app update would otherwise leave the new
                    // product_types/containers tables empty. onOpen() fires every time (fresh
                    // create, post-migration, and every normal open after that), and
                    // CONFLICT_IGNORE makes re-running it on an already-seeded db a no-op.
                    @Override
                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
                        super.onOpen(db);
                        for (String name : DEFAULT_TYPE_NAMES) {
                            ContentValues values = new ContentValues();
                            values.put("name", name);
                            db.insert("product_types", SQLiteDatabase.CONFLICT_IGNORE, values);
                        }
                        for (String name : DEFAULT_CONTAINER_NAMES) {
                            ContentValues values = new ContentValues();
                            values.put("name", name);
                            db.insert("containers", SQLiteDatabase.CONFLICT_IGNORE, values);
                        }
                    }
                })
                .build();
    }

    /**
     * Closes and drops the cached instance so the database file on disk can be safely
     * replaced (import) or inspected externally. The next {@link #getInstance(Context)}
     * call reopens a fresh instance.
     */
    public static synchronized void closeInstance() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }
}
