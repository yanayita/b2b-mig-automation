package com.schneider.ei.b2b.mig.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.schneider.ei.b2b.mig.model.MigAutomationException;
import com.schneider.ei.b2b.mig.model.codelists.Code;
import com.schneider.ei.b2b.mig.model.codelists.Codelist;
import com.schneider.ei.b2b.mig.model.migs.ArtifactValue;
import com.schneider.ei.b2b.mig.model.migs.CodelistReference;
import com.schneider.ei.b2b.mig.model.migs.Domain;
import com.schneider.ei.b2b.mig.model.migs.Mig;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MigQualificationService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MigUtils migUtils;

    @Autowired
    private EdifactAnalyzerService edifactAnalyzerService;

    public Mig qualifyMig(String filePath, AnalysisResults analysisResults) throws MigAutomationException {
        try {
            // Test the qualifyNode method
            InputStream is = new FileInputStream(filePath);
            Mig mig = mapper.readValue(is, Mig.class);
            List<Node> inputNodes = mig.getNodes();
            for (Node nodes : inputNodes) {
                migUtils.populateParent(nodes);
            }
            LinkedHashMap<String, QualifierPath> qualiferPaths = new LinkedHashMap<>();
            mig.getQualifiers().forEach(item -> qualiferPaths.put(item.getKey(), item));

            var sortedResults =  analysisResults.getQualifierXPathsFound().entrySet().stream()
                    .sorted(Comparator.comparing((item) -> item.getKey().getDomainXpath()))
                    .toList();

            for (QualifierMarkerData qualifierMarkerData : analysisResults.getSelectedXPathsFound()) {
                Node foundNode = findNode(qualifierMarkerData.getDomainXpath(), inputNodes);
                if (foundNode != null){
                    foundNode.setIsSelected(true);
                    foundNode.setNodeStatus(NodeStatus.builder()
                            .status("Default")
                            .comment("")
                            .build());
                }
            }

            List<Node> endNodes = new ArrayList<>();
            for (Node node : inputNodes) {
                getEndNodes(node, endNodes);
            }
            for (Node node : endNodes) {
                setSelectedParents(node);
            }

            for (Map.Entry<QualifierMarkerData, Set<String>> entry : sortedResults) {
                QualifierMarkerData qualifierMarkerData = entry.getKey();
                String xPathDomain = qualifierMarkerData.getDomainXpath();
                Set<String> values = entry.getValue();
                Node sourceNode = findNode(xPathDomain, inputNodes);
                if (sourceNode != null) {
                    if (isQualifyingNodeUsed(sourceNode)) {
                        sourceNode.setIsSelected(true);
                        continue;
                    }
                    for (String value : values) {
                        qualifyNode(sourceNode, value, qualiferPaths, mig, qualifierMarkerData);
                    }
                    List<Node> siblings = sourceNode.getParent().getNodes();
                    siblings.remove(sourceNode);
                } else {
                    throw new MigAutomationException("Node not found for xPath: " + xPathDomain);
                }
            }

            mig.setQualifiers(qualiferPaths.values().stream().toList());

            return mig;
        } catch (IOException e) {
            throw new MigAutomationException("Error while reading file: " + filePath, e);
        }
    }

    private boolean isQualifyingNodeUsed(Node node) throws MigAutomationException {
        for (QualifierMarker qualifierMarker : node.getQualifierMarkers()) {
            Node foundNode = findSelectingNode(node, qualifierMarker.getRelativeXPath());
            if (foundNode.getSelectedCodelist() != null && !foundNode.getSelectedCodelist().getSelectedCodes().isEmpty() && !qualifierMarker.getQualifierType().equals("Peer")) {
                return true;
            }
        }
        return false;
    }

    public Set<QualifierMarkerData> getQualifierXPaths(String filePath) throws MigAutomationException {
        try {
            InputStream is = new FileInputStream(filePath);
            Mig rootNodeInput = mapper.readValue(is, Mig.class);
            List<Node> nodes = rootNodeInput.getNodes();
            Set<QualifierMarkerData> qualifierXpaths = new LinkedHashSet<>();
            for (Node node : nodes) {
                getXpaths(node, qualifierXpaths);
            }

            return qualifierXpaths;
        } catch (IOException e) {
            throw new MigAutomationException("Error while reading file: " + filePath, e);
        }
    }

    public void getXpaths(Node node, Set<QualifierMarkerData> qualifierXpaths) {
        String nodeXpath = node.getDomain().getXPath().replaceAll("(\\[.*\\])", "");
        if (node.getNodes().isEmpty() && node.getQualifierMarkers().isEmpty() && node.getQualifiers().isEmpty()) {
            QualifierMarkerData qualifierMarkerData = QualifierMarkerData.builder()
                    .domainXpath(nodeXpath)
                    .qualifyingXpath(nodeXpath)
                    .isQualifier(false)
                    .build();
            qualifierXpaths.add(qualifierMarkerData);
        }
        if (!node.getQualifierMarkers().isEmpty()) {
            for (QualifierMarker qualifierMarker : node.getQualifierMarkers()) {
                QualifierMarkerData qualifierMarkerData = QualifierMarkerData.builder()
                        .qualifyingXpath(nodeXpath + "/" + qualifierMarker.getRelativeXPath().replaceAll("(\\[.*\\])", ""))
                        .domainXpath(nodeXpath)
                        .qualifyingRelativeXpath(qualifierMarker.getRelativeXPath())
                        .isQualifier(true)
                        .build();
                qualifierXpaths.add(qualifierMarkerData);
            }
        }
        if (!node.getQualifiers().isEmpty()) {
            for (Qualifier qualifier : node.getQualifiers()) {

                QualifierMarkerData qualifierMarkerData = QualifierMarkerData.builder()
                        .qualifyingXpath(nodeXpath + "/" + qualifier.getRelativeXPath().replaceAll("(\\[.*\\])", ""))
                        .domainXpath(nodeXpath)
                        .isQualifier(true)
                        .build();
                qualifierXpaths.add(qualifierMarkerData);
            }
        }
        for (Node child : node.getNodes()) {
            getXpaths(child, qualifierXpaths);
        }

    }

    private QualifierMarker findQualifierMarker(Node sourceNode, String xPath) {
        for (QualifierMarker qualifierMarker : sourceNode.getQualifierMarkers()) {
            if (qualifierMarker.getRelativeXPath().equals(xPath)) {
                return qualifierMarker;
            }
        }
        return null;

    }


    public void qualifyNode(Node sourceNode, String value, Map<String, QualifierPath> qualiferPaths,
                            Mig mig, QualifierMarkerData markerData) throws MigAutomationException {

        QualifierMarker qualifierMarker = findQualifierMarker(sourceNode, markerData.getQualifyingRelativeXpath());
        String originalDomainXpath = sourceNode.getDomain().getXPath();
        String newDomainXpath = originalDomainXpath + "[" + qualifierMarker.getRelativeXPath() + "=" + value + "]";
        String xmlNodeName;
        if (qualifierMarker.getQualifierType().equals("Peer")) {
            xmlNodeName = sourceNode.getXMLNodeName() + "_pq_" + value;
        } else {
            xmlNodeName = sourceNode.getXMLNodeName() + "_gq_" + value;
        }

        String domainGUID = generateGUID();
        String baseDomainGUID = generateGUID();

        Node correspondingNode = findSelectingNode(sourceNode, qualifierMarker.getRelativeXPath());
        if (correspondingNode == null) {
            throw new MigAutomationException("Could not find corresponding node for xPath: " + qualifierMarker.getRelativeXPath());
        }

        String version = ((String)((Map<String, ?>) mig.getMessageTemplate()).get("VersionId"));

        Codelist codeList = getCodeList(version, correspondingNode.getId());
        Optional<Code> code = codeList.getCodes().stream().filter(item -> item.getId().equals(value)).findFirst();
        if (code.isEmpty()) {
            log.error("Code not found in code list {} - value {}. Skipping node qualification", correspondingNode.getId(), value);
            Node newNode = findNode(sourceNode.getDomain().getXPath(), sourceNode.getParent().getNodes());
            newNode.setIsSelected(true);
            qualifyChildrenNodes(newNode, qualiferPaths);
            return;
        }

        String idDoc = code.get().getDocumentation().getName().getBaseArtifactValue().getId();
        String codelistdesc = codeList.getDocumentationArtifacts().get(idDoc);

        // create a deep copy of the original node
        Node targetNode;
        try {
            targetNode = mapper.readValue(mapper.writeValueAsString(sourceNode), Node.class);
        } catch (JsonProcessingException e) {
            throw new MigAutomationException(e);
        }

        Node.NodeBuilder<?, ?> nodeBuilder = targetNode.toBuilder()
                .isSelected(true)
                .isOriginalNode(true) //only first time
                .codelistReferences(Collections.emptyList())
                .xMLNodeName(xmlNodeName)
                .domain(Domain.builder()
                        .unqualifiedDomainGUID(sourceNode.getDomain().getDomainGUID())
                        .alternativeDomainGUID(sourceNode.getDomain().getDomainGUID())
                        .xPath(newDomainXpath)
                        .domainGUID(domainGUID)
                        .parentDomainGUID(sourceNode.getDomain().getParentDomainGUID())
                        .build())
                .baseTypeDomain(Domain.builder()
                        .parentDomainGUID(domainGUID)
                        .unqualifiedDomainGUID(sourceNode.getBaseTypeDomain().getDomainGUID())
                        .domainGUID(baseDomainGUID)
                        .build());

        if (sourceNode.getXMLNodeName().endsWith("]")) {
            nodeBuilder = nodeBuilder.isOriginalNode(false);
        }

        nodeBuilder = nodeBuilder
                .nodeStatus(NodeStatus.builder()
                        .status("Default")
                        .comment("")
                        .build())
                .qualifierMarkers(Collections.emptyList())
                .qualifiers(Arrays.asList(Qualifier.builder()
                        .qualifierValues(Collections.singletonList(QualifierValue.builder()
                                .vertexGUID(generateGUID())
                                .qualifierName(codelistdesc)
                                .qualifierValue(value)
                                .build()))
                        .qualifierNotUsed(false)
                        .id(qualifierMarker.getId())
                        .qualifyingNodeVertexGUID(qualifierMarker.getQualifyingNodeVertexGUID())
                        .codelistReferenceVertexGUID(qualifierMarker.getCodelistReferenceVertexGUID())
                        .qualifierType(qualifierMarker.getQualifierType())
                        .relativeXPath(qualifierMarker.getRelativeXPath())
                        .build()
                ));

        targetNode = nodeBuilder.build();
        QualifierPath qualifierPath = QualifierPath.builder()
                .key(targetNode.getDomain().getXPath() + "[" + targetNode.getQualifiers().get(0).getRelativeXPath() + "]")
                .qualifyingXPath(targetNode.getQualifiers().get(0).getRelativeXPath())
                .qualifyingValue(value)
                .build();
        qualiferPaths.put(qualifierPath.getKey(), qualifierPath);

        // Create new description contatenating current text and code list text  and set new Name artifact value
        String nodeName = mig.getDocumentationArtifacts().get(targetNode.getDocumentation().getName().getBaseArtifactValue().getId()) + " - " + codelistdesc;
        String nodeNameGUID = generateGUID();
        targetNode.getDocumentation().getName().setArtifactValue(ArtifactValue.builder()
                .vertexGUID(generateGUID())
                .id(nodeNameGUID)
                .languageCode("en-us")
                .action("MODIFIED")
                .build());
        mig.getDocumentationArtifacts().put(nodeNameGUID, nodeName);

        List<Node> siblings = sourceNode.getParent().getNodes();
        int index = siblings.indexOf(sourceNode);
        siblings.add(index, targetNode);
        targetNode.setParent(sourceNode.getParent());
        populateParent(targetNode);


        Node selectingNode = findSelectingNode(targetNode, qualifierMarker.getRelativeXPath());
        qualifyCorrespondingNode(selectingNode, code.get(), codeList, targetNode.getQualifiers().get(0));
        List<Node> childrenNodes = new ArrayList<>(targetNode.getNodes());
        for (Node child : childrenNodes) {
            qualifyChildrenNodes(child, qualiferPaths);
        }
    }

    private void qualifyChildrenNodes(Node sourceNode, Map<String, QualifierPath> qualiferPaths) throws MigAutomationException {
        String newDomainXpath = sourceNode.getParent().getDomain().getXPath() + "/" + sourceNode.getId();

        // create a deep copy of the original node
        Node targetNode;
        try {
            targetNode = mapper.readValue(mapper.writeValueAsString(sourceNode), Node.class);
        } catch (JsonProcessingException e) {
            throw new MigAutomationException(e);
        }

        Node.NodeBuilder<?, ?> nodeBUilder = setDomainValues(targetNode.toBuilder(), sourceNode, newDomainXpath);
        nodeBUilder.isOriginalNode(true); // only first time
        targetNode = nodeBUilder.build();

        // TODO: check first and do this only if there is no value found for this qualifier
        for (QualifierMarker qualifierMarker : targetNode.getQualifierMarkers()) {
            QualifierPath qualifierPath = QualifierPath.builder()
                    .key(targetNode.getDomain().getXPath() + "[" + qualifierMarker.getRelativeXPath() + "]")
                    .qualifyingXPath(qualifierMarker.getRelativeXPath())
                    .build();
            qualiferPaths.put(qualifierPath.getKey(), qualifierPath);
        }


        List<Node> siblings = sourceNode.getParent().getNodes();
        int index = siblings.indexOf(sourceNode);
        siblings.remove(index);
        siblings.add(index, targetNode);
        targetNode.setParent(sourceNode.getParent());
        populateParent(targetNode);
        List<Node> children = new ArrayList<>(targetNode.getNodes());

        for (Node child : children) {
            qualifyChildrenNodes(child, qualiferPaths);
        }
    }


    private void qualifyCorrespondingNode(Node sourceNode, Code code, Codelist codeList, Qualifier qualifier) {
        if (sourceNode.getSelectedCodelist() == null) {

            CodelistReference reference = CodelistReference.builder()
                    .vertexGUID(qualifier.getCodelistReferenceVertexGUID())
                    .id(codeList.getIdentification().getId())
                    .typeSystemId(codeList.getIdentification().getTypeSystemId())
                    .versionId(codeList.getIdentification().getVersionId())
                    .versionMode("Current")
                    .properties(Properties.builder()
                            .isModifiable(Value.builder()
                                    .artifactValue(ArtifactValue.builder()
                                            .vertexGUID(generateGUID())
                                            .id("false")
                                            .action("CREATED")
                                            .build())
                                    .propertyName("IsModifiable")
                                    .propertyDataType("boolean")
                                    .build())
                            .isSelected(Value.builder()
                                    .artifactValue(ArtifactValue.builder()
                                            .vertexGUID(generateGUID())
                                            .id("false")
                                            .action("CREATED")
                                            .build())
                                    .propertyName("IsSelected")
                                    .propertyDataType("boolean")
                                    .build())
                            .allValuesSelected(Value.builder()
                                    .artifactValue(ArtifactValue.builder()
                                            .vertexGUID(generateGUID())
                                            .id("false")
                                            .action("CREATED")
                                            .build())
                                    .propertyName("AllValuesSelected")
                                    .propertyDataType("boolean")
                                    .build())
                            .build())
                    .build();

            sourceNode.setNodeStatus(NodeStatus.builder()
                    .status("Default")
                    .comment("")
                    .build());
            sourceNode.setCodelistReferences(Collections.singletonList(reference));
            sourceNode.setSelectedCodelist(SelectedCodelist.builder()
                    .codelistReference(reference)
                    .build());
            sourceNode.setOverrideSimpleTypeCodelistReferences(true);
            sourceNode.setIsSelected(true);
        }

        sourceNode.getSelectedCodelist().setSelectedCodes(Arrays.asList(code));
    }

    private Node.NodeBuilder<?, ?> setDomainValues(Node.NodeBuilder<?, ?> targetNode, Node sourceNode, String newDomainXpath) {
        String domainGUID = generateGUID();
        String baseDomainGUID = generateGUID();
        return targetNode
                .domain(Domain.builder()
                        .unqualifiedDomainGUID(sourceNode.getDomain().getDomainGUID())
                        .alternativeDomainGUID(sourceNode.getDomain().getDomainGUID())
                        .xPath(newDomainXpath)
                        .domainGUID(domainGUID)
                        .parentDomainGUID(sourceNode.getParent().getBaseTypeDomain().getDomainGUID())
                        .build())
                .baseTypeDomain(Domain.builder()
                        .parentDomainGUID(domainGUID)
                        .unqualifiedDomainGUID(sourceNode.getBaseTypeDomain().getDomainGUID())
                        .domainGUID(baseDomainGUID)
                        .build());
    }

    private Node findNode(String xPath, List<Node> nodes) {
        for (Node node : nodes) {
            if (node.getDomain().getXPath().equals(xPath)) {
                return node;
            }
            if (node.getDomain().getXPath().replaceAll("\\[.*?\\]", "").equals(xPath)) {
                return node;
            }
            Node foundNode = findNode(xPath, node.getNodes());
            if (foundNode != null) {
                return foundNode;
            }
        }
        return null;
    }

    public String generateGUID() {
        return MigUtils.generateGUID();
    }

    private Codelist getCodeList(String version, String codeListType) throws MigAutomationException {
        String filePath = "./codelists/" + version + "/" + codeListType + ".json";
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                Codelist codelist = retrieveCodeList(version, codeListType);
                Files.createDirectories(file.getParentFile().toPath());
                mapper.writeValue(file, codelist);
                return codelist;
            } catch (IOException e) {
                throw new MigAutomationException(e);
            }
            //throw new MigAutomationException("Code list file not found: " + filePath);
        }
        try {
            return this.mapper.readValue(file, Codelist.class);
        } catch (IOException e) {
            throw new MigAutomationException("Error while parsing CodeList", e);
        }
    }

    private Codelist retrieveCodeList(String version, String codeListType) {
        String url = UriComponentsBuilder.fromUriString("/api/1.0/typesystems/UNEDIFACT/versions/{version}/codelists/{codeListType}")
                .buildAndExpand(version, codeListType).encode()
                .toUriString();
        return restTemplate.getForObject(url, Codelist.class);
    }

    private void populateParent(Node node) {
        for (Node child : node.getNodes()) {
            child.setParent(node);
            populateParent(child);
        }
    }

    private void getEndNodes(Node node, List<Node> results) {
        if (node.getNodes().isEmpty()) {
            results.add(node);
        } else {
            for (Node child : node.getNodes()) {
                getEndNodes(child, results);
            }
        }
    }

    private void setSelectedParents(Node node) {
        if (node.getParent() == null) {
            return;
        }
        if (node.getIsSelected()) {
            node.getParent().setIsSelected(true);
        }
        setSelectedParents(node.getParent());
    }

    private Node findSelectingNode(Node sourceNode, String xPath) throws MigAutomationException {
        String[] xPathTokens = StringUtils.split(xPath, "/");
        Node tmpNode = sourceNode;
        for (int i = 0; i < xPathTokens.length; i++) {
            String token = xPathTokens[i];
            if (token.equals(".")) {
                continue;
            } else if (token.equals("..")) {
                tmpNode = tmpNode.getParent();
            } else {
                tmpNode = tmpNode.getNodes().stream().filter(item -> item.getId().equals(token)).findFirst().get();
            }
        }
        if (xPathTokens.length > 0 && sourceNode == tmpNode) {
            throw new MigAutomationException("Cound not find node with xPath: " + xPath);
        }

        return tmpNode;
    }
}

