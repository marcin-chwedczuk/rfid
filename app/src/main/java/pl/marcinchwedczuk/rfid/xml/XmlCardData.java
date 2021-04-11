package pl.marcinchwedczuk.rfid.xml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import pl.marcinchwedczuk.rfid.gui.DataRow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@JacksonXmlRootElement(localName = "cardData")
public class XmlCardData {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "row")
    public List<XmlDataRow> rows;

    public XmlCardData() {
        this.rows = new ArrayList<>();
    }

    public XmlCardData(Stream<DataRow> rows) {
        this.rows = rows
                .map(XmlDataRow::new)
                .collect(toList());
    }

    public String toXml() {
        try {
            XmlMapper xmlMapper = new XmlMapper();
            return xmlMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
