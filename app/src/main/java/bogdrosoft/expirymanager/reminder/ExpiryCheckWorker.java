package bogdrosoft.expirymanager.reminder;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.time.LocalDate;
import java.util.List;

import bogdrosoft.expirymanager.data.AppDatabase;
import bogdrosoft.expirymanager.data.entity.Product;
import bogdrosoft.expirymanager.util.SharedPrefsHelper;

/**
 * Runs roughly once a day (see {@link ReminderScheduler}) and posts a single summary
 * notification for products expiring within the user-configured lead time. There is no
 * lower bound on the query, so already-overdue items keep surfacing until deleted/renewed.
 */
public class ExpiryCheckWorker extends Worker {

    public ExpiryCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        int leadTimeDays = SharedPrefsHelper.getLeadTimeDays(context);
        long thresholdEpochDay = LocalDate.now().plusDays(leadTimeDays).toEpochDay();

        List<Product> expiring = AppDatabase.getInstance(context)
                .productDao()
                .getExpiringByThresholdSync(thresholdEpochDay);

        NotificationHelper.showExpirySummary(context, expiring);
        return Result.success();
    }
}
