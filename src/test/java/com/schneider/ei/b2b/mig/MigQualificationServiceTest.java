package com.schneider.ei.b2b.mig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schneider.ei.b2b.mig.model.MigAutomationException;
import com.schneider.ei.b2b.mig.model.migs.Mig;
import com.schneider.ei.b2b.mig.model.migs.Node;
import com.schneider.ei.b2b.mig.model.migs.QualifierPath;
import com.schneider.ei.b2b.mig.model.process.AnalysisResults;
import com.schneider.ei.b2b.mig.model.process.QualifierMarkerData;
import com.schneider.ei.b2b.mig.service.EdifactAnalyzerService;
import com.schneider.ei.b2b.mig.service.MigQualificationService;
import com.schneider.ei.b2b.mig.service.MigUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

@SpringBootTest
public class MigQualificationServiceTest {

    @Autowired
    private MigQualificationService migQualificationService;

    @Autowired
    private EdifactAnalyzerService edifactAnalyzerService;

    @Autowired
    private MigUtils migUtils;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void testQualifyMig() throws IOException, MigAutomationException {
        String migFile = "./src/test/resources/test_01/MIG_input.json";
        Set<QualifierMarkerData> qualifyingXpaths = migQualificationService.getQualifierXPaths(migFile);
        AnalysisResults results = edifactAnalyzerService.parseEdifactFiles("./TOP50Customers/Partners/ADALBERT ZAJADACZ/ORDERS", qualifyingXpaths);
        migQualificationService.qualifyMig(migFile, results);
    }

    @Test
    public void testQualifyNode() throws IOException, MigAutomationException {
        // Test the qualifyNode method
        InputStream is = QualifyNodeTest.class.getClassLoader().getResourceAsStream("test_01/MIG_input.json");
        Mig rootNodeInput = mapper.readValue(is, Mig.class);
        List<Node> inputNodes = rootNodeInput.getNodes();
        for (Node nodes : inputNodes) {
            migUtils.populateParent(nodes);
        }
        LinkedHashMap<String, QualifierPath> qualiferPaths = new LinkedHashMap<>();
        rootNodeInput.getQualifiers().forEach(item -> qualiferPaths.put(item.getKey(), item));

        Node inputDTM = migUtils.findNode("/51C96EACDDBAE8ED:Interchange/ORDERS/DTM", inputNodes);

        //migQualificationService.qualifyNode(inputDTM, "137", qualiferPaths, rootNodeInput);
        System.out.println(inputDTM);
    }
}
