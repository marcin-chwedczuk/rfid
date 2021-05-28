package pl.marcinchwedczuk.rfid.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import pl.marcinchwedczuk.rfid.gui.DataRow;

import java.util.ArrayList;
import java.util.List;

public class XmlDataRow {
    @JacksonXmlProperty(isAttribute = true)
    public int sector;

    @JacksonXmlProperty(isAttribute = true)
    public int block;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "byte")
    public List<XmlByte> bytes;

    public XmlDataRow() {
    }

    public XmlDataRow(DataRow dataRow) {
        this.sector = dataRow.sector;
        this.block = dataRow.block;

        this.bytes = new ArrayList<>();
        for (byte b : dataRow.bytes) {
            this.bytes.add(new XmlByte(b));
        }
    }
}
