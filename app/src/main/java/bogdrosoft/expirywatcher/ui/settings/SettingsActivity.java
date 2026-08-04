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

package bogdrosoft.expirywatcher.ui.settings;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationManagerCompat;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import bogdrosoft.expirywatcher.R;
import bogdrosoft.expirywatcher.databinding.ActivitySettingsBinding;
import bogdrosoft.expirywatcher.reminder.NotificationHelper;
import bogdrosoft.expirywatcher.reminder.ReminderScheduler;
import bogdrosoft.expirywatcher.util.NoFilterArrayAdapter;
import bogdrosoft.expirywatcher.util.SharedPrefsHelper;
import bogdrosoft.expirywatcher.util.UiMode;

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

        String[] uiModeLabels = getResources().getStringArray(R.array.ui_mode_options);
        binding.editUiMode.setAdapter(new NoFilterArrayAdapter<>(this, android.R.layout.simple_list_item_1, uiModeLabels));
        binding.editUiMode.setText(uiModeLabels[SharedPrefsHelper.getUiMode(this).ordinal()], false);
        binding.editUiMode.setOnItemClickListener((parent, view, position, id) -> {
            UiMode selected = UiMode.fromOrdinal(position);
            SharedPrefsHelper.setUiMode(this, selected);
            AppCompatDelegate.setDefaultNightMode(selected.getNightMode());
        });

        binding.editLeadTime.setText(String.valueOf(SharedPrefsHelper.getLeadTimeDays(this)));
        binding.switchScanSound.setChecked(SharedPrefsHelper.isScanSoundEnabled(this));
        binding.switchNotifyExpired.setChecked(SharedPrefsHelper.isNotifyExpiredEnabled(this));
        binding.switchNotifyExpiringSoon.setChecked(SharedPrefsHelper.isNotifyExpiringSoonEnabled(this));
        binding.switchHideExhausted.setChecked(SharedPrefsHelper.isHideExhaustedProductsEnabled(this));
        updateReminderTimeButtonText();

        binding.buttonSaveLeadTime.setOnClickListener(v -> saveLeadTime());
        binding.buttonReminderTime.setOnClickListener(v -> showReminderTimePicker());
        binding.buttonOpenNotificationSettings.setOnClickListener(v -> openNotificationSettings());
        binding.switchScanSound.setOnCheckedChangeListener((buttonView, isChecked) ->
                SharedPrefsHelper.setScanSoundEnabled(this, isChecked));
        binding.switchNotifyExpired.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPrefsHelper.setNotifyExpiredEnabled(this, isChecked);
            if (!isChecked) {
                NotificationHelper.cancelExpiredSummary(this);
            }
        });
        binding.switchNotifyExpiringSoon.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPrefsHelper.setNotifyExpiringSoonEnabled(this, isChecked);
            if (!isChecked) {
                NotificationHelper.cancelExpiringSoonSummary(this);
            }
        });
        binding.switchHideExhausted.setOnCheckedChangeListener((buttonView, isChecked) ->
                SharedPrefsHelper.setHideExhaustedProductsEnabled(this, isChecked));
        binding.rowDeleteData.setOnClickListener(v -> startActivity(new Intent(this, DeleteDataActivity.class)));
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

    private void showReminderTimePicker() {
        int currentHour = SharedPrefsHelper.getReminderHour(this);
        int currentMinute = SharedPrefsHelper.getReminderMinute(this);

        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            SharedPrefsHelper.setReminderTime(this, hourOfDay, minute);
            ReminderScheduler.reschedule(this);
            updateReminderTimeButtonText();
        }, currentHour, currentMinute, DateFormat.is24HourFormat(this)).show();
    }

    private void updateReminderTimeButtonText() {
        LocalTime time = LocalTime.of(SharedPrefsHelper.getReminderHour(this), SharedPrefsHelper.getReminderMinute(this));
        String pattern = DateFormat.is24HourFormat(this) ? "HH:mm" : "h:mm a";
        binding.buttonReminderTime.setText(time.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault())));
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
