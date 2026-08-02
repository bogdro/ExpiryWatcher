package bogdrosoft.expirywatcher.reminder;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bogdrosoft.expirywatcher.data.AppDatabase;
import bogdrosoft.expirywatcher.data.entity.Product;
import bogdrosoft.expirywatcher.data.entity.ProductType;
import bogdrosoft.expirywatcher.util.SharedPrefsHelper;

/**
 * Runs roughly once a day (see {@link ReminderScheduler}) and posts up to two summary
 * notifications, one for already-overdue products and one for products expiring within their
 * effective lead time (a product type's own override if it has one, otherwise the global
 * default). Each category is independently toggleable in Settings; already-overdue items keep
 * surfacing until deleted/renewed.
 */
public class ExpiryCheckWorker extends Worker {

    public ExpiryCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        int defaultLeadTimeDays = SharedPrefsHelper.getLeadTimeDays(context);

        Map<String, Integer> typeLeadTimeOverrides = new HashMap<>();
        for (ProductType type : db.productTypeDao().getAllSync()) {
            if (type.leadTimeDays != null) {
                typeLeadTimeOverrides.put(type.name, type.leadTimeDays);
            }
        }

        LocalDate today = LocalDate.now();
        List<Product> expired = new ArrayList<>();
        List<Product> expiringSoon = new ArrayList<>();
        for (Product product : db.productDao().getAllSortedByExpirySync()) {
            if (product.quantity == 0) {
                // Exhausted: nothing left to use or throw out, so it's no longer relevant to
                // either reminder category regardless of its expiry date.
                continue;
            }
            int leadTimeDays = typeLeadTimeOverrides.getOrDefault(product.type, defaultLeadTimeDays);
            long daysLeft = product.expiryDate.toEpochDay() - today.toEpochDay();
            if (daysLeft < 0) {
                expired.add(product);
            } else if (daysLeft <= leadTimeDays) {
                expiringSoon.add(product);
            }
        }

        if (SharedPrefsHelper.isNotifyExpiredEnabled(context)) {
            NotificationHelper.showExpiredSummary(context, expired);
        } else {
            NotificationHelper.cancelExpiredSummary(context);
        }

        if (SharedPrefsHelper.isNotifyExpiringSoonEnabled(context)) {
            NotificationHelper.showExpiringSoonSummary(context, expiringSoon);
        } else {
            NotificationHelper.cancelExpiringSoonSummary(context);
        }

        return Result.success();
    }
}
