package com.example.smartwallet.network;

import com.example.smartwallet.network.dto.DemoSeedResponse;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DemoApi {

    @POST("demo/seed")
    Call<DemoSeedResponse> seed(
            @Header("Authorization") String token,
            @Query("reset") boolean reset
    );
}
