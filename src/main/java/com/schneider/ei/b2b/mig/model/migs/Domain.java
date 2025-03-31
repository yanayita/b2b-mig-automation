
package com.schneider.ei.b2b.mig.model.migs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "DomainGUID",
    "AlternativeDomainGUID",
    "ParentDomainGUID",
    "XPath",
    "UnqualifiedDomainGUID"
})
public class Domain {

    @JsonProperty("DomainGUID")
    private String domainGUID;
    @JsonProperty("AlternativeDomainGUID")
    private String alternativeDomainGUID;
    @JsonProperty("ParentDomainGUID")
    private String parentDomainGUID;
    @JsonProperty("XPath")
    private String xPath;
    @JsonProperty("UnqualifiedDomainGUID")
    private String unqualifiedDomainGUID;

}
