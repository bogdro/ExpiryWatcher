package bogdrosoft.expirymanager.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import bogdrosoft.expirymanager.data.entity.Container;
import bogdrosoft.expirymanager.data.entity.Product;
import bogdrosoft.expirymanager.data.entity.ProductType;
import bogdrosoft.expirymanager.repository.ProductRepository;
import bogdrosoft.expirymanager.util.ProductStatusFilter;
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
        @Nullable final String containerFilter;
        @Nullable final String typeFilter;
        @Nullable final ProductStatusFilter statusFilter;
        final int defaultLeadTimeDays;

        Filter(String query, boolean hideExhausted, SortOrder sortOrder, @Nullable String containerFilter,
                @Nullable String typeFilter, @Nullable ProductStatusFilter statusFilter, int defaultLeadTimeDays) {
            this.query = query;
            this.hideExhausted = hideExhausted;
            this.sortOrder = sortOrder;
            this.containerFilter = containerFilter;
            this.typeFilter = typeFilter;
            this.statusFilter = statusFilter;
            this.defaultLeadTimeDays = defaultLeadTimeDays;
        }
    }

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
        filter = new MutableLiveData<>(new Filter("", SharedPrefsHelper.isHideExhaustedProductsEnabled(application),
                SharedPrefsHelper.getSortOrder(application), null, null, null,
                SharedPrefsHelper.getLeadTimeDays(application)));
        products = Transformations.switchMap(filter, f -> repository.searchProducts(f.query, f.hideExhausted, f.sortOrder,
                f.containerFilter, f.typeFilter, f.statusFilter, f.defaultLeadTimeDays));
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public void setSearchQuery(String query) {
        Filter current = filter.getValue();
        if (current == null) {
            return;
        }
        filter.setValue(new Filter(query == null ? "" : query, current.hideExhausted, current.sortOrder,
                current.containerFilter, current.typeFilter, current.statusFilter, current.defaultLeadTimeDays));
    }

    /**
     * Called from {@code MainActivity.onResume()}, since the setting itself lives in plain
     * SharedPreferences rather than something this ViewModel can observe on its own.
     */
    public void setHideExhausted(boolean hideExhausted) {
        Filter current = filter.getValue();
        if (current == null || current.hideExhausted == hideExhausted) {
            return;
        }
        filter.setValue(new Filter(current.query, hideExhausted, current.sortOrder,
                current.containerFilter, current.typeFilter, current.statusFilter, current.defaultLeadTimeDays));
    }

    /**
     * Called when the user picks a sort order from the main screen's "Sort" dialog.
     */
    public void setSortOrder(SortOrder sortOrder) {
        Filter current = filter.getValue();
        if (current == null || current.sortOrder == sortOrder) {
            return;
        }
        filter.setValue(new Filter(current.query, current.hideExhausted, sortOrder,
                current.containerFilter, current.typeFilter, current.statusFilter, current.defaultLeadTimeDays));
    }

    /**
     * Called when the user picks a container from the main screen's "Filter" dialog;
     * {@code null} means "any container".
     */
    public void setContainerFilter(@Nullable String containerFilter) {
        Filter current = filter.getValue();
        if (current == null || Objects.equals(current.containerFilter, containerFilter)) {
            return;
        }
        filter.setValue(new Filter(current.query, current.hideExhausted, current.sortOrder,
                containerFilter, current.typeFilter, current.statusFilter, current.defaultLeadTimeDays));
    }

    /**
     * Called when the user picks a type from the main screen's "Filter" dialog;
     * {@code null} means "any type".
     */
    public void setTypeFilter(@Nullable String typeFilter) {
        Filter current = filter.getValue();
        if (current == null || Objects.equals(current.typeFilter, typeFilter)) {
            return;
        }
        filter.setValue(new Filter(current.query, current.hideExhausted, current.sortOrder,
                current.containerFilter, typeFilter, current.statusFilter, current.defaultLeadTimeDays));
    }

    /**
     * Called when the user picks a status from the main screen's "Filter" dialog;
     * {@code null} means "any status".
     */
    public void setStatusFilter(@Nullable ProductStatusFilter statusFilter) {
        Filter current = filter.getValue();
        if (current == null || current.statusFilter == statusFilter) {
            return;
        }
        filter.setValue(new Filter(current.query, current.hideExhausted, current.sortOrder,
                current.containerFilter, current.typeFilter, statusFilter, current.defaultLeadTimeDays));
    }

    /**
     * Called from {@code MainActivity.onResume()}: the global default lead time can change in
     * Settings while this screen isn't visible, and the status filter's "about to expire"/"not
     * expiring" buckets depend on it for products without their own type override.
     */
    public void refreshDefaultLeadTimeDays(int defaultLeadTimeDays) {
        Filter current = filter.getValue();
        if (current == null || current.defaultLeadTimeDays == defaultLeadTimeDays) {
            return;
        }
        filter.setValue(new Filter(current.query, current.hideExhausted, current.sortOrder,
                current.containerFilter, current.typeFilter, current.statusFilter, defaultLeadTimeDays));
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
        copy.notes = source.notes;
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

    /**
     * Container/type names for the main screen's "Filter" dialog to build its value pickers
     * from, kept as plain name lists since the dialog has no use for the rest of either entity.
     */
    public LiveData<List<String>> getAllContainerNames() {
        return Transformations.map(repository.getAllContainers(), containers -> {
            List<String> names = new ArrayList<>();
            for (Container container : containers) {
                names.add(container.name);
            }
            return names;
        });
    }

    public LiveData<List<String>> getAllTypeNames() {
        return Transformations.map(repository.getAllTypes(), types -> {
            List<String> names = new ArrayList<>();
            for (ProductType type : types) {
                names.add(type.name);
            }
            return names;
        });
    }
}
