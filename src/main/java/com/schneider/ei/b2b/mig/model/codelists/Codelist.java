
package com.schneider.ei.b2b.mig.model.codelists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    "ArtifactMetadata",
    "Identification",
    "VertexGUID",
    "AdministrativeData",
    "Documentation",
    "Properties",
    "ComplexProperties",
    "NumberOfCodeValues",
    "Codes",
    "DocumentationArtifacts"
})
public class Codelist {

    @JsonProperty("ArtifactMetadata")
    private ArtifactMetadata artifactMetadata;
    @JsonProperty("Identification")
    private Identification identification;
    @JsonProperty("VertexGUID")
    private String vertexGUID;
    @JsonProperty("AdministrativeData")
    private AdministrativeData administrativeData;
    @JsonProperty("Documentation")
    private Documentation documentation;
    @JsonProperty("Properties")
    private Properties properties;
    @JsonProperty("ComplexProperties")
    private List<Object> complexProperties = new ArrayList<>();
    @JsonProperty("NumberOfCodeValues")
    private Integer numberOfCodeValues;
    @JsonProperty("Codes")
    private List<Code> codes = new ArrayList<>();
    @JsonProperty("DocumentationArtifacts")
    private Map<String, String> documentationArtifacts;

}
