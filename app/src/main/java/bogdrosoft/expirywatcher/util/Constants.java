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

package bogdrosoft.expirywatcher.util;

public final class Constants {

    private Constants() {
    }

    public static final String DATABASE_NAME = "expirywatcher.db";

    public static final String PREFS_NAME = "expirywatcher_prefs";
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
    public static final String EXTRA_STATUS_FILTER = "extra_status_filter";
    public static final long NO_PRODUCT_ID = -1L;
}
