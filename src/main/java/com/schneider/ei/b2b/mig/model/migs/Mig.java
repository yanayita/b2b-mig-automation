package com.schneider.ei.b2b.mig.model.migs;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.schneider.ei.b2b.mig.model.codelists.AdministrativeData;
import com.schneider.ei.b2b.mig.model.codelists.ArtifactMetadata;
import com.schneider.ei.b2b.mig.model.codelists.Code;
import com.schneider.ei.b2b.mig.model.codelists.Documentation;
import com.schneider.ei.b2b.mig.model.codelists.Identification;
import com.schneider.ei.b2b.mig.model.codelists.Properties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Mig {
    @JsonProperty("ArtifactMetadata")
    private ArtifactMetadata artifactMetadata;
    @JsonProperty("Identification")
    private Map<String, String> identification;
    @JsonProperty("EnvelopeIdentification")
    private Object envelopeIdentification;
    @JsonProperty("EnvelopeVertexGUID")
    private String envelopeVertexGUID;
    @JsonProperty("HasEnvelope")
    private Boolean hasEnvelope;
    @JsonProperty("EnvelopeProperties")
    private Object envelopeProperties;
    @JsonProperty("IDProperties")
    private Object idProperties;
    @JsonProperty("EnvelopeIDProperties")
    private Object envelopeIDProperties;
    @JsonProperty("Direction")
    private String direction;
    @JsonProperty("MessageRootDomainGuid")
    private String messageRootDomainGuid;
    @JsonProperty("EnvelopeRootDomainGuid")
    private String envelopeRootDomainGuid;
    @JsonProperty("MessageRootNodeParentXPath")
    private String messageRootNodeParentXPath;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("OwnBusinessContext")
    private List<Object> ownBusinessContext;
    @JsonProperty("PartnerBusinessContext")
    private List<Object> partnerBusinessContext;
    @JsonProperty("MessageTemplate")
    private Object messageTemplate;
    @JsonProperty("EnvelopeTemplate")
    private Object envelopeTemplate;
    @JsonProperty("RootNodeNSMigrated")
    private Boolean rootNodeNSMigrated;
    @JsonProperty("RuntimeContext")
    private List<Object> runtimeContext;
    @JsonProperty("VertexGUID")
    private String VertexGUID;
    @JsonProperty("AdministrativeData")
    private AdministrativeData administrativeData;
    @JsonProperty("Documentation")
    private Object documentation;
    @JsonProperty("Properties")
    private Object properties;
    @JsonProperty("ComplexProperties")
    private List<Object> complexProperties;
    @JsonProperty("XmlNamespaces")
    private List<Object> XmlNamespaces;
    @JsonProperty("Nodes")
    private List<Node> nodes;
    @JsonProperty("ComplexTypes")
    private Map<String, ComplexType> complexTypes;
    @JsonProperty("SimpleTypes")
    private Map<String, ComplexType> simpleTypes;
    @JsonProperty("STLocalCodelists")
    private Object stLocalCodelists;
    @JsonProperty("DocumentationArtifacts")
    private Map<String, String> documentationArtifacts;
    @JsonProperty("qualifiers")
    private List<QualifierPath> qualifiers;
    @JsonProperty("NamespaceHashes")
    private Object namespaceHashes;
}
