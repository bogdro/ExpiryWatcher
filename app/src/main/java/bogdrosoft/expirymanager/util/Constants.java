package bogdrosoft.expirymanager.util;

public final class Constants {

    private Constants() {
    }

    public static final String DATABASE_NAME = "expirymanager.db";

    public static final String PREFS_NAME = "expirymanager_prefs";
    public static final String PREF_LEAD_TIME_DAYS = "lead_time_days";
    public static final int DEFAULT_LEAD_TIME_DAYS = 3;
    public static final String PREF_ASKED_NOTIFICATION_PERMISSION = "asked_notification_permission";

    public static final String NOTIFICATION_CHANNEL_ID = "expiry_reminders";
    public static final int NOTIFICATION_ID_EXPIRY_SUMMARY = 1001;

    public static final String WORK_NAME_EXPIRY_CHECK = "expiry-check";

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";
    public static final long NO_PRODUCT_ID = -1L;
}
