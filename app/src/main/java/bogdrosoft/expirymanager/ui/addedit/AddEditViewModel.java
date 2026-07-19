package bogdrosoft.expirymanager.ui.addedit;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import bogdrosoft.expirymanager.data.entity.BarcodeDefaults;
import bogdrosoft.expirymanager.data.entity.Product;
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

    public void save(Product product) {
        repository.saveProduct(product, isNew());
    }

    public void delete(Product product) {
        repository.deleteProduct(product);
    }

    public void lookupBarcodeDefaults(String barcode, ProductRepository.Callback<BarcodeDefaults> callback) {
        repository.lookupBarcodeDefaults(barcode, callback);
    }
}
