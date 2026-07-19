package bogdrosoft.expirymanager.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import bogdrosoft.expirymanager.R;
import bogdrosoft.expirymanager.databinding.ActivitySettingsBinding;
import bogdrosoft.expirymanager.util.SharedPrefsHelper;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        setTitle(R.string.title_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.editLeadTime.setText(String.valueOf(SharedPrefsHelper.getLeadTimeDays(this)));
        binding.switchScanSound.setChecked(SharedPrefsHelper.isScanSoundEnabled(this));

        binding.buttonSaveLeadTime.setOnClickListener(v -> saveLeadTime());
        binding.buttonOpenNotificationSettings.setOnClickListener(v -> openNotificationSettings());
        binding.switchScanSound.setOnCheckedChangeListener((buttonView, isChecked) ->
                SharedPrefsHelper.setScanSoundEnabled(this, isChecked));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNotificationStatusUi();
    }

    private void saveLeadTime() {
        String text = binding.editLeadTime.getText() == null ? "" : binding.editLeadTime.getText().toString().trim();
        int days;
        try {
            days = Integer.parseInt(text);
            if (days < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            binding.layoutLeadTime.setError(getString(R.string.error_quantity_invalid));
            return;
        }
        binding.layoutLeadTime.setError(null);
        SharedPrefsHelper.setLeadTimeDays(this, days);
        Toast.makeText(this, R.string.action_save, Toast.LENGTH_SHORT).show();
    }

    private void openNotificationSettings() {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(intent);
    }

    private void updateNotificationStatusUi() {
        boolean enabled = NotificationManagerCompat.from(this).areNotificationsEnabled();
        binding.textNotificationsSummary.setVisibility(enabled ? android.view.View.GONE : android.view.View.VISIBLE);
        binding.buttonOpenNotificationSettings.setVisibility(enabled ? android.view.View.GONE : android.view.View.VISIBLE);
    }
}
