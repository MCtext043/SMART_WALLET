package com.example.smartwallet.utils;

import android.os.Build;

/**
 * Грубая эвристика: эмулятор vs физическое устройство (для подсказок про api.base.url).
 */
public final class DeviceUtils {

    private DeviceUtils() {}

    public static boolean isProbablyEmulator() {
        String fp = Build.FINGERPRINT;
        if (fp != null) {
            if (fp.startsWith("generic") || fp.startsWith("unknown")) {
                return true;
            }
        }
        String model = Build.MODEL;
        if (model != null) {
            String m = model.toLowerCase();
            if (m.contains("google_sdk") || m.contains("emulator")
                    || m.contains("android sdk built for x86")) {
                return true;
            }
        }
        String man = Build.MANUFACTURER;
        if (man != null && man.toLowerCase().contains("genymotion")) {
            return true;
        }
        if (Build.BRAND != null && Build.DEVICE != null
                && Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) {
            return true;
        }
        return "google_sdk".equals(Build.PRODUCT);
    }
}
