package bogdrosoft.expirymanager.reminder;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import bogdrosoft.expirymanager.util.Constants;
import bogdrosoft.expirymanager.util.SharedPrefsHelper;

public final class ReminderScheduler {

    private ReminderScheduler() {
    }

    /**
     * Idempotent: safe to call on every app start. {@link ExistingPeriodicWorkPolicy#KEEP}
     * leaves an already-scheduled job's timing anchor untouched rather than resetting it.
     */
    public static void schedule(Context context) {
        enqueue(context, ExistingPeriodicWorkPolicy.KEEP);
    }

    /**
     * Used when the user changes the reminder time in Settings: forces WorkManager to drop the
     * existing job and re-anchor to the new time, which {@link ExistingPeriodicWorkPolicy#KEEP}
     * would otherwise ignore.
     */
    public static void reschedule(Context context) {
        enqueue(context, ExistingPeriodicWorkPolicy.REPLACE);
    }

    private static void enqueue(Context context, ExistingPeriodicWorkPolicy policy) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                ExpiryCheckWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(millisUntilNextReminderTime(context), TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                Constants.WORK_NAME_EXPIRY_CHECK,
                policy,
                request);
    }

    private static long millisUntilNextReminderTime(Context context) {
        int hour = SharedPrefsHelper.getReminderHour(context);
        int minute = SharedPrefsHelper.getReminderMinute(context);

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0);
        if (!next.isAfter(now)) {
            next = next.plusDays(1);
        }
        return Duration.between(now, next).toMillis();
    }
}
