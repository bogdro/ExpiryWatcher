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

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import bogdrosoft.expirywatcher.R;
import bogdrosoft.expirywatcher.data.entity.ProductType;
import bogdrosoft.expirywatcher.databinding.ActivityManageTypesBinding;
import bogdrosoft.expirywatcher.databinding.DialogTypeEditorBinding;
import bogdrosoft.expirywatcher.util.SharedPrefsHelper;

public class ManageTypesActivity extends AppCompatActivity {

    private ActivityManageTypesBinding binding;
    private ManageTypesViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageTypesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        setTitle(R.string.title_manage_types);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        ProductTypeAdapter adapter = new ProductTypeAdapter(new ProductTypeAdapter.Listener() {
            @Override
            public void onEditType(ProductType type) {
                showEditorDialog(type);
            }

            @Override
            public void onDeleteType(ProductType type) {
                confirmDelete(type);
            }
        });
        binding.recyclerTypes.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTypes.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ManageTypesViewModel.class);
        viewModel.getTypes().observe(this, types -> {
            adapter.submitList(types);
            boolean empty = types == null || types.isEmpty();
            binding.textEmpty.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
        });

        binding.fabAddType.setOnClickListener(v -> showEditorDialog(null));
    }

    private void confirmDelete(ProductType type) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_type_title)
                .setMessage(R.string.dialog_delete_type_message)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> viewModel.deleteType(type))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showEditorDialog(@Nullable ProductType existing) {
        boolean isEdit = existing != null;
        DialogTypeEditorBinding dialogBinding = DialogTypeEditorBinding.inflate(getLayoutInflater());

        dialogBinding.textTypeLeadTimeHelper.setText(
                getString(R.string.type_lead_time_helper, SharedPrefsHelper.getLeadTimeDays(this)));

        if (isEdit) {
            dialogBinding.editTypeName.setText(existing.name);
            if (existing.leadTimeDays != null) {
                dialogBinding.editTypeLeadTime.setText(String.valueOf(existing.leadTimeDays));
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEdit ? R.string.title_edit_type : R.string.title_add_type)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v ->
                onEditorSaveClicked(dialog, dialogBinding, existing)));
        dialog.show();
    }

    private void onEditorSaveClicked(AlertDialog dialog, DialogTypeEditorBinding dialogBinding,
            @Nullable ProductType existing) {
        boolean isEdit = existing != null;

        String name = text(dialogBinding.editTypeName);
        if (name.isEmpty()) {
            dialogBinding.layoutTypeName.setError(getString(R.string.error_type_name_required));
            return;
        }
        dialogBinding.layoutTypeName.setError(null);

        Integer leadTimeDays;
        String leadTimeText = text(dialogBinding.editTypeLeadTime);
        if (leadTimeText.isEmpty()) {
            leadTimeDays = null;
        } else {
            try {
                leadTimeDays = Integer.parseInt(leadTimeText);
                if (leadTimeDays < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                dialogBinding.layoutTypeLeadTime.setError(getString(R.string.error_lead_time_invalid));
                return;
            }
        }
        dialogBinding.layoutTypeLeadTime.setError(null);

        if (isEdit) {
            if (name.equals(existing.name)) {
                // A fresh instance, not a mutation of `existing`: that reference is the exact
                // object the adapter's currently-displayed list is holding, and mutating it in
                // place would corrupt the "old" snapshot DiffUtil later compares the refreshed
                // Room query result against, making it see no change and skip re-binding the row.
                ProductType updated = new ProductType();
                updated.name = existing.name;
                updated.leadTimeDays = leadTimeDays;
                viewModel.updateType(updated);
                dialog.dismiss();
                return;
            }

            viewModel.renameType(existing.name, name, leadTimeDays, success -> {
                if (success) {
                    dialog.dismiss();
                } else {
                    dialogBinding.layoutTypeName.setError(getString(R.string.error_type_name_exists));
                }
            });
            return;
        }

        ProductType newType = new ProductType();
        newType.name = name;
        newType.leadTimeDays = leadTimeDays;
        viewModel.addType(newType, success -> {
            if (success) {
                dialog.dismiss();
            } else {
                dialogBinding.layoutTypeName.setError(getString(R.string.error_type_name_exists));
            }
        });
    }

    private static String text(TextView view) {
        return view.getText() == null ? "" : view.getText().toString().trim();
    }
}
