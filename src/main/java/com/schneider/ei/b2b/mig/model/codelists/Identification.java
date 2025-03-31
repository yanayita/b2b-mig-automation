
package com.schneider.ei.b2b.mig.model.codelists;

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
    "VersionMode",
    "Category",
    "Id",
    "TypeSystemId",
    "TypeSystemAcronym",
    "VersionId",
    "IsLatestVersion",
    "Revision"
})
public class Identification {

    @JsonProperty("VersionMode")
    private String versionMode;
    @JsonProperty("Category")
    private String category;
    @JsonProperty("Id")
    private String id;
    @JsonProperty("TypeSystemId")
    private String typeSystemId;
    @JsonProperty("TypeSystemAcronym")
    private String typeSystemAcronym;
    @JsonProperty("VersionId")
    private String versionId;
    @JsonProperty("IsLatestVersion")
    private Boolean isLatestVersion;
    @JsonProperty("Revision")
    private Integer revision;

}
