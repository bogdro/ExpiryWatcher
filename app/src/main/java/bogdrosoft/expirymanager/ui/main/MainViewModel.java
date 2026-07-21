package bogdrosoft.expirymanager.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import bogdrosoft.expirymanager.data.entity.Product;
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
}
