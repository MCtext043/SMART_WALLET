package com.example.smartwallet.network;

import com.example.smartwallet.network.dto.Transaction;
import com.example.smartwallet.network.dto.TransactionImportRequest;
import com.example.smartwallet.network.dto.TransactionImportResponse;
import com.example.smartwallet.network.dto.TransactionRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface TransactionsApi {
    
    @POST("transactions")
    Call<Void> createTransaction(@Header("Authorization") String token, 
                                @Body TransactionRequest request);

    @POST("transactions/import")
    Call<TransactionImportResponse> importTransactions(
            @Header("Authorization") String token,
            @Body TransactionImportRequest body
    );
    
    @GET("transactions")
    Call<List<Transaction>> getTransactions(@Header("Authorization") String token);
}
