package bogdrosoft.expirymanager.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class SharedPrefsHelper {

    private SharedPrefsHelper() {
    }

    public static int getLeadTimeDays(Context context) {
        return prefs(context).getInt(Constants.PREF_LEAD_TIME_DAYS, Constants.DEFAULT_LEAD_TIME_DAYS);
    }

    public static void setLeadTimeDays(Context context, int days) {
        prefs(context).edit().putInt(Constants.PREF_LEAD_TIME_DAYS, days).apply();
    }

    public static boolean isScanSoundEnabled(Context context) {
        return prefs(context).getBoolean(Constants.PREF_SCAN_SOUND_ENABLED, Constants.DEFAULT_SCAN_SOUND_ENABLED);
    }

    public static void setScanSoundEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(Constants.PREF_SCAN_SOUND_ENABLED, enabled).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }
}
