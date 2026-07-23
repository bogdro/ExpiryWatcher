package bogdrosoft.expirymanager.ui.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_products_title)
                .setMessage(R.string.dialog_delete_products_message)
                .setPositiveButton(R.string.action_delete_products, (d, which) ->
                        repository.deleteAllProducts(() ->
                                Toast.makeText(this, R.string.toast_products_deleted, Toast.LENGTH_SHORT).show()))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
        recolorAsDestructive(dialog);
    }

    private void confirmDeleteAllData() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_all_data_title)
                .setMessage(R.string.dialog_delete_all_data_message)
                .setPositiveButton(R.string.action_delete_all_data, (d, which) ->
                        repository.deleteAllData(() ->
                                Toast.makeText(this, R.string.toast_all_data_deleted, Toast.LENGTH_SHORT).show()))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
        recolorAsDestructive(dialog);
    }

    /**
     * Recolors just the positive ("Delete...") button red, leaving the negative ("Cancel")
     * button in the theme's default green.
     */
    private void recolorAsDestructive(AlertDialog dialog) {
        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this, R.color.delete_color));
    }
}
