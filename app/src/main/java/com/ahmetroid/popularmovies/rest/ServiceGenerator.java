package com.ahmetroid.popularmovies.rest;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.ahmetroid.popularmovies.BuildConfig.API_KEY;

public class ServiceGenerator {

    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();
                    HttpUrl httpUrl = request.url();

                    httpUrl = httpUrl.newBuilder()
                            .addQueryParameter("api_key", API_KEY)
                            .build();

                    request = request.newBuilder().url(httpUrl).build();
                    return chain.proceed(request);
                }
            }).build();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = builder.build();

    public static <S> S createService(Class<S> serviceClass) {
        return retrofit.create(serviceClass);
    }
}