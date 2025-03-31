
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
    "TypeSystemId",
    "VersionId",
    "VersionMode",
    "Properties"
})
public class CodelistReference {

    @JsonProperty("VertexGUID")
    private String vertexGUID;
    @JsonProperty("Id")
    private String id;
    @JsonProperty("TypeSystemId")
    private String typeSystemId;
    @JsonProperty("VersionId")
    private String versionId;
    @JsonProperty("VersionMode")
    private String versionMode;
    @JsonProperty("Properties")
    private Properties properties;

}
