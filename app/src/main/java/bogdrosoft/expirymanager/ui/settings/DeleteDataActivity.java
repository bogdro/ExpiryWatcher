package bogdrosoft.expirymanager.ui.settings;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import bogdrosoft.expirymanager.R;
import bogdrosoft.expirymanager.databinding.ActivityDeleteDataBinding;
import bogdrosoft.expirymanager.repository.ProductRepository;

public class DeleteDataActivity extends AppCompatActivity {

    private ActivityDeleteDataBinding binding;
    private ProductRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeleteDataBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        setTitle(R.string.title_delete_data);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        repository = new ProductRepository(this);

        binding.buttonDeleteProducts.setOnClickListener(v -> confirmDeleteProducts());
        binding.buttonDeleteAllData.setOnClickListener(v -> confirmDeleteAllData());
    }

    private void confirmDeleteProducts() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_products_title)
                .setMessage(R.string.dialog_delete_products_message)
                .setPositiveButton(R.string.action_delete_products, (dialog, which) ->
                        repository.deleteAllProducts(() ->
                                Toast.makeText(this, R.string.toast_products_deleted, Toast.LENGTH_SHORT).show()))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void confirmDeleteAllData() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_all_data_title)
                .setMessage(R.string.dialog_delete_all_data_message)
                .setPositiveButton(R.string.action_delete_all_data, (dialog, which) ->
                        repository.deleteAllData(() ->
                                Toast.makeText(this, R.string.toast_all_data_deleted, Toast.LENGTH_SHORT).show()))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
