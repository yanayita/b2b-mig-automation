package org.example;

import io.xlate.edi.schema.EDISchemaException;
import io.xlate.edi.schema.Schema;
import io.xlate.edi.schema.SchemaFactory;
import io.xlate.edi.stream.EDIInputFactory;
import io.xlate.edi.stream.EDIOutputFactory;
import io.xlate.edi.stream.EDIStreamConstants;
import io.xlate.edi.stream.EDIStreamEvent;
import io.xlate.edi.stream.EDIStreamException;
import io.xlate.edi.stream.EDIStreamReader;
import io.xlate.edi.stream.EDIStreamValidationError;
import io.xlate.edi.stream.EDIStreamWriter;
import io.xlate.edi.stream.Location;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainStaedi {

    public static void main(String... args) throws IOException, EDIStreamException, EDISchemaException {
        MainStaedi mainStaedi = new MainStaedi();
        mainStaedi.parseFile();
    }

    public void parseFile() throws IOException, EDIStreamException, EDISchemaException {
        InputStream inputStream = new FileInputStream("./src/main/resources/20241217_00000021._IE.20241118105810");
        EDIInputFactory factory = EDIInputFactory.newFactory();
        EDIStreamReader reader = factory.createEDIStreamReader(inputStream);



        Set<String> uniqueElements = new HashSet<>();
        List<EDIStreamValidationError> errors = new ArrayList<>();
        List<Location> errorLocations = new ArrayList<>();// Store unique elements
        while (reader.hasNext()) {
            EDIStreamEvent event = reader.next();
            switch (event) {
                case START_INTERCHANGE:
                    /* Retrieve the standard - "X12", "EDIFACT", or "TRADACOMS" */
                    String standard = reader.getStandard();

                    /*
                     * Retrieve the version string array. An array is used to support
                     * the componentized version element used in the EDIFACT standard.
                     *
                     * e.g. [ "00501" ] (X12) or [ "UNOA", "3" ] (EDIFACT)
                     */
                    String[] version = reader.getVersion();
                    System.out.println("standard : " + standard + " - " + version);
                    SchemaFactory schemaFactory = SchemaFactory.newFactory();
                    Schema schema = schemaFactory.getControlSchema(EDIStreamConstants.Standards.EDIFACT, new String[]{"D","96", "A"});
                    reader.setControlSchema(schema);
                    break;

                case START_SEGMENT:
                    // Retrieve the segment name - e.g. "ISA" (X12), "UNB" (EDIFACT), or "STX" (TRADACOMS)
                    String segmentName = reader.getText();
                    System.out.println("segment : " + segmentName);
                    break;

                case END_SEGMENT:
                    break;

                case START_COMPOSITE:
                    break;

                case END_COMPOSITE:
                    break;

                case ELEMENT_DATA:
                    // Retrieve the value of the current element
                    String data = reader.getText();
                    System.out.println("data : " + data);
                    break;
                case SEGMENT_ERROR:
                case ELEMENT_OCCURRENCE_ERROR:
                case ELEMENT_DATA_ERROR:
                    errors.add(reader.getErrorType());
                    errorLocations.add(reader.getLocation().copy());
                    break;
                default:
                    System.out.println("unknown : " + event);
                    break;
            }
        }
        inputStream.close();


        // Generate output file
        String outputFilePath = "./output/output.edi";
        OutputStream outputStream = new FileOutputStream(outputFilePath);
        EDIOutputFactory outputFactory = EDIOutputFactory.newFactory();
        EDIStreamWriter writer = outputFactory.createEDIStreamWriter(outputStream);

        // Write EDIFACT structure and unique elements (conceptual - StAEDI API details needed)
        writer.startInterchange();
        for (String uniqueElement : uniqueElements) {
            // Write each unique element as a segment/element in the output
             writer.writeStartSegment("UNH"); // Example segment - adapt as needed
            writer.writeElement(uniqueElement);
            // writer.writeSegmentEnd();
        }
    }
}
