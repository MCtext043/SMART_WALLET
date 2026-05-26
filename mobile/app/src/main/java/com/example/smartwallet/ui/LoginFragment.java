package com.example.smartwallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartwallet.BuildConfig;
import com.example.smartwallet.R;
import com.example.smartwallet.network.ApiClient;
import com.example.smartwallet.network.AuthApi;
import com.example.smartwallet.network.dto.LoginRequest;
import com.example.smartwallet.network.dto.TokenResponse;
import com.example.smartwallet.utils.AuthFieldCardsGlow;
import com.example.smartwallet.utils.ErrorHandler;
import com.example.smartwallet.utils.Logger;
import com.example.smartwallet.utils.PhoneMaskHelper;
import com.example.smartwallet.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginFragment extends Fragment {

    private EditText phoneInput;
    private EditText passwordInput;
    private CheckBox checkboxRememberMe;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        phoneInput = view.findViewById(R.id.inputPhone);
        passwordInput = view.findViewById(R.id.inputPassword);
        checkboxRememberMe = view.findViewById(R.id.checkboxRememberMe);
        progressBar = view.findViewById(R.id.progress);
        Button loginButton = view.findViewById(R.id.buttonLogin);

        PhoneMaskHelper.attach(phoneInput);
        loginButton.setOnClickListener(v -> attemptLogin());
        AuthFieldCardsGlow.applyToTree(view, requireContext());
        return view;
    }

    private void attemptLogin() {
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        // Валидация полей
        boolean isValid = true;
        
        if (TextUtils.isEmpty(phone)) {
            phoneInput.setError("Введите номер телефона");
            isValid = false;
        } else if (!PhoneMaskHelper.isCompleteValid(phone)) {
            phoneInput.setError("Введите номер полностью: 8-XXX-XXX-XX-XX");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Введите пароль");
            isValid = false;
        } else if (password.length() < 6) {
            passwordInput.setError("Пароль должен содержать минимум 6 символов");
            isValid = false;
        }
        
        if (!isValid) {
            return;
        }

        setLoading(true);
        AuthApi api = ApiClient.getAuthApi();
        String phoneDigits = PhoneMaskHelper.digitsOnly(phone);
        api.login(new LoginRequest(phoneDigits, password)).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(@NonNull Call<TokenResponse> call, @NonNull Response<TokenResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    boolean remember = checkboxRememberMe == null || checkboxRememberMe.isChecked();
                    TokenManager.getInstance(requireContext()).saveToken(response.body().accessToken, remember);
                    openWelcome();
                } else {
                    Toast.makeText(
                            requireContext(),
                            ErrorHandler.httpErrorMessage(response),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TokenResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Logger.e("LoginFragment", "Login failed; API_BASE_URL=" + BuildConfig.API_BASE_URL, t);
                ErrorHandler.showError(requireContext(), t);
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
    
    private void openWelcome() {
        Intent intent = new Intent(requireContext(), DashboardActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}


