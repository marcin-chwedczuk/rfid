package pl.marcinchwedczuk.rfid.xml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import pl.marcinchwedczuk.rfid.lib.ByteUtils;

public class XmlByte {
    public String hexByte;

    @JsonCreator
    public XmlByte(final String s) {
        this.hexByte = s;
    }

    public XmlByte(byte b) {
        this.hexByte = String.format("%02X", ((int)b & 0xFF));
    }

    public byte toByte() {
        return (byte)Integer.parseInt(hexByte, 16);
    }

    @JsonValue
    public String toXmlValue() {
        return hexByte;
    }
}
