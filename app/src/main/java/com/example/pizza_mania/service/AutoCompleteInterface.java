package com.example.pizza_mania.service;

import com.example.pizza_mania.model.AutoCompleteResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AutoCompleteInterface {
    @GET("api/")
    Call<AutoCompleteResult> getAutocompletions(@Query("q") String text, @Query("limit") int limit, @Query("lat") Double lat, @Query("lon") Double lon);
}
