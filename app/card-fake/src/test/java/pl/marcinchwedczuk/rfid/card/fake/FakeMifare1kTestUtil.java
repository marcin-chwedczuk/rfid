package pl.marcinchwedczuk.rfid.card.fake;

import org.assertj.core.api.AbstractStringAssert;
import pl.marcinchwedczuk.rfid.card.commons.ByteArrays;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.marcinchwedczuk.rfid.card.fake.KeyType.KEY_A;

public class FakeMifare1kTestUtil {
    public static final String RESP_OK = "90 00";
    public static final String RESP_ERR = "63 00";

    private final FakeCard card;

    public FakeMifare1kTestUtil(FakeCard card) {
        this.card = Objects.requireNonNull(card);
    }

    public void assertBlockOnCardContainsData(int blockNumber, String expectedData) {
        ResponseAPDU response = execReadBlock(blockNumber);
        assertThatResponseBytes(response)
                .isEqualTo(expectedData + " 90 00");
    }

    public void assertErrorWhenReadingBlock(int blockNumber) {
        ResponseAPDU response = execReadBlock(blockNumber);
        assertThatResponseBytes(response)
                .isEqualTo(RESP_ERR);
    }

    public void assertCanWriteData(int blockNumber, String data) {
        ResponseAPDU response = execWriteBlock(blockNumber, data);
        assertThatResponseBytes(response)
                .isEqualTo(RESP_OK);

        // Check data was actually written
        assertBlockOnCardContainsData(blockNumber, data);
    }

    public void assertCannotWriteData(int blockNumber) {
        assertCannotWriteData(blockNumber, "DE AD BE EF FF FF FF FF 01 02 03 04 CA FE BA BE");
    }

    public void assertCannotWriteData(int blockNumber, String data) {
        ResponseAPDU dataBeforeWrite = execReadBlock(blockNumber);

        ResponseAPDU response = execWriteBlock(blockNumber, data);
        assertThatResponseBytes(response)
                .isEqualTo(RESP_ERR);

        ResponseAPDU dataAfterFailedWrite = execReadBlock(blockNumber);

        // Check data not changed
        assertThatResponseBytes(dataAfterFailedWrite)
                .isEqualTo(ByteArrays.toHexString(dataBeforeWrite.getBytes()));
    }

    public CommandAPDU cmd(String bytesHexString) {
        return new CommandAPDU(ByteArrays.fromHexString(bytesHexString));
    }

    public ResponseAPDU executeCommand(String commandHex) {
        CommandAPDU cmd = cmd(commandHex);
        return executeCommand(cmd);
    }

    public ResponseAPDU execLoadKeyToRegister(Register reg, String key) {
        CommandAPDU cmd = cmd(String.format("FF 82 00 %02X 06 %s", (reg == Register.REGISTER_0 ? 0 : 1), key));
        ResponseAPDU response = executeCommand(cmd);
        return response;
    }

    public ResponseAPDU execAuthenticateToBlockNumber(int blockNumber, KeyType keyType, Register reg) {
        CommandAPDU cmd = cmd(String.format(
                "FF 86 00 00 05 01 00 %02X %02X %02X",
                blockNumber,
                (keyType == KEY_A) ? 0x60 : 0x61,
                (reg == Register.REGISTER_0 ? 0 : 1)));

        ResponseAPDU response = executeCommand(cmd);
        return response;
    }

    public ResponseAPDU execReadBlock(int blockNumber) {
        CommandAPDU readCmd = cmd(String.format("FF B0 00 %02X 10", blockNumber));
        ResponseAPDU response = executeCommand(readCmd);
        return response;
    }

    public ResponseAPDU execWriteBlock(int blockNumber, String hexData) {
        CommandAPDU writeCmd = cmd(String.format("FF D6 00 %02X 10 %s", blockNumber, hexData));
        ResponseAPDU response = executeCommand(writeCmd);
        return response;
    }

    public ResponseAPDU executeCommand(CommandAPDU cmd) {
        try {
            return card
                    .getBasicChannel()
                    .transmit(cmd);
        } catch (CardException e) {
            throw new RuntimeException(e);
        }
    }

    public AbstractStringAssert<?> assertThatResponseBytes(ResponseAPDU r) {
        return assertThat(ByteArrays.toHexString(r.getBytes()));
    }

    public int toBlockNumber(int sectorIndex, int blockIndex) {
        return 4*sectorIndex + blockIndex;
    }
}
