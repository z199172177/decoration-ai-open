package com.jd.decoration.ai.vearch.request;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class VectorSearchRequest {
    @SerializedName(value = "db_name")
    private String dbName;
    @SerializedName(value = "space_name")
    private String spaceName;
    private Integer size;
    private Query query;
    @SerializedName(value = "retrieval_param")
    private RetrievalParam retrievalParam;
    @SerializedName(value = "vector_value")
    private Boolean vectorValue;
    @Getter
    @Setter
    public static class FieldInt {
        private Integer gte;
        private Integer gt;
        private Integer lte;
        private Integer lt;
    }


    @Getter
    @Setter
    public static class Filter {
        private Map<String, Object> range;
    }

    @Getter
    @Setter
    public static class Query {
        @SerializedName(value = "document_ids")
        private List<String> documentIds;
        private List<Filter> filter;
        private List<Vector> vector;
        private List<Vector> sum;
    }

    @Getter
    @Setter
    public static class RetrievalParam {
        @SerializedName(value = "metric_type")
        private String metricType;
    }

    @Getter
    @Setter
    public static class Vector{
        private String field;
        private List<Float> feature;
        @SerializedName(value = "min_score")
        private Double minScore;
    }
}
