package bogdrosoft.expirymanager.ui.containers;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import bogdrosoft.expirymanager.data.entity.Container;
import bogdrosoft.expirymanager.repository.ProductRepository;

public class ManageContainersViewModel extends AndroidViewModel {

    private final ProductRepository repository;

    public ManageContainersViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
    }

    public LiveData<List<Container>> getContainers() {
        return repository.getAllContainers();
    }

    public void addContainer(Container container, ProductRepository.Callback<Boolean> callback) {
        repository.addContainer(container, callback);
    }

    public void deleteContainer(Container container) {
        repository.deleteContainer(container);
    }
}
