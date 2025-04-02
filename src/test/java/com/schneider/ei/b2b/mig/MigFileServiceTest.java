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
        String zipPath = "./src/test/resources/test_01/SAP_IA_MIGs_Export_ORDERS_96A.zip";
        migFileService.processMigZip(zipPath, ediSamplesPath);
    }

    @Test
    public void testProcessMigZipORDRSP() throws MigAutomationException {
        String ediSamplesPath = "./TOP50Customers/Partners/ADALBERT ZAJADACZ/ORDRSP";
        String zipPath = "./src/test/resources/test_01/SAP_IA_MIGs_Export_ORDRSP_96A.zip";
        migFileService.processMigZip(zipPath, ediSamplesPath);
    }

    @Test
    public void testProcessMigZipINVOIC() throws MigAutomationException {
        String ediSamplesPath = "./TOP50Customers/Partners/ADALBERT ZAJADACZ/INVOIC";
        String zipPath = "./src/test/resources/test_01/SAP_IA_MIGs_Export_INVOIC_96A.zip";
        migFileService.processMigZip(zipPath, ediSamplesPath);
    }
    @Test
    public void testProcessMigZipDESADV() throws MigAutomationException {
        String ediSamplesPath = "./TOP50Customers/Partners/ADALBERT ZAJADACZ/DESADV";
        String zipPath = "./src/test/resources/test_01/SAP_IA_MIGs_Export_DESADV_96A.zip";
        migFileService.processMigZip(zipPath, ediSamplesPath);
    }
}
