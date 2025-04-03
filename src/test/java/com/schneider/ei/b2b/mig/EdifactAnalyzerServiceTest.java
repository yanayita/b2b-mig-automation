package com.schneider.ei.b2b.mig;

import com.schneider.ei.b2b.mig.model.MigAutomationException;
import com.schneider.ei.b2b.mig.model.process.AnalysisResults;
import com.schneider.ei.b2b.mig.model.process.QualifierMarkerData;
import com.schneider.ei.b2b.mig.service.EdifactAnalyzerService;
import com.schneider.ei.b2b.mig.service.MigQualificationService;
import com.schneider.ei.b2b.mig.service.MigUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@SpringBootTest
public class EdifactAnalyzerServiceTest {

    @Autowired
    private EdifactAnalyzerService edifactAnalyzerService;

    @Autowired
    private MigQualificationService migQualificationService;

    @Autowired
    private MigUtils migUtils;

    @Test
    public void testDetermineEdifactVersion() throws MigAutomationException {
        edifactAnalyzerService.determineEdifactVersion("./src/main/resources/20241217_00000021._IE.20241118105810");
    }

    @Test
    public void testDetermineEdifactVersionMultiple() throws MigAutomationException {
        List<Path> files =  this.migUtils.listFilesUsingFileWalk("./TOP50Customers/Partners");
        edifactAnalyzerService.determineEdifactVersionMultiple(files);
    }

    @Test
    public void testParseEdifactFiles() throws MigAutomationException {
        Set<QualifierMarkerData> qualifyingXpaths = migQualificationService.getQualifierXPaths("./src/test/resources/test_01/MIG_input.json");
        AnalysisResults results = edifactAnalyzerService.parseEdifactFiles("./TOP50Customers/Partners/ADALBERT ZAJADACZ/ORDERS", qualifyingXpaths);
        System.out.println(results);
    }
}
