
package com.schneider.ei.b2b.mig.model.manifest;

import java.util.ArrayList;
import java.util.List;
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
    "Mags",
    "Migs",
    "Msgs",
    "GCVMs",
    "PTSs",
    "CCLs"
})
public class Value {

    @JsonProperty("Mags")
    private List<Object> mags = new ArrayList<>();
    @JsonProperty("Migs")
    private List<Mig> migs = new ArrayList<>();
    @JsonProperty("Msgs")
    private List<Object> msgs = new ArrayList<>();
    @JsonProperty("GCVMs")
    private List<Object> gCVMs = new ArrayList<>();
    @JsonProperty("PTSs")
    private List<Object> pTSs = new ArrayList<>();
    @JsonProperty("CCLs")
    private List<Object> cCLs = new ArrayList<>();

}
