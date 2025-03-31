
package com.schneider.ei.b2b.mig.model.migs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "VertexGUID",
    "Id",
    "LanguageCode",
    "action"
})
public class ArtifactValue {

    @JsonProperty("VertexGUID")
    private String vertexGUID;
    @JsonProperty("Id")
    private String id;
    @JsonProperty("LanguageCode")
    private String languageCode;
    @JsonProperty("action")
    private String action;

}
