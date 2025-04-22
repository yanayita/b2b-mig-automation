
package com.schneider.ei.b2b.mig.model.export;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.schneider.ei.b2b.mig.model.codelists.AdministrativeData;
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
    "ObjectGUID",
    "MIGGUID",
    "ImportCorrelationObjectId",
    "ImportCorrelationGroupId",
    "VersionId",
    "Name",
    "Status",
    "HasEnvelope",
    "MessageTemplate",
    "EnvelopeTemplate",
    "AdministrativeData"
})
public class Mig {

    @JsonProperty("ObjectGUID")
    private String objectGUID;
    @JsonProperty("MIGGUID")
    private String migguid;
    @JsonProperty("ImportCorrelationObjectId")
    private String importCorrelationObjectId;
    @JsonProperty("ImportCorrelationGroupId")
    private String importCorrelationGroupId;
    @JsonProperty("VersionId")
    private String versionId;
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("HasEnvelope")
    private Boolean hasEnvelope;
    @JsonProperty("MessageTemplate")
    private MessageTemplate messageTemplate;
    @JsonProperty("EnvelopeTemplate")
    private EnvelopeTemplate envelopeTemplate;
    @JsonProperty("AdministrativeData")
    private AdministrativeData administrativeData;

}
