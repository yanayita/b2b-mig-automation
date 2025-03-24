
package com.schneider.ei.b2b.mig;

import java.util.ArrayList;
import java.util.List;
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
    "QualifierValues",
    "QualifierNotUsed",
    "Id",
    "QualifyingNodeVertexGUID",
    "CodelistReferenceVertexGUID",
    "QualifierType",
    "RelativeXPath"
})
public class Qualifier {

    @JsonProperty("QualifierValues")
    private List<QualifierValue> qualifierValues = new ArrayList<QualifierValue>();
    @JsonProperty("QualifierNotUsed")
    private Boolean qualifierNotUsed;
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
