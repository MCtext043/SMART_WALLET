package com.example.smartwallet;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smartwallet.utils.Logger;
import com.example.smartwallet.utils.PermissionHelper;

/**
 * Зелёный экран {@link R.layout#activity_splash}, затем разрешения и сразу {@link com.example.smartwallet.ui.AuthActivity}.
 */
public class MainActivity extends AppCompatActivity implements PermissionHelper.PermissionCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        if (PermissionHelper.hasAllRequiredPermissions(this)) {
            launchAuth();
        } else {
            Logger.i("MainActivity", "Requesting required permissions");
            PermissionHelper.requestRequiredPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.handlePermissionResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onAllPermissionsGranted() {
        Logger.i("MainActivity", "All permissions granted");
        launchAuth();
    }

    @Override
    public void onPermissionsDenied(String[] deniedPermissions) {
        Logger.w("MainActivity", "Some permissions denied, opening auth anyway");
        launchAuth();
    }

    private void launchAuth() {
        startActivity(new Intent(this, com.example.smartwallet.ui.AuthActivity.class));
        finish();
    }
}
