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
