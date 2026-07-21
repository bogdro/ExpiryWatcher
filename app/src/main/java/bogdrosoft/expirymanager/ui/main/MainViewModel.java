package bogdrosoft.expirymanager.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bogdrosoft.expirymanager.data.entity.Product;
import bogdrosoft.expirymanager.data.entity.ProductType;
import bogdrosoft.expirymanager.repository.ProductRepository;
import bogdrosoft.expirymanager.util.SharedPrefsHelper;

public class MainViewModel extends AndroidViewModel {

    private final ProductRepository repository;
    // Bundles the search text and the "hide exhausted" setting together so both can drive the
    // same switchMap: the setting lives in plain SharedPreferences (not its own LiveData), so
    // MainActivity pushes its current value in here on every resume, same as it already does
    // for the lead-time-based list coloring.
    private final MutableLiveData<Filter> filter;
    private final LiveData<List<Product>> products;

    private static final class Filter {
        final String query;
        final boolean hideExhausted;

        Filter(String query, boolean hideExhausted) {
            this.query = query;
            this.hideExhausted = hideExhausted;
        }
    }

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
        filter = new MutableLiveData<>(new Filter("", SharedPrefsHelper.isHideExhaustedProductsEnabled(application)));
        products = Transformations.switchMap(filter, f -> repository.searchProducts(f.query, f.hideExhausted));
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public void setSearchQuery(String query) {
        Filter current = filter.getValue();
        boolean hideExhausted = current != null && current.hideExhausted;
        filter.setValue(new Filter(query == null ? "" : query, hideExhausted));
    }

    /**
     * Called from {@code MainActivity.onResume()}, since the setting itself lives in plain
     * SharedPreferences rather than something this ViewModel can observe on its own.
     */
    public void setHideExhausted(boolean hideExhausted) {
        Filter current = filter.getValue();
        String query = current != null ? current.query : "";
        if (current != null && current.hideExhausted == hideExhausted) {
            return;
        }
        filter.setValue(new Filter(query, hideExhausted));
    }

    public void deleteProduct(Product product) {
        repository.deleteProduct(product);
    }

    /**
     * Per-type expiry reminder lead time overrides, keyed by type name, for coloring the list
     * by expiry status the same way {@code ExpiryCheckWorker} decides what's "expiring soon".
     * Types with no override (null leadTimeDays) are left out; the caller falls back to the
     * global default lead time for those.
     */
    public LiveData<Map<String, Integer>> getTypeLeadTimeOverrides() {
        return Transformations.map(repository.getAllTypes(), types -> {
            Map<String, Integer> overrides = new HashMap<>();
            for (ProductType type : types) {
                if (type.leadTimeDays != null) {
                    overrides.put(type.name, type.leadTimeDays);
                }
            }
            return overrides;
        });
    }
}
