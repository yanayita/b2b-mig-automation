package com.schneider.ei.b2b.mig.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.schneider.ei.b2b.mig.model.MigAutomationException;
import com.schneider.ei.b2b.mig.model.codelists.Codelist;
import com.schneider.ei.b2b.mig.model.export.ExportRequest;
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
import com.schneider.ei.b2b.mig.model.search.Mig;
import com.schneider.ei.b2b.mig.model.search.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MigCreationService {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MigUtils migUtils;

    @Autowired
    private RestTemplate restTemplate;

    private String xCsrfToken;



    public MigCreationService() {
        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = new JacksonJsonProvider();
            private final MappingProvider mappingProvider = new JacksonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }

    public MigRequest createMigRequest(String messageType, String versionId, String migName) throws MigAutomationException {

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
        } else if (messageType.equalsIgnoreCase("INVOIC")) {
            documentationValue = "Invoice message";
            contextValues.add(ContextValue.builder()
                    .key("00035")
                    .name("Create Invoice")
                    .build());
        } else if (messageType.equalsIgnoreCase("ORDRSP")) {
            documentationValue = "Purchase order response message";
            contextValues.add(ContextValue.builder()
                    .key("00029")
                    .name("Confirm/Verify Payment Orders")
                    .build());
        } else if (messageType.equalsIgnoreCase("DESADV")) {
            documentationValue = "Despatch advice message";
            contextValues.add(ContextValue.builder()
                    .key("00134")
                    .name("Notify Of Shipment Status")
                    .build());
        } else {
            throw new MigAutomationException("Unsupported message type: " + messageType);
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

        List<SearchResult> remoteMigs = findRemoteMigs();
        Optional<Mig> foundMig =  remoteMigs.get(0).getMigs().stream()
                .filter(item -> StringUtils.equals(item.getDocumentation().getName().getArtifactValue().getId(), migName))
                .findAny();

        MigMetadata migMetadata;
        if (foundMig.isPresent()) {
            throw new MigAutomationException("MIG with name " + migName + " already exists.");
            // TODO: add support to update existing MIG
            //log.warn("MIG with name " + migName + " already exists.");
            //migMetadata = getMigMetadata(foundMig.get().getObjectGUID());
        } else {
            MigRequest migRequest = createMigRequest(messageType, versionId, migName);
            MigResponse migResponse = createRemoteMig(migRequest);
            migMetadata = getMigMetadata(migResponse.getId());
        }


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


    public List<SearchResult> findRemoteMigs() {
        String url = UriComponentsBuilder.fromUriString("/api/1.0/migs")
                .toUriString();

        ResponseEntity<SearchResult[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, createRequestEntityRead(), SearchResult[].class);
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

    public void deleteRemoteMig(String migId) {
        String url = UriComponentsBuilder.fromUriString("/api/1.0/migs/{migId}")
                .buildAndExpand(migId)
                .toUriString();
        HttpEntity<?> request = createRequestEntityUpdate(null, this::findRemoteMigs);
        restTemplate.exchange(url, HttpMethod.DELETE, request, Void.class);;
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
