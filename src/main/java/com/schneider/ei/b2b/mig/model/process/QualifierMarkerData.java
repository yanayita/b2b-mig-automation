package com.schneider.ei.b2b.mig.model.process;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class QualifierMarkerData {
    @EqualsAndHashCode.Include
    private String domainXpath;
    private String qualifyingXpath;
    private String qualifyingRelativeXpath;
    private boolean isQualifier;
}
