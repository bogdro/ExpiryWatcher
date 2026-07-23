package bogdrosoft.expirymanager.scan;

import androidx.annotation.NonNull;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import bogdrosoft.expirymanager.R;

/**
 * The library's default {@link CaptureActivity} leaves the camera in single-shot autofocus mode,
 * which only re-focuses every couple of seconds and is what makes scanning feel like it needs
 * "many attempts". Continuous mode keeps the camera constantly re-focusing in the background
 * instead, and the barcode scene mode (where the device supports it) additionally tunes focus/
 * exposure specifically for close-up, high-contrast subjects like barcodes.
 */
public class BarcodeCaptureActivity extends CaptureActivity {

    @NonNull
    @Override
    protected DecoratedBarcodeView initializeContent() {
        DecoratedBarcodeView view = super.initializeContent();

        CameraSettings settings = new CameraSettings();
        settings.setFocusMode(CameraSettings.FocusMode.CONTINUOUS);
        settings.setBarcodeSceneModeEnabled(true);
        settings.setMeteringEnabled(true);
        view.setCameraSettings(settings);

        view.setStatusText(getString(R.string.scan_status_hint));
        // Continuous autofocus can still get stuck on some devices - e.g. locked onto whatever
        // was in frame when the camera opened - and never re-converge on its own, which is what
        // previously made restarting the whole scan screen necessary. A tap closes and reopens
        // just the camera preview (not the activity), which resets its focus state in place.
        view.setOnClickListener(v -> {
            view.pauseAndWait();
            view.resume();
        });

        return view;
    }
}
