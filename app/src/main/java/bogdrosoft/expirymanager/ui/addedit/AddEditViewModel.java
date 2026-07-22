package bogdrosoft.expirymanager.ui.addedit;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

import bogdrosoft.expirymanager.data.entity.BarcodeDefaults;
import bogdrosoft.expirymanager.data.entity.Container;
import bogdrosoft.expirymanager.data.entity.Product;
import bogdrosoft.expirymanager.data.entity.ProductType;
import bogdrosoft.expirymanager.repository.ProductRepository;
import bogdrosoft.expirymanager.util.Constants;

public class AddEditViewModel extends AndroidViewModel {

    private final ProductRepository repository;
    private long productId = Constants.NO_PRODUCT_ID;

    public AddEditViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
    }

    public boolean isNew() {
        return productId == Constants.NO_PRODUCT_ID;
    }

    public LiveData<Product> loadProduct(long id) {
        this.productId = id;
        return repository.getProduct(id);
    }

    /**
     * Loads a product to prefill a duplicate of it, without marking this screen as editing
     * that product's row: {@code productId} is left untouched so {@link #isNew()} stays true
     * and {@link #save} inserts a brand-new product instead of overwriting the original.
     */
    public LiveData<Product> loadProductForDuplicate(long id) {
        return repository.getProduct(id);
    }

    public void save(Product product) {
        repository.saveProduct(product, isNew());
    }

    public void delete(Product product) {
        repository.deleteProduct(product);
    }

    public void lookupBarcodeDefaults(String barcode, ProductRepository.Callback<BarcodeDefaults> callback) {
        repository.lookupBarcodeDefaults(barcode, callback);
    }

    public LiveData<List<String>> getTypeNames() {
        return Transformations.map(repository.getAllTypes(), types -> {
            List<String> names = new ArrayList<>();
            for (ProductType type : types) {
                names.add(type.name);
            }
            return names;
        });
    }

    public LiveData<List<String>> getContainerNames() {
        return Transformations.map(repository.getAllContainers(), containers -> {
            List<String> names = new ArrayList<>();
            for (Container container : containers) {
                names.add(container.name);
            }
            return names;
        });
    }
}
