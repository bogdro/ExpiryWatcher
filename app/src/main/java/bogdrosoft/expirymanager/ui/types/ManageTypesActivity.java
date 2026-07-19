package bogdrosoft.expirymanager.ui.types;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import bogdrosoft.expirymanager.R;
import bogdrosoft.expirymanager.data.entity.ProductType;
import bogdrosoft.expirymanager.databinding.ActivityManageTypesBinding;
import bogdrosoft.expirymanager.databinding.DialogTypeEditorBinding;
import bogdrosoft.expirymanager.util.SharedPrefsHelper;

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
            dialogBinding.editTypeName.setEnabled(false);
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
            existing.leadTimeDays = leadTimeDays;
            viewModel.updateType(existing);
            dialog.dismiss();
            return;
        }

        String name = text(dialogBinding.editTypeName);
        if (name.isEmpty()) {
            dialogBinding.layoutTypeName.setError(getString(R.string.error_type_name_required));
            return;
        }
        dialogBinding.layoutTypeName.setError(null);

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
