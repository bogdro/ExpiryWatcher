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
