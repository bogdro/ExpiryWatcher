package bogdrosoft.expirymanager.ui.containers;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import bogdrosoft.expirymanager.R;
import bogdrosoft.expirymanager.data.entity.Container;
import bogdrosoft.expirymanager.databinding.ActivityManageContainersBinding;
import bogdrosoft.expirymanager.databinding.DialogContainerEditorBinding;

public class ManageContainersActivity extends AppCompatActivity {

    private ActivityManageContainersBinding binding;
    private ManageContainersViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityManageContainersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        setTitle(R.string.title_manage_containers);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        ContainerAdapter adapter = new ContainerAdapter(this::confirmDelete);
        binding.recyclerContainers.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerContainers.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ManageContainersViewModel.class);
        viewModel.getContainers().observe(this, containers -> {
            adapter.submitList(containers);
            boolean empty = containers == null || containers.isEmpty();
            binding.textEmpty.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
        });

        binding.fabAddContainer.setOnClickListener(v -> showAddDialog());
    }

    private void confirmDelete(Container container) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_container_title)
                .setMessage(R.string.dialog_delete_container_message)
                .setPositiveButton(R.string.action_delete, (dialog, which) -> viewModel.deleteContainer(container))
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showAddDialog() {
        DialogContainerEditorBinding dialogBinding = DialogContainerEditorBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.title_add_container)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v ->
                onAddSaveClicked(dialog, dialogBinding)));
        dialog.show();
    }

    private void onAddSaveClicked(AlertDialog dialog, DialogContainerEditorBinding dialogBinding) {
        String name = text(dialogBinding.editContainerName);
        if (name.isEmpty()) {
            dialogBinding.layoutContainerName.setError(getString(R.string.error_container_name_required));
            return;
        }
        dialogBinding.layoutContainerName.setError(null);

        Container container = new Container();
        container.name = name;
        viewModel.addContainer(container, success -> {
            if (success) {
                dialog.dismiss();
            } else {
                dialogBinding.layoutContainerName.setError(getString(R.string.error_container_name_exists));
            }
        });
    }

    private static String text(TextView view) {
        return view.getText() == null ? "" : view.getText().toString().trim();
    }
}
