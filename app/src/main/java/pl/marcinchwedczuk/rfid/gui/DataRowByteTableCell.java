package pl.marcinchwedczuk.rfid.gui;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.TextFieldTableCell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataRowByteTableCell extends TextFieldTableCell<DataRow, Byte> {
    private static final Logger logger = LogManager.getLogger(DataRowByteTableCell.class);

    private final DataRowTableCellStylist stylist = new DataRowTableCellStylist();

    public DataRowByteTableCell(ReadOnlyObjectProperty<Encoding> encoding) {
        super(new DataRowByteStringConverter(encoding));
    }

    @Override
    public void updateItem(Byte item, boolean empty) {
        super.updateItem(item, empty);
        stylist.styleCellOnUpdate(this, empty);
    }

    @Override
    public void commitEdit(Byte pattern) {
        if (!isEditing()) return;

        boolean validValue = (pattern != null);
        stylist.styleOnEditCommit(this, validValue);
        if (validValue) {
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
