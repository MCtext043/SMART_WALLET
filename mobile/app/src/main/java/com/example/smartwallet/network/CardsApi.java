package com.example.smartwallet.network;

import java.util.List;

import com.example.smartwallet.network.dto.Card;
import com.example.smartwallet.network.dto.CardCashbackPatchRequest;
import com.example.smartwallet.network.dto.CardRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CardsApi {
    
    @GET("cards/")
    Call<List<Card>> getCards(@Header("Authorization") String token);
    
    @GET("cards/{card_id}")
    Call<Card> getCard(@Header("Authorization") String token, @Path("card_id") int cardId);
    
    @POST("cards/")
    Call<Card> createCard(@Header("Authorization") String token, @Body CardRequest request);

    @PATCH("cards/{card_id}/")
    Call<Card> patchCardCashback(
            @Header("Authorization") String token,
            @Path("card_id") int cardId,
            @Body CardCashbackPatchRequest body);
}
