package com.schneider.ei.b2b.mig.service;

import com.schneider.ei.b2b.mig.model.MigAutomationException;
import com.schneider.ei.b2b.mig.model.process.AnalysisResults;
import com.schneider.ei.b2b.mig.model.process.QualifierMarkerData;
import io.xlate.edi.stream.EDIInputFactory;
import io.xlate.edi.stream.EDIStreamEvent;
import io.xlate.edi.stream.EDIStreamReader;
import org.apache.commons.lang3.StringUtils;
import org.example.EdifactInfo;
import org.example.Main;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.cartridges.edifact.EdifactReaderConfigurator;
import org.smooks.engine.DefaultApplicationContextBuilder;
import org.smooks.engine.resource.config.DefaultResourceConfigFactory;
import org.smooks.io.sink.JavaSink;
import org.smooks.io.sink.WriterSink;
import org.smooks.io.source.StreamSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class EdifactAnalyzerService {

    @Autowired
    private MigUtils migUtils;

    private static final List<String> EDIFACT_VERSIONS = Arrays.asList("d99b", "d97a", "d93a", "d96a", "d96b");

    private final NamespaceContext namespaceContext = new NamespaceContext() {
        @Override
        public Iterator getPrefixes(String arg0) {
            return null;
        }

        @Override
        public String getPrefix(String arg0) {
            if ("http://www.ibm.com/dfdl/edi/un/edifact/D99B".equals(arg0)) {
                return "D99B";
            } else if ("http://www.ibm.com/dfdl/edi/un/edifact/D97A".equals(arg0)) {
                return "D97A";
            } else if ("http://www.ibm.com/dfdl/edi/un/edifact/D93A".equals(arg0)) {
                return "D93A";
            } else if ("http://www.ibm.com/dfdl/edi/un/edifact/D96A".equals(arg0)) {
                return "D96A";
            } else if ("http://www.ibm.com/dfdl/edi/un/edifact/D96B".equals(arg0)) {
                return "D96B";
            }
            return null;
        }

        @Override
        public String getNamespaceURI(String arg0) {
            if ("D99B".equals(arg0)) {
                return "http://www.ibm.com/dfdl/edi/un/edifact/D99B";
            } else if ("D97A".equals(arg0)) {
                return "http://www.ibm.com/dfdl/edi/un/edifact/D97A";
            } else if ("D93A".equals(arg0)) {
                return "http://www.ibm.com/dfdl/edi/un/edifact/D93A";
            } else if ("D96A".equals(arg0)) {
                return "http://www.ibm.com/dfdl/edi/un/edifact/D96A";
            } else if ("D96B".equals(arg0)) {
                return "http://www.ibm.com/dfdl/edi/un/edifact/D96B";
            }
            return null;
        }
    };

    public AnalysisResults parseEdifactFiles(String parentFolder, Set<QualifierMarkerData> qualifyingXPaths) throws MigAutomationException {
        List<Path> files =  listFilesUsingFileWalk(parentFolder);
        String version = determineEdifactVersionMultiple(files);

        Smooks smooks = new Smooks(new DefaultApplicationContextBuilder().withClassLoader(Main.class.getClassLoader()).build());
        smooks.setReaderConfig(new EdifactReaderConfigurator("/" + version + "/EDIFACT-Messages.dfdl.xsd").setMessageTypes(Arrays.asList("ORDERS", "ORDRSP", "INVOIC", "DESADV")));
        ExecutionContext executionContext = smooks.createExecutionContext();

        Map<QualifierMarkerData, Set<String>> xpathsFound = new HashMap<>();
        Set<QualifierMarkerData> selectedXPathsFound = new LinkedHashSet<>();

        for (Path file : files) {
            try {
                File inputFile = file.toFile();
                StringWriter writer = new StringWriter();
                smooks.filterSource(executionContext, new StreamSource<>(new FileInputStream(inputFile)), new WriterSink<>(writer));

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                dbFactory.setNamespaceAware(true);
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8)));
                doc.getDocumentElement().normalize();
                XPath xPath = XPathFactory.newInstance().newXPath();

                xPath.setNamespaceContext(this.namespaceContext);

                for (QualifierMarkerData qualifyingMarker : qualifyingXPaths) {
                    /**String[] pathTokens = qualifyingXPath.replaceAll("(.*):Interchange", "D96A:Interchange").split("/");
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < pathTokens.length; i++) {
                        if (pathTokens[i].contains(":")) {
                            String[] tokens = pathTokens[i].split(":");
                            pathTokens[i] = tokens[1];
                        }
                    }*/
                    String correctedPath = qualifyingMarker.getQualifyingXpath()
                            .replaceAll("(.*):Interchange", version.toUpperCase() + ":Interchange")
                            .replaceAll("/(\\d)", "/E$1")
                            .replaceAll("/ORDERS/", "/" + version.toUpperCase() + ":Message/" + version.toUpperCase() + ":ORDERS/")
                            .replaceAll("/SG", "/SegGrp-");

                    XPathExpression expr = xPath.compile(correctedPath);
                    NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

                    if (qualifyingMarker.isQualifier()) {
                        for (int i = 0; i < nodeList.getLength(); i++) {
                            Element element = (Element) nodeList.item(i);
                            Set<String> xpathSet = xpathsFound.computeIfAbsent(qualifyingMarker, k -> new HashSet<>());
                            String elementValue = element.getFirstChild().getTextContent();
                            if (StringUtils.isNotBlank(elementValue)) {
                                xpathSet.add(elementValue);
                            }
                        }
                    } else if (nodeList.getLength() > 0) {
                        selectedXPathsFound.add(qualifyingMarker);
                    }

                }



            } catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException e) {
                throw new MigAutomationException("Error processing file: " + file, e);
            }
        }
        return AnalysisResults.builder()
                .qualifierXPathsFound(xpathsFound)
                .selectedXPathsFound(selectedXPathsFound)
                .build();
    }


    private List<Path> listFilesUsingFileWalk(String dir) throws MigAutomationException {
        try (Stream<Path> stream = Files.walk(Paths.get(dir))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new MigAutomationException("Error reading files from directory: " + dir, e);
        }
    }

    public String determineEdifactVersionMultiple(List<Path> files) throws MigAutomationException {
        Set<String> versionsFound = new HashSet<>();
        for (Path path : files) {
            EdifactInfo edifactInfo = determineEdifactVersion(path.toFile().getPath());
            String version = edifactInfo.getVersion();
            if (!EDIFACT_VERSIONS.contains(version)) {
                throw new MigAutomationException("Unsupported EDIFACT version: " + version);
            }
            versionsFound.add(edifactInfo.getVersion());
        }
        if (versionsFound.size() > 1) {
            throw new MigAutomationException("Multiple EDIFACT versions found: " + versionsFound);
        }
        if (versionsFound.isEmpty()) {
            throw new MigAutomationException("No EDIFACT versions found");
        }
        return versionsFound.stream().findFirst().get();
    }

    public EdifactInfo determineEdifactVersion(String ediFilePath) throws MigAutomationException {
        EdifactInfo edifactInfo = new EdifactInfo();
        try {
            InputStream inputStream = new FileInputStream(ediFilePath);
            EDIInputFactory factory = EDIInputFactory.newFactory();
            EDIStreamReader reader = factory.createEDIStreamReader(inputStream);
            while (reader.hasNext()) {
                EDIStreamEvent event = reader.next();
                if (reader.getSchemaTypeReference() == null) {
                    continue;
                }
                String typeId = reader.getSchemaTypeReference().getReferencedType().getId();

                switch (typeId) {
                    case "DE0054":
                        edifactInfo.setE0054(reader.getText());
                        break;
                    case "DE0065":
                        edifactInfo.setE0065(reader.getText());
                        break;
                    case "DE0052":
                        edifactInfo.setE0052(reader.getText());
                        break;
                }
                if (event == EDIStreamEvent.END_SEGMENT) {
                    String segment = reader.getText();
                    if (segment.startsWith("UNH")) {
                        break;
                    }
                }
            }
            if (edifactInfo.getE0054() == null) {
                throw new MigAutomationException("Could not determine Edifact version");
            }
            return edifactInfo;
        } catch (Exception e) {
            throw new MigAutomationException(e);
        }

    }
}
