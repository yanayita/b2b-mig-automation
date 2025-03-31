package com.schneider.ei.b2b.mig;

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
import com.schneider.ei.b2b.mig.model.migs.Node;
import com.schneider.ei.b2b.mig.model.migs.NodeStatus;
import com.schneider.ei.b2b.mig.model.migs.Properties;
import com.schneider.ei.b2b.mig.model.migs.Qualifier;
import com.schneider.ei.b2b.mig.model.migs.QualifierMarker;
import com.schneider.ei.b2b.mig.model.migs.QualifierValue;
import com.schneider.ei.b2b.mig.model.migs.SelectedCodelist;
import com.schneider.ei.b2b.mig.model.migs.Value;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class QualifyNodeTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void test() throws IOException, MigAutomationException {
        InputStream is = QualifyNodeTest.class.getClassLoader().getResourceAsStream("test_01/MIG_input.json");
        ObjectNode rootNodeInput = mapper.readValue(is, ObjectNode.class);
        List<Node> inputNodes = mapper.convertValue(rootNodeInput.get("Nodes"), new TypeReference<>() {
        });
        for (Node nodes : inputNodes) {
            populateParent(nodes);
        }

        Node inputDTM = findNode("/51C96EACDDBAE8ED:Interchange/ORDERS/DTM", inputNodes);

        qualifyNode(inputDTM, "137");
        inputDTM = findNode("/51C96EACDDBAE8ED:Interchange/ORDERS/DTM[./C507/2005=137]", inputNodes);

        is = QualifyNodeTest.class.getClassLoader().getResourceAsStream("test_01/MIG_output.json");
        ObjectNode rootNodeOutput = mapper.readValue(is, ObjectNode.class);
        List<Node> outputNodes = mapper.convertValue(rootNodeOutput.get("Nodes"), new TypeReference<>() {
        });
        Node outputDTM = findNode("/51C96EACDDBAE8ED:Interchange/ORDERS/DTM[./C507/2005=137]", outputNodes);

        String inputDTMString = mapper.writeValueAsString(inputDTM);
        String outputDTMString = mapper.writeValueAsString(outputDTM);

        //assertThat(inputDTMString).isEqualTo(outputDTMString);

        rootNodeInput.set("Nodes", mapper.valueToTree(inputNodes));
        String outputMIG = mapper.writeValueAsString(rootNodeInput);
        System.out.println(outputMIG);

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

    private void qualifyNode(Node sourceNode, String value) throws MigAutomationException {

        QualifierMarker qualifierMarker = sourceNode.getQualifierMarkers().get(0);
        String originalDomainXpath = sourceNode.getDomain().getXPath();
        String newDomainXpath = originalDomainXpath + "[" + qualifierMarker.getRelativeXPath() + "=" + value + "]";
        String xmlNodeName = sourceNode.getXMLNodeName() + "_gq_" + value;

        String domainGUID = generateGUID();
        String baseDomainGUID = generateGUID();

        Codelist codeList = getCodeList("d96a", "2005");
        Code code = codeList.getCodes().stream().filter(item -> item.getId().equals(value)).findFirst().get();
        String idDoc = code.getDocumentation().getName().getBaseArtifactValue().getId();
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
        // Set properties values accordingly
        /**
         targetNode.getProperties().getSequenceNumber().setArtifactValue(ArtifactValue.builder()
         .vertexGUID(generateGUID())
         .id("1.001")
         .action("NONE")
         .build());
         */
        // Create new description contatenating current text and code list text  and set new Name artifact value
        /**
         targetNode.getDocumentation().getName().setArtifactValue(ArtifactValue.builder()
         .vertexGUID(generateGUID())
         .build());
         */

        List<Node> siblings = sourceNode.getParent().getNodes();
        int index = siblings.indexOf(sourceNode);
        siblings.remove(index);
        siblings.add(index, targetNode);
        targetNode.setParent(sourceNode.getParent());
        populateParent(targetNode);



        Node selectingNode = findSelectingNode(targetNode, qualifierMarker.getRelativeXPath());
        qualifyCorrespondingNode(selectingNode, code, codeList, targetNode.getQualifiers().get(0));
        for (Node child : targetNode.getNodes()) {
            qualifyChildrenNodes(child);
        }
    }

    private void qualifyChildrenNodes(Node sourceNode) throws MigAutomationException {
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

        List<Node> siblings = sourceNode.getParent().getNodes();
        int index = siblings.indexOf(sourceNode);
        siblings.remove(index);
        siblings.add(index, targetNode);
        targetNode.setParent(sourceNode.getParent());
        populateParent(targetNode);
        List<Node> children = new ArrayList<>(targetNode.getNodes());

        for (Node child : children) {
            qualifyChildrenNodes(child);
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
        }

        sourceNode.getSelectedCodelist().setSelectedCodes(Arrays.asList(code));

        //change this, as we should select the node based on path
        //Qualifier qualifier = sourceNode.getQualifiers().get(0);
        //QualifierValue qualifierValue = qualifier.getQualifierValues().get(0);
          /*
        Node.NodeBuilder<?, ?> nodeBuilder = targetNode.toBuilder()
                       .selectedCodelist(SelectedCodelist.builder()
                                       .codelistReference(sourceNode.getSelectedCodelist().getCodelistReference())

                        .codelistReference(CodelistReference.builder()
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
                                .build())

                        .selectedCodes(Arrays.asList(code))
                        .build());
*/
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
            Node foundNode = findNode(xPath, node.getNodes());
            if (foundNode != null) {
                return foundNode;
            }
        }
        return null;
    }

    public static String generateGUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    private Codelist getCodeList(String version, String codeListType) throws MigAutomationException {
        String filePath = "codelists/" + version + "/" + codeListType + ".json";
        File file = new File(filePath);
        if (!file.exists()) {
            throw new MigAutomationException("Code list file not found: " + filePath);
        }
        try {
            return this.mapper.readValue(file, Codelist.class);
        } catch (IOException e) {
            throw new MigAutomationException("Error while parsing CodeList", e);
        }

    }

    private void populateParent(Node node) {
        for (Node child : node.getNodes()) {
            child.setParent(node);
            populateParent(child);
        }
    }
}

/**
 * {
 *             "key": "/51C96EACDDBAE8ED:Interchange/ORDERS/DTM[./C507/2005=137]/C507/2380[../2005]",
 *             "qualifyingXPath": "../2005"
 *         },
 *         {
 *             "key": "/51C96EACDDBAE8ED:Interchange/ORDERS/DTM[./C507/2005=137]/C507[./2005]",
 *             "qualifyingXPath": "./2005"
 *         },
 *         {
 *             "key": "/51C96EACDDBAE8ED:Interchange/ORDERS/DTM[./C507/2005=137][./C507/2005]",
 *             "qualifyingXPath": "./C507/2005",
 *             "qualifyingValue": "137"
 *         }
 */