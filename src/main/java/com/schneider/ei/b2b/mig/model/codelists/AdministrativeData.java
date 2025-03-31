
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
    "CreatedBy",
    "CreatedOn",
    "ModifiedBy",
    "ModifiedOn"
})
public class AdministrativeData {

    @JsonProperty("CreatedBy")
    private String createdBy;
    @JsonProperty("CreatedOn")
    private Long createdOn;
    @JsonProperty("ModifiedBy")
    private String modifiedBy;
    @JsonProperty("ModifiedOn")
    private Long modifiedOn;

}
