
package com.schneider.ei.b2b.mig;

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
    "MinLength",
    "PrimitiveType",
    "MaxLength",
    "DateTimeFormat"
})
public class BaseTypeFacetProperties {

    @JsonProperty("MinLength")
    private Value minLength;
    @JsonProperty("PrimitiveType")
    private Value primitiveType;
    @JsonProperty("MaxLength")
    private Value maxLength;
    @JsonProperty("DateTimeFormat")
    private Value dateTimeFormat;

}
