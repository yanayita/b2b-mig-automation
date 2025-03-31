
package com.schneider.ei.b2b.mig.model.migs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Properties {

    @JsonProperty("IsModifiable")
    private Value isModifiable;
    @JsonProperty("VersionMode")
    private Value versionMode;
    @JsonProperty("IsSelected")
    private Value isSelected;
    @JsonProperty("AllCodeValuesSelected")
    private Value allCodeValuesSelected;
    @JsonProperty("ExternalCategory")
    private Value externalCategory;
    @JsonProperty("Usage")
    private Value usage;
    @JsonProperty("Position")
    private Value position;
    @JsonProperty("MinOccurs")
    private Value minOccurs;
    @JsonProperty("SequenceNumber")
    private Value sequenceNumber;
    @JsonProperty("MaxOccurs")
    private Value maxOccurs;
    @JsonProperty("QualifyingStatus")
    private Value qualifyingStatus;
    @JsonProperty("Level")
    private Value level;
    @JsonProperty("Tag")
    private Value tag;
    @JsonProperty("AllValuesSelected")
    private Value allValuesSelected;
    @JsonProperty("IsMessageLevel")
    private Value isMessageLevel;
}
