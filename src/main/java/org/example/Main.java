package org.example;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.cartridges.edifact.EdifactReaderConfigurator;
import org.smooks.edifact.binding.d96a.Interchange;
import org.smooks.edifact.binding.service.UNBInterchangeHeader;
import org.smooks.edifact.binding.service.UNHMessageHeader;
import org.smooks.engine.DefaultApplicationContextBuilder;
import org.smooks.engine.report.HtmlReportGenerator;
import org.smooks.io.sink.JavaSink;
import org.smooks.io.sink.WriterSink;
import org.smooks.io.source.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws JAXBException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        Main main = new Main();
        //main.runSmooksTransform3();
        //UNHMessageHeader unhMessageHeader = (UNHMessageHeader)((JAXBElement<?>)interchange.getMessage().getFirst().getContent().getFirst()).getValue();


        //System.out.println(interchange);

        Interchange interchange = main.runSmooksTransform3();
        //System.out.println(interchange);
        /*
        compositeSeparator = ":"
        fieldSeparator = "+"
        decimalSeparator = "."
        escapeCharacter = "?"
        repeatSeparator = " "
        segmentTerminator = "'"
         */
    }

    public void runSmooksTransform2() throws IOException, SAXException, SmooksException, JAXBException {
        // Configure Smooks using a Smooks config...
        Smooks smooks = new Smooks(new DefaultApplicationContextBuilder().withClassLoader(Main.class.getClassLoader()).build());
        //smooks.addResourceConfigs("smooks-config.bak.xml");
        smooks.setReaderConfig(new EdifactReaderConfigurator("/d96a/EDIFACT-Messages.dfdl.xsd").setMessageTypes(Arrays.asList("ORDERS", "ORDRSP")));
        ExecutionContext executionContext = smooks.createExecutionContext();

        try {
            JavaSink javaSink = new JavaSink();
            // Configure the execution context to generate a report...
            executionContext.getContentDeliveryRuntime().addExecutionEventListener(new HtmlReportGenerator("output/report/report.html", executionContext.getApplicationContext()));

            smooks.filterSource(executionContext, new StreamSource<>(Main.class.getResourceAsStream("/20241217_00000021._IE.20241118105810")), javaSink);

            javaSink.getBean("order");
        } finally {
            smooks.close();
        }

    }

    public void runSmooksTransform() throws IOException, SAXException, SmooksException, JAXBException {
        // Configure Smooks using a Smooks config...
        Smooks smooks = new Smooks(new DefaultApplicationContextBuilder().withClassLoader(Main.class.getClassLoader()).build());
        smooks.addResourceConfigs("smooks-config.bak.xml");
        ExecutionContext context = smooks.createExecutionContext();

        try {
            JavaSink javaSink = new JavaSink();
            UNBInterchangeHeader unbHeader = new UNBInterchangeHeader();
            smooks.filterSource(context, new StreamSource<>(Main.class.getResourceAsStream("/20241217_00000021._IE.20241118105810")), javaSink);
            javaSink.getBean("edifactInfo");

        } finally {
            smooks.close();
        }
    }

    public Interchange runSmooksTransform3() throws IOException, SAXException, SmooksException, JAXBException, ParserConfigurationException, XPathExpressionException {
        // Configure Smooks using a Smooks config...
        Smooks smooks = new Smooks(new DefaultApplicationContextBuilder().withClassLoader(Main.class.getClassLoader()).build());
        smooks.addResourceConfigs("smooks-config.bak.xml");
        ExecutionContext context = smooks.createExecutionContext();

        try {
            final StringWriter writer = new StringWriter();
            smooks.filterSource(context, new StreamSource<>(Main.class.getResourceAsStream("/20241217_00000021._IE.20241118105810")), new WriterSink<>(writer));

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8)));
            doc.getDocumentElement().normalize();
            XPath xPath = XPathFactory.newInstance().newXPath();

            xPath.setNamespaceContext(new NamespaceContext() {
                @Override
                public Iterator getPrefixes(String arg0) {
                    return null;
                }

                @Override
                public String getPrefix(String arg0) {
                    if ("http://www.ibm.com/dfdl/edi/un/edifact/D96A".equals(arg0)) {
                        return "D96A";
                    }
                    return null;
                }

                @Override
                public String getNamespaceURI(String arg0) {
                    if ("D96A".equals(arg0)) {
                        return "http://www.ibm.com/dfdl/edi/un/edifact/D96A";
                    }
                    return null;
                }
            });
            String expression = "/D96A:Interchange/D96A:Message/D96A:ORDERS/SegGrp-2/NAD/C082/E3039/../E3055";
            XPathExpression expr = xPath.compile(expression);
            NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            Set<String> xpathsFound = new HashSet<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);
                xpathsFound.add(getXPath(element));
            }

            for (String xpath : xpathsFound) {
                System.out.println(xpath);
            }

            JAXBContext jaxbContext = JAXBContext.newInstance(Interchange.class, org.smooks.edifact.binding.service.ObjectFactory.class, org.smooks.edifact.binding.d96a.ObjectFactory.class);
            return (Interchange) jaxbContext.createUnmarshaller().unmarshal(new javax.xml.transform.stream.StreamSource(new StringReader(writer.toString())));
        } finally {
            smooks.close();
        }


    }

    public static String getXPath(Element element) {
        if (element == null || !(element instanceof Element)) {
            return null;
        }

        String elementValue = element.getFirstChild().getTextContent();

        if (element.getParentNode() == null || !(element.getParentNode() instanceof Element)) {
            return "/" + element.getTagName(); // Root element
        }

        StringBuilder path = new StringBuilder();
        path.insert(0, "[" + elementValue + "]");
        Node currentNode = element;
        while (currentNode != null && currentNode instanceof Element) {
            Element currentElement = (Element) currentNode;
            String tagName = currentElement.getTagName();
            Node parentNode = currentElement.getParentNode();

            if (parentNode != null && parentNode instanceof Element) {
                NodeList siblings = parentNode.getChildNodes();
                for (int i = 0; i < siblings.getLength(); i++) {
                    Node sibling = siblings.item(i);
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