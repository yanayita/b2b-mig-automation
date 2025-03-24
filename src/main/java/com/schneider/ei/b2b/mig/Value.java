
package com.schneider.ei.b2b.mig;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Value {

    @JsonProperty("BaseArtifactValue")
    private ArtifactValue baseArtifactValue;
    @JsonProperty("PropertyName")
    private String propertyName;
    @JsonProperty("ArtifactValue")
    private ArtifactValue artifactValue;
    @JsonProperty("PropertyDataType")
    private String propertyDataType;
    @JsonProperty("Name")
    private Value name;
    @JsonProperty("NumberOfNotes")
    private Integer numberOfNotes;
    @JsonProperty("Notes")
    private List<?> notes;
    @JsonProperty("Definition")
    private Value definition;
}
