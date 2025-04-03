
package com.schneider.ei.b2b.mig.model.manifest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.schneider.ei.b2b.mig.model.codelists.ArtifactMetadata;
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
    "Value",
    "CreatedOn",
    "CreatedBy",
    "Tenant"
})
public class Manifest {

    @JsonProperty("ArtifactMetadata")
    private ArtifactMetadata artifactMetadata;
    @JsonProperty("Value")
    private Value value;
    @JsonProperty("CreatedOn")
    private Long createdOn;
    @JsonProperty("CreatedBy")
    private String createdBy;
    @JsonProperty("Tenant")
    private String tenant;

}
