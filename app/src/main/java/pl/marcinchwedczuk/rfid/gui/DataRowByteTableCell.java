package pl.marcinchwedczuk.rfid.gui;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.TextFieldTableCell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataRowByteTableCell extends TextFieldTableCell<DataRow, Byte> {
    private static final Logger logger = LogManager.getLogger(DataRowByteTableCell.class);

    private final PseudoClass cssReadOnlyCell = PseudoClass.getPseudoClass("readonly-table-cell");
    private final PseudoClass cssInvalidCell = PseudoClass.getPseudoClass("invalid-table-cell");

    public DataRowByteTableCell(ReadOnlyBooleanProperty hexMode) {
        super(new DataRowByteStringConverter(hexMode));

        /*
        super.itemProperty().addListener((prop, oldValue, newValue) -> {
            try {

            } catch (Exception e) {
                logger.error("ITEM PROPERTY", e);
            }
        });*/
    }

    @Override
    public void updateItem(Byte item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            this.setEditable(true);
            this.pseudoClassStateChanged(cssReadOnlyCell, false);
            return;
        }

        TableRow<DataRow> row = this.getTableRow();

        boolean editable = row.getItem() != null &&
                !row.getItem().isSectorTrailer;

        this.setEditable(editable);
        this.pseudoClassStateChanged(cssReadOnlyCell, !editable);
    }

    @Override
    public void commitEdit(Byte pattern) {
        if (!isEditing()) return;
        pseudoClassStateChanged(cssInvalidCell, pattern == null);
        if (pattern != null) {
            super.commitEdit(pattern);
        }
    }
}
