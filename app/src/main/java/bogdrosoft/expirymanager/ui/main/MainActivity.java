package bogdrosoft.expirymanager.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import bogdrosoft.expirymanager.R;
import bogdrosoft.expirymanager.data.entity.Product;
import bogdrosoft.expirymanager.databinding.ActivityMainBinding;
import bogdrosoft.expirymanager.export.DbExportManager;
import bogdrosoft.expirymanager.export.DbImportManager;
import bogdrosoft.expirymanager.ui.addedit.AddEditActivity;
import bogdrosoft.expirymanager.ui.settings.SettingsActivity;
import bogdrosoft.expirymanager.util.Constants;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private DbImportManager importManager;

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

        ProductListAdapter adapter = new ProductListAdapter(this::onProductClicked);
        binding.recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerProducts.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getProducts().observe(this, products -> {
            adapter.submitList(products);
            boolean empty = products == null || products.isEmpty();
            binding.textEmpty.setVisibility(empty ? android.view.View.VISIBLE : android.view.View.GONE);
        });

        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditActivity.class);
            startActivity(intent);
        });
    }

    private void onProductClicked(Product product) {
        Intent intent = new Intent(this, AddEditActivity.class);
        intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.id);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_export) {
            new DbExportManager(this).export();
            return true;
        } else if (id == R.id.action_import) {
            openDocumentLauncher.launch(new String[]{"*/*"});
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onImportFilePicked(Uri uri) {
        if (uri != null) {
            importManager.importFrom(uri);
        }
    }
}
