package pl.marcinchwedczuk.rfid.lib;

import java.util.Arrays;

/**
 * ATR (Answer To Reset) information.
 *
 * This info is send by card to the reader upon first contact.
 */
public class AtrInfo {
    public final CardName cardName;
    public final CardStandard cardStandard;

    private AtrInfo(CardName cardName, CardStandard cardStandard) {
        this.cardName = cardName;
        this.cardStandard = cardStandard;
    }

    public static AtrInfo parse(byte[] atrBytes) {
        assertCond(atrBytes.length == 20, "Expecting 19 bytes for MIFARE ATR, but got %d.", atrBytes.length);

        assertByte(atrBytes[0], 0x3B, "TS");
        assertByte(atrBytes[1], 0x8F, "T0");
        assertByte(atrBytes[2], 0x80, "TD1");
        assertByte(atrBytes[3], 0x01, "TD2");

        byte[] historicalBytes = new byte[15];
        System.arraycopy(atrBytes, 4, historicalBytes, 0, historicalBytes.length);

        assertByte(historicalBytes[0], 0x80, "H1");
        assertByte(historicalBytes[1], 0x4F, "H2 (Application identifier presence indicator)");
        assertByte(historicalBytes[2], 0x0C, "H3 (Length to follow - 12 bytes)");

        byte[] registeredAppProviderId = new byte[5];
        byte[] expectedRegisteredAppProviderId = new byte[] { (byte)0xA0, 0x00, 0x00, 0x03, 0x06 };
        System.arraycopy(historicalBytes, 3, registeredAppProviderId, 0, 5);
        assertCond(
                Arrays.equals(registeredAppProviderId, expectedRegisteredAppProviderId),
                "Registered Application Provider Identifier is not the expected 'PC/SC workgroup'. " +
                "Expected bytes %s but got bytes %s.",
                ByteUtils.asHexString(registeredAppProviderId, ":"),
                ByteUtils.asHexString(expectedRegisteredAppProviderId, ":"));

        int pixSS = historicalBytes[8];
        CardStandard cardStandard = CardStandard.fromPixSS(pixSS);

        int pixNN = historicalBytes[9] << 8 | historicalBytes[10];
        CardName cardName = CardName.fromPixNN(pixNN);

        // Assert RFU bytes are zeros
        for (int i = 11, bc = 0; i < 15; i++, bc++) {
            assertByte(historicalBytes[i], 0, "RFU" + bc);
        }

        validateChecksum(atrBytes);

        return new AtrInfo(cardName, cardStandard);
    }

    private static void assertByte(int actual, int expected, String byteName) {
        actual = actual & 0xFF;
        expected = expected & 0xFF;
        assertCond(actual == expected, "ATR %s byte should be 0x%02x but is 0x%02x.", byteName, expected, actual);
    }

    private static void assertCond(boolean cond, String errorFormat, Object... args) {
        if (!cond) {
            throw new AcrException(String.format(errorFormat, args));
        }
    }

    private static void validateChecksum(byte[] atrBytes) {
        int sum = 0;

        // First byte is not accounted in the check sum
        for (int i = 1; i < atrBytes.length; i++) {
            sum ^= atrBytes[i];
        }

        assertCond((sum & 0xFF) == 0, "ATR Checksum mismatch.");
    }
}
