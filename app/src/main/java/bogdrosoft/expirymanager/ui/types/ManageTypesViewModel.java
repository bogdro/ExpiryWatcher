package bogdrosoft.expirymanager.ui.types;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    public void renameType(String oldName, String newName, @Nullable Integer leadTimeDays,
            ProductRepository.Callback<Boolean> callback) {
        repository.renameType(oldName, newName, leadTimeDays, callback);
    }
}
