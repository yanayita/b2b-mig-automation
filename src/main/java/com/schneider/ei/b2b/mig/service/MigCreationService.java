package com.schneider.ei.b2b.mig.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schneider.ei.b2b.mig.model.MigAutomationException;
import com.schneider.ei.b2b.mig.model.codelists.Codelist;
import com.schneider.ei.b2b.mig.model.export.ExportRequest;
import com.schneider.ei.b2b.mig.model.export.Mig;
import com.schneider.ei.b2b.mig.model.export.MigMetadata;
import com.schneider.ei.b2b.mig.model.migs.ArtifactValue;
import com.schneider.ei.b2b.mig.model.migs.Documentation;
import com.schneider.ei.b2b.mig.model.migs.Value;
import com.schneider.ei.b2b.mig.model.request.ContextValue;
import com.schneider.ei.b2b.mig.model.request.Identification;
import com.schneider.ei.b2b.mig.model.request.MessageTemplate;
import com.schneider.ei.b2b.mig.model.request.MigRequest;
import com.schneider.ei.b2b.mig.model.request.MigResponse;
import com.schneider.ei.b2b.mig.model.request.OwnBusinessContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class MigCreationService {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MigUtils migUtils;

    @Autowired
    private RestTemplate restTemplate;

    private String xCsrfToken;

    public MigRequest createMigRequest(String messageType, String versionId, String migName) {

        String nameGUID = "UID-" + MigUtils.generateGUIDv2();
        String summaryGUID = "UID-" + MigUtils.generateGUIDv2();

        String documentationValue;
        List<ContextValue> contextValues = new ArrayList<>();
        if (messageType.equalsIgnoreCase("ORDERS")) {
            documentationValue = "Purchase order message";
            contextValues.add(ContextValue.builder()
                    .key("00036")
                    .name("Create Order")
                    .build());
        } else {
            documentationValue = "Unknown";
        }

        Map<String, String> documentationArtifacts = new LinkedHashMap<>();
        documentationArtifacts.put(nameGUID, migName);
        documentationArtifacts.put(summaryGUID, "");

        MigRequest migRequest = MigRequest.builder()
                .identification(Identification.builder()
                        .mIGVersion("1.0")
                        .build())
                .direction("Both")
                .status("Draft")
                .documentation(Documentation.builder()
                        .name(Value.builder()
                                .artifactValue(ArtifactValue.builder()
                                        .id(nameGUID)
                                        .vertexGUID("UID-")
                                        .languageCode("en-us")
                                        .build())
                                .build())
                        .summary(Value.builder()
                                .artifactValue(ArtifactValue.builder()
                                        .id(summaryGUID)
                                        .vertexGUID("UID-")
                                        .languageCode("en-us")
                                        .build())
                                .build())
                        .build())
                .messageTemplate(createMessageTemplate(messageType, versionId, documentationValue))
                .envelopeTemplate(createMessageTemplate("EnvelopeForMessagesS3", versionId, "Envelope for Messages (Syntax Versions 1/2/3)"))
                .ownBusinessContext(Collections.singletonList(OwnBusinessContext.builder()
                                .contextType("BusinessProcess")
                                .name("Business Process")
                                .codeListId("CommonBusinessProcessCodelist")
                                .contextValues(contextValues)
                        .build()))
                .partnerBusinessContext(new ArrayList<>())
                .documentationArtifacts(documentationArtifacts)
                .build();

        return migRequest;
    }

    private MessageTemplate createMessageTemplate(String messageType, String versionId, String documentationValue) {
        return MessageTemplate.builder()
                .objectGUID("")
                .isCustomObject(false)
                .id(messageType)
                .tag(messageType)
                .typeSystemAcronym("UN/EDIFACT")
                .typeSystemId("UNEDIFACT")
                .versionId(versionId)
                .documentation(Documentation.builder()
                        .name(Value.builder()
                                .baseArtifactValue(ArtifactValue.builder()
                                        .id(documentationValue)
                                        .build())
                                .build())
                        .build())
                .displayTag(messageType)
                .versionAcronym("UN/EDIFACT")
                .build();
    }

    public MigResponse createRemoteMig(MigRequest migRequest) {
        String url = UriComponentsBuilder.fromUriString("/api/1.0/migs")
                .toUriString();
        return restTemplate.postForObject(url, createRequestEntityUpdate(migRequest, this::findRemoteMigs), MigResponse.class);
    }

    public File createAndExportMig(String messageType, String versionId, String migName) throws MigAutomationException {
        MigRequest migRequest = createMigRequest(messageType, versionId, migName);
        MigResponse migResponse = createRemoteMig(migRequest);
        MigMetadata migMetadata = getMigMetadata(migResponse.getId());
        ExportRequest exportRequest = ExportRequest.builder()
                .migs(Collections.singletonList(migMetadata.getValue()))
                .build();
        byte[] export = exportData(exportRequest);

        File outputFile = new File("./output/" + migName + ".zip");

        try {
            DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(outputFile, false));
            outputStream.write(export);
            outputStream.close();
        } catch (IOException e) {
            throw new MigAutomationException(e);
        }

        return outputFile;
    }


    public List<?> findRemoteMigs() {
        String url = UriComponentsBuilder.fromUriString("/api/1.0/migs")
                .toUriString();

        ResponseEntity<Object[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, createRequestEntityRead(), Object[].class);
        retrieveXCsrfToken(responseEntity.getHeaders());
        Objects.requireNonNull(responseEntity.getBody());
        return Arrays.asList(responseEntity.getBody());
    }

    public MigMetadata getMigMetadata(String migId) {
        String url = UriComponentsBuilder.fromUriString("/externalApi/1.0/migs/{id}")
                .buildAndExpand(migId).encode()
                .toUriString();

        ResponseEntity<MigMetadata> responseEntity = restTemplate.exchange(url, HttpMethod.GET, createRequestEntityRead(), MigMetadata.class);
        Objects.requireNonNull(responseEntity.getBody());
        return responseEntity.getBody();
    }

    public byte[] exportData(ExportRequest exportRequest) {
        String url = UriComponentsBuilder.fromUriString("/api/1.0/exporter")
                .toUriString();

        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.POST, createRequestEntityUpdate(exportRequest, this::findRemoteMigs), byte[].class);
        Objects.requireNonNull(responseEntity.getBody());
        return responseEntity.getBody();
    }


    protected void retrieveXCsrfToken(MultiValueMap<String, String> inputHeaders) {
        this.xCsrfToken = Optional.ofNullable(inputHeaders.get("X-Csrf-Token")).stream().flatMap(Collection::stream).findFirst().orElse(null);
    }

    protected HttpEntity<?> createRequestEntityRead() {
        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("X-Csrf-Token", "fetch");
        return new HttpEntity<>(headers);
    }

    protected <T> HttpEntity<T> createRequestEntityUpdate(T body, Supplier<?> function) {
        MultiValueMap<String, String> headers = new HttpHeaders();
        if (this.xCsrfToken == null) {
            function.get();
        }
        headers.add("X-Csrf-Token", this.xCsrfToken);
        return new HttpEntity<>(body, headers);
    }
}
