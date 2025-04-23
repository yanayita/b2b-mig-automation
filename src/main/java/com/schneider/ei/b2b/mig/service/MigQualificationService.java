package com.schneider.ei.b2b.mig.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schneider.ei.b2b.mig.model.MigAutomationException;
import com.schneider.ei.b2b.mig.model.codelists.Code;
import com.schneider.ei.b2b.mig.model.codelists.Codelist;
import com.schneider.ei.b2b.mig.model.migs.ArtifactValue;
import com.schneider.ei.b2b.mig.model.migs.CodelistReference;
import com.schneider.ei.b2b.mig.model.migs.ComplexType;
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

@Service
@Slf4j
public class MigQualificationService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private MigUtils migUtils;

    /**
     * Qualifies the MIG file based on the provided analysis results.
     * @param filePath File Path
     * @param analysisResults Analysis Results
     * @return Qualified MIG model
     * @throws MigAutomationException ex
     */
    public Mig qualifyMig(String filePath, AnalysisResults analysisResults) throws MigAutomationException {
        try {
            // open the file and read the content as Mig object
            InputStream is = new FileInputStream(filePath);
            Mig mig = mapper.readValue(is, Mig.class);
            List<Node> inputNodes = mig.getNodes();
            // for each of the input nodes, populate the parent field of each recursively
            for (Node nodes : inputNodes) {
                migUtils.populateParent(nodes);
            }

            // Build a Map with all the qualifiers in the Qualifiers section, indexed by key
            LinkedHashMap<String, QualifierPath> qualiferPaths = new LinkedHashMap<>();
            mig.getQualifiers().forEach(item -> qualiferPaths.put(item.getKey(), item));

            // Sort the analysis results based on the domain xPath, to process them alphabetically
            var sortedResults =  analysisResults.getQualifierXPathsFound().entrySet().stream()
                    .sorted(Comparator.comparing((item) -> item.getKey().getDomainXpath()))
                    .toList();

            // for each present paths coming from the Analysis Results of the EDI payloads, if they are present in the MIG, mark them as selected
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

            // for any node that is selected, mark all its parents as selected as well so that the tree is consistent
            List<Node> endNodes = new ArrayList<>();
            for (Node node : inputNodes) {
                getEndNodes(node, endNodes);
            }
            for (Node node : endNodes) {
                setSelectedParents(node);
            }

            // for each of the Qualifier Markers coming from the Analysis Results of the EDI payloads, process the qualifier
            for (Map.Entry<QualifierMarkerData, Set<String>> entry : sortedResults) {
                QualifierMarkerData qualifierMarkerData = entry.getKey();
                String xPathDomain = qualifierMarkerData.getDomainXpath();
                Set<String> values = entry.getValue();
                // try to find the node that corresponds to the xPath from the analysis results
                Node sourceNode = findNode(xPathDomain, inputNodes);
                if (sourceNode != null) {
                    // first check if teh qualifying node is already used, if so, set it as selected and skip it
                    if (isQualifyingNodeUsed(sourceNode)) {
                        sourceNode.setIsSelected(true);
                        continue;
                    }

                    boolean anyFound = false;
                    // for each value found in the analysis results, for this qualifying path qualify the node
                    for (String value : values) {
                        Node newNode = qualifyNode(sourceNode, value, qualiferPaths, mig, qualifierMarkerData);
                        if (newNode != null) {
                            anyFound = true;
                        }
                    }

                    // if none of the values were correct code lists, set it to selected
                    if (!anyFound) {
                        Node newNode = findNode(sourceNode.getDomain().getXPath(), sourceNode.getParent().getNodes());
                        newNode.setIsSelected(true);
                        qualifyChildrenNodes(newNode, qualiferPaths);
                    }

                    // the previous will create a clone node, after done, remove the original unqualified node
                    List<Node> siblings = sourceNode.getParent().getNodes();
                    siblings.remove(sourceNode);
                } else {
                    throw new MigAutomationException("Node not found for xPath: " + xPathDomain);
                }
            }

            // update the MIG object with the new qualified nodes found during Qualification
            mig.setQualifiers(qualiferPaths.values().stream().toList());

            return mig;
        } catch (IOException e) {
            throw new MigAutomationException("Error while reading file: " + filePath, e);
        }
    }

    /**
     * Check if the qualifying node is already used.
     * @param node Node
     * @return boolean
     * @throws MigAutomationException ex
     */
    private boolean isQualifyingNodeUsed(Node node) throws MigAutomationException {
        // iterate through all qualifier markers
        for (QualifierMarker qualifierMarker : node.getQualifierMarkers()) {
            // Find the corresponding selecting Node that holds the value
            Node foundNode = findSelectingNode(node, qualifierMarker.getRelativeXPath());
            // if the node already has a selected codelist and the selected codes are not empty, return true (except for Peer qualifier markers)
            // For Group qualifiers, it would generate an error to have the same qualifying nodes selected multiple times.
            // Since we're processing the paths in order, normally the high level nodes will be qualified first over nodes lower in the tree
            if (foundNode.getSelectedCodelist() != null && !foundNode.getSelectedCodelist().getSelectedCodes().isEmpty() && !qualifierMarker.getQualifierType().equals("Peer")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the xPaths of the Qualifier Markers and standard nodes from the MIG file.
     * @param filePath File Path
     * @return Set of QualifierMarkerData
     * @throws MigAutomationException ex
     */
    public Set<QualifierMarkerData> getQualifierXPaths(String filePath) throws MigAutomationException {
        try {
            // open the MIG file and read the content as Mig object
            InputStream is = new FileInputStream(filePath);
            Mig rootNodeInput = mapper.readValue(is, Mig.class);
            List<Node> nodes = rootNodeInput.getNodes();
            Set<QualifierMarkerData> qualifierXpaths = new LinkedHashSet<>();
            // get the xPaths of all the nodes
            for (Node node : nodes) {
                getXpaths(node, qualifierXpaths);
            }

            return qualifierXpaths;
        } catch (IOException e) {
            throw new MigAutomationException("Error while reading file: " + filePath, e);
        }
    }

    /**
     * Get the xPaths of the Qualifier Markers and standard nodes from the children nodes.
     * @param node Node
     * @param qualifierXpaths Set of QualifierMarkerData
     */
    private void getXpaths(Node node, Set<QualifierMarkerData> qualifierXpaths) {
        // remove from the Domain Xpath any specific qualifying value
        String nodeXpath = node.getDomain().getXPath().replaceAll("(\\[.*\\])", "");
        // if the node has no children and no qualifiers, add it to the list of qualifierXpaths with isQualifier=false
        if (node.getNodes().isEmpty() && node.getQualifierMarkers().isEmpty() && node.getQualifiers().isEmpty()) {
            QualifierMarkerData qualifierMarkerData = QualifierMarkerData.builder()
                    .domainXpath(nodeXpath)
                    .qualifyingXpath(nodeXpath)
                    .isQualifier(false)
                    .build();
            qualifierXpaths.add(qualifierMarkerData);
        }

        // if the node contains Qualifier Markers, add them to the list of qualifierXpaths with isQualifier=true
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

        // if the node contains Qualifier, add them to the list of qualifierXpaths with isQualifier=true
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

        // recursively call this method for each child node
        for (Node child : node.getNodes()) {
            getXpaths(child, qualifierXpaths);
        }

    }

    /**
     * Find the Qualifier Marker in the source node based on the xPath.
     * @param sourceNode Source Node
     * @param xPath xPath
     * @return QualifierMarker
     */
    private QualifierMarker findQualifierMarker(Node sourceNode, String xPath) {
        for (QualifierMarker qualifierMarker : sourceNode.getQualifierMarkers()) {
            if (qualifierMarker.getRelativeXPath().equals(xPath)) {
                return qualifierMarker;
            }
        }
        return null;

    }


    /**
     * Qualify the node based on the value and the qualifier paths.
     * @param sourceNode Source Node
     * @param value Value
     * @param qualiferPaths Qualifier Paths
     * @param mig MIG object
     * @param markerData Qualifier Marker Data
     * @throws MigAutomationException ex
     * @return Qualified Node
     */
    public Node qualifyNode(Node sourceNode, String value, Map<String, QualifierPath> qualiferPaths,
                            Mig mig, QualifierMarkerData markerData) throws MigAutomationException {

        QualifierMarker qualifierMarker = findQualifierMarker(sourceNode, markerData.getQualifyingRelativeXpath());
        String originalDomainXpath = sourceNode.getDomain().getXPath();
        // the new domain Path is the same as the original one, but with the value of the qualifier marker = value
        String newDomainXpath = originalDomainXpath + "[" + qualifierMarker.getRelativeXPath() + "=" + value + "]";
        String xmlNodeName;
        // Compute what should be the new values for the xmlNodeName
        if (qualifierMarker.getQualifierType().equals("Peer")) {
            xmlNodeName = sourceNode.getXMLNodeName() + "_pq_" + value;
        } else {
            xmlNodeName = sourceNode.getXMLNodeName() + "_gq_" + value;
        }

        String domainGUID = generateGUID();
        String baseDomainGUID = generateGUID();

        // Find the corresponding node that provides qualifying value to the source node based on the relative xPath
        Node correspondingNode = findSelectingNode(sourceNode, qualifierMarker.getRelativeXPath());
        if (correspondingNode == null) {
            throw new MigAutomationException("Could not find corresponding node for xPath: " + qualifierMarker.getRelativeXPath());
        }

        // Retrieve the version id of the MIG File (e.g. D96A S3)
        String version = ((String)((Map<String, ?>) mig.getMessageTemplate()).get("VersionId"));

        // Retrieve the code list based on the version and the id of the corresponding node, then find the code matching the found value from the payloads

        Optional<CodelistReference> codeListRefOpt = Optional.ofNullable(correspondingNode.getSimpleTypeVertexGUID())
                .map(item -> mig.getSimpleTypes().get(item))
                .map(ComplexType::getCodelistReferences)
                        .orElse(Collections.emptyList()).stream().findFirst();
        CodelistReference codeListRef;
        if (codeListRefOpt.isEmpty()) {
            throw new MigAutomationException("Could not find code list reference for xPath: " + qualifierMarker.getRelativeXPath());
        } else {
            codeListRef = codeListRefOpt.get();
        }

        Codelist codeList = getCodeList(codeListRef.getTypeSystemId(), codeListRef.getVersionId(), codeListRef.getId());
        Optional<Code> code = codeList.getCodes().stream().filter(item -> item.getId().equals(value)).findFirst();

        // if the code is not found in the code list, log an error and skip the qualification
        // TODO: provide capability to support custom Code Lists
        if (code.isEmpty()) {
            log.error("Code not found in code list {} - value {}. Skipping node qualification", correspondingNode.getId(), value);
            return null;
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

        // Set the values of the new node we're creating
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

        // if the source Node was already a qualified Node, we will set it as not original
        if (sourceNode.getXMLNodeName().endsWith("]")) {
            nodeBuilder = nodeBuilder.isOriginalNode(false);
        }

        // set additional fields to the Node
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

        // Add the qualifier path to the Qualifier Paths, that will be later on added to the MIG object
        QualifierPath qualifierPath = QualifierPath.builder()
                .key(targetNode.getDomain().getXPath() + "[" + targetNode.getQualifiers().get(0).getRelativeXPath() + "]")
                .qualifyingXPath(targetNode.getQualifiers().get(0).getRelativeXPath())
                .qualifyingValue(value)
                .build();
        qualiferPaths.put(qualifierPath.getKey(), qualifierPath);

        // Create new description concatenating current text and code list text  and set new Name artifact value
        String nodeName = mig.getDocumentationArtifacts().get(targetNode.getDocumentation().getName().getBaseArtifactValue().getId()) + " - " + codelistdesc;
        String nodeNameGUID = generateGUID();
        targetNode.getDocumentation().getName().setArtifactValue(ArtifactValue.builder()
                .vertexGUID(generateGUID())
                .id(nodeNameGUID)
                .languageCode("en-us")
                .action("MODIFIED")
                .build());
        mig.getDocumentationArtifacts().put(nodeNameGUID, nodeName);

        // Remove the original node from the parent and add the new node
        List<Node> siblings = sourceNode.getParent().getNodes();
        int index = siblings.indexOf(sourceNode);
        siblings.add(index, targetNode);
        targetNode.setParent(sourceNode.getParent());
        populateParent(targetNode);

        // Retrieve the Selecting node and qualify it accordingly
        Node selectingNode = findSelectingNode(targetNode, qualifierMarker.getRelativeXPath());
        qualifyCorrespondingNode(selectingNode, code.get(), codeList, targetNode.getQualifiers().get(0));

        // for each children node of the qualified node, update the domain values
        List<Node> childrenNodes = new ArrayList<>(targetNode.getNodes());
        for (Node child : childrenNodes) {
            qualifyChildrenNodes(child, qualiferPaths);
        }
        return targetNode;
    }

    /**
     * Qualify the children nodes of the Qualified Node.
     * @param sourceNode Source Node
     * @param qualifierPaths xPath
     */
    private void qualifyChildrenNodes(Node sourceNode, Map<String, QualifierPath> qualifierPaths) throws MigAutomationException {
        String newDomainXpath = sourceNode.getParent().getDomain().getXPath() + "/" + sourceNode.getId();

        // create a deep copy of the original node
        Node targetNode;
        try {
            targetNode = mapper.readValue(mapper.writeValueAsString(sourceNode), Node.class);
        } catch (JsonProcessingException e) {
            throw new MigAutomationException(e);
        }

        // Set the Domain values to the node
        Node.NodeBuilder<?, ?> nodeBUilder = setDomainValues(targetNode.toBuilder(), sourceNode, newDomainXpath);
        nodeBUilder.isOriginalNode(true); // only first time
        targetNode = nodeBUilder.build();

        // TODO: check first and do this only if there is no value found for this qualifier
        for (QualifierMarker qualifierMarker : targetNode.getQualifierMarkers()) {
            QualifierPath qualifierPath = QualifierPath.builder()
                    .key(targetNode.getDomain().getXPath() + "[" + qualifierMarker.getRelativeXPath() + "]")
                    .qualifyingXPath(qualifierMarker.getRelativeXPath())
                    .build();
            qualifierPaths.put(qualifierPath.getKey(), qualifierPath);
        }


        // remove the original node from the parent and add the new node
        List<Node> siblings = sourceNode.getParent().getNodes();
        int index = siblings.indexOf(sourceNode);
        siblings.remove(index);
        siblings.add(index, targetNode);
        targetNode.setParent(sourceNode.getParent());
        populateParent(targetNode);
        List<Node> children = new ArrayList<>(targetNode.getNodes());

        // perform the same action recursively for each child node
        for (Node child : children) {
            qualifyChildrenNodes(child, qualifierPaths);
        }
    }


    /**
     * Qualify the corresponding node based on the source node and the code list.
     * @param sourceNode Source Node
     * @param code code
     * @param codeList codeList
     * @return Node
     */
    private void qualifyCorrespondingNode(Node sourceNode, Code code, Codelist codeList, Qualifier qualifier) {
        if (sourceNode.getSelectedCodelist() == null) {

            // create a new CodelistReference object
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

            // set the status and set the Code list reference, codes, etc
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

    private Codelist getCodeList(String typeSystem, String version, String codeListType) throws MigAutomationException {
        String filePath = "./codelists/" + version + "/" + codeListType + ".json";
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                Codelist codelist = retrieveCodeList(typeSystem, version, codeListType);
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

    private Codelist retrieveCodeList(String typeSystem, String version, String codeListType) {
        String url = UriComponentsBuilder.fromUriString("/api/1.0/typesystems/{typeSystem}/versions/{version}/codelists/{codeListType}")
                .buildAndExpand(typeSystem, version, codeListType).encode()
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

