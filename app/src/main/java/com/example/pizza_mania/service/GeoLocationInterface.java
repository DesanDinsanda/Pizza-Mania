package com.example.pizza_mania.service;

import com.example.pizza_mania.model.GeoLocationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeoLocationInterface {
    @GET("search")
    Call<List<GeoLocationResponse>> getCoords(@Query("q") String query, @Query("format") String format, @Query("limit") int limit);
}
