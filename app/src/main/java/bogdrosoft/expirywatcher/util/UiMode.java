package bogdrosoft.expirywatcher.util;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Ordinal doubles as the persisted SharedPreferences value and the selected index in the
 * settings dropdown (see R.array.ui_mode_options), so the declaration order here matters.
 */
public enum UiMode {
    SYSTEM_DEFAULT(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
    LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
    DARK(AppCompatDelegate.MODE_NIGHT_YES);

    private final int nightMode;

    UiMode(int nightMode) {
        this.nightMode = nightMode;
    }

    public int getNightMode() {
        return nightMode;
    }

    public static UiMode fromOrdinal(int ordinal) {
        UiMode[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return SYSTEM_DEFAULT;
        }
        return values[ordinal];
    }
}
