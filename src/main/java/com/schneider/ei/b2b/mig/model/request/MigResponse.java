
package com.schneider.ei.b2b.mig.model.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.schneider.ei.b2b.mig.model.migs.Documentation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MigResponse {

    @JsonProperty("migguid")
    private String migguid;
    @JsonProperty("responseCode")
    private MigResponseCode responseCode;
    @JsonProperty("id")
    private String id;

}
