
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
    "ExternalCategory"
})
public class Properties {

    @JsonProperty("ExternalCategory")
    private ExternalCategory externalCategory;

}
