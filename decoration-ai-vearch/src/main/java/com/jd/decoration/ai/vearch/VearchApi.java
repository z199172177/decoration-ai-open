package com.jd.decoration.ai.vearch;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jd.decoration.ai.vearch.request.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface VearchApi {

    @GET("/_cluster/stats")
    @Headers({"Content-Type: application/json"})
    Call<JsonArray> clusterStats();

    @GET("/list/db")
    @Headers({"Content-Type: application/json"})
    Call<JsonObject> listDb();

    @PUT("/db/_create")
    @Headers({"Content-Type: application/json"})
    Call<JsonObject> dbCreate(@Body CreateDbRequest createDbRequest);

    @GET("/db/{db_name}")
    @Headers({"Content-Type: application/json"})
    Call<JsonObject> queryDb(@Path("db_name") String dbName);

    @DELETE("/db/{db_name}")
    @Headers({"Content-Type: application/json"})
    Call<JsonObject> deleteDb(@Path("db_name") String dbName);

    @GET("/list/space")
    @Headers({"Content-Type: application/json"})
    Call<JsonObject> listSpace(@Query("db") String dbName);

    @PUT("/space/{db_name}/_create")
    @Headers({"Content-Type: application/json"})
    Call<JsonObject> spaceCreate(@Path("db_name") String dbName, @Body CreateSpaceRequest createSpaceRequest);

    @GET("/space/{db_name}/{space_name}")
    @Headers({"Content-Type: application/json"})
    Call<JsonObject> querySpace(@Path("db_name") String dbName, @Path("space_name") String spaceName);

    @DELETE("/space/{db_name}/{space_name}")
    @Headers({"Content-Type: application/json"})
    Call<JsonObject> deleteSpace(@Path("db_name") String dbName, @Path("space_name") String spaceName);

    @POST("/config/{db_name}/{space_name}")
    @Headers({"Content-Type: application/json"})
    Call<JsonObject> updateSpaceCache(@Path("db_name") String dbName, @Path("space_name") String spaceName, @Body UpdateSpaceCacheRequest updateSpaceCacheRequest);

    @GET("/config/{db_name}/{space_name}")
    @Headers({"Content-Type: application/json"})
    Call<JsonObject> querySpaceCache(@Path("db_name") String dbName, @Path("space_name") String spaceName);

    @POST("/document/upsert")
    @Headers({"Content-Type: application/json"})
    Call<JsonObject> documentUpsert(@Body DocumentUpsertRequest documentUpsertRequest);

    @Deprecated
    @POST("/{db_name}/{space_name}/{id}")
    @Headers({"Content-Type: application/json"})
    Call<JsonObject> documentAdd(@Path("db_name") String dbName,
                                 @Path("space_name") String spaceName,
                                 @Path("id") String id,
                                 @Body Map<String, Object> body);

    @POST("/document/query")
    @Headers({"Content-Type: application/json"})
    Call<JsonObject> documentQuery(@Body DocumentQueryRequest documentQueryRequest);

    @POST("/document/search")
    @Headers({"Content-Type: application/json"})
    Call<JsonObject> documentSearch(@Body DocumentSearchRequest documentSearchRequest);

    @Deprecated
    @POST("/{db_name}/{space_name}/_search")
    @Headers({"Content-Type: application/json"})
    Call<JsonObject> vectorSearch(
            @Path("db_name") String dbName,
            @Path("space_name") String spaceName,
            @Body VectorSearchRequest documentSearchRequest);


    @Headers({"Content-Type: application/json"})
    @POST("/document/delete")
    Call<JsonObject> documentDelete(@Body DocumentDeleteRequest documentDeleteRequest);

}
