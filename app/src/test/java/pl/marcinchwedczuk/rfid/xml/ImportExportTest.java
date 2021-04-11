package pl.marcinchwedczuk.rfid.xml;

import org.junit.jupiter.api.Test;
import pl.marcinchwedczuk.rfid.gui.DataRow;
import pl.marcinchwedczuk.rfid.lib.ByteUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.*;

class ImportExportTest {

    @Test void export() {
        Stream<DataRow> data = Arrays.stream(new DataRow[] {
            // sector, block, bytes, isSectorTrailer
            new DataRow(2, 0, ByteUtils.fromHexString("00:01:02:03:04:05:06:07:08:09:0a:0b:0c:0d:0e:0f", ":"), false),
            new DataRow(2, 3, ByteUtils.fromHexString("f0:f1:f2:f3:f4:f5:f6:f7:f8:f9:fa:fb:fc:fd:fe:ff", ":"), true),
        });

        String actualXml = new XmlCardData(data)
                .toXml()
                .trim();

        String expectedXml = readResource("/pl/marcinchwedczuk/rfid/xml/test-expected-xml-1.xml")
                // Normalize spaces
                .replaceAll("[ ]{4}", "  ")
                .trim();

        assertEquals(expectedXml, actualXml);
    }

    @Test void import_() {
        String xml = readResource("/pl/marcinchwedczuk/rfid/xml/test-expected-xml-1.xml");
        List<DataRow> dataRows = XmlCardData.fromXml(xml).toDataRows();

        List<DataRow> expected = List.of(
                // sector, block, data, trailer?
                new DataRow(2, 0, ByteUtils.fromHexString("00:01:02:03:04:05:06:07:08:09:0a:0b:0c:0d:0e:0f", ":"), false),
                new DataRow(2, 3, ByteUtils.fromHexString("f0:f1:f2:f3:f4:f5:f6:f7:f8:f9:fa:fb:fc:fd:fe:ff", ":"), true));

        assertEquals(expected, dataRows);
    }

    static String readResource(String fileName) {
        try (InputStream is = ImportExportTest.class.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(joining(System.lineSeparator()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}