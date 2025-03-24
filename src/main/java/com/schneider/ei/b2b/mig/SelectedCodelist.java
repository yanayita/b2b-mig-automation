
package com.schneider.ei.b2b.mig;

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
    "CodelistReference",
    "SelectedCodes"
})
public class SelectedCodelist {

    @JsonProperty("CodelistReference")
    private CodelistReference codelistReference;
    @JsonProperty("SelectedCodes")
    private List<SelectedCode> selectedCodes = new ArrayList<SelectedCode>();

}
