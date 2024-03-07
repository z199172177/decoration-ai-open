package com.jd.decoration.ai.vearch;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jd.decoration.ai.vearch.request.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.*;

public class VearchClientTest {

    private static VearchClient vearchMasterClient;
    private static VearchClient vearchRouterClient;
    private static String dbName;
    private static String spaceName;
    private static String commonId;

    @BeforeAll
    public static void before() {
        vearchMasterClient = new VearchClient("", Duration.ofMinutes(1));
        vearchRouterClient = new VearchClient("", Duration.ofMinutes(1));
        dbName = "db";
        spaceName = "ts_space";
        commonId = "110111112";
    }

    @Test
    public void clusterStats() {
        JsonArray ret = vearchMasterClient.clusterStats();
        System.out.println("ret = " + ret);
    }

    @Test
    public void listDb() {
        JsonObject ret = vearchMasterClient.listDb();
        System.out.println("ret = " + ret);
    }

    @Test
    public void dbCreate() {
        CreateDbRequest createDbRequest = new CreateDbRequest(dbName);
        JsonObject ret = vearchMasterClient.dbCreate(createDbRequest);
        System.out.println("ret = " + ret);
    }

    @Test
    public void queryDb() {
        JsonObject ret = vearchMasterClient.queryDb(dbName);
        System.out.println("ret = " + ret);
    }

    @Test
    public void deleteDb() {
        JsonObject ret = vearchMasterClient.deleteDb(dbName);
        System.out.println("ret = " + ret);
    }

    @Test
    public void listSpace() {
        JsonObject ret = vearchMasterClient.listSpace(dbName);
        System.out.println("ret = " + ret);
    }

    @Test
    public void spaceCreate() {
//        CreateSpaceRequest.Retrieval retrieval = new CreateSpaceRequest.Retrieval();
//        retrieval.setMetricType("InnerProduct");
//        retrieval.setNlinks(32);
//        retrieval.setEfConstruction(40);
//        retrieval.setNcentroids(2048);
//        retrieval.setNsubvector(32);

        CreateSpaceRequest.Engine engine = new CreateSpaceRequest.Engine();
//        engine.setIndexSize(60000);
        engine.setIndexSize(1);
        engine.setIdType("String");
        engine.setRetrievalType("HNSW");
//        engine.setRetrievalParam(retrieval);

        CreateSpaceRequest.Field field1 = new CreateSpaceRequest.Field();
        field1.setType("keyword");


        CreateSpaceRequest.Field field6 = new CreateSpaceRequest.Field();
        field6.setType("vector");
        field6.setDimension(1536);
        field6.setIndex(true);

        CreateSpaceRequest.StoreParam storeParam = new CreateSpaceRequest.StoreParam();
        storeParam.setCacheSize(512);

        CreateSpaceRequest.Field field7 = new CreateSpaceRequest.Field();
        field7.setType("vector");
        field7.setDimension(1536);
        field7.setFormat("normalization");
        field7.setStoreType("RocksDB");
        field7.setStoreParam(storeParam);
        field7.setIndex(true);

        Map<String, CreateSpaceRequest.Field> properties = new HashMap<>();
        properties.put("ts_keyword", field1);
        properties.put("ts_vector", field6);
//        properties.put("ts_vector_ext", field7);

        CreateSpaceRequest request = new CreateSpaceRequest();
        request.setName(spaceName);
        request.setPartitionNum(1);
        request.setReplicaNum(1);
        request.setEngine(engine);
        request.setProperties(properties);

        Gson gson = new Gson();
        String json = gson.toJson(request);
        System.out.println( json);
        JsonObject ret = vearchMasterClient.spaceCreate(dbName, request);
        System.out.println("ret = " + ret);
    }

    @Test
    public void querySpace() {
        JsonObject ret = vearchMasterClient.querySpace(dbName, spaceName);
        System.out.println("ret = " + ret);
    }

    @Test
    public void deleteSpace() {
        JsonObject ret = vearchMasterClient.deleteSpace(dbName, spaceName);
        System.out.println("ret = " + ret);
    }

    //TODO 未测试
    @Test
    public void updateSpaceCache() {
        UpdateSpaceCacheRequest cacheRequest = new UpdateSpaceCacheRequest();
        JsonObject ret = vearchMasterClient.updateSpaceCache(dbName, spaceName, cacheRequest);
        System.out.println("ret = " + ret);
    }

    @Test
    public void querySpaceCache() {
        JsonObject ret = vearchMasterClient.querySpaceCache(dbName, spaceName);
        System.out.println("ret = " + ret);
    }

