package bogdrosoft.expirymanager.ui.addedit;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import bogdrosoft.expirymanager.R;
import bogdrosoft.expirymanager.data.entity.Product;
import bogdrosoft.expirymanager.databinding.ActivityAddEditBinding;
import bogdrosoft.expirymanager.reminder.NotificationPermissionHelper;
import bogdrosoft.expirymanager.scan.BarcodeScanHelper;
import bogdrosoft.expirymanager.util.Constants;

public class AddEditActivity extends AppCompatActivity {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private ActivityAddEditBinding binding;
    private AddEditViewModel viewModel;

    private long productId = Constants.NO_PRODUCT_ID;
    @Nullable
    private Product currentProduct;
    @Nullable
    private LocalDate selectedExpiryDate;

    private final BarcodeScanHelper scanHelper = new BarcodeScanHelper(this, this::onBarcodeScanned);

    // Registered at construction time (required by the Activity Result API contract);
    // actually launched conditionally after the first successful save. NotificationHelper
    // re-checks the permission before posting either way, so a denial here is harmless.
    private final androidx.activity.result.ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
                    granted -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.editUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.default_units)));

        viewModel = new ViewModelProvider(this).get(AddEditViewModel.class);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        binding.editType.setAdapter(typeAdapter);
        viewModel.getTypeNames().observe(this, names -> {
            typeAdapter.clear();
            typeAdapter.addAll(names);
            typeAdapter.notifyDataSetChanged();
        });

        productId = getIntent().getLongExtra(Constants.EXTRA_PRODUCT_ID, Constants.NO_PRODUCT_ID);
        if (productId == Constants.NO_PRODUCT_ID) {
            setTitle(R.string.title_add_product);
            selectedExpiryDate = null;
        } else {
            setTitle(R.string.title_edit_product);
            viewModel.loadProduct(productId).observe(this, this::populateFields);
        }

        binding.editExpiryDate.setOnClickListener(v -> showDatePicker());
        binding.buttonScan.setOnClickListener(v -> {
            hideKeyboardAndClearFocus();
            scanHelper.startScan();
        });
        binding.buttonSave.setOnClickListener(v -> onSaveClicked());
    }

    /**
     * The expiry-date field and the scan button are not text inputs, so tapping them doesn't
     * naturally move focus (and hide the keyboard) away from whichever EditText was focused
     * beforehand. Called explicitly before acting on either.
     */
    private void hideKeyboardAndClearFocus() {
        View focused = getCurrentFocus();
        if (focused == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
        focused.clearFocus();
    }

    private void populateFields(@Nullable Product product) {
        if (product == null) {
            return;
        }
        currentProduct = product;
        binding.editName.setText(product.name);
        binding.editType.setText(product.type, false);
        binding.editQuantity.setText(String.valueOf(product.quantity));
        binding.editUnit.setText(product.unit, false);
        binding.editBarcode.setText(product.barcode);
        selectedExpiryDate = product.expiryDate;
        binding.editExpiryDate.setText(product.expiryDate.format(DATE_FORMATTER));
        invalidateOptionsMenu();
    }

    private void showDatePicker() {
        hideKeyboardAndClearFocus();
        LocalDate seed = selectedExpiryDate != null ? selectedExpiryDate : LocalDate.now();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedExpiryDate = LocalDate.of(year, month + 1, dayOfMonth);
            binding.editExpiryDate.setText(selectedExpiryDate.format(DATE_FORMATTER));
        }, seed.getYear(), seed.getMonthValue() - 1, seed.getDayOfMonth()).show();
    }

    private void onBarcodeScanned(String barcode) {
        binding.editBarcode.setText(barcode);
        viewModel.lookupBarcodeDefaults(barcode, defaults -> {
            if (defaults == null) {
                binding.textBarcodePrefillHint.setVisibility(android.view.View.GONE);
                return;
            }
            binding.editName.setText(defaults.name);
            binding.editType.setText(defaults.type, false);
            binding.editQuantity.setText(String.valueOf(defaults.quantity));
            binding.editUnit.setText(defaults.unit, false);
            binding.textBarcodePrefillHint.setVisibility(android.view.View.VISIBLE);
        });
    }

    private void onSaveClicked() {
        String name = text(binding.editName);
        String type = text(binding.editType);
        String unit = text(binding.editUnit);
        String quantityText = text(binding.editQuantity);
        String barcode = text(binding.editBarcode);

        if (name.isEmpty()) {
            binding.layoutName.setError(getString(R.string.error_name_required));
            return;
        }
        binding.layoutName.setError(null);

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
            if (quantity < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            binding.layoutQuantity.setError(getString(R.string.error_quantity_invalid));
            return;
        }
        binding.layoutQuantity.setError(null);

        if (selectedExpiryDate == null) {
            Toast.makeText(this, R.string.error_expiry_required, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isNew = viewModel.isNew();
        Product product = isNew || currentProduct == null ? new Product() : currentProduct;
        product.name = name;
        product.type = type;
        product.quantity = quantity;
        product.unit = unit;
        product.expiryDate = selectedExpiryDate;
        product.barcode = barcode.isEmpty() ? null : barcode;

        viewModel.save(product);

        if (isNew && NotificationPermissionHelper.shouldRequest(this)) {
            NotificationPermissionHelper.markAsked(this);
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
        }

        finish();
    }

    private static String text(android.widget.TextView view) {
        return view.getText() == null ? "" : view.getText().toString().trim();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!viewModel.isNew()) {
            getMenuInflater().inflate(R.menu.menu_add_edit, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    if (currentProduct != null) {
                        viewModel.delete(currentProduct);
                    }
                    finish();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
