package bogdrosoft.expirymanager.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bogdrosoft.expirymanager.data.entity.Product;
import bogdrosoft.expirymanager.data.entity.ProductType;
import bogdrosoft.expirymanager.repository.ProductRepository;
import bogdrosoft.expirymanager.util.SharedPrefsHelper;
import bogdrosoft.expirymanager.util.SortOrder;

public class MainViewModel extends AndroidViewModel {

    private final ProductRepository repository;
    // Bundles the search text, the "hide exhausted" setting and the sort order together so all
    // three can drive the same switchMap: the setting/sort order live in plain SharedPreferences
    // (not their own LiveData), so MainActivity pushes their current value in here, same as it
    // already does for the lead-time-based list coloring.
    private final MutableLiveData<Filter> filter;
    private final LiveData<List<Product>> products;

    private static final class Filter {
        final String query;
        final boolean hideExhausted;
        final SortOrder sortOrder;

        Filter(String query, boolean hideExhausted, SortOrder sortOrder) {
            this.query = query;
            this.hideExhausted = hideExhausted;
            this.sortOrder = sortOrder;
        }
    }

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
        filter = new MutableLiveData<>(new Filter("", SharedPrefsHelper.isHideExhaustedProductsEnabled(application),
                SharedPrefsHelper.getSortOrder(application)));
        products = Transformations.switchMap(filter, f -> repository.searchProducts(f.query, f.hideExhausted, f.sortOrder));
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public void setSearchQuery(String query) {
        Filter current = filter.getValue();
        boolean hideExhausted = current != null && current.hideExhausted;
        SortOrder sortOrder = current != null ? current.sortOrder : SortOrder.DEFAULT;
        filter.setValue(new Filter(query == null ? "" : query, hideExhausted, sortOrder));
    }

    /**
     * Called from {@code MainActivity.onResume()}, since the setting itself lives in plain
     * SharedPreferences rather than something this ViewModel can observe on its own.
     */
    public void setHideExhausted(boolean hideExhausted) {
        Filter current = filter.getValue();
        String query = current != null ? current.query : "";
        SortOrder sortOrder = current != null ? current.sortOrder : SortOrder.DEFAULT;
        if (current != null && current.hideExhausted == hideExhausted) {
            return;
        }
        filter.setValue(new Filter(query, hideExhausted, sortOrder));
    }

    /**
     * Called when the user picks a sort order from the main screen's "Sort" dialog.
     */
    public void setSortOrder(SortOrder sortOrder) {
        Filter current = filter.getValue();
        String query = current != null ? current.query : "";
        boolean hideExhausted = current != null && current.hideExhausted;
        if (current != null && current.sortOrder == sortOrder) {
            return;
        }
        filter.setValue(new Filter(query, hideExhausted, sortOrder));
    }

    public void deleteProduct(Product product) {
        repository.deleteProduct(product);
    }

    /**
     * Sets today as the product's open date, from the main list's "Mark as opened" action.
     */
    public void markAsOpened(Product product) {
        Product updated = copyOf(product);
        updated.openDate = LocalDate.now();
        repository.saveProduct(updated, false);
    }

    /**
     * Zeroes out the product's quantity, from the main list's "Set quantity to 0" action; this
     * also makes it "exhausted", so it sinks to the bottom of the default sort and gets its own
     * card color, same as if it had been edited down to 0 by hand.
     */
    public void setQuantityToZero(Product product) {
        Product updated = copyOf(product);
        updated.quantity = 0;
        repository.saveProduct(updated, false);
    }

    /**
     * A fresh instance, not a mutation of {@code source}: that reference is the exact object
     * the adapter's currently-displayed list is holding, and mutating it in place would corrupt
     * the "old" snapshot DiffUtil later compares the refreshed Room query result against, making
     * it see no change and skip re-binding the row (same pitfall fixed for the type editor).
     */
    private static Product copyOf(Product source) {
        Product copy = new Product();
        copy.id = source.id;
        copy.name = source.name;
        copy.type = source.type;
        copy.quantity = source.quantity;
        copy.unit = source.unit;
        copy.expiryDate = source.expiryDate;
        copy.openDate = source.openDate;
        copy.container = source.container;
        copy.barcode = source.barcode;
        copy.createdAt = source.createdAt;
        copy.updatedAt = source.updatedAt;
        return copy;
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
