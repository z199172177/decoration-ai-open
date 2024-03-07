package com.jd.decoration.ai.vearch.request;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DocumentUpsertRequest {
    @SerializedName(value = "db_name")
    private String dbName;
    @SerializedName(value = "space_name")
    private String spaceName;
    private List<Map<String, Object>> documents;
}
