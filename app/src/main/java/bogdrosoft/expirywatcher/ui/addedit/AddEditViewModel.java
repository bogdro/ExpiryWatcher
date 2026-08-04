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

package bogdrosoft.expirywatcher.ui.addedit;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

import bogdrosoft.expirywatcher.data.entity.BarcodeDefaults;
import bogdrosoft.expirywatcher.data.entity.Container;
import bogdrosoft.expirywatcher.data.entity.Product;
import bogdrosoft.expirywatcher.data.entity.ProductType;
import bogdrosoft.expirywatcher.repository.ProductRepository;
import bogdrosoft.expirywatcher.util.Constants;

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
