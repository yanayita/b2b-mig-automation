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
    public void testGetZipContents() throws IOException, MigAutomationException {
        String ediSamplesPath = "./TOP50Customers/Partners/ADALBERT ZAJADACZ/ORDERS";
        String zipPath = "./src/test/resources/test_01/SAP_IA_MIGs_Export_2025-03-26T13-49-26.zip";
        migFileService.processMigZip(zipPath, ediSamplesPath);
    }
}
