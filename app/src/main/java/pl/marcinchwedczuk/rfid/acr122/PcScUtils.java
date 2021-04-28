package pl.marcinchwedczuk.rfid.acr122;

public class PcScUtils {
    private PcScUtils() {
    }

    public static int IOCTL_CCID_ESCAPE() {
        if (isWindows()) {
            return SCARD_CTL_CODE(3500);
        } else {
            return SCARD_CTL_CODE(1);
        }
    }

    private static int SCARD_CTL_CODE(int command) {
        if (isWindows()) {
            return 0x00310000 | (command << 2);
        } else {
            return 0x42000000 | command;
        }
    }

    private static boolean isWindows() {
        boolean isWindows =
                System.getProperty("os.name").startsWith("Windows");
        return isWindows;
    }
}
