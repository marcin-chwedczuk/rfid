package pl.marcinchwedczuk.rfid.gui;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.TextFieldTableCell;

public class DataRowByteTableCell extends TextFieldTableCell<DataRow, Byte> {
    private final PseudoClass cssReadOnlyCell = PseudoClass.getPseudoClass("readonly-table-cell");
    private final PseudoClass cssInvalidCell = PseudoClass.getPseudoClass("invalid-table-cell");

    public DataRowByteTableCell(ReadOnlyBooleanProperty hexMode) {
        super(new DataRowByteStringConverter(hexMode));

        super.itemProperty().addListener((prop, newValue, oldValue) -> {
            TableRow<DataRow> row = this.getTableRow();

            boolean editable =
                    row.getItem() != null &&
                    row.getItem().isSectorTrailer;

            this.setEditable(editable);
            this.pseudoClassStateChanged(cssReadOnlyCell, !this.isEditable());
        });
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
