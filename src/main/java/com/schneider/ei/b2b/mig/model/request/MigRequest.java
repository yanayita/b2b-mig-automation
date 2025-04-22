
package com.schneider.ei.b2b.mig.model.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
public class MigRequest {

    @JsonProperty("Identification")
    private Identification identification;
    @JsonProperty("Direction")
    private String direction;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("Documentation")
    private Documentation documentation;
    @JsonProperty("MessageTemplate")
    private MessageTemplate messageTemplate;
    @JsonProperty("EnvelopeTemplate")
    private MessageTemplate envelopeTemplate;
    @JsonProperty("OwnBusinessContext")
    private List<OwnBusinessContext> ownBusinessContext = new ArrayList<>();
    @JsonProperty("PartnerBusinessContext")
    private List<Object> partnerBusinessContext = new ArrayList<>();
    @JsonProperty("DocumentationArtifacts")
    private Map<String, String> documentationArtifacts;

}
