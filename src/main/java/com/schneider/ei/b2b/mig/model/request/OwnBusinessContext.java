
package com.schneider.ei.b2b.mig.model.request;

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
public class OwnBusinessContext {

    @JsonProperty("contextType")
    private String contextType;
    @JsonProperty("name")
    private String name;
    @JsonProperty("codeListId")
    private String codeListId;
    @JsonProperty("contextValues")
    private List<ContextValue> contextValues = new ArrayList<>();

}