    @Test
    public void documentUpsert() {
        Gson gson = new Gson();

        Map<String, List<Float>> map1_1 = new HashMap<>();
        map1_1.put("feature", VearchDto.get1536Feature());
        System.out.println(gson.toJson(map1_1));

        Map<String, Object> map1 = new HashMap<>();
        map1.put("_id", commonId);
        map1.put("ts_keyword", "abc");
        map1.put("ts_integer", 123);
        map1.put("ts_float", 123.23f);
        map1.put("ts_string_ary", "cbd");
        map1.put("ts_integer_index", 567);
        map1.put("ts_vector", map1_1);
//        map1.put("ts_vector_ext", map1_1);

        List<Map<String, Object>> documentsList = new ArrayList<>();
        documentsList.add(map1);

        DocumentUpsertRequest request = new DocumentUpsertRequest();
        request.setDbName(dbName);
        request.setSpaceName(spaceName);
        request.setDocuments(documentsList);

        System.out.println(gson.toJson(request));

        JsonObject ret = vearchRouterClient.documentUpsert(request);
        System.out.println("ret = " + ret);
    }


    @Test
    public void documentAdd() {
        Gson gson = new Gson();

        Map<String, List<Float>> map1_1 = new HashMap<>();
        map1_1.put("feature", VearchDto.get1536Feature());
        System.out.println(gson.toJson(map1_1));

        Map<String, Object> map1 = new HashMap<>();
        map1.put("ts_vector", map1_1);
        map1.put("ts_keyword", "test_data");

        System.out.println(gson.toJson(map1));

        JsonObject ret = vearchRouterClient.documentAdd(dbName, spaceName, commonId, map1);
        System.out.println("ret = " + ret);
    }


    @Test
    public void documentQuery() {
        Gson gson = new Gson();

        DocumentQueryRequest.Query query = new DocumentQueryRequest.Query();
        query.setDocumentIds(Collections.singletonList(commonId));
        DocumentQueryRequest request = new DocumentQueryRequest();
        request.setDbName(dbName);
        request.setSpaceName(spaceName);
        request.setQuery(query);
        request.setVectorValue(false);

        System.out.println(gson.toJson(request));
        JsonObject ret = vearchRouterClient.documentQuery(request);
        System.out.println("ret = " + ret);
    }

    @Test
    public void documentSearch() {
        Gson gson = new Gson();

        DocumentSearchRequest.FieldInt fieldInt = new DocumentSearchRequest.FieldInt();
        fieldInt.setGte(1);
        fieldInt.setLte(10000);

        Map<String, Object> range = new HashMap<>();
        range.put("ts_integer_index", fieldInt);

        DocumentSearchRequest.Filter filter = new DocumentSearchRequest.Filter();
        filter.setRange(range);

        List<DocumentSearchRequest.Filter> filterList = new ArrayList<>();
        filterList.add(filter);

        DocumentSearchRequest.Query query = new DocumentSearchRequest.Query();
        query.setDocumentIds(Collections.singletonList(commonId));
        query.setFilter(filterList);

        DocumentSearchRequest request = new DocumentSearchRequest();
        request.setDbName(dbName);
        request.setSpaceName(spaceName);
        request.setQuery(query);
        request.setSize(3);

        System.out.println("request = " + gson.toJson(request));

        JsonObject ret = vearchRouterClient.documentSearch(request);
        System.out.println("ret = " + ret);
    }

    @Test
    public void documentSearch_vearch() {
        Gson gson = new Gson();

        DocumentSearchRequest.Vector vector = new DocumentSearchRequest.Vector();
        vector.setField("ts_vector");
        vector.setMinScore(0.9);
        vector.setFeature(VearchDto.get1536Feature());

        List<DocumentSearchRequest.Vector> vectorList = new ArrayList<>();
        vectorList.add(vector);

        DocumentSearchRequest.Query query = new DocumentSearchRequest.Query();
        query.setVector(vectorList);

        DocumentSearchRequest request = new DocumentSearchRequest();
        request.setDbName(dbName);
        request.setSpaceName(spaceName);
        request.setQuery(query);
        request.setSize(3);

        System.out.println("request = " + gson.toJson(request));

        JsonObject ret = vearchRouterClient.documentSearch(request);
        System.out.println("ret = " + ret);
    }


    @Test
    public void vectorSearch() {
        Gson gson = new Gson();

        VectorSearchRequest.Vector vector = new VectorSearchRequest.Vector();
        vector.setField("ts_vector");
        vector.setMinScore(0.0);
        vector.setFeature(VearchDto.get1536Feature());

        List<VectorSearchRequest.Vector> vectorList = new ArrayList<>();
        vectorList.add(vector);

        VectorSearchRequest.Query query = new VectorSearchRequest.Query();
        query.setSum(vectorList);

        VectorSearchRequest request = new VectorSearchRequest();
        request.setQuery(query);
        request.setSize(3);

        System.out.println("request = " + gson.toJson(request));

        JsonObject ret = vearchRouterClient.vectorSearch(dbName, spaceName, request);
        System.out.println("ret = " + ret);
    }

    @Test
    public void documentDelete() {
        Gson gson = new Gson();

        DocumentDeleteRequest.Query query = new DocumentDeleteRequest.Query();
        query.setDocumentIds(Collections.singletonList(commonId));

        DocumentDeleteRequest request = new DocumentDeleteRequest();
        request.setDbName(dbName);
        request.setSpaceName(spaceName);
        request.setQuery(query);

        System.out.println(gson.toJson(request));

        JsonObject ret = vearchRouterClient.documentDelete(request);
        System.out.println("ret = " + ret);
    }

}