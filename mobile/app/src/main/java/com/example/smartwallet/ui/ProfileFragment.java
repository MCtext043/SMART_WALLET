package com.example.smartwallet.ui;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.example.smartwallet.BuildConfig;
import com.example.smartwallet.R;
import com.example.smartwallet.network.ApiClient;
import com.example.smartwallet.network.AuthApi;
import com.example.smartwallet.network.dto.ProfileResponse;
import com.example.smartwallet.utils.PhoneMaskHelper;
import com.example.smartwallet.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private ImageView imageAvatar;
    private View avatarButton;
    private TextView textName;
    private TextView textPhone;
    private TextView textEmail;
    private View buttonProfileInfo;
    private ProgressBar progress;
    private MaterialButton buttonAiChat;
    private MaterialButton buttonLogout;

    /** Меняется после успешной загрузки аватара — иначе Glide не перечитывает тот же URL. */
    private long avatarGlideSignature = 0L;

    private final ActivityResultLauncher<String> pickAvatarLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onAvatarUriPicked);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imageAvatar = view.findViewById(R.id.imageAvatar);
        avatarButton = view.findViewById(R.id.avatarButton);
        textName = view.findViewById(R.id.textName);
        textPhone = view.findViewById(R.id.textPhone);
        textEmail = view.findViewById(R.id.textEmail);
        buttonProfileInfo = view.findViewById(R.id.buttonProfileInfo);
        progress = view.findViewById(R.id.progress);
        buttonAiChat = view.findViewById(R.id.buttonAiChat);
        buttonLogout = view.findViewById(R.id.buttonLogout);

        buttonAiChat.setOnClickListener(v -> openAssistant());
        buttonLogout.setOnClickListener(v -> logout());
        buttonAiChat.post(this::applyProfileButtonGlow);
        if (buttonProfileInfo != null) {
            buttonProfileInfo.setOnClickListener(v -> showProfileInfoDialog());
        }
        if (avatarButton != null) {
            avatarButton.setOnClickListener(v -> pickAvatarLauncher.launch("image/*"));
        }

        bindAvatar(null);
        loadProfile();
        return view;
    }

    private void showProfileInfoDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_profile_info);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }

        TextView textInfo = dialog.findViewById(R.id.textProfileInfo);
        if (textInfo != null) {
            textInfo.setText(R.string.profile_info_dialog_full);
        }
        View close = dialog.findViewById(R.id.buttonCloseInfo);
        if (close != null) {
            close.setOnClickListener(v -> dialog.dismiss());
        }
        dialog.show();
    }

    private void onAvatarUriPicked(@Nullable Uri uri) {
        if (uri != null) {
            uploadAvatar(uri);
        }
    }

    /** Лёгкая тень у белых кнопок и круглой «инфо» (API 28+ — цвет контура тени). */
    private void applyProfileButtonGlow() {
        float elev = getResources().getDimension(R.dimen.profile_white_button_elevation);
        buttonAiChat.setElevation(elev);
        buttonLogout.setElevation(elev);
        buttonAiChat.setTranslationZ(0f);
        buttonLogout.setTranslationZ(0f);
        if (buttonProfileInfo != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                buttonProfileInfo.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            }
            buttonProfileInfo.setElevation(elev);
            buttonProfileInfo.setTranslationZ(0f);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return;
        }
        int ambient = ContextCompat.getColor(requireContext(), R.color.profile_button_glow_ambient);
        int spot = ContextCompat.getColor(requireContext(), R.color.profile_button_glow_spot);
        buttonAiChat.setOutlineAmbientShadowColor(ambient);
        buttonAiChat.setOutlineSpotShadowColor(spot);
        buttonLogout.setOutlineAmbientShadowColor(ambient);
        buttonLogout.setOutlineSpotShadowColor(spot);
        if (buttonProfileInfo != null) {
            buttonProfileInfo.setOutlineAmbientShadowColor(ambient);
            buttonProfileInfo.setOutlineSpotShadowColor(spot);
        }
    }

    private void loadProfile() {
        TokenManager tokenManager = TokenManager.getInstance(requireContext());
        String token = tokenManager.getToken();

        if (token == null) {
            Toast.makeText(requireContext(), "Токен не найден. Войдите в систему.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        AuthApi api = ApiClient.getAuthApi();
        String authToken = "Bearer " + token;
        api.getProfile(authToken).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    bindProfile(response.body());
                } else if (response.code() == 401) {
                    Toast.makeText(requireContext(), "Сессия истекла. Войдите в систему.", Toast.LENGTH_SHORT).show();
                    tokenManager.clearToken();
                    logout();
                } else {
                    Toast.makeText(requireContext(), "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(requireContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindProfile(@NonNull ProfileResponse profile) {
        textName.setText(profile.name);
        textEmail.setText(profile.email != null ? profile.email : "");
        bindPhoneDisplay(profile.phone);
        bindAvatar(profile.avatarUrl);
    }

    private void bindPhoneDisplay(@Nullable String rawPhone) {
        if (rawPhone == null || rawPhone.trim().isEmpty()) {
            textPhone.setText("—");
            textPhone.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant));
            return;
        }
        String masked = PhoneMaskHelper.formatForDisplay(rawPhone);
        String digits = PhoneMaskHelper.digitsOnly(masked);
        if (digits.isEmpty()) {
            textPhone.setText(rawPhone.trim());
            textPhone.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant));
            return;
        }
        textPhone.setText(masked);
        int gray = ContextCompat.getColor(requireContext(), R.color.md_theme_onSurfaceVariant);
        int error = ContextCompat.getColor(requireContext(), R.color.md_theme_error);
        textPhone.setTextColor(PhoneMaskHelper.isCompleteValid(masked) ? gray : error);
    }

    private void bindAvatar(@Nullable String avatarUrl) {
        if (imageAvatar == null) return;
        String url = resolveAvatarUrl(avatarUrl);
        if (url == null) {
            imageAvatar.setImageResource(R.drawable.ic_person_24);
            imageAvatar.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.md_theme_onPrimaryContainer)));
            return;
        }
        imageAvatar.setImageTintList(null);
        Glide.with(this)
                .load(url)
                .circleCrop()
                .signature(new ObjectKey(avatarGlideSignature + "|" + url))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_person_24)
                .into(imageAvatar);
    }

    @Nullable
    private static String resolveAvatarUrl(@Nullable String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isEmpty()) return null;
        String t = pathOrUrl.trim();
        if (t.startsWith("http://") || t.startsWith("https://")) return t;
        String base = BuildConfig.API_BASE_URL;
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        if (t.startsWith("/")) return base + t;
        return base + "/" + t;
    }

    private void uploadAvatar(@NonNull Uri uri) {
        TokenManager tokenManager = TokenManager.getInstance(requireContext());
        String token = tokenManager.getToken();
        if (token == null) {
            Toast.makeText(requireContext(), "Токен не найден", Toast.LENGTH_SHORT).show();
            return;
        }

        File file;
        try {
            file = copyUriToCacheFile(uri);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Не удалось прочитать файл", Toast.LENGTH_SHORT).show();
            return;
        }

        String mime = requireContext().getContentResolver().getType(uri);
        if (mime == null || mime.isEmpty()) {
            mime = "image/jpeg";
        }
        RequestBody body = RequestBody.create(MediaType.parse(mime), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), body);

        setLoading(true);
        String authToken = "Bearer " + token;
        ApiClient.getAuthApi().uploadAvatar(authToken, part).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    avatarGlideSignature = System.nanoTime();
                    bindProfile(response.body());
                    Toast.makeText(requireContext(), R.string.profile_avatar_upload_ok, Toast.LENGTH_SHORT).show();
                } else if (response.code() == 401) {
                    tokenManager.clearToken();
                    logout();
                } else {
                    Toast.makeText(requireContext(), R.string.profile_avatar_no_endpoint, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(requireContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @NonNull
    private File copyUriToCacheFile(@NonNull Uri uri) throws IOException {
        String mime = requireContext().getContentResolver().getType(uri);
        String ext = ".jpg";
        if (mime != null) {
            if (mime.contains("png")) ext = ".png";
            else if (mime.contains("webp")) ext = ".webp";
        }
        File out = new File(requireContext().getCacheDir(), "avatar_upload_" + System.currentTimeMillis() + ext);
        try (InputStream in = requireContext().getContentResolver().openInputStream(uri);
             OutputStream os = new FileOutputStream(out)) {
            if (in == null) throw new IOException("openInputStream null");
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) {
                os.write(buf, 0, n);
            }
        }
        return out;
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void openAssistant() {
        Intent intent = new Intent(requireContext(), AssistantActivity.class);
        startActivity(intent);
    }

    private void logout() {
        TokenManager.getInstance(requireContext()).clearToken();
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
