package bogdrosoft.expirywatcher.util;

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

    public static int getReminderHour(Context context) {
        return prefs(context).getInt(Constants.PREF_REMINDER_HOUR, Constants.DEFAULT_REMINDER_HOUR);
    }

    public static int getReminderMinute(Context context) {
        return prefs(context).getInt(Constants.PREF_REMINDER_MINUTE, Constants.DEFAULT_REMINDER_MINUTE);
    }

    public static void setReminderTime(Context context, int hour, int minute) {
        prefs(context).edit()
                .putInt(Constants.PREF_REMINDER_HOUR, hour)
                .putInt(Constants.PREF_REMINDER_MINUTE, minute)
                .apply();
    }

    public static boolean isScanSoundEnabled(Context context) {
        return prefs(context).getBoolean(Constants.PREF_SCAN_SOUND_ENABLED, Constants.DEFAULT_SCAN_SOUND_ENABLED);
    }

    public static void setScanSoundEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(Constants.PREF_SCAN_SOUND_ENABLED, enabled).apply();
    }

    public static boolean isNotifyExpiredEnabled(Context context) {
        return prefs(context).getBoolean(Constants.PREF_NOTIFY_EXPIRED_ENABLED, Constants.DEFAULT_NOTIFY_EXPIRED_ENABLED);
    }

    public static void setNotifyExpiredEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(Constants.PREF_NOTIFY_EXPIRED_ENABLED, enabled).apply();
    }

    public static boolean isNotifyExpiringSoonEnabled(Context context) {
        return prefs(context).getBoolean(Constants.PREF_NOTIFY_EXPIRING_SOON_ENABLED, Constants.DEFAULT_NOTIFY_EXPIRING_SOON_ENABLED);
    }

    public static void setNotifyExpiringSoonEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(Constants.PREF_NOTIFY_EXPIRING_SOON_ENABLED, enabled).apply();
    }

    public static boolean isHideExhaustedProductsEnabled(Context context) {
        return prefs(context).getBoolean(Constants.PREF_HIDE_EXHAUSTED_PRODUCTS, Constants.DEFAULT_HIDE_EXHAUSTED_PRODUCTS);
    }

    public static void setHideExhaustedProductsEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(Constants.PREF_HIDE_EXHAUSTED_PRODUCTS, enabled).apply();
    }

    public static SortOrder getSortOrder(Context context) {
        int ordinal = prefs(context).getInt(Constants.PREF_SORT_ORDER, Constants.DEFAULT_SORT_ORDER);
        return SortOrder.fromOrdinal(ordinal);
    }

    public static void setSortOrder(Context context, SortOrder sortOrder) {
        prefs(context).edit().putInt(Constants.PREF_SORT_ORDER, sortOrder.ordinal()).apply();
    }

    public static UiMode getUiMode(Context context) {
        int ordinal = prefs(context).getInt(Constants.PREF_UI_MODE, Constants.DEFAULT_UI_MODE);
        return UiMode.fromOrdinal(ordinal);
    }

    public static void setUiMode(Context context, UiMode uiMode) {
        prefs(context).edit().putInt(Constants.PREF_UI_MODE, uiMode.ordinal()).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }
}
