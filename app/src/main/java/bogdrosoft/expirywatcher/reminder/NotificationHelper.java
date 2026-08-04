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

package bogdrosoft.expirywatcher.reminder;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

import bogdrosoft.expirywatcher.R;
import bogdrosoft.expirywatcher.data.entity.Product;
import bogdrosoft.expirywatcher.ui.main.MainActivity;
import bogdrosoft.expirywatcher.util.Constants;
import bogdrosoft.expirywatcher.util.ProductStatusFilter;

public final class NotificationHelper {

    private NotificationHelper() {
    }

    public static void createChannel(Context context) {
        NotificationChannel channel = new NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(context.getString(R.string.notification_channel_description));

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(channel);
    }

    public static void showExpiredSummary(Context context, List<Product> expiredProducts) {
        showSummary(context, expiredProducts, Constants.NOTIFICATION_ID_EXPIRED_SUMMARY,
                R.string.notification_title_expired_single, R.string.notification_title_expired_plural,
                ProductStatusFilter.EXPIRED);
    }

    public static void cancelExpiredSummary(Context context) {
        NotificationManagerCompat.from(context).cancel(Constants.NOTIFICATION_ID_EXPIRED_SUMMARY);
    }

    public static void showExpiringSoonSummary(Context context, List<Product> expiringSoonProducts) {
        showSummary(context, expiringSoonProducts, Constants.NOTIFICATION_ID_EXPIRING_SOON_SUMMARY,
                R.string.notification_title_expiring_soon_single, R.string.notification_title_expiring_soon_plural,
                ProductStatusFilter.EXPIRING_SOON);
    }

    public static void cancelExpiringSoonSummary(Context context) {
        NotificationManagerCompat.from(context).cancel(Constants.NOTIFICATION_ID_EXPIRING_SOON_SUMMARY);
    }

    // Shared by both categories: an empty list cancels any previously-shown notification for
    // this id instead of leaving a stale one in the shade (e.g. the last expired product was
    // just deleted, or the user turned the category off after one was already posted).
    private static void showSummary(Context context, List<Product> products, int notificationId,
            int titleSingleRes, int titlePluralRes, ProductStatusFilter statusFilter) {
        if (products.isEmpty()) {
            NotificationManagerCompat.from(context).cancel(notificationId);
            return;
        }
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent contentIntent = new Intent(context, MainActivity.class);
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        contentIntent.putExtra(Constants.EXTRA_STATUS_FILTER, statusFilter.name());
        // Request code must differ between the two categories: PendingIntent identity ignores
        // extras, so sharing one (as both used to, via a hardcoded 0) would let whichever
        // category's summary is (re)posted more recently silently overwrite the other's tap
        // target, since PendingIntent.getActivity would just hand back the same instance.
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notificationId, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        int shown = Math.min(products.size(), 5);
        for (int i = 0; i < shown; i++) {
            style.addLine(products.get(i).name);
        }
        if (products.size() > shown) {
            style.setSummaryText(context.getString(R.string.notification_more_items, products.size() - shown));
        }

        int count = products.size();
        String title = count == 1
                ? context.getString(titleSingleRes, count)
                : context.getString(titlePluralRes, count);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setStyle(style)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }
}
