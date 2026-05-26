package com.example.smartwallet.network;

import com.example.smartwallet.BuildConfig;
import com.example.smartwallet.utils.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    /** Задаётся в local.properties → api.base.url (см. app/build.gradle.kts). */
    private static final String BASE_URL = BuildConfig.API_BASE_URL;
    private static Retrofit retrofitInstance;

    private ApiClient() {}

    private static Retrofit getRetrofit() {
        if (retrofitInstance == null) {
            Logger.i("ApiClient", "Initializing Retrofit with base URL: " + BASE_URL);
            
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> {
                Logger.d("HTTP", message);
            });
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            Gson gson = new GsonBuilder().create();

            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
                    
            Logger.i("ApiClient", "Retrofit initialized successfully");
        }
        return retrofitInstance;
    }

    public static AuthApi getAuthApi() {
        return getRetrofit().create(AuthApi.class);
    }
    
    public static CardsApi getCardsApi() {
        return getRetrofit().create(CardsApi.class);
    }
    
    public static CashbackApi getCashbackApi() {
        return getRetrofit().create(CashbackApi.class);
    }
    
    public static TransactionsApi getTransactionsApi() {
        return getRetrofit().create(TransactionsApi.class);
    }
    
    public static AssistantApi getAssistantApi() {
        return getRetrofit().create(AssistantApi.class);
    }

    public static DemoApi getDemoApi() {
        return getRetrofit().create(DemoApi.class);
    }
}




