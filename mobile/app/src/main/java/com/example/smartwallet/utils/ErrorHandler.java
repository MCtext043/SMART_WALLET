package com.example.smartwallet.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Locale;

import retrofit2.HttpException;
import retrofit2.Response;

public class ErrorHandler {

    /** В URL сборки и в сообщениях OkHttp при недоступном «хосте эмулятора». */
    private static final String EMULATOR_LOOPBACK_HOST = "10.0.2.2";

    private static boolean failureMentionsEmulatorLoopback(@Nullable Throwable throwable) {
        if (throwable == null) return false;
        String m = throwable.getMessage();
        return m != null && m.contains(EMULATOR_LOOPBACK_HOST);
    }

    @NonNull
    private static String wrongHost10o2OnRealDeviceMessage() {
        return "Сборка с адресом " + EMULATOR_LOOPBACK_HOST + " — только для эмулятора Android. "
                + "На телефоне в local.properties укажите LAN-IP вашего ПК, например "
                + "api.base.url=http://192.168.0.5:8001/ (тот же Wi‑Fi), пересоберите APK.";
    }

    public static String getErrorMessage(Throwable throwable) {
        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            int code = httpException.code();
            
            switch (code) {
                case 400:
                    return "Неверный запрос. Проверьте введенные данные.";
                case 401:
                    return "Необходимо войти в систему.";
                case 403:
                    return "Доступ запрещен.";
                case 404:
                    return "Ресурс не найден.";
                case 422:
                    return "Ошибка валидации данных.";
                case 500:
                    return "Внутренняя ошибка сервера.";
                case 502:
                    return "Сервер временно недоступен.";
                case 503:
                    return "Сервис временно недоступен.";
                default:
                    return "Ошибка сервера: " + code;
            }
        } else if (throwable instanceof ConnectException) {
            if (failureMentionsEmulatorLoopback(throwable)) {
                return wrongHost10o2OnRealDeviceMessage();
            }
            return "Нет связи с сервером. На телефоне в local.properties задайте api.base.url=http://IP_ВАШЕГО_ПК:ПОРТ/ "
                    + "(тот же Wi‑Fi; не 10.0.2.2 — это только эмулятор). Сервер запускайте на 0.0.0.0, откройте порт в брандмауэре.";
        } else if (throwable instanceof SocketTimeoutException) {
            if (failureMentionsEmulatorLoopback(throwable)) {
                return wrongHost10o2OnRealDeviceMessage();
            }
            return "Превышено время ожидания. Проверьте, что сервер запущен (0.0.0.0), порт открыт в брандмауэре и в сборке верный IP.";
        } else if (throwable instanceof UnknownHostException) {
            return "Неизвестный хост в адресе API. Проверьте api.base.url в local.properties и что телефон в одной сети с ПК.";
        } else if (throwable instanceof IOException) {
            if (failureMentionsEmulatorLoopback(throwable)) {
                return wrongHost10o2OnRealDeviceMessage();
            }
            String m = throwable.getMessage();
            if (m != null) {
                String low = m.toLowerCase(Locale.ROOT);
                if (low.contains("cleartext") || m.contains("CLEARTEXT")) {
                    return "Сеть запретила HTTP. Обновите приложение (network_security_config) или используйте HTTPS.";
                }
            }
            return "Ошибка сети. Проверьте Wi‑Fi и адрес API (IP компьютера, не localhost с телефона).";
        } else if (throwable instanceof SecurityException) {
            return "Ошибка доступа. Проверьте разрешения приложения.";
        } else {
            String message = throwable.getMessage();
            if (message != null && message.contains("ACCESS_NETWORK_STATE")) {
                return "Ошибка доступа к состоянию сети. Приложение будет работать в ограниченном режиме.";
            }
            return "Неизвестная ошибка: " + message;
        }
    }
    
    public static void showError(Context context, Throwable throwable) {
        String message = getErrorMessage(throwable);
        Logger.e("ErrorHandler", "Showing error: " + message, throwable);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    
    public static void showError(Context context, String message) {
        Logger.e("ErrorHandler", "Showing error: " + message);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    
    public static boolean isNetworkError(Throwable throwable) {
        return throwable instanceof ConnectException ||
               throwable instanceof SocketTimeoutException ||
               throwable instanceof UnknownHostException ||
               throwable instanceof IOException;
    }
    
    public static boolean isAuthError(Throwable throwable) {
        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            int code = httpException.code();
            return code == 401 || code == 403;
        }
        return false;
    }

    @Nullable
    public static String readErrorBody(@NonNull Response<?> response) {
        if (response.errorBody() == null) return null;
        try {
            return response.errorBody().string();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Сообщение для пользователя при неуспешном HTTP (Retrofit onResponse).
     */
    @NonNull
    public static String httpErrorMessage(@NonNull Response<?> response) {
        int code = response.code();
        String body = readErrorBody(response);
        if (body != null) {
            String t = body.trim();
            if (!t.isEmpty()) {
                if (t.length() > 180) {
                    t = t.substring(0, 177) + "…";
                }
                if (code == 500 && "Internal Server Error".equalsIgnoreCase(t)) {
                    return "Ошибка сервера (500). Откройте консоль, где запущен uvicorn — там будет traceback.";
                }
                return "Ошибка " + code + ": " + t;
            }
        }
        switch (code) {
            case 400:
                return "Неверный запрос.";
            case 401:
                return "Необходимо войти в систему.";
            case 403:
                return "Доступ запрещён.";
            case 404:
                return "Ресурс не найден.";
            case 409:
                return "Такой пользователь уже есть.";
            case 422:
                return "Ошибка валидации данных.";
            case 500:
                return "Ошибка сервера (500). Смотрите логи uvicorn на ПК.";
            default:
                return "Ошибка сервера: " + code;
        }
    }
}
