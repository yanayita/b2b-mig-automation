
package com.schneider.ei.b2b.mig.model.codelists;

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
    "Name",
    "Definition",
    "NumberOfNotes",
    "Notes"
})
public class Documentation {

    @JsonProperty("Name")
    private Name name;
    @JsonProperty("Definition")
    private Definition definition;
    @JsonProperty("NumberOfNotes")
    private Integer numberOfNotes;
    @JsonProperty("Notes")
    private List<Object> notes = new ArrayList<>();

}
