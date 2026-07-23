package bogdrosoft.expirymanager.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.Map;

import bogdrosoft.expirymanager.R;
import bogdrosoft.expirymanager.data.entity.Product;
import bogdrosoft.expirymanager.databinding.ActivityMainBinding;
import bogdrosoft.expirymanager.export.DbExportManager;
import bogdrosoft.expirymanager.export.DbImportManager;
import bogdrosoft.expirymanager.ui.addedit.AddEditActivity;
import bogdrosoft.expirymanager.ui.containers.ManageContainersActivity;
import bogdrosoft.expirymanager.ui.settings.SettingsActivity;
import bogdrosoft.expirymanager.ui.types.ManageTypesActivity;
import bogdrosoft.expirymanager.util.Constants;
import bogdrosoft.expirymanager.util.SharedPrefsHelper;
import bogdrosoft.expirymanager.util.SortOrder;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private DbImportManager importManager;
    private ProductListAdapter adapter;
    private Map<String, Integer> typeLeadTimeOverrides = new HashMap<>();
    private boolean searchActive;

    private final ActivityResultLauncher<String[]> openDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onImportFilePicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        setTitle(R.string.app_name);

        importManager = new DbImportManager(this);

        adapter = new ProductListAdapter(this::onProductClicked);
        binding.recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerProducts.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getProducts().observe(this, products -> {
            adapter.submitList(products);
            boolean empty = products == null || products.isEmpty();
            binding.textEmpty.setText(searchActive ? R.string.empty_search_message : R.string.empty_list_message);
            binding.textEmpty.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
        });
        viewModel.getTypeLeadTimeOverrides().observe(this, overrides -> {
            typeLeadTimeOverrides = overrides;
            adapter.setLeadTimeSettings(typeLeadTimeOverrides, SharedPrefsHelper.getLeadTimeDays(this));
        });

        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The global default lead time lives in plain SharedPreferences (not LiveData-backed),
        // so pick up any change made in Settings since we were last visible.
        adapter.setLeadTimeSettings(typeLeadTimeOverrides, SharedPrefsHelper.getLeadTimeDays(this));
        viewModel.setHideExhausted(SharedPrefsHelper.isHideExhaustedProductsEnabled(this));
    }

    private void onProductClicked(Product product, View anchorView) {
        MaterialCardView card = (MaterialCardView) anchorView;
        setCardHighlighted(card, true);

        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.setForceShowIcon(true);
        popup.getMenuInflater().inflate(R.menu.menu_product_actions, popup.getMenu());
        MenuItem deleteItem = popup.getMenu().findItem(R.id.action_delete);
        SpannableString deleteTitle = new SpannableString(deleteItem.getTitle());
        deleteTitle.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.delete_color)),
                0, deleteTitle.length(), 0);
        deleteItem.setTitle(deleteTitle);
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit) {
                openEdit(product);
                return true;
            } else if (id == R.id.action_duplicate) {
                openDuplicate(product);
                return true;
            } else if (id == R.id.action_mark_opened) {
                viewModel.markAsOpened(product);
                return true;
            } else if (id == R.id.action_set_quantity_zero) {
                viewModel.setQuantityToZero(product);
                return true;
            } else if (id == R.id.action_delete) {
                confirmDeleteProduct(product);
                return true;
            }
            return false;
        });
        // Fires whether the menu closed via an action, an outside tap, or the back button, so
        // the highlight never gets stuck on a row after the menu is gone either way.
        popup.setOnDismissListener(menu -> setCardHighlighted(card, false));
        popup.show();
    }

    private void setCardHighlighted(MaterialCardView card, boolean highlighted) {
        if (highlighted) {
            card.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.product_card_highlight_stroke_width));
            card.setStrokeColor(ContextCompat.getColor(this, R.color.accent));
        } else {
            card.setStrokeWidth(0);
        }
    }

    private void openEdit(Product product) {
        Intent intent = new Intent(this, AddEditActivity.class);
        intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.id);
        startActivity(intent);
    }

    private void openDuplicate(Product product) {
        Intent intent = new Intent(this, AddEditActivity.class);
        intent.putExtra(Constants.EXTRA_DUPLICATE_FROM_ID, product.id);
        startActivity(intent);
    }

    private void confirmDeleteProduct(Product product) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> viewModel.deleteProduct(product))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchActive = newText != null && !newText.isEmpty();
                viewModel.setSearchQuery(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_sort) {
            showSortDialog();
            return true;
        } else if (id == R.id.action_export) {
            new DbExportManager(this).export();
            return true;
        } else if (id == R.id.action_import) {
            openDocumentLauncher.launch(new String[]{"*/*"});
            return true;
        } else if (id == R.id.action_manage_types) {
            startActivity(new Intent(this, ManageTypesActivity.class));
            return true;
        } else if (id == R.id.action_manage_containers) {
            startActivity(new Intent(this, ManageContainersActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortDialog() {
        String[] options = getResources().getStringArray(R.array.sort_order_options);
        SortOrder current = SharedPrefsHelper.getSortOrder(this);
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_sort_dialog)
                .setSingleChoiceItems(options, current.ordinal(), (dialog, which) -> {
                    SortOrder selected = SortOrder.fromOrdinal(which);
                    SharedPrefsHelper.setSortOrder(this, selected);
                    viewModel.setSortOrder(selected);
                    dialog.dismiss();
                })
                .show();
    }

    private void onImportFilePicked(Uri uri) {
        if (uri != null) {
            importManager.importFrom(uri);
        }
    }
}
