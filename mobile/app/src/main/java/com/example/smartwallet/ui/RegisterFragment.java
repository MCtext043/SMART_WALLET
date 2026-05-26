package com.example.smartwallet.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartwallet.R;
import com.example.smartwallet.network.ApiClient;
import com.example.smartwallet.network.AuthApi;
import com.example.smartwallet.network.dto.RegisterRequest;
import com.example.smartwallet.network.dto.TokenResponse;
import com.example.smartwallet.utils.AuthFieldCardsGlow;
import com.example.smartwallet.utils.ErrorHandler;
import com.example.smartwallet.utils.PhoneMaskHelper;
import com.example.smartwallet.utils.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    private EditText phoneInput;
    private EditText nameInput;
    private EditText emailInput;
    private EditText passwordInput;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        phoneInput = view.findViewById(R.id.inputPhone);
        nameInput = view.findViewById(R.id.inputName);
        emailInput = view.findViewById(R.id.inputEmail);
        passwordInput = view.findViewById(R.id.inputPassword);
        progressBar = view.findViewById(R.id.progress);
        Button registerButton = view.findViewById(R.id.buttonRegister);

        PhoneMaskHelper.attach(phoneInput);
        registerButton.setOnClickListener(v -> attemptRegister());
        AuthFieldCardsGlow.applyToTree(view, requireContext());
        return view;
    }

    private void attemptRegister() {
        String phone = phoneInput.getText().toString().trim();
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
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
        
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Введите имя");
            isValid = false;
        } else if (name.length() < 2) {
            nameInput.setError("Имя должно содержать минимум 2 символа");
            isValid = false;
        }
        
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Введите email");
            isValid = false;
        } else if (!isValidEmail(email)) {
            emailInput.setError("Неверный формат email");
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
        api.register(new RegisterRequest(phoneDigits, email, name, password)).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(@NonNull Call<TokenResponse> call, @NonNull Response<TokenResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    String access = response.body().accessToken;
                    if (access != null && !access.isEmpty()) {
                        TokenManager.getInstance(requireContext()).saveToken(access, true);
                        Toast.makeText(requireContext(), R.string.register_welcome, Toast.LENGTH_SHORT).show();
                        openDashboard();
                    } else {
                        Toast.makeText(requireContext(), R.string.register_success_need_login, Toast.LENGTH_SHORT).show();
                        goToLogin();
                    }
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
                ErrorHandler.showError(requireContext(), t);
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
    
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void goToLogin() {
        // Switch to login tab in AuthActivity
        if (getActivity() instanceof AuthActivity) {
            ((AuthActivity) getActivity()).switchToLogin();
        }
    }

    private void openDashboard() {
        Intent intent = new Intent(requireContext(), DashboardActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}


