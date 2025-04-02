package com.schneider.ei.b2b.mig.model.process;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class AnalysisResults {
    private Map<QualifierMarkerData, Set<String>> qualifierXPathsFound;
    private Set<QualifierMarkerData> selectedXPathsFound;
}
