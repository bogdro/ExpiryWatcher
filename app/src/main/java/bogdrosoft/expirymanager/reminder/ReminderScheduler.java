package bogdrosoft.expirymanager.reminder;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import bogdrosoft.expirymanager.util.Constants;

public final class ReminderScheduler {

    private ReminderScheduler() {
    }

    /**
     * Idempotent: safe to call on every app start. {@link ExistingPeriodicWorkPolicy#KEEP}
     * leaves an already-scheduled job's timing anchor untouched rather than resetting it.
     */
    public static void schedule(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                ExpiryCheckWorker.class, 24, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                Constants.WORK_NAME_EXPIRY_CHECK,
                ExistingPeriodicWorkPolicy.KEEP,
                request);
    }
}
