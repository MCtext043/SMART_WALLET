package com.example.smartwallet.network;

import com.example.smartwallet.network.dto.ChatRequest;
import com.example.smartwallet.network.dto.ChatResponse;
import com.example.smartwallet.network.dto.Recommendation;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AssistantApi {
    
    @POST("assistant/chat")
    Call<ChatResponse> sendMessage(@Header("Authorization") String token, 
                                  @Body ChatRequest request);
    
    @GET("assistant/recommendations")
    Call<List<Recommendation>> getRecommendations(@Header("Authorization") String token);
}
