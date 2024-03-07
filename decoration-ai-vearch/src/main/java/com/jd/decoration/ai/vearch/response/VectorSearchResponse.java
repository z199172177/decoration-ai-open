package com.jd.decoration.ai.vearch.response;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Deprecated
public class VectorSearchResponse {

    private int took;
    @SerializedName("timed_out")
    private Boolean timedOut;
    @SerializedName("_shards")
    private Shards shards;
    private Hit hits;

    @Getter
    @Setter
    public static class Shards {
        private Integer total;
        private Integer successful;
    }

    @Getter
    @Setter
    public static class Hit {
        private Integer total;
        @SerializedName("max_score")
        private Double maxScore;
        private List<HitDetail> hits;
    }


    @Getter
    @Setter
    public static class HitDetail {

        @SerializedName("_index")
        private String index;
        @SerializedName("_type")
        private String type;
        @SerializedName("_id")
        private String id;
        @SerializedName("_score")
        private Double score;
        @SerializedName("_source")
        private Map<String, Object> source;
    }


}
