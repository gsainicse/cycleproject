package com.cycleproject.b2b.api;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    // Change this to your server IP
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static ApiService getApiService(Context context) {
        if (apiService == null) {
            Context appContext = context.getApplicationContext();
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        SharedPreferences prefs = appContext.getSharedPreferences("auth", Context.MODE_PRIVATE);
                        String token = prefs.getString("token", "");
                        String role = prefs.getString("role", "");

                        android.util.Log.d("RetrofitClient", "URL: " + original.url());
                        android.util.Log.d("RetrofitClient", "Token: " + (token.isEmpty() ? "Empty" : "Present"));
                        android.util.Log.d("RetrofitClient", "Role in Prefs: " + role);

                        Request.Builder builder = original.newBuilder();
                        if (!token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(builder.build());
                    })
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    public static void resetClient() {
        apiService = null;
        retrofit = null;
    }
}
