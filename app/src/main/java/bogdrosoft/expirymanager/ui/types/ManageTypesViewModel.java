package bogdrosoft.expirymanager.ui.types;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import bogdrosoft.expirymanager.data.entity.ProductType;
import bogdrosoft.expirymanager.repository.ProductRepository;

public class ManageTypesViewModel extends AndroidViewModel {

    private final ProductRepository repository;

    public ManageTypesViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
    }

    public LiveData<List<ProductType>> getTypes() {
        return repository.getAllTypes();
    }

    public void addType(ProductType type, ProductRepository.Callback<Boolean> callback) {
        repository.addType(type, callback);
    }

    public void updateType(ProductType type) {
        repository.updateType(type);
    }

    public void deleteType(ProductType type) {
        repository.deleteType(type);
    }
}
