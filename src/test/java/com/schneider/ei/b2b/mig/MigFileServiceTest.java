package com.schneider.ei.b2b.mig;

import com.schneider.ei.b2b.mig.model.MigAutomationException;
import com.schneider.ei.b2b.mig.service.MigFileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class MigFileServiceTest {

    @Autowired
    private MigFileService migFileService;

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
        migFileService.processMigZip(zipPath, ediSamplesPath);

        //ediSamplesPath = "./TOP50Customers/Partners/WINDMOELLER & HOELSCHER KG/ORDRSP";
        //zipPath = "./src/test/resources/test_01/MIG-EDI-01A WINDMOELLER-GERMANY - UNEDIFACT D.96A ORDRSP–TARGET.zip";
        //migFileService.processMigZip(zipPath, ediSamplesPath);

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
}
