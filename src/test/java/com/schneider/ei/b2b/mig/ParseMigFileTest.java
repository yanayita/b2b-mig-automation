package com.schneider.ei.b2b.mig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ParseMigFileTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void test() throws IOException {
        InputStream is = ParseMigFileTest.class.getClassLoader().getResourceAsStream("MIG_bd3a05bdc035425eb5ba1c2ec9ca65b5.json");
        ObjectNode rootNode = mapper.readValue(is, ObjectNode.class);
        List<Node> nodes = mapper.convertValue(rootNode.get("Nodes"), new TypeReference<>() {});
        Set<String> qualifierXpaths = new LinkedHashSet<>();
        Set<String> valueXpaths = new LinkedHashSet<>();
        //findQualifierMarkers(nodes, qualifierXpaths);
        for (Node node : nodes) {
            getXpaths(node, qualifierXpaths, valueXpaths);
        }

        System.out.println("Qualifier Xpaths:");
        for (String xpath : qualifierXpaths) {
            System.out.println(xpath);
        }
        System.out.println("Value Xpaths:");
        for (String xpath : valueXpaths) {
            System.out.println(xpath);
        }

    }

    public void getXpaths(Node node, Set<String> qualifierXpaths, Set<String> valueXpaths) {
        String nodeXpath = node.getDomain().getXPath().replaceAll("(\\[.*\\])", "");
        if (node.getNodes().isEmpty()) {
            valueXpaths.add(nodeXpath);
        }
        if (!node.getQualifierMarkers().isEmpty()) {
            for (QualifierMarker qualifierMarker : node.getQualifierMarkers()) {
                qualifierXpaths.add(nodeXpath + "/" + qualifierMarker.getRelativeXPath().replaceAll("(\\[.*\\])", ""));
            }
        }
        if (!node.getQualifiers().isEmpty()) {
            for (Qualifier qualifier : node.getQualifiers()) {

                qualifierXpaths.add(nodeXpath + "/" + qualifier.getRelativeXPath().replaceAll("(\\[.*\\])", ""));
            }
        }
        for (Node child : node.getNodes()) {
            getXpaths(child, qualifierXpaths, valueXpaths);
        }

    }

    public void qualifyNode(Node node, String value) {
        QualifierMarker qualifierMarker = node.getQualifierMarkers().get(0);
        String topOriginalDomainXpath = node.getDomain().getXPath();
        String topNewDomainXpath = topOriginalDomainXpath + "[" + qualifierMarker.getRelativeXPath() + "=" + value + "]";
        String domainGUID = generateGUID();
        String baseDomainGUID = generateGUID();

        node.toBuilder()
                .nodeStatus(NodeStatus.builder()
                        .status("Default")
                        .comment("")
                        .build())
                .xMLNodeName(node.getXMLNodeName() + "_gq_" + value)
                .domain(Domain.builder()
                        .unqualifiedDomainGUID(node.getDomain().getDomainGUID())
                        .alternativeDomainGUID(node.getDomain().getDomainGUID())
                        .xPath(topNewDomainXpath)
                        .domainGUID(domainGUID)
                        .build())
                .baseTypeDomain(Domain.builder()
                        .parentDomainGUID(domainGUID)
                        .unqualifiedDomainGUID(node.getBaseTypeDomain().getDomainGUID())
                        .domainGUID(baseDomainGUID)
                        .build())
                .qualifierMarkers(Collections.emptyList())
                .qualifiers(Arrays.asList(Qualifier.builder()
                                .qualifierValues(Collections.singletonList(QualifierValue.builder()
                                        .vertexGUID(generateGUID())
                                        .qualifierName("Document/message date/time")
                                        .qualifierValue(value)
                                        .build()))
                                .qualifierNotUsed(false)
                                .id(qualifierMarker.getId())
                                .qualifyingNodeVertexGUID(qualifierMarker.getQualifyingNodeVertexGUID())
                                .codelistReferenceVertexGUID(qualifierMarker.getCodelistReferenceVertexGUID())
                                .qualifierType(qualifierMarker.getQualifierType())
                                .relativeXPath(qualifierMarker.getRelativeXPath())
                        .build()));

    }

    public static String generateGUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    public void findQualifierMarkers(List<Node> nodes, List<Node> results) {
        for (Node node : nodes) {
            if (!node.getQualifierMarkers().isEmpty()) {
                if ("/51C96EACDDBAE8ED:Interchange/ORDERS/DTM".equals(node.getDomain().getXPath())) {
                    results.add(node);
                }
            }
            findQualifierMarkers(node.getNodes(), results);
        }
    }
}
