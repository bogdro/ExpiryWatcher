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

public class MainViewModel extends AndroidViewModel {

    private final ProductRepository repository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final LiveData<List<Product>> products;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
        products = Transformations.switchMap(searchQuery, repository::searchProducts);
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query == null ? "" : query);
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
