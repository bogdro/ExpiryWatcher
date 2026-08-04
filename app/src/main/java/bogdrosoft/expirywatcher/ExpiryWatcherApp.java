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

package bogdrosoft.expirywatcher;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import bogdrosoft.expirywatcher.reminder.NotificationHelper;
import bogdrosoft.expirywatcher.reminder.ReminderScheduler;
import bogdrosoft.expirywatcher.util.SharedPrefsHelper;

public class ExpiryWatcherApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(SharedPrefsHelper.getUiMode(this).getNightMode());
        NotificationHelper.createChannel(this);
        ReminderScheduler.schedule(this);
    }
}
