package bogdrosoft.expirymanager.export;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import bogdrosoft.expirymanager.R;
import bogdrosoft.expirymanager.data.AppDatabase;
import bogdrosoft.expirymanager.util.Constants;

/**
 * Replaces the current product list with the contents of a picked SQLite file.
 * Destructive by nature, so every step is guarded: the picked file is validated against the
 * expected schema before anything current is touched, the current database is backed up before
 * being overwritten, and any failure restores that backup rather than leaving a half-swapped db.
 */
public class DbImportManager {

    private static final int BACKUPS_TO_KEEP = 3;

    private final ComponentActivity activity;

    public DbImportManager(ComponentActivity activity) {
        this.activity = activity;
    }

    public void importFrom(Uri uri) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            File candidate;
            try {
                candidate = copyToTemp(uri);
            } catch (IOException e) {
                activity.runOnUiThread(() ->
                        Toast.makeText(activity, R.string.import_invalid_file, Toast.LENGTH_LONG).show());
                return;
            }

            if (!isValidExport(candidate)) {
                candidate.delete();
                activity.runOnUiThread(() ->
                        Toast.makeText(activity, R.string.import_invalid_file, Toast.LENGTH_LONG).show());
                return;
            }

            int currentCount = AppDatabase.getInstance(activity).productDao().countSync();
            activity.runOnUiThread(() -> showConfirmDialog(candidate, currentCount));
        });
    }

    private File copyToTemp(Uri uri) throws IOException {
        File tmpDir = new File(activity.getFilesDir(), "import_tmp");
        if (!tmpDir.exists() && !tmpDir.mkdirs()) {
            throw new IOException("Could not create import_tmp directory");
        }
        File candidate = new File(tmpDir, "candidate.db");
        try (InputStream in = activity.getContentResolver().openInputStream(uri)) {
            if (in == null) {
                throw new IOException("Could not open picked file");
            }
            Files.copy(in, candidate.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return candidate;
    }

    /**
     * Opens the candidate read-only via the raw framework API (not Room) and checks it actually
     * has the tables/columns this app expects, rather than trusting the file extension.
     */
    private boolean isValidExport(File candidate) {
        try (SQLiteDatabase db = SQLiteDatabase.openDatabase(
                candidate.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY)) {
            Set<String> tables = new HashSet<>();
            try (Cursor cursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type = 'table'", null)) {
                while (cursor.moveToNext()) {
                    tables.add(cursor.getString(0));
                }
            }
            if (!tables.contains("products") || !tables.contains("barcode_defaults")
                    || !tables.contains("product_types")) {
                return false;
            }
            return hasColumns(db, "products", "id", "name", "expiry_date", "open_date", "barcode")
                    && hasColumns(db, "barcode_defaults", "barcode", "name", "quantity", "unit")
                    && hasColumns(db, "product_types", "name", "lead_time_days");
        } catch (SQLiteException e) {
            return false;
        }
    }

    private boolean hasColumns(SQLiteDatabase db, String table, String... expectedColumns) {
        Set<String> columns = new HashSet<>();
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            int nameIndex = cursor.getColumnIndex("name");
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(nameIndex));
            }
        }
        return columns.containsAll(Arrays.asList(expectedColumns));
    }

    private void showConfirmDialog(File candidate, int currentCount) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.import_confirm_title)
                .setMessage(activity.getString(R.string.import_confirm_message, currentCount))
                .setCancelable(false)
                .setPositiveButton(R.string.import_confirm_action, (dialog, which) -> performImport(candidate))
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> candidate.delete())
                .show();
    }

    private void performImport(File candidate) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            File dbFile = activity.getDatabasePath(Constants.DATABASE_NAME);
            File backupDir = new File(activity.getFilesDir(), "backups");
            backupDir.mkdirs();
            File backup = new File(backupDir, "expirymanager_backup_" + System.currentTimeMillis() + ".db");

            try {
                AppDatabase.closeInstance();

                if (dbFile.exists()) {
                    Files.copy(dbFile.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                deleteSidecarFiles(dbFile);
                Files.copy(candidate.toPath(), dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Sanity read through Room before committing to the swap.
                AppDatabase.getInstance(activity).productDao().countSync();

                candidate.delete();
                rotateOldBackups(backupDir);
                activity.runOnUiThread(this::relaunchApp);
            } catch (Exception e) {
                restoreBackup(dbFile, backup);
                candidate.delete();
                activity.runOnUiThread(() ->
                        Toast.makeText(activity, R.string.import_failed_restored, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void restoreBackup(File dbFile, File backup) {
        try {
            AppDatabase.closeInstance();
            if (backup.exists()) {
                deleteSidecarFiles(dbFile);
                Files.copy(backup.toPath(), dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) {
            // Nothing more we can do here; the next getInstance() call surfaces any
            // remaining problem to the user via a failed query rather than silently.
        }
        AppDatabase.getInstance(activity);
    }

    private void deleteSidecarFiles(File dbFile) {
        new File(dbFile.getPath() + "-wal").delete();
        new File(dbFile.getPath() + "-shm").delete();
        new File(dbFile.getPath() + "-journal").delete();
    }

    private void rotateOldBackups(File backupDir) {
        File[] files = backupDir.listFiles();
        if (files == null || files.length <= BACKUPS_TO_KEEP) {
            return;
        }
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        for (int i = BACKUPS_TO_KEEP; i < files.length; i++) {
            files[i].delete();
        }
    }

    private void relaunchApp() {
        Intent intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        }
        Runtime.getRuntime().exit(0);
    }
}
