package com.example.smartwallet.network;

import com.example.smartwallet.network.dto.BestCardResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface CashbackApi {
    
    @GET("cashback/best-card")
    Call<BestCardResponse> getBestCard(@Header("Authorization") String token, 
                                      @Query("category") String category);
}
