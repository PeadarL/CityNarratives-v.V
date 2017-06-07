package com.example.peter.citynarrativesvv;

import com.example.peter.citynarrativesvv.POJO.Example;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

public interface RetrofitMaps {

    /*Retrofit is developed by square, it is currently open source and free to use but this may not always
* be the case, Volley methods were added to this application to compensate for this
 * as a back-up for communicating with Google Server and retrieving JSON*/

    @GET("api/directions/json?key=AIzaSyCKv-kwVgsCyXgf8pJkrppepyGmJHpXwgg")
    Call<Example> getDistanceDuration(@Query("units") String units,
                                      @Query("origin") String origin,
                                      @Query("destination") String destination,
                                      @Query("mode") String mode);
}
