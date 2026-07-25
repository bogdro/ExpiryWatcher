package bogdrosoft.expirymanager;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import bogdrosoft.expirymanager.reminder.NotificationHelper;
import bogdrosoft.expirymanager.reminder.ReminderScheduler;
import bogdrosoft.expirymanager.util.SharedPrefsHelper;

public class ExpiryManagerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(SharedPrefsHelper.getUiMode(this).getNightMode());
        NotificationHelper.createChannel(this);
        ReminderScheduler.schedule(this);
    }
}
