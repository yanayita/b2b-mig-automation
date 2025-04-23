package com.schneider.ei.b2b.mig.model.migs;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ComplexType {
    @JsonProperty("VertexGUID")
    private String vertexGUID;
    @JsonProperty("Id")
    private String id;
    @JsonProperty("IsLocal")
    private Boolean isLocal;
    @JsonProperty("Documentation")
    private Documentation documentation;
    @JsonProperty("Properties")
    private Properties properties;
    @JsonProperty("FacetProperties")
    private Properties facetProperties;
    @JsonProperty("ComplexProperties")
    private List<?> complexProperties;
    @JsonProperty("NumberOfCodelistReferences")
    private Integer numberOfCodelistReferences;
    @JsonProperty("CodelistReferences")
    private List<CodelistReference> codelistReferences;
}
