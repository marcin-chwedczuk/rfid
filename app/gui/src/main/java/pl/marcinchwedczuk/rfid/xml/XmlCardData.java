package pl.marcinchwedczuk.rfid.xml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import pl.marcinchwedczuk.rfid.card.acr122.Block;
import pl.marcinchwedczuk.rfid.card.acr122.Sector;
import pl.marcinchwedczuk.rfid.gui.DataRow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@JacksonXmlRootElement(localName = "cardData")
public class XmlCardData {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "row")
    public List<XmlDataRow> rows;

    XmlCardData() {
        this.rows = new ArrayList<>();
    }

    public XmlCardData(Stream<DataRow> rows) {
        this.rows = rows
                .map(XmlDataRow::new)
                .collect(toList());
    }

    public List<DataRow> toDataRows() {
        return rows.stream()
                .map(row -> new DataRow(
                        Sector.of(row.sector),
                        Block.of(row.block),
                        toArrayOfBytes(row.bytes)))
                .collect(toList());
    }

    private byte[] toArrayOfBytes(List<XmlByte> bytes) {
        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            result[i] = bytes.get(i).toByte();
        }
        return result;
    }

    public String toXml() {
        XmlMapper xmlMapper = createMapper();

        try {
            return xmlMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Serialization failed.", e);
        }
    }

    public static XmlCardData fromXml(String xml) {
        XmlMapper xmlMapper = createMapper();
        try {
            return xmlMapper.readValue(xml, XmlCardData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Deserialization failed.", e);
        }
    }

    private static XmlMapper createMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return xmlMapper;
    }
}
