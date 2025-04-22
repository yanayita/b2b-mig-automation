package com.schneider.ei.b2b.mig;

import com.schneider.ei.b2b.mig.model.MigAutomationException;
import com.schneider.ei.b2b.mig.service.EdifactAnalyzerService;
import com.schneider.ei.b2b.mig.service.MigFileService;
import com.schneider.ei.b2b.mig.service.MigUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;


@SpringBootTest
@Slf4j
public class MigFileServiceTest {

    @Autowired
    private MigFileService migFileService;

    @Autowired
    private MigUtils migUtils;

    @Autowired
    private EdifactAnalyzerService edifactAnalyzerService;

    @Test
    public void testProcessMigZipORDERS() throws MigAutomationException {
        String ediSamplesPath = "./TOP50Customers/Partners/REXEL-GERMANY/ORDERS";
        String zipPath = "./src/test/resources/test_01/SAP_IA_MIGs_Export_2025-04-03T10-31-48.zip";
        migFileService.processMigZip(zipPath, ediSamplesPath);
    }

    @Test
    public void testProcessMigZipMultiple() throws MigAutomationException {
        String ediSamplesPath = "./TOP50Customers/Partners/WINDMOELLER & HOELSCHER KG/ORDERS";
        String zipPath = "./src/test/resources/test_01/MIG-EDI-01A WINDMOELLER-GERMANY - UNEDIFACT D.96A ORDERS–SOURCE.zip";
        //migFileService.processMigZip(zipPath, ediSamplesPath);

        ediSamplesPath = "./TOP50Customers/Partners/WINDMOELLER & HOELSCHER KG/ORDRSP";
        zipPath = "./src/test/resources/test_01/MIG-EDI-01A WINDMOELLER-GERMANY - UNEDIFACT D.96A ORDRSP–TARGET.zip";
        migFileService.processMigZip(zipPath, ediSamplesPath);

        //MIG-EDI-01A WINDMOELLER-GERMANY - UN/EDIFACT D.96A ORDERS–SOURCE
        //MIG-EDI-02 ELEKTRO BRAUN - UN/EDIFACT D.96A DESADV – TARGET

        /*
        String ediSamplesPath = "./TOP50Customers/Partners/RS COMPONENTS/ORDERS";
        String zipPath = "./src/test/resources/test_01/MIG-EDI-01A RS COMPONENTS-GERMANY - UNEDIFACT D.96A ORDERS–SOURCE.zip";
        migFileService.processMigZip(zipPath, ediSamplesPath);

        ediSamplesPath = "./TOP50Customers/Partners/RS COMPONENTS/ORDRSP";
        zipPath = "./src/test/resources/test_01/MIG-EDI-01A RS COMPONENTS-GERMANY - UNEDIFACT D.96A ORDRSP–TARGET.zip";
        migFileService.processMigZip(zipPath, ediSamplesPath);

        ediSamplesPath = "./TOP50Customers/Partners/RS COMPONENTS/INVOIC";
        zipPath = "./src/test/resources/test_01/MIG-EDI-01A RS COMPONENTS-GERMANY - UNEDIFACT D.97A INVOIC–TARGET.zip";
        migFileService.processMigZip(zipPath, ediSamplesPath);
         */
    }

    @Test
    public void testCreateMigAndQualify() throws MigAutomationException {
        this.migFileService.createMigAndQualify("ORDERS", "D.96A S3",
                "MIG-EDI-01A RS COMPONENTS-GERMANY - UNEDIFACT D.96A ORDERS–SOURCE (ED)_2",
                "./TOP50Customers/Partners/RS COMPONENTS/ORDERS", true);
    }

    @Test
    public void testProcessMigZipMultiple2() throws MigAutomationException {
        String ediSamplesPath = "./TOP50Customers/Partners/WINDMOELLER & HOELSCHER KG/ORDERS";
        String zipPath = "./src/test/resources/test_01/MIG-EDI-01A WINDMOELLER-GERMANY - UNEDIFACT D.96A ORDERS–SOURCE.zip";
        //migFileService.processMigZip(zipPath, ediSamplesPath);

        ediSamplesPath = "./TOP50Customers/Partners/RS COMPONENTS/ORDERS";
        zipPath = "./output/MIG-EDI-01A RS COMPONENTS-GERMANY - UNEDIFACT D.96A ORDERS–SOURCE (ED).zip";
        migFileService.processMigZip(zipPath, ediSamplesPath);
    }

    @Test
    public void testProcessPartnerFolder() throws MigAutomationException {
        List<String> folders = Arrays.asList("WINDMOELLER & HOELSCHER KG",
                "RS COMPONENTS",
                "UHLMANN PAC-SYSTEME");

        migFileService.processPartnerFolders("./TOP50Customers/Partners/", folders, "GERMANY");
        migFileService.mergeAllOutput();
    }

    @Test
    public void mergeAllOutput() throws MigAutomationException {
        migFileService.mergeAllOutput();
    }


}
