package com.example.smartwallet.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.smartwallet.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Сканирование QR: превью камеры, рамка, «Сфотографировать», при успехе — {@link PayConfirmActivity}.
 */
public final class PayQrScanActivity extends AppCompatActivity {

    private static final long ANALYZE_INTERVAL_MS = 280L;

    private PreviewView previewView;
    private MaterialButton buttonCapture;
    private final AtomicBoolean decoded = new AtomicBoolean(false);
    private final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    private BarcodeScanner barcodeScanner;
    @Nullable
    private ProcessCameraProvider cameraProvider;
    @Nullable
    private ImageCapture imageCapture;
    private long lastAnalyzeMs;

    private final ActivityResultLauncher<String> requestCameraPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            granted -> {
                if (granted) {
                    startCamera();
                } else {
                    Toast.makeText(this, R.string.pay_qr_camera_denied, Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_qr_scan);

        MaterialToolbar toolbar = findViewById(R.id.toolbarQrScan);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        previewView = findViewById(R.id.previewView);
        buttonCapture = findViewById(R.id.buttonCaptureQr);
        buttonCapture.setOnClickListener(v -> captureStillAndScan());

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (barcodeScanner != null) {
            barcodeScanner.close();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, R.string.pay_qr_camera_error, Toast.LENGTH_SHORT).show();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        ProcessCameraProvider provider = cameraProvider;
        if (provider == null) {
            return;
        }

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        analysis.setAnalyzer(cameraExecutor, this::analyzeFrame);

        try {
            provider.unbindAll();
            provider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                    analysis
            );
        } catch (Exception e) {
            Toast.makeText(this, R.string.pay_qr_camera_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void analyzeFrame(@NonNull ImageProxy imageProxy) {
        if (decoded.get()) {
            imageProxy.close();
            return;
        }
        long now = SystemClock.uptimeMillis();
        if (now - lastAnalyzeMs < ANALYZE_INTERVAL_MS) {
            imageProxy.close();
            return;
        }
        lastAnalyzeMs = now;

        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
        barcodeScanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (decoded.get()) {
                        return;
                    }
                    for (Barcode b : barcodes) {
                        String raw = b.getRawValue();
                        if (raw != null && !raw.isEmpty()) {
                            decoded.set(true);
                            runOnUiThread(() -> onQrDecoded(raw));
                            return;
                        }
                    }
                })
                .addOnCompleteListener(task -> imageProxy.close());
    }

    private void captureStillAndScan() {
        if (decoded.get()) {
            return;
        }
        ImageCapture capture = imageCapture;
        if (capture == null) {
            Toast.makeText(this, R.string.pay_qr_camera_error, Toast.LENGTH_SHORT).show();
            return;
        }
        capture.takePicture(cameraExecutor, new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                processStillImage(image);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(() -> Toast.makeText(PayQrScanActivity.this,
                        R.string.pay_qr_capture_failed, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void processStillImage(@NonNull ImageProxy image) {
        android.media.Image mediaImage = image.getImage();
        if (mediaImage == null) {
            image.close();
            runOnUiThread(() -> Toast.makeText(this, R.string.pay_qr_not_found, Toast.LENGTH_SHORT).show());
            return;
        }
        InputImage input = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
        barcodeScanner.process(input)
                .addOnSuccessListener(barcodes -> {
                    image.close();
                    if (decoded.get()) {
                        return;
                    }
                    String found = firstQrRaw(barcodes);
                    if (found != null) {
                        decoded.set(true);
                        runOnUiThread(() -> onQrDecoded(found));
                    } else {
                        runOnUiThread(() -> Toast.makeText(PayQrScanActivity.this,
                                R.string.pay_qr_not_found, Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> {
                    image.close();
                    runOnUiThread(() -> Toast.makeText(PayQrScanActivity.this,
                            R.string.pay_qr_capture_failed, Toast.LENGTH_SHORT).show());
                });
    }

    @Nullable
    private static String firstQrRaw(@NonNull List<Barcode> barcodes) {
        for (Barcode b : barcodes) {
            String raw = b.getRawValue();
            if (raw != null && !raw.isEmpty()) {
                return raw;
            }
        }
        return null;
    }

    private void onQrDecoded(@NonNull String raw) {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        Intent intent = new Intent(this, PayConfirmActivity.class);
        intent.putExtra(PayConfirmActivity.EXTRA_QR_RAW, raw);
        startActivity(intent);
        finish();
    }
}
