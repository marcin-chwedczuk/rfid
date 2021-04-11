package pl.marcinchwedczuk.rfid.gui;

import javafx.css.PseudoClass;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.TextFieldTableCell;

public class DataRowTableCellStylist {
    private final PseudoClass cssReadOnlyCell = PseudoClass.getPseudoClass("readonly-table-cell");
    private final PseudoClass cssManufacturerBlock = PseudoClass.getPseudoClass("manufacturer-block-table-cell");
    private final PseudoClass cssInvalidCell = PseudoClass.getPseudoClass("invalid-table-cell");

    public void styleCellOnUpdate(TextFieldTableCell<DataRow, ?> cell, boolean empty) {
        if (empty) {
            cell.setEditable(true);
            cell.pseudoClassStateChanged(cssReadOnlyCell, false);
            return;
        }

        TableRow<DataRow> row = cell.getTableRow();

        boolean isSectorTrailer = row.getItem() != null &&
                row.getItem().isSectorTrailer;

        boolean isManufacturerBlock = row.getItem() != null &&
                row.getItem().isManufacturerDataBlock();

        boolean isEditable = (row.getItem() != null) && !isSectorTrailer && !isManufacturerBlock;

        cell.setEditable(isEditable);
        cell.pseudoClassStateChanged(cssReadOnlyCell, !isEditable);
        cell.pseudoClassStateChanged(cssManufacturerBlock, isManufacturerBlock);
    }

    public void styleOnEditCommit(TextFieldTableCell<DataRow, ?> cell, boolean validValue) {
        cell.pseudoClassStateChanged(cssInvalidCell, !validValue);
    }
}
