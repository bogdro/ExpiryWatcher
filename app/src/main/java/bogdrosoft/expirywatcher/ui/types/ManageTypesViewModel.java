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

package bogdrosoft.expirywatcher.ui.types;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import bogdrosoft.expirywatcher.data.entity.ProductType;
import bogdrosoft.expirywatcher.repository.ProductRepository;

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
