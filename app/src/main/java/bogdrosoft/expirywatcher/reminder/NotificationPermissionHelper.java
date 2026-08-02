package bogdrosoft.expirywatcher.reminder;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import bogdrosoft.expirywatcher.util.Constants;

/**
 * Decides when to show the one-time POST_NOTIFICATIONS (API 33+) request: right after the
 * user's first successful product add, and never again automatically after that.
 */
public final class NotificationPermissionHelper {

    private NotificationPermissionHelper() {
    }

    public static boolean shouldRequest(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return false;
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return !prefs(context).getBoolean(Constants.PREF_ASKED_NOTIFICATION_PERMISSION, false);
    }

    public static void markAsked(Context context) {
        prefs(context).edit().putBoolean(Constants.PREF_ASKED_NOTIFICATION_PERMISSION, true).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }
}
