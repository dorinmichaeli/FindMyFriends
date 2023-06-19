package com.example.maplord.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RestApi {
  @GET("/group/exists")
  Call<GroupExistsResponse> doesGroupExist(@Query("groupId") String groupId);
}
