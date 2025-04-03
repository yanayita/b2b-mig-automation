package com.schneider.ei.b2b.mig.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schneider.ei.b2b.mig.model.MigAutomationException;
import com.schneider.ei.b2b.mig.model.codelists.Code;
import com.schneider.ei.b2b.mig.model.codelists.Codelist;
import com.schneider.ei.b2b.mig.model.manifest.Manifest;
import com.schneider.ei.b2b.mig.model.manifest.Mig;
import com.schneider.ei.b2b.mig.model.migs.ArtifactValue;
import com.schneider.ei.b2b.mig.model.migs.CodelistReference;
import com.schneider.ei.b2b.mig.model.migs.Domain;
import com.schneider.ei.b2b.mig.model.migs.Node;
import com.schneider.ei.b2b.mig.model.migs.NodeStatus;
import com.schneider.ei.b2b.mig.model.migs.Properties;
import com.schneider.ei.b2b.mig.model.migs.Qualifier;
import com.schneider.ei.b2b.mig.model.migs.QualifierMarker;
import com.schneider.ei.b2b.mig.model.migs.QualifierPath;
import com.schneider.ei.b2b.mig.model.migs.QualifierValue;
import com.schneider.ei.b2b.mig.model.migs.SelectedCodelist;
import com.schneider.ei.b2b.mig.model.migs.Value;
import com.schneider.ei.b2b.mig.model.process.AnalysisResults;
import com.schneider.ei.b2b.mig.model.process.QualifierMarkerData;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class MigFileService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MigUtils migUtils;

    @Autowired
    private EdifactAnalyzerService edifactAnalyzerService;

    @Autowired
    private MigQualificationService migQualificationService;

    public void processMigZip(String zipFilePath, String ediSamplesFolder) throws MigAutomationException {
        String unzippedFolder = extractZipFile(zipFilePath);
        new File(unzippedFolder).deleteOnExit();
        if (zipFilePath.endsWith("_output.zip")) {
            throw new MigAutomationException("Output zip file is not supported.");
        }
        List<Path> files = migUtils.listFilesUsingFileWalk(unzippedFolder);
        Path manifestFile = null;
        Path migFile = null;
        for (Path file : files) {
            if (file.getFileName().toString().equals("manifest.json")) {
                manifestFile = file;
            } else if (file.toFile().getParentFile().getName().equals("migs")) {
                migFile = file;
            }
        }
        if (manifestFile == null || migFile == null) {
            throw new MigAutomationException("Manifest or MIG file not found in the zip.");
        }

        long modifiedTime = System.currentTimeMillis();

        try {
            InputStream inputStream = new FileInputStream(manifestFile.toFile());
            Manifest manifest = mapper.readValue(inputStream, Manifest.class);
            inputStream.close();
            if (manifest.getValue().getMigs().size() != 1) {
                throw new MigAutomationException("Only one MIG file is supported.");
            }
            /*Mig mig = manifest.getValue().getMigs().get(0);
            mig.getAdministrativeData().setModifiedOn(modifiedTime);
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(manifestFile.toFile(), false));
            outputStream.write(mapper.writeValueAsString(manifest).getBytes());
            outputStream.close();
            */
        } catch (IOException e) {
            throw new MigAutomationException("Error reading manifest file: " + manifestFile, e);
        }

        Set<QualifierMarkerData> qualifyingXpaths = migQualificationService.getQualifierXPaths(migFile.toString());
        AnalysisResults results = edifactAnalyzerService.parseEdifactFiles(ediSamplesFolder, qualifyingXpaths);
        com.schneider.ei.b2b.mig.model.migs.Mig outputMig = migQualificationService.qualifyMig(migFile.toString(), results);
        outputMig.getAdministrativeData().setModifiedOn(modifiedTime);


        try {
            String outputMigContents = mapper.writeValueAsString(outputMig);
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(migFile.toFile(), false));
            outputStream.write(outputMigContents.getBytes());
            outputStream.close();


            String newZipFilePath = zipFilePath.replace(".zip", "_output.zip");
            ZipFile zipFile = new ZipFile(newZipFilePath);

            // Add single file
            zipFile.addFile(manifestFile.toFile());
            zipFile.addFolder(migFile.toFile().getParentFile());

        } catch (IOException ex) {
            throw new MigAutomationException("Error writing to file: " + migFile.toString(), ex);
        }

    }

    private String extractZipFile(String zipFilePath) throws MigAutomationException {
        try {
            ZipFile zipFile = new ZipFile(zipFilePath);
            String targetFolder = "./tmp/" + System.currentTimeMillis();
            // Extract to target directory
            zipFile.extractAll(targetFolder);
            return targetFolder;
        } catch (IOException e) {
            throw new MigAutomationException("Error reading file: " + zipFilePath, e);
        }
    }
}

