package bogdrosoft.expirymanager.util;

public final class Constants {

    private Constants() {
    }

    public static final String DATABASE_NAME = "expirymanager.db";

    public static final String PREFS_NAME = "expirymanager_prefs";
    public static final String PREF_LEAD_TIME_DAYS = "lead_time_days";
    public static final int DEFAULT_LEAD_TIME_DAYS = 3;
    public static final String PREF_REMINDER_HOUR = "reminder_hour";
    public static final int DEFAULT_REMINDER_HOUR = 9;
    public static final String PREF_REMINDER_MINUTE = "reminder_minute";
    public static final int DEFAULT_REMINDER_MINUTE = 0;
    public static final String PREF_ASKED_NOTIFICATION_PERMISSION = "asked_notification_permission";
    public static final String PREF_SCAN_SOUND_ENABLED = "scan_sound_enabled";
    public static final boolean DEFAULT_SCAN_SOUND_ENABLED = true;
    public static final String PREF_NOTIFY_EXPIRED_ENABLED = "notify_expired_enabled";
    public static final boolean DEFAULT_NOTIFY_EXPIRED_ENABLED = true;
    public static final String PREF_NOTIFY_EXPIRING_SOON_ENABLED = "notify_expiring_soon_enabled";
    public static final boolean DEFAULT_NOTIFY_EXPIRING_SOON_ENABLED = true;
    public static final String PREF_HIDE_EXHAUSTED_PRODUCTS = "hide_exhausted_products";
    public static final boolean DEFAULT_HIDE_EXHAUSTED_PRODUCTS = false;
    public static final String PREF_SORT_ORDER = "sort_order";
    public static final int DEFAULT_SORT_ORDER = 0; // SortOrder.EXPIRY_ASC.ordinal()
    public static final String PREF_UI_MODE = "ui_mode";
    public static final int DEFAULT_UI_MODE = 0; // UiMode.SYSTEM_DEFAULT.ordinal()

    public static final String NOTIFICATION_CHANNEL_ID = "expiry_reminders";
    public static final int NOTIFICATION_ID_EXPIRED_SUMMARY = 1001;
    public static final int NOTIFICATION_ID_EXPIRING_SOON_SUMMARY = 1002;

    public static final String WORK_NAME_EXPIRY_CHECK = "expiry-check";

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";
    public static final String EXTRA_DUPLICATE_FROM_ID = "extra_duplicate_from_id";
    public static final long NO_PRODUCT_ID = -1L;
}
