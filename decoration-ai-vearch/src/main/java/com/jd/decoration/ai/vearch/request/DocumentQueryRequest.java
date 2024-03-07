package com.jd.decoration.ai.vearch.request;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DocumentQueryRequest {
    @SerializedName(value = "db_name")
    private String dbName;
    @SerializedName(value = "space_name")
    private String spaceName;
    @SerializedName(value = "vector_value")
    private Boolean vectorValue;
    private Query query;

    @Getter
    @Setter
    public static class Query {

        @SerializedName(value = "document_ids")
        private List<String> documentIds;

        @SerializedName(value = "partition_id")
        private String partitionId;
    }
}
