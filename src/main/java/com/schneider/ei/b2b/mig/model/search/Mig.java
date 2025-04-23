package com.schneider.ei.b2b.mig.model.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class Mig {

    @JsonProperty("MIGGUID")
    private String migGuid;

    @JsonProperty("ObjectGUID")
    private String objectGUID;

    @JsonProperty("Documentation")
    private Documentation documentation;
}
