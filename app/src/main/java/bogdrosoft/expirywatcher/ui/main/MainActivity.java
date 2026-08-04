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

package bogdrosoft.expirywatcher.ui.main;

import android.content.DialogInterface;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bogdrosoft.expirywatcher.R;
import bogdrosoft.expirywatcher.data.entity.Product;
import bogdrosoft.expirywatcher.databinding.ActivityMainBinding;
import bogdrosoft.expirywatcher.export.DbExportManager;
import bogdrosoft.expirywatcher.export.DbImportManager;
import bogdrosoft.expirywatcher.ui.about.AboutActivity;
import bogdrosoft.expirywatcher.ui.addedit.AddEditActivity;
import bogdrosoft.expirywatcher.ui.containers.ManageContainersActivity;
import bogdrosoft.expirywatcher.ui.settings.SettingsActivity;
import bogdrosoft.expirywatcher.ui.types.ManageTypesActivity;
import bogdrosoft.expirywatcher.util.Constants;
import bogdrosoft.expirywatcher.util.ProductStatusFilter;
import bogdrosoft.expirywatcher.util.SharedPrefsHelper;
import bogdrosoft.expirywatcher.util.SortOrder;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private DbImportManager importManager;
    private ProductListAdapter adapter;
    private Map<String, Integer> typeLeadTimeOverrides = new HashMap<>();
    private boolean searchActive;

    // Kept in sync with the DB via LiveData observers so the "Filter" dialog can build its
    // container/type pickers instantly, without a fresh query each time it's opened.
    private List<String> allContainerNames = new ArrayList<>();
    private List<String> allTypeNames = new ArrayList<>();
    // The filter dialogs are the only source of truth for these (not persisted across restarts,
    // same as the search text), so plain fields are enough to track the current selection.
    @Nullable
    private String currentContainerFilter;
    @Nullable
    private String currentTypeFilter;
    @Nullable
    private ProductStatusFilter currentStatusFilter;

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
            int count = products == null ? 0 : products.size();
            boolean empty = count == 0;
            int emptyMessageRes;
            if (searchActive) {
                emptyMessageRes = R.string.empty_search_message;
            } else if (isAnyFilterActive()) {
                emptyMessageRes = R.string.empty_filtered_message;
            } else {
                emptyMessageRes = R.string.empty_list_message;
            }
            binding.textEmpty.setText(emptyMessageRes);
            binding.textEmpty.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.textItemCount.setText(getResources().getQuantityString(R.plurals.item_count, count, count));
            binding.textItemCount.setVisibility(empty ? android.view.View.GONE : android.view.View.VISIBLE);
        });
        viewModel.getTypeLeadTimeOverrides().observe(this, overrides -> {
            typeLeadTimeOverrides = overrides;
            adapter.setLeadTimeSettings(typeLeadTimeOverrides, SharedPrefsHelper.getLeadTimeDays(this));
        });
        viewModel.getAllContainerNames().observe(this, names -> allContainerNames = names);
        viewModel.getAllTypeNames().observe(this, names -> allTypeNames = names);

        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditActivity.class);
            startActivity(intent);
        });

        // Only on a genuinely fresh launch (e.g. tapping an expiry notification), not when this
        // same instance is merely being recreated for a configuration change - otherwise a
        // rotation would keep re-applying the original notification's filter, silently
        // overriding anything the user picked from the Filter dialog since then.
        if (savedInstanceState == null) {
            applyStatusFilterFromIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        applyStatusFilterFromIntent(intent);
    }

    /**
     * Jumps straight to the relevant status filter when launched from an expiry notification,
     * clearing any other active filter first so the notification's promise ("N items expired")
     * isn't silently narrowed by a leftover container/type filter from an earlier session.
     */
    private void applyStatusFilterFromIntent(@Nullable Intent intent) {
        if (intent == null || !intent.hasExtra(Constants.EXTRA_STATUS_FILTER)) {
            return;
        }
        ProductStatusFilter status = ProductStatusFilter.valueOf(intent.getStringExtra(Constants.EXTRA_STATUS_FILTER));
        currentContainerFilter = null;
        currentTypeFilter = null;
        currentStatusFilter = status;
        viewModel.setContainerFilter(null);
        viewModel.setTypeFilter(null);
        viewModel.setStatusFilter(status);
        invalidateOptionsMenu();
    }

    private boolean isAnyFilterActive() {
        return currentContainerFilter != null || currentTypeFilter != null || currentStatusFilter != null;
    }

    private void onProductClicked(Product product, View anchorView) {
        MaterialCardView card = (MaterialCardView) anchorView;
        setCardHighlighted(card, true);

        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.setForceShowIcon(true);
        popup.getMenuInflater().inflate(R.menu.menu_product_actions, popup.getMenu());
        popup.getMenu().findItem(R.id.action_mark_opened).setVisible(product.openDate == null && product.quantity != 0);
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
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.action_delete, (d, which) -> viewModel.deleteProduct(product))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.delete_color));
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_filter)
                .setIcon(isAnyFilterActive() ? R.drawable.ic_filter_active : R.drawable.ic_filter);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter) {
            showFilterDialog();
            return true;
        } else if (id == R.id.action_sort) {
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
        } else if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
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

    /**
     * Top-level "Filter" entry point: a row per dimension showing its current value (or "Any"),
     * each opening its own single-choice value picker.
     */
    private void showFilterDialog() {
        String[] rows = {
                getString(R.string.filter_row_format, getString(R.string.filter_dimension_container),
                        currentContainerFilter != null ? currentContainerFilter : getString(R.string.filter_value_any)),
                getString(R.string.filter_row_format, getString(R.string.filter_dimension_type),
                        currentTypeFilter != null ? currentTypeFilter : getString(R.string.filter_value_any)),
                getString(R.string.filter_row_format, getString(R.string.filter_dimension_status),
                        currentStatusFilter != null ? getStatusFilterLabel(currentStatusFilter) : getString(R.string.filter_value_any)),
        };
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_filter_dialog)
                .setItems(rows, (dialog, which) -> {
                    if (which == 0) {
                        showContainerFilterDialog();
                    } else if (which == 1) {
                        showTypeFilterDialog();
                    } else {
                        showStatusFilterDialog();
                    }
                })
                .setNeutralButton(R.string.action_clear_filter, (dialog, which) -> clearFilters())
                .show();
    }

    /**
     * Resets container/type/status back to "Any" and applies immediately; the search text and
     * sort order are untouched, since "Clear filter" is only about the Filter dialog's own
     * dimensions.
     */
    private void clearFilters() {
        currentContainerFilter = null;
        currentTypeFilter = null;
        currentStatusFilter = null;
        viewModel.setContainerFilter(null);
        viewModel.setTypeFilter(null);
        viewModel.setStatusFilter(null);
        invalidateOptionsMenu();
    }

    private void showContainerFilterDialog() {
        List<String> options = new ArrayList<>();
        options.add(getString(R.string.filter_value_any));
        options.addAll(allContainerNames);
        int selected = currentContainerFilter == null ? 0 : Math.max(options.indexOf(currentContainerFilter), 0);
        new AlertDialog.Builder(this)
                .setTitle(R.string.filter_dimension_container)
                .setSingleChoiceItems(options.toArray(new String[0]), selected, (dialog, which) -> {
                    currentContainerFilter = which == 0 ? null : options.get(which);
                    viewModel.setContainerFilter(currentContainerFilter);
                    invalidateOptionsMenu();
                    dialog.dismiss();
                })
                .show();
    }

    private void showTypeFilterDialog() {
        List<String> options = new ArrayList<>();
        options.add(getString(R.string.filter_value_any));
        options.addAll(allTypeNames);
        int selected = currentTypeFilter == null ? 0 : Math.max(options.indexOf(currentTypeFilter), 0);
        new AlertDialog.Builder(this)
                .setTitle(R.string.filter_dimension_type)
                .setSingleChoiceItems(options.toArray(new String[0]), selected, (dialog, which) -> {
                    currentTypeFilter = which == 0 ? null : options.get(which);
                    viewModel.setTypeFilter(currentTypeFilter);
                    invalidateOptionsMenu();
                    dialog.dismiss();
                })
                .show();
    }

    private void showStatusFilterDialog() {
        String[] options = {
                getString(R.string.filter_value_any),
                getString(R.string.status_filter_expired),
                getString(R.string.status_filter_expiring_soon),
                getString(R.string.status_filter_not_expiring),
                getString(R.string.status_filter_exhausted),
        };
        int selected = currentStatusFilter == null ? 0 : currentStatusFilter.ordinal() + 1;
        new AlertDialog.Builder(this)
                .setTitle(R.string.filter_dimension_status)
                .setSingleChoiceItems(options, selected, (dialog, which) -> {
                    currentStatusFilter = which == 0 ? null : ProductStatusFilter.values()[which - 1];
                    viewModel.setStatusFilter(currentStatusFilter);
                    invalidateOptionsMenu();
                    dialog.dismiss();
                })
                .show();
    }

    private String getStatusFilterLabel(ProductStatusFilter status) {
        switch (status) {
            case EXPIRED:
                return getString(R.string.status_filter_expired);
            case EXPIRING_SOON:
                return getString(R.string.status_filter_expiring_soon);
            case NOT_EXPIRING:
                return getString(R.string.status_filter_not_expiring);
            case EXHAUSTED:
                return getString(R.string.status_filter_exhausted);
            default:
                throw new IllegalArgumentException("Unknown status filter: " + status);
        }
    }

    private void onImportFilePicked(Uri uri) {
        if (uri != null) {
            importManager.importFrom(uri);
        }
    }
}
