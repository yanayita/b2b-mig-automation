package com.schneider.ei.b2b.mig.model.export;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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

public class MigMetadata {

    @JsonProperty("ArtifactMetadata")
    private ArtifactMetadata artifactMetadata;
    @JsonProperty("value")
    private Mig value;
}
