
package com.schneider.ei.b2b.mig.model.codelists;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.schneider.ei.b2b.mig.model.migs.ArtifactValue;
import com.schneider.ei.b2b.mig.model.migs.Value;
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
    "BaseArtifactValue",
    "PropertyName"
})
public class ExternalCategory {

    @JsonProperty("BaseArtifactValue")
    private ArtifactValue baseArtifactValue;
    @JsonProperty("PropertyName")
    private String propertyName;

}
