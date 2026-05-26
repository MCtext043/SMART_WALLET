package com.example.smartwallet.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartwallet.R;
import com.example.smartwallet.model.ChatMessage;
import com.example.smartwallet.ui.adapter.ChatAdapter;
import com.example.smartwallet.utils.TokenManager;
import com.example.smartwallet.viewmodel.ChatViewModel;

public class AssistantActivity extends AppCompatActivity {

    private RecyclerView recyclerChat;
    private EditText editMessage;
    private ImageButton buttonSend;
    private ImageButton buttonBack;
    private ChatAdapter chatAdapter;
    private ChatViewModel chatViewModel;

    private final TextWatcher sendButtonTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            refreshSendButtonEnabled();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);

        initViews();
        setupChat();
        setupKeyboardBehavior();
    }

    private void initViews() {
        recyclerChat = findViewById(R.id.recyclerChat);
        editMessage = findViewById(R.id.editMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonBack = findViewById(R.id.buttonBack);
        editMessage.addTextChangedListener(sendButtonTextWatcher);
    }

    private void setupChat() {
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        chatViewModel.setTokenManager(TokenManager.getInstance(this));

        chatAdapter = new ChatAdapter();
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerChat.setAdapter(chatAdapter);

        chatViewModel.getMessages().observe(this, messages -> {
            chatAdapter.setMessages(messages);
            if (!messages.isEmpty()) {
                recyclerChat.scrollToPosition(messages.size() - 1);
            }
        });

        chatViewModel.getIsLoading().observe(this, isLoading -> refreshSendButtonEnabled());

        chatViewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                chatViewModel.clearError();
            }
        });

        buttonSend.setOnClickListener(v -> sendMessage());
        buttonBack.setOnClickListener(v -> finish());

        addWelcomeMessage();
        refreshSendButtonEnabled();
    }

    private void setupKeyboardBehavior() {
        editMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && chatAdapter != null && chatAdapter.getItemCount() > 0) {
                recyclerChat.postDelayed(() ->
                        recyclerChat.scrollToPosition(chatAdapter.getItemCount() - 1), 100);
            }
        });
    }

    private void refreshSendButtonEnabled() {
        boolean loading = Boolean.TRUE.equals(chatViewModel.getIsLoading().getValue());
        boolean hasText = !TextUtils.isEmpty(editMessage.getText().toString().trim());
        buttonSend.setEnabled(hasText && !loading);
    }

    private void addWelcomeMessage() {
        ChatMessage welcomeMessage = new ChatMessage(
                "Привет! Я ваш AI ассистент по финансовой грамотности. "
                        + "Могу помочь с вопросами о бюджетировании, инвестициях, кредитах и других финансовых темах. "
                        + "Чем могу помочь?",
                ChatMessage.TYPE_ASSISTANT
        );
        chatAdapter.addMessage(welcomeMessage);
    }

    private void sendMessage() {
        String message = editMessage.getText().toString().trim();
        if (!TextUtils.isEmpty(message)) {
            chatViewModel.sendMessage(message);
            editMessage.setText("");
        }
    }
}
