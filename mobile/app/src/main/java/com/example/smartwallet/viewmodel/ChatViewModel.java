package com.example.smartwallet.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.smartwallet.model.ChatMessage;
import com.example.smartwallet.network.ApiClient;
import com.example.smartwallet.network.AssistantApi;
import com.example.smartwallet.network.dto.ChatRequest;
import com.example.smartwallet.network.dto.ChatResponse;
import com.example.smartwallet.utils.ErrorHandler;
import com.example.smartwallet.utils.TokenManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatViewModel extends ViewModel {
    
    private MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    
    private AssistantApi assistantApi;
    private TokenManager tokenManager;
    private List<ChatMessage> messageList = new ArrayList<>();
    
    public ChatViewModel() {
        assistantApi = ApiClient.getAssistantApi();
        messages.setValue(messageList);
        isLoading.setValue(false);
    }
    
    public void setTokenManager(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }
    
    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    public void sendMessage(String messageText) {
        if (messageText.trim().isEmpty()) {
            return;
        }
        
        // Add user message
        ChatMessage userMessage = new ChatMessage(messageText, ChatMessage.TYPE_USER);
        messageList.add(userMessage);
        messages.setValue(new ArrayList<>(messageList));
        
        // Send to API
        sendToAssistant(messageText);
    }
    
    private void sendToAssistant(String message) {
        String token = tokenManager.getToken();
        if (token == null) {
            error.setValue("Токен не найден. Войдите в систему.");
            return;
        }
        
        isLoading.setValue(true);
        String authToken = "Bearer " + token;
        
        ChatRequest request = new ChatRequest(message);
        assistantApi.sendMessage(authToken, request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    // Add assistant response
                    ChatMessage assistantMessage = new ChatMessage(response.body().reply, ChatMessage.TYPE_ASSISTANT);
                    messageList.add(assistantMessage);
                    messages.setValue(new ArrayList<>(messageList));
                } else {
                    error.setValue("Ошибка получения ответа от ассистента");
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue(ErrorHandler.getErrorMessage(t));
            }
        });
    }
    
    public void clearError() {
        error.setValue(null);
    }
}
