package pl.marcinchwedczuk.rfid.gui.abstractions;

public interface ProgressDialog {
    void updateProgress(double percentage);

    void close();
}
