package com.jd.decoration.ai.vearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jd.decoration.ai.vearch.request.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

@Slf4j
public class VearchClient {

    private final VearchApi vearchApi;

    private final Gson GSON = new Gson();

    public VearchClient(String baseUrl, Duration timeout) {
        log.info("VearchClient init: param baseUrl:{}, timeout:{}", baseUrl, GSON.toJson(timeout));
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .callTimeout(timeout)
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout)
                .build();

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        this.vearchApi = retrofit.create(VearchApi.class);
    }

    public JsonArray clusterStats() {
        try {
            log.info("VearchClient clusterStats");
            Response<JsonArray> response = vearchApi.clusterStats().execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject listDb() {
        try {
            log.info("VearchClient listDb");
            Response<JsonObject> response = vearchApi.listDb().execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public JsonObject dbCreate(CreateDbRequest createDbRequest) {
        try {
            log.info("VearchClient dbCreate: param createDbRequest:{}", GSON.toJson(createDbRequest));
            Response<JsonObject> response = vearchApi.dbCreate(createDbRequest).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public JsonObject queryDb(String dbname) {
        try {
            log.info("VearchClient queryDb: param dbname:{}", dbname);
            Response<JsonObject> response = vearchApi.queryDb(dbname).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public JsonObject deleteDb(String dbname) {
        try {
            log.info("VearchClient deleteDb: param dbname:{}", dbname);
            Response<JsonObject> response = vearchApi.deleteDb(dbname).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject listSpace(String dbname) {
        try {
            log.info("VearchClient listSpace: param dbname:{}", dbname);
            Response<JsonObject> response = vearchApi.listSpace(dbname).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject spaceCreate(String dbname, CreateSpaceRequest createSpaceRequest) {
        try {
            log.info("VearchClient spaceCreate: param dbname:{}, createSpaceRequest:{}", dbname, GSON.toJson(createSpaceRequest));
            Response<JsonObject> response = vearchApi.spaceCreate(dbname, createSpaceRequest).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject querySpace(String dbname, String spaceName) {
        try {
            log.info("VearchClient querySpace: param dbname:{}, spaceName:{}", dbname, spaceName);
            Response<JsonObject> response = vearchApi.querySpace(dbname, spaceName).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject deleteSpace(String dbname, String spaceName) {
        try {
            log.info("VearchClient deleteSpace: param dbname:{}, spaceName:{}", dbname, spaceName);
            Response<JsonObject> response = vearchApi.deleteSpace(dbname, spaceName).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject updateSpaceCache(String dbname, String spaceName, UpdateSpaceCacheRequest updateSpaceCacheRequest) {
        try {
            log.info("VearchClient updateSpaceCache: param dbname:{}, spaceName:{}, updateSpaceCacheRequest:{}", dbname, spaceName, GSON.toJson(updateSpaceCacheRequest));
            Response<JsonObject> response = vearchApi.updateSpaceCache(dbname, spaceName, updateSpaceCacheRequest).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject querySpaceCache(String dbname, String spaceName) {
        try {
            log.info("VearchClient querySpaceCache: param dbname:{}, spaceName:{}", dbname, spaceName);
            Response<JsonObject> response = vearchApi.querySpaceCache(dbname, spaceName).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject documentUpsert(DocumentUpsertRequest request) {
        try {
            log.info("VearchClient documentUpsert: param request:{}", GSON.toJson(request));
            Response<JsonObject> response = vearchApi.documentUpsert(request).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public JsonObject documentAdd(String dbname, String spaceName, String id, Map<String, Object> body) {
        try {
            log.info("VearchClient documentAdd: param dbname:{}, spaceName:{}, id:{}, body:{}", dbname, spaceName, id, GSON.toJson(body));
            Response<JsonObject> response = vearchApi.documentAdd(dbname, spaceName, id, body).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject documentQuery(DocumentQueryRequest request) {
        try {
            log.info("VearchClient documentQuery: param request:{}", GSON.toJson(request));
            Response<JsonObject> response = vearchApi.documentQuery(request).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    public JsonObject documentSearch(DocumentSearchRequest request) {
        try {
            log.info("VearchClient documentSearch: param request:{}", GSON.toJson(request));
            Response<JsonObject> response = vearchApi.documentSearch(request).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject vectorSearch(String dbname, String spaceName, VectorSearchRequest request) {
        try {
            log.info("VearchClient vectorSearch: param dbname:{}, spaceName:{}, VectorSearchRequest:{}", dbname, spaceName, GSON.toJson(request));
            Response<JsonObject> response = vearchApi.vectorSearch(dbname,spaceName, request).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonObject documentDelete(DocumentDeleteRequest request) {
        try {
            log.info("VearchClient documentDelete: param request:{}", GSON.toJson(request));
            Response<JsonObject> response = vearchApi.documentDelete(request).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public JsonObject addEmbeddings(DocumentDeleteRequest request) {
        try {
            Response<JsonObject> response = vearchApi.documentDelete(request).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static RuntimeException toException(Response<?> response) throws IOException {

        int code = response.code();
        String body = response.errorBody().string();

        String errorMessage = String.format("status code: %s; body: %s", code, body);
        return new RuntimeException(errorMessage);
    }

}
