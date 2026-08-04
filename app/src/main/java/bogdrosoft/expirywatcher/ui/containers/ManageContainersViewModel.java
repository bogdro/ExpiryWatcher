/*
 * Copyright (C) 2026 Bogdan Drozdowski, bogdro (at) users . sourceforge . net
 * License: GNU General Public License, v3+
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
