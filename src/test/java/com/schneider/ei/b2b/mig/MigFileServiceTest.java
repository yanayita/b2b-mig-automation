package com.schneider.ei.b2b.mig;

import com.schneider.ei.b2b.mig.model.MigAutomationException;
import com.schneider.ei.b2b.mig.model.search.Mig;
import com.schneider.ei.b2b.mig.model.search.SearchResult;
import com.schneider.ei.b2b.mig.service.EdifactAnalyzerService;
import com.schneider.ei.b2b.mig.service.MigCreationService;
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
import java.util.stream.Collectors;


@SpringBootTest
@Slf4j
public class MigFileServiceTest {

    @Autowired
    private MigFileService migFileService;

    @Autowired
    private MigUtils migUtils;

    @Autowired
    private EdifactAnalyzerService edifactAnalyzerService;

    @Autowired
    private MigCreationService migCreationService;

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
                "./TOP50Customers/Partners/RS COMPONENTS/ORDERS");
    }

    @Test
    public void testProcessMigZipMultiple2() throws MigAutomationException {
        String ediSamplesPath = "./TOP50Customers/Partners/WINDMOELLER & HOELSCHER KG/ORDERS";
        String zipPath = "./src/test/resources/test_01/MIG-EDI-01A WINDMOELLER-GERMANY - UNEDIFACT D.96A ORDERS–SOURCE.zip";
        //migFileService.processMigZip(zipPath, ediSamplesPath);

        ediSamplesPath = "./TOP50Customers/Partners/EMIL LOEFFELHARDT/ORDERS";
        zipPath = "./output/MIG-EDI-01A EMIL LOEFFELHARDT-GERMANY - UNEDIFACT D.96A ORDERS–SOURCE (ED).zip";
        migFileService.processMigZip(zipPath, ediSamplesPath);
    }

    @Test
    public void testProcessPartnerFolder() throws MigAutomationException {
        List<String> folders = Arrays.asList(
        //        "ADALBERT ZAJADACZ",
        //        "ALEXANDER BUERKLE",
        //        "BLUMENBECKER AUTOMATISIERUNGS",
        //        "CARL METTLER",
        //        "CARL METTLER GMBH",
        //        "CARL METTLER S.A R.L",
        //        "CL. BERGMANN GMBH",
        //        "EFG GIENGER KG",
        //        "EFG RHEINLAND",
        //        "EFG SACHSEN KG",
        //        "EGU ELEKTRO GROSSHANDELS UNION",
        //        "ELEKTRO SEIWERT(FEGIME)",
        //       "EMIL LOEFFELHARDT",
        //       "ERNST GRANZOW GMBH",
        //       "FAMO GMBH",
        //       "FEGA & SCHMITT ANSBACH",
        //       "FISCHER-J.W.ZANDER",
        //       "FRITZ KRIEGER",
        //       "GEA FARM TECHNOLOGIES",
        //       "GEBHARDT FÖRDERTECHNIK GMBH",
        //       "GEBR. EBERHARD",
        //       "GÄFGEN ELEKTROGROßHANDEL",
        //        "H. GAUTZSCH ELEKTRO",
                "H. GAUTZSCH GROßHANDEL",
                "H. GAUTZSCH GROßHANDEL BAYERN",
                "HARDY SCHMITZ",
                "HARRO HÖFLIGER",
                "HERMANN WALDNER",
                "HILLMANN & PLOOG",
                "LIFTKET HOFFMANN",
                "LOGISTIK- UND DIENSTLEISTUNGS",
                "LUDZ MITTE GMBH",
                "OSKAR BÖTTCHER",
                "REXEL-GERMANY",
                "RS COMPONENTS",
                "UHLMANN PAC-SYSTEME",
                "UNI ELEKTRO FACHGROSSHANDEL",
                "Weber Food Technology",
                "WILHELM RINK",
                "WINDMOELLER & HOELSCHER KG",
                "YESSS ELEKTROFACHGROßHANDLUNG");

        migFileService.processPartnerFolders("./TOP50Customers/Partners/", folders, "GERMANY");
        migFileService.mergeAllOutput();
    }

    @Test
    public void mergeAllOutput() throws MigAutomationException {
        migFileService.mergeAllOutput();
    }

    @Test
    public void deleteTestMigs() {
        List<SearchResult> searchResults = migCreationService.findRemoteMigs();
        List<Mig> foundMigs = searchResults.get(0).getMigs().stream()
                .filter(item -> item.getDocumentation().getName().getArtifactValue().getId().endsWith("(ED)"))
                .toList();
        for (Mig mig : foundMigs) {
            migCreationService.deleteRemoteMig(mig.getMigGuid());
        }
    }

}
