package com.schneider.ei.b2b.mig;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schneider.ei.b2b.mig.model.MigAutomationException;
import com.schneider.ei.b2b.mig.model.export.ExportRequest;
import com.schneider.ei.b2b.mig.model.export.MigMetadata;
import com.schneider.ei.b2b.mig.model.manifest.Manifest;
import com.schneider.ei.b2b.mig.model.request.MigRequest;
import com.schneider.ei.b2b.mig.model.request.MigResponse;
import com.schneider.ei.b2b.mig.service.MigCreationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

@SpringBootTest
public class MigCreationServiceTest {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MigCreationService migCreationService;

    @Test
    public void testMigParsing() throws IOException {
        // Add your test logic here
        InputStream inputStream = new FileInputStream("./src/main/resources/b2b-payload/MigRequest.json");
        MigRequest migRequest = mapper.readValue(inputStream, MigRequest.class);
        System.out.println(migRequest);
    }

    @Test
    public void testCreateMigRequest() throws JsonProcessingException, MigAutomationException {
        MigRequest migRequest = this.migCreationService.createMigRequest("ORDERS", "D.96A S3", "ED_Test");
        String migRequestString = this.mapper.writeValueAsString(migRequest);
        System.out.println(migRequestString);
    }

    @Test
    public void testCreateRemoteMig() throws MigAutomationException {
        MigRequest migRequest = this.migCreationService.createMigRequest("ORDERS", "D.96A S3", "ED_Test");
        MigResponse migResponse = this.migCreationService.createRemoteMig(migRequest);
        System.out.println(migResponse);
    }

    @Test
    public void testGetMigMetadata() {
        MigMetadata migMetadata = this.migCreationService.getMigMetadata("1af79f63bdbb4e63affc0ae7bdbc3941");
        System.out.println(migMetadata);
    }

    @Test
    public void testExportMig() {
        MigMetadata migMetadata = this.migCreationService.getMigMetadata("1af79f63bdbb4e63affc0ae7bdbc3941");
        ExportRequest exportRequest = ExportRequest.builder()
                .migs(Collections.singletonList(migMetadata.getValue()))
                .build();
        byte[] export = this.migCreationService.exportData(exportRequest);
        System.out.println(export);
    }

    @Test
    public void testCreateAndExportMig() throws MigAutomationException {
        File exportFile = this.migCreationService.createAndExportMig("ORDERS", "D.96A S3", "ED_Test2", true);
        System.out.println(exportFile);
    }
}
