package pl.marcinchwedczuk.rfid.gui;

import javafx.application.Platform;
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
            editNextCell();
        }
    }

    private void editNextCell() {
        DataRow row = this.getTableRow().getItem();
        var items = this.getTableView().getItems();

        if (row == null || items == null) return;

        // Find index
        int rowIndex = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == row) {
                rowIndex = i;
                break;
            }
        }

        var thisColumn = this.getTableColumn();
        var allColumns = this.getTableView().getColumns();
        int columnIndex = -1;
        for (int i = 0; i < allColumns.size(); i++) {
            if (allColumns.get(i) == thisColumn) {
                columnIndex = i;
                break;
            }
        }

        if (rowIndex == -1 || columnIndex == -1 )
            return;

        columnIndex += 1; // Select next column
        if (columnIndex >= allColumns.size()-1)
            return;

        int finalRowIndex = rowIndex, finalColumnIndex = columnIndex;
        Platform.runLater(() -> {
            this.getTableView().edit(finalRowIndex, allColumns.get(finalColumnIndex));
        });
    }
}
