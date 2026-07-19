package bogdrosoft.expirymanager.scan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import bogdrosoft.expirymanager.R;
import bogdrosoft.expirymanager.util.SharedPrefsHelper;

/**
 * Wraps zxing-android-embedded's scan contract plus the CAMERA runtime permission flow.
 * Must be constructed before the host activity reaches STARTED (e.g. as an instance field),
 * since {@link ComponentActivity#registerForActivityResult} requires that.
 */
public class BarcodeScanHelper {

    public interface Listener {
        void onBarcodeScanned(String barcode);
    }

    private final ComponentActivity activity;
    private final Listener listener;
    private final ActivityResultLauncher<ScanOptions> scanLauncher;
    private final ActivityResultLauncher<String> permissionLauncher;

    public BarcodeScanHelper(ComponentActivity activity, Listener listener) {
        this.activity = activity;
        this.listener = listener;

        scanLauncher = activity.registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                listener.onBarcodeScanned(result.getContents());
            }
        });

        permissionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> {
                    if (granted) {
                        launchScan();
                    } else {
                        Toast.makeText(activity, R.string.camera_permission_required, Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void startScan() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchScan();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchScan() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
        options.setBeepEnabled(SharedPrefsHelper.isScanSoundEnabled(activity));
        options.setOrientationLocked(false);
        scanLauncher.launch(options);
    }
}
