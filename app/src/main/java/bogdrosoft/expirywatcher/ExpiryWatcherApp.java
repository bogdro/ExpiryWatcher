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
