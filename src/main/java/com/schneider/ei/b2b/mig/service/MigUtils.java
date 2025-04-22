package com.schneider.ei.b2b.mig.service;

import com.schneider.ei.b2b.mig.model.MigAutomationException;
import com.schneider.ei.b2b.mig.model.migs.Node;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MigUtils {

    public static String generateGUID() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    public static String generateGUIDv2() {
        return UUID.randomUUID().toString();
    }

    public void populateParent(Node node) {
        for (Node child : node.getNodes()) {
            child.setParent(node);
            populateParent(child);
        }
    }

    public Node findNode(String xPath, List<Node> nodes) {
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

    public List<Path> listFilesUsingFileWalk(String dir) throws MigAutomationException {
        try (Stream<Path> stream = Files.walk(Paths.get(dir))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new MigAutomationException("Error reading files from directory: " + dir, e);
        }
    }

    public String getXPath(Element element) {
        if (element == null || !(element instanceof Element)) {
            return null;
        }

        String elementValue = element.getFirstChild().getTextContent();

        if (element.getParentNode() == null || !(element.getParentNode() instanceof Element)) {
            return "/" + element.getTagName(); // Root element
        }

        StringBuilder path = new StringBuilder();
        path.insert(0, "[" + elementValue + "]");
        org.w3c.dom.Node currentNode = element;
        while (currentNode != null && currentNode instanceof Element) {
            Element currentElement = (Element) currentNode;
            String tagName = currentElement.getTagName();
            org.w3c.dom.Node parentNode = currentElement.getParentNode();

            if (parentNode != null && parentNode instanceof Element) {
                NodeList siblings = parentNode.getChildNodes();
                for (int i = 0; i < siblings.getLength(); i++) {
                    org.w3c.dom.Node sibling = siblings.item(i);
                    if (sibling == currentElement) {
                        break;
                    }
                }
                path.insert(0, "/" + tagName);
            } else {
                path.insert(0, "/" + tagName);
            }
            currentNode = parentNode;
        }

        return path.toString();
    }
}
