package bogdrosoft.expirymanager.reminder;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bogdrosoft.expirymanager.data.AppDatabase;
import bogdrosoft.expirymanager.data.entity.Product;
import bogdrosoft.expirymanager.data.entity.ProductType;
import bogdrosoft.expirymanager.util.SharedPrefsHelper;

/**
 * Runs roughly once a day (see {@link ReminderScheduler}) and posts a single summary
 * notification for products expiring within their effective lead time: a product type's own
 * override if it has one, otherwise the global default. There is no lower bound on the
 * comparison, so already-overdue items keep surfacing until deleted/renewed.
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
        List<Product> expiring = new ArrayList<>();
        for (Product product : db.productDao().getAllSortedByExpirySync()) {
            int leadTimeDays = typeLeadTimeOverrides.getOrDefault(product.type, defaultLeadTimeDays);
            if (product.expiryDate.toEpochDay() <= today.plusDays(leadTimeDays).toEpochDay()) {
                expiring.add(product);
            }
        }

        NotificationHelper.showExpirySummary(context, expiring);
        return Result.success();
    }
}
