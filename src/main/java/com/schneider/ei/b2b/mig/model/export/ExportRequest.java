
package com.schneider.ei.b2b.mig.model.export;

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
    "PTSs",
    "Dependencies"
})
public class ExportRequest {

    @JsonProperty("Mags")
    private List<Object> mags = new ArrayList<Object>();
    @JsonProperty("Migs")
    private List<Mig> migs = new ArrayList<Mig>();
    @JsonProperty("Msgs")
    private List<Object> msgs = new ArrayList<Object>();
    @JsonProperty("PTSs")
    private List<Object> pTSs = new ArrayList<Object>();
    @JsonProperty("Dependencies")
    private List<Object> dependencies = new ArrayList<Object>();

}
