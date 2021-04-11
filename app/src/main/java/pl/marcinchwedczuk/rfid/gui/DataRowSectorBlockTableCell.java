package pl.marcinchwedczuk.rfid.gui;

import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataRowSectorBlockTableCell extends TextFieldTableCell<DataRow, Integer> {
    private static final Logger logger = LogManager.getLogger(DataRowSectorBlockTableCell.class);

    private final DataRowTableCellStylist stylist = new DataRowTableCellStylist();

    public DataRowSectorBlockTableCell() {
        super(new IntegerStringConverter());
    }

    @Override
    public void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        stylist.styleCellOnUpdate(this, empty);
    }
}
