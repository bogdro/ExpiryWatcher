package bogdrosoft.expirymanager.export;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import bogdrosoft.expirymanager.R;
import bogdrosoft.expirymanager.util.Constants;

/**
 * Shares the current product database as a raw SQLite file via the system share sheet.
 * Relies on {@link bogdrosoft.expirymanager.data.AppDatabase} using TRUNCATE journal mode,
 * which guarantees the on-disk file is a complete snapshot with no separate -wal/-shm parts
 * to worry about copying atomically.
 */
public class DbExportManager {

    // Two-digit month/day/hour/minute/second, four-digit year - "yyyy_MM_dd_HH_mm_ss".
    private static final DateTimeFormatter EXPORT_TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");

    private final Context context;

    public DbExportManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void export() {
        try {
            File dbFile = context.getDatabasePath(Constants.DATABASE_NAME);

            File exportsDir = new File(context.getCacheDir(), "exports");
            if (!exportsDir.exists() && !exportsDir.mkdirs()) {
                throw new IOException("Could not create exports directory");
            }

            String timestamp = LocalDateTime.now().format(EXPORT_TIMESTAMP_FORMATTER);
            File exportFile = new File(exportsDir, "expirymanager_export_" + timestamp + ".db");
            Files.copy(dbFile.toPath(), exportFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", exportFile);

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("application/octet-stream");
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooser = Intent.createChooser(sendIntent, context.getString(R.string.export_chooser_title));
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
        } catch (IOException e) {
            Toast.makeText(context, context.getString(R.string.export_failed, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }
}
