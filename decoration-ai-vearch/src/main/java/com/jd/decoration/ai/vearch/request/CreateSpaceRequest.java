package com.jd.decoration.ai.vearch.request;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;


@Getter
@Setter
public class CreateSpaceRequest {
    private String name;

    @SerializedName(value = "partition_num")
    private Integer partitionNum;

    @SerializedName(value = "replica_num")
    private Integer replicaNum;

    private Engine engine;

    private Map<String, Field> properties;

    @Getter
    @Setter
    public static class Engine{
        @SerializedName(value = "index_size")
        private Integer indexSize;
        @SerializedName(value = "id_type")
        private String idType;
        @SerializedName(value = "retrieval_type")
        private String retrievalType;
        @SerializedName(value = "retrieval_param")
        private Retrieval retrievalParam;
    }

    @Getter
    @Setter
    public static class Retrieval {
        @SerializedName(value = "metric_type")
        private String metricType;
        private Integer ncentroids;
        private Integer nsubvector;
        @SerializedName(value = "Nlinks")
        private Integer nlinks;
        @SerializedName(value = "EfConstruction")
        private Integer efConstruction;
    }

    @Getter
    @Setter
    public static class Field{
        private String type;
        private Integer dimension;
        private String format;
        private Boolean index;
        private Boolean array;
        @SerializedName(value = "store_type")
        private String storeType;
        @SerializedName(value = "store_param")
        private StoreParam storeParam;
    }

    @Getter
    @Setter
    public static class StoreParam{
        @SerializedName(value = "cache_size")
        private Integer cacheSize;
    }
}
