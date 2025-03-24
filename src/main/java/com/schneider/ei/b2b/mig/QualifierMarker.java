
package com.schneider.ei.b2b.mig;

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
    "QualifyingNodeVertexGUID",
    "CodelistReferenceVertexGUID",
    "QualifierType",
    "RelativeXPath"
})
public class QualifierMarker {

    @JsonProperty("VertexGUID")
    private String vertexGUID;
    @JsonProperty("Id")
    private String id;
    @JsonProperty("QualifyingNodeVertexGUID")
    private String qualifyingNodeVertexGUID;
    @JsonProperty("CodelistReferenceVertexGUID")
    private String codelistReferenceVertexGUID;
    @JsonProperty("QualifierType")
    private String qualifierType;
    @JsonProperty("RelativeXPath")
    private String relativeXPath;

}
