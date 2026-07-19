package bogdrosoft.expirymanager.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import bogdrosoft.expirymanager.data.entity.Product;
import bogdrosoft.expirymanager.repository.ProductRepository;

public class MainViewModel extends AndroidViewModel {

    private final ProductRepository repository;
    private final LiveData<List<Product>> products;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
        products = repository.getAllProducts();
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public void deleteProduct(Product product) {
        repository.deleteProduct(product);
    }
}
