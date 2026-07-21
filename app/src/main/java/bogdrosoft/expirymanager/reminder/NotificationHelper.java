package bogdrosoft.expirymanager.reminder;

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

import bogdrosoft.expirymanager.R;
import bogdrosoft.expirymanager.data.entity.Product;
import bogdrosoft.expirymanager.ui.main.MainActivity;
import bogdrosoft.expirymanager.util.Constants;

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
                R.string.notification_title_expired_single, R.string.notification_title_expired_plural);
    }

    public static void cancelExpiredSummary(Context context) {
        NotificationManagerCompat.from(context).cancel(Constants.NOTIFICATION_ID_EXPIRED_SUMMARY);
    }

    public static void showExpiringSoonSummary(Context context, List<Product> expiringSoonProducts) {
        showSummary(context, expiringSoonProducts, Constants.NOTIFICATION_ID_EXPIRING_SOON_SUMMARY,
                R.string.notification_title_expiring_soon_single, R.string.notification_title_expiring_soon_plural);
    }

    public static void cancelExpiringSoonSummary(Context context) {
        NotificationManagerCompat.from(context).cancel(Constants.NOTIFICATION_ID_EXPIRING_SOON_SUMMARY);
    }

    // Shared by both categories: an empty list cancels any previously-shown notification for
    // this id instead of leaving a stale one in the shade (e.g. the last expired product was
    // just deleted, or the user turned the category off after one was already posted).
    private static void showSummary(Context context, List<Product> products, int notificationId,
            int titleSingleRes, int titlePluralRes) {
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
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

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
