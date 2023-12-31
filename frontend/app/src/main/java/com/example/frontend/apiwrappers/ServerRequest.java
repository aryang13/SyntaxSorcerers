package com.example.frontend.apiwrappers;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.JsonElement;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Url;

public class ServerRequest {

    public static final String RequestTag = "Server Requests";
    private static final String BASE_URL = "https://grgq6ss4i9.execute-api.us-east-2.amazonaws.com/";

    private ApiService apiService;

    public ServerRequest(){}

    /* ChatGPT usage: Partial */
    public ServerRequest(String userId) {
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @NonNull
            @Override
            public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
                Request newRequest  = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer " + userId)
                        .build();
                return chain.proceed(newRequest);
            }
        }).build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    /* ChatGPT usage: No */
    public void makeGetRequest(String endpoint, final ServerRequest.ApiRequestListener listener) throws UnsupportedEncodingException {
        Call<JsonElement> call = apiService.getData("/prod" + endpoint);
        callHandler(listener, call);
    }

    /* ChatGPT usage: No */
    public void makePostRequest(String endpoint, JsonElement body, final ServerRequest.ApiRequestListener listener) throws UnsupportedEncodingException {
        Call<JsonElement> call = apiService.postData("/prod" + endpoint, body);
        callHandler(listener, call);
    }

    /* ChatGPT usage: Partial */
    public void makePutRequest(String endpoint, JsonElement body, final ServerRequest.ApiRequestListener listener) throws UnsupportedEncodingException {
        Call<JsonElement> call = apiService.putData("/prod" + endpoint, body);
        callHandler(listener, call);
    }

    /* ChatGPT usage: No */
    public void makeDeleteRequest(String endpoint, final ServerRequest.ApiRequestListener listener) throws UnsupportedEncodingException {
        Call<JsonElement> call = apiService.deleteData("/prod" + endpoint);
        callHandler(listener, call);
    }

    /* ChatGPT usage: Partial */
    protected static void callHandler(ApiRequestListener listener, Call<JsonElement> call) {
        Log.d(RequestTag, call.request().url().toString());
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful()) {
                    JsonElement jsonResponse = response.body();
                    Log.d(RequestTag, jsonResponse.toString());
                    try {
                        listener.onApiRequestComplete(jsonResponse);
                    } catch (ParseException e) {
                        throw new InternalError(e);
                    }
                } else {
                    listener.onApiRequestError("Error response: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                listener.onApiRequestError(t.getMessage());
            }
        });
    }

    public interface ApiRequestListener {
        void onApiRequestComplete(JsonElement response) throws ParseException;
        void onApiRequestError(String error);
    }

    protected interface ApiService {
        @GET
        Call<JsonElement> getData(@Url String endpoint);
        @POST
        Call<JsonElement> postData(@Url String endpoint, @Body JsonElement data);

        @PUT
        Call<JsonElement> putData(@Url String endpoint, @Body JsonElement data);

        @DELETE
        Call<JsonElement> deleteData(@Url String endpoint);
    }
}
