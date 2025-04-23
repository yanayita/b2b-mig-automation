package com.schneider.ei.b2b.mig.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zafarkhaja.semver.Version;
import com.schneider.ei.b2b.mig.model.MigAutomationException;
import com.schneider.ei.b2b.mig.model.manifest.Manifest;
import com.schneider.ei.b2b.mig.model.manifest.Mig;
import com.schneider.ei.b2b.mig.model.process.AnalysisResults;
import com.schneider.ei.b2b.mig.model.process.QualifierMarkerData;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
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

    @Autowired
    private MigCreationService migCreationService;

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
        String newVersion;
        try {
            InputStream inputStream = new FileInputStream(manifestFile.toFile());
            Manifest manifest = mapper.readValue(inputStream, Manifest.class);
            inputStream.close();
            if (manifest.getValue().getMigs().size() != 1) {
                throw new MigAutomationException("Only one MIG file is supported.");
            }
            Mig mig = manifest.getValue().getMigs().get(0);
            String migVersion  = mig.getVersionId();
            Version v = Version.parse(migVersion, false);
            newVersion = v.majorVersion() + "." +  (v.minorVersion() + 1);
            mig.setVersionId(newVersion);
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(manifestFile.toFile(), false));
            outputStream.write(mapper.writeValueAsString(manifest).getBytes());
            outputStream.close();

        } catch (IOException e) {
            throw new MigAutomationException("Error reading manifest file: " + manifestFile, e);
        }

        Set<QualifierMarkerData> qualifyingXpaths = migQualificationService.getQualifierXPaths(migFile.toString());
        AnalysisResults results = edifactAnalyzerService.parseEdifactFiles(ediSamplesFolder, qualifyingXpaths);
        com.schneider.ei.b2b.mig.model.migs.Mig outputMig = migQualificationService.qualifyMig(migFile.toString(), results);
        outputMig.getAdministrativeData().setModifiedOn(modifiedTime);
        Map<String, String> identification = outputMig.getIdentification();
        identification.put("MIGVersion", newVersion);

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

    public void createMigAndQualify(String messageType, String versionId, String migName, String ediSamplesFolder) throws MigAutomationException {
        File exportFile = this.migCreationService.createAndExportMig(messageType, versionId, migName);
        this.processMigZip(exportFile.getPath(), ediSamplesFolder);
    }

    public void processPartnerFolders(String rootFolder, List<String> folders, String country) {
        for (String folder : folders) {
            processPartnerFolder(rootFolder + "/" + folder, country);
        }
    }

    public void processPartnerFolder(String folder, String country) {
        File[] directories = new File(folder).listFiles(File::isDirectory);
        for (File directory : directories) {
            try {
                processDirectory(directory, country);
            } catch (MigAutomationException ex) {
                log.error("Error processing directory: " + directory.getPath(), ex);
            }
        }
    }

    private void processDirectory(File directory, String country) throws MigAutomationException {
        List<Path> files =  this.migUtils.listFilesUsingFileWalk(directory.getPath());
        String version = this.edifactAnalyzerService.determineEdifactVersionMultiple(files);
        String versionName = version.toUpperCase().replaceAll("D", "D.");
        String migName = generateMigName(directory.getParentFile().getName(), country, versionName, directory.getName());
        log.info("Starting to process directory: " + directory.getPath());
        createMigAndQualify(directory.getName(), versionName + " S3",
                migName,
                directory.getPath());

    }

    private String generateMigName(String customerName, String country, String version, String messageType) throws MigAutomationException {
        String direction;
        String scenario;
        switch (messageType) {
            case "ORDERS":
                direction = "SOURCE";
                scenario = "01A";
                break;
            case "ORDRSP":
                direction = "TARGET";
                scenario = "01B";
                break;
            case "INVOIC":
                direction = "TARGET";
                scenario = "03";
                break;
            case "DESADV":
                direction = "TARGET";
                scenario = "02";
                break;
            default:
                throw new MigAutomationException("Folder name " + messageType + " is not supported.");
        }
        return String.format("MIG-EDI-%s %s-%s - UNEDIFACT %s %s–%s (ED)", scenario, customerName, country, version, messageType, direction);
    }

    public void mergeAllOutput() throws MigAutomationException {
        String outputPath = "./output";
        File[] outputFiles = new File(outputPath).listFiles(item -> item.isFile()
                &&  item.getName().endsWith("_output.zip")
                && !item.getName().contains("summary"));

        List<File> migFiles = new ArrayList<>();
        Manifest mergedManifest = null;

        List<File> tmpFolders = new ArrayList<>();

        for (File outputFile : outputFiles) {
            String unzippedFolder = extractZipFile(outputFile.getPath());
            tmpFolders.add(new File(unzippedFolder));

            List<Path> files = migUtils.listFilesUsingFileWalk(unzippedFolder);
            Path manifestFile = null;
            Path migFile = null;
            for (Path file : files) {
                if (file.getFileName().toString().equals("manifest.json")) {
                    manifestFile = file;
                    InputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(manifestFile.toFile());
                        Manifest manifest = mapper.readValue(inputStream, Manifest.class);
                        inputStream.close();
                        if (mergedManifest == null) {
                            mergedManifest = manifest;
                        } else {
                            if (manifest.getValue().getMigs().size() != 1) {
                                throw new MigAutomationException("Only one MIG file is supported.");
                            }
                            Mig mig = manifest.getValue().getMigs().get(0);
                            mergedManifest.getValue().getMigs().add(mig);
                        }
                    } catch (IOException e) {
                        throw new MigAutomationException(e);
                    }


                } else if (file.toFile().getParentFile().getName().equals("migs")) {
                    migFiles.add(file.toFile());
                }
            }




        }

        String tmpFolder = "./tmp/" + System.currentTimeMillis() + "/";
        new File(tmpFolder).mkdirs();

        DataOutputStream outputStream = null;
        try {
            outputStream = new DataOutputStream(new FileOutputStream(tmpFolder + "manifest.json", false));
            outputStream.write(mapper.writeValueAsString(mergedManifest).getBytes());
            outputStream.close();

            for (File migFile : migFiles) {
                Path targetPath = Path.of(tmpFolder + "migs/" +  migFile.getName());
                targetPath.toFile().getParentFile().mkdirs();
                if (targetPath.toFile().exists()) {
                    log.info("file already exists!");
                }
                Files.move(migFile.toPath(), targetPath);
            }


            String newZipFilePath = outputPath + "/" + System.currentTimeMillis() +  "_summary_output.zip";
            ZipFile zipFile = new ZipFile(newZipFilePath);

            // Add single file
            zipFile.addFile(tmpFolder + "manifest.json");
            zipFile.addFolder(new File(tmpFolder + "migs"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        tmpFolders.add(new File(tmpFolder));
        for (File tmpFolderToDelete : tmpFolders) {
            tmpFolderToDelete.delete();
        }
    }
}

