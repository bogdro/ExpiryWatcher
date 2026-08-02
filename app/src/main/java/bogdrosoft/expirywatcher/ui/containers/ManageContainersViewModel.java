package bogdrosoft.expirywatcher.ui.containers;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import bogdrosoft.expirywatcher.data.entity.Container;
import bogdrosoft.expirywatcher.repository.ProductRepository;

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

    public void renameContainer(String oldName, String newName, ProductRepository.Callback<Boolean> callback) {
        repository.renameContainer(oldName, newName, callback);
    }
}
