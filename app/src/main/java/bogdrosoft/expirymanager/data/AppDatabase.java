package bogdrosoft.expirymanager.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bogdrosoft.expirymanager.data.dao.BarcodeDefaultsDao;
import bogdrosoft.expirymanager.data.dao.ProductDao;
import bogdrosoft.expirymanager.data.entity.BarcodeDefaults;
import bogdrosoft.expirymanager.data.entity.Product;
import bogdrosoft.expirymanager.util.Constants;

@Database(entities = {Product.class, BarcodeDefaults.class}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(2);

    public abstract ProductDao productDao();

    public abstract BarcodeDefaultsDao barcodeDefaultsDao();

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
