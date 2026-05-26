package com.example.smartwallet.nfc;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class EmvNfcReader {

    public interface Callback {
        void onEmvCardDetected(@NonNull EmvCardHint hint);
        void onNfcNotAvailable();
        void onNfcDisabled();
        void onError(@NonNull String message);
    }

    public static final class EmvCardHint {
        @Nullable public final String scheme;
        @Nullable public final String aid;
        @Nullable public final String panLast4;

        public EmvCardHint(@Nullable String scheme, @Nullable String aid, @Nullable String panLast4) {
            this.scheme = scheme;
            this.aid = aid;
            this.panLast4 = panLast4;
        }
    }

    public static void start(@NonNull Activity activity, @NonNull Callback callback) {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        if (adapter == null) {
            callback.onNfcNotAvailable();
            return;
        }
        if (!adapter.isEnabled()) {
            callback.onNfcDisabled();
            return;
        }

        adapter.enableReaderMode(
                activity,
                tag -> onTag(activity, callback, tag),
                NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B,
                null
        );
    }

    public static void stop(@NonNull Activity activity) {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(activity);
        if (adapter != null) adapter.disableReaderMode(activity);
    }

    private static void onTag(@NonNull Activity activity, @NonNull Callback callback, @NonNull Tag tag) {
        IsoDep isoDep = IsoDep.get(tag);
        if (isoDep == null) return; // not EMV-like

        try {
            isoDep.connect();
            isoDep.setTimeout(3000);

            // SELECT PPSE: "2PAY.SYS.DDF01"
            byte[] ppse = buildSelectByNameApdu("2PAY.SYS.DDF01");
            byte[] r1 = isoDep.transceive(ppse);
            if (!is9000(r1)) throw new IOException("PPSE SELECT failed: SW=" + sw(r1));

            // Try to parse AID(s) from PPSE response (tag 4F)
            List<String> aids = extractAidsFromPpseResponse(r1);
            String firstAid = aids.isEmpty() ? null : aids.get(0);
            String scheme = firstAid == null ? null : schemeFromAid(firstAid);

            String last4 = null;
            if (firstAid != null) {
                try {
                    last4 = tryReadPanLast4(isoDep, firstAid);
                } catch (Exception ignored) {
                    // best-effort; many cards won't expose PAN over NFC
                }
            }

            EmvCardHint hint = new EmvCardHint(scheme, firstAid, last4);
            activity.runOnUiThread(() -> callback.onEmvCardDetected(hint));
        } catch (Exception e) {
            activity.runOnUiThread(() -> callback.onError(e.getMessage() == null ? "NFC error" : e.getMessage()));
        } finally {
            try { isoDep.close(); } catch (Exception ignored) {}
        }
    }

    private static byte[] buildSelectByNameApdu(@NonNull String name) {
        byte[] data = name.getBytes(StandardCharsets.US_ASCII);
        int lc = data.length;
        byte[] apdu = new byte[6 + lc];
        apdu[0] = 0x00;
        apdu[1] = (byte) 0xA4;
        apdu[2] = 0x04;
        apdu[3] = 0x00;
        apdu[4] = (byte) lc;
        System.arraycopy(data, 0, apdu, 5, lc);
        apdu[5 + lc] = 0x00;
        return apdu;
    }

    private static byte[] buildSelectByAidApdu(@NonNull String aidHex) {
        byte[] aid = hexToBytes(aidHex);
        int lc = aid.length;
        byte[] apdu = new byte[6 + lc];
        apdu[0] = 0x00;
        apdu[1] = (byte) 0xA4;
        apdu[2] = 0x04;
        apdu[3] = 0x00;
        apdu[4] = (byte) lc;
        System.arraycopy(aid, 0, apdu, 5, lc);
        apdu[5 + lc] = 0x00;
        return apdu;
    }

    private static byte[] buildGpoEmptyPdolApdu() {
        // 80 A8 00 00 02 83 00 00
        return new byte[] {(byte)0x80, (byte)0xA8, 0x00, 0x00, 0x02, (byte)0x83, 0x00, 0x00};
    }

    private static byte[] buildReadRecordApdu(int sfi, int record) {
        // 00 B2 <record> <(sfi<<3)|4> 00
        return new byte[] {0x00, (byte)0xB2, (byte)record, (byte)((sfi << 3) | 0x04), 0x00};
    }

    @Nullable
    private static String tryReadPanLast4(@NonNull IsoDep isoDep, @NonNull String aidHex) throws IOException {
        byte[] sel = isoDep.transceive(buildSelectByAidApdu(aidHex));
        if (!is9000(sel)) return null;

        byte[] gpo = isoDep.transceive(buildGpoEmptyPdolApdu());
        if (!is9000(gpo)) return null;

        byte[] gpoData = Arrays.copyOf(gpo, gpo.length - 2); // strip SW
        byte[] afl = findTagValue(gpoData, 0x94);
        if (afl == null || afl.length < 4) return null;

        // AFL entries are 4 bytes: SFI, first record, last record, # offline auth records
        for (int i = 0; i + 3 < afl.length; i += 4) {
            int sfi = (afl[i] & 0xFF) >> 3;
            int firstRec = afl[i + 1] & 0xFF;
            int lastRec = afl[i + 2] & 0xFF;
            for (int rec = firstRec; rec <= lastRec; rec++) {
                byte[] rr = isoDep.transceive(buildReadRecordApdu(sfi, rec));
                if (!is9000(rr)) continue;

                byte[] rrData = Arrays.copyOf(rr, rr.length - 2);
                // Prefer Track2 Equivalent (57), fallback PAN (5A)
                byte[] track2 = findTagValue(rrData, 0x57);
                String pan = track2 == null ? null : panFromTrack2(track2);
                if (pan == null) {
                    byte[] panBytes = findTagValue(rrData, 0x5A);
                    pan = panBytes == null ? null : panFromBcd(panBytes);
                }
                if (pan != null) {
                    String digits = pan.replaceAll("[^0-9]", "");
                    if (digits.length() >= 13) {
                        return digits.substring(digits.length() - 4);
                    }
                }
            }
        }
        return null;
    }

    private static boolean is9000(@Nullable byte[] resp) {
        if (resp == null || resp.length < 2) return false;
        int n = resp.length;
        return resp[n - 2] == (byte) 0x90 && resp[n - 1] == (byte) 0x00;
    }

    private static String sw(@Nullable byte[] resp) {
        if (resp == null || resp.length < 2) return "????";
        int n = resp.length;
        return String.format("%02X%02X", resp[n - 2], resp[n - 1]);
    }

    // Very small TLV walker that looks for tag 0x4F (AID) anywhere inside PPSE FCI.
    @NonNull
    private static List<String> extractAidsFromPpseResponse(@NonNull byte[] respWithSw) {
        byte[] data = Arrays.copyOf(respWithSw, respWithSw.length - 2); // strip SW1SW2
        List<String> aids = new ArrayList<>();
        int i = 0;
        while (i < data.length) {
            int tag = data[i] & 0xFF;
            i++;
            if (i >= data.length) break;

            // Handle multi-byte tags (basic BER-TLV)
            if ((tag & 0x1F) == 0x1F) {
                while (i < data.length) {
                    int t2 = data[i] & 0xFF;
                    i++;
                    if ((t2 & 0x80) == 0) break;
                }
                // For our needs, we only match single-byte 0x4F below, so skip parsing multibyte tag values.
                if (i >= data.length) break;
            }

            int len = data[i] & 0xFF;
            i++;
            if (i >= data.length) break;
            if ((len & 0x80) != 0) {
                int n = len & 0x7F;
                if (n == 0 || i + n > data.length) break;
                len = 0;
                for (int k = 0; k < n; k++) {
                    len = (len << 8) | (data[i] & 0xFF);
                    i++;
                }
            }
            if (i + len > data.length) break;

            if (tag == 0x4F && len >= 5 && len <= 16) {
                aids.add(bytesToHex(data, i, len));
            }
            i += len;
        }
        return aids;
    }

    @NonNull
    private static String bytesToHex(@NonNull byte[] b, int off, int len) {
        StringBuilder sb = new StringBuilder(len * 2);
        for (int i = 0; i < len; i++) sb.append(String.format("%02X", b[off + i]));
        return sb.toString();
    }

    @Nullable
    private static byte[] findTagValue(@NonNull byte[] tlv, int wantedTag) {
        int i = 0;
        while (i < tlv.length) {
            int tag = tlv[i] & 0xFF;
            i++;
            if (i >= tlv.length) break;

            // multi-byte tags
            if ((tag & 0x1F) == 0x1F) {
                // not supported for matching here; skip tag bytes
                while (i < tlv.length) {
                    int t2 = tlv[i] & 0xFF;
                    i++;
                    if ((t2 & 0x80) == 0) break;
                }
                if (i >= tlv.length) break;
            }

            int len = tlv[i] & 0xFF;
            i++;
            if (i > tlv.length) break;
            if ((len & 0x80) != 0) {
                int n = len & 0x7F;
                if (n == 0 || i + n > tlv.length) break;
                len = 0;
                for (int k = 0; k < n; k++) {
                    len = (len << 8) | (tlv[i] & 0xFF);
                    i++;
                }
            }
            if (i + len > tlv.length) break;

            if (tag == wantedTag) {
                return Arrays.copyOfRange(tlv, i, i + len);
            }
            i += len;
        }
        return null;
    }

    @NonNull
    private static byte[] hexToBytes(@NonNull String hex) {
        String s = hex.replaceAll("[^0-9A-Fa-f]", "");
        int len = s.length();
        if (len % 2 != 0) s = "0" + s;
        byte[] out = new byte[s.length() / 2];
        for (int i = 0; i < out.length; i++) {
            int hi = Character.digit(s.charAt(i * 2), 16);
            int lo = Character.digit(s.charAt(i * 2 + 1), 16);
            out[i] = (byte) ((hi << 4) | lo);
        }
        return out;
    }

    @Nullable
    private static String panFromTrack2(@NonNull byte[] track2Bcd) {
        // Track2 equivalent data is BCD with 'D' separator (0xD) and padded with 0xF.
        String hex = bytesToHex(track2Bcd, 0, track2Bcd.length);
        int d = hex.indexOf('D');
        if (d < 0) d = hex.indexOf('d');
        String panHex = d < 0 ? hex : hex.substring(0, d);
        // Strip padding Fs
        panHex = panHex.replaceAll("F+$", "");
        return panHex.matches("[0-9]+") ? panHex : null;
    }

    @Nullable
    private static String panFromBcd(@NonNull byte[] panBcd) {
        String hex = bytesToHex(panBcd, 0, panBcd.length).replaceAll("F+$", "");
        return hex.matches("[0-9]+") ? hex : null;
    }

    @Nullable
    private static String schemeFromAid(@NonNull String aidHex) {
        // RID = first 5 bytes (10 hex chars)
        if (aidHex.length() < 10) return null;
        String rid = aidHex.substring(0, 10).toUpperCase();
        switch (rid) {
            case "A000000003": return "VISA";
            case "A000000004": return "MASTERCARD";
            case "A000000025": return "AMEX";
            case "A000000065": return "JCB";
            case "A000000152": return "DISCOVER";
            case "A000000333": return "UNIONPAY";
            case "A000000658": return "MIR";
            default: return null;
        }
    }

    private EmvNfcReader() {}
}

