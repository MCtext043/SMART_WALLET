package com.example.smartwallet.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Разбор строки QR (в т.ч. ST00012): сумма и грубая категория для запроса кэшбэка.
 */
public final class PayQrPayloadHelper {

    private static final Pattern SUM_KOPEKS = Pattern.compile("Sum=(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SUM_RUB = Pattern.compile("SumRub=([\\d.,]+)", Pattern.CASE_INSENSITIVE);
    /** Текст вроде «стоимость: 150» / «в отправке написана стоимость: 1 234,50». */
    private static final Pattern SUM_STOIMOST_LABEL = Pattern.compile(
            "(?i)стоимость\\s*[:\\-]?\\s*([\\d\\s\\u00A0.,]+)");
    private static final Pattern NAME_FIELD = Pattern.compile("(?i)(?:^|\\|)Name=([^|\\n]+)");
    private static final Pattern PAYEE_NAME_FIELD = Pattern.compile("(?i)(?:^|\\|)PayeeName=([^|\\n]+)");

    private PayQrPayloadHelper() {}

    /** Сумма в рублях или null, если не распознали. */
    @Nullable
    public static Double parseAmountRub(@Nullable String raw) {
        if (raw == null || raw.isEmpty()) return null;
        Matcher m = SUM_RUB.matcher(raw);
        if (m.find()) {
            try {
                String s = m.group(1).replace(',', '.');
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        Matcher mk = SUM_KOPEKS.matcher(raw);
        if (mk.find()) {
            try {
                long k = Long.parseLong(mk.group(1));
                return k / 100.0;
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        Matcher ms = SUM_STOIMOST_LABEL.matcher(raw);
        if (ms.find()) {
            String num = ms.group(1).replace("\u00A0", "").replace(" ", "").replace(',', '.');
            if (!num.isEmpty()) {
                try {
                    return Double.parseDouble(num);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    /** Категория для API best-card (упрощённо по ключевым словам в payload). */
    @NonNull
    public static String detectCategory(@Nullable String raw) {
        if (raw == null) return "прочее";
        String low = raw.toLowerCase(Locale.forLanguageTag("ru"));
        if (low.contains("еда") || low.contains("рестор") || low.contains("кафе") || low.contains("food")) {
            return "еда";
        }
        if (low.contains("транспорт") || low.contains("метро") || low.contains("автобус") || low.contains("такси")) {
            return "транспорт";
        }
        if (low.contains("аптек") || low.contains("здоров") || low.contains("мед")) {
            return "здоровье";
        }
        return "прочее";
    }

    /** Человекочитаемое название категории для UI. */
    @NonNull
    public static String categoryDisplayTitle(@NonNull String categoryKey) {
        switch (categoryKey.toLowerCase(Locale.forLanguageTag("ru"))) {
            case "еда":
                return "Еда";
            case "транспорт":
                return "Транспорт";
            case "здоровье":
                return "Здоровье";
            case "прочее":
            default:
                return "Прочее";
        }
    }

    /**
     * Название получателя из QR (формат НСПК и аналоги), если есть.
     */
    @Nullable
    public static String parseMerchantName(@Nullable String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        Matcher m = NAME_FIELD.matcher(raw);
        if (m.find()) {
            String s = m.group(1).trim();
            return s.isEmpty() ? null : s;
        }
        m = PAYEE_NAME_FIELD.matcher(raw);
        if (m.find()) {
            String s = m.group(1).trim();
            return s.isEmpty() ? null : s;
        }
        return null;
    }

    @NonNull
    public static String summaryLine(@Nullable String raw) {
        if (raw == null || raw.isEmpty()) return "—";
        Double amount = parseAmountRub(raw);
        if (amount != null) {
            return String.format(Locale.forLanguageTag("ru"), "Сумма: %.2f ₽", amount);
        }
        String shortRaw = raw.length() > 120 ? raw.substring(0, 117) + "…" : raw;
        return shortRaw;
    }
}
