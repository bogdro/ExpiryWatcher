/*
 * Copyright (C) 2026 Bogdan Drozdowski, bogdro (at) users . sourceforge . net
 * License: GNU General Public License, v3+
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package bogdrosoft.expirywatcher.export;

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

import bogdrosoft.expirywatcher.R;
import bogdrosoft.expirywatcher.util.Constants;

/**
 * Shares the current product database as a raw SQLite file via the system share sheet.
 * Relies on {@link bogdrosoft.expirywatcher.data.AppDatabase} using TRUNCATE journal mode,
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
            File exportFile = new File(exportsDir, "expirywatcher_export_" + timestamp + ".db");
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
