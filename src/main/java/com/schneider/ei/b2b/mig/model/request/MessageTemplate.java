
package com.schneider.ei.b2b.mig.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.schneider.ei.b2b.mig.model.migs.Documentation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MessageTemplate {

    @JsonProperty("TypeSystemId")
    private String typeSystemId;
    @JsonProperty("TypeSystemDisplayId")
    private String typeSystemDisplayId;
    @JsonProperty("VersionId")
    private String versionId;
    @JsonProperty("VersionDisplayId")
    private String versionDisplayId;
    @JsonProperty("Revision")
    private Integer revision;
    @JsonProperty("Id")
    private String id;
    @JsonProperty("DisplayId")
    private String displayId;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("IsCustomMessage")
    private Boolean isCustomMessage;
    @JsonProperty("ObjectGUID")
    private String objectGUID;
    @JsonProperty("IsCustomObject")
    private Boolean isCustomObject;
    @JsonProperty("Tag")
    private String tag;
    @JsonProperty("TypeSystemAcronym")
    private String typeSystemAcronym;
    @JsonProperty("Documentation")
    private Documentation documentation;
    @JsonProperty("DisplayTag")
    private String displayTag;
    @JsonProperty("VersionAcronym")
    private String versionAcronym;


}
