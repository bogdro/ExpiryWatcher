package bogdrosoft.expirymanager;

import android.app.Application;

import bogdrosoft.expirymanager.reminder.NotificationHelper;
import bogdrosoft.expirymanager.reminder.ReminderScheduler;

public class ExpiryManagerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createChannel(this);
        ReminderScheduler.schedule(this);
    }
}
