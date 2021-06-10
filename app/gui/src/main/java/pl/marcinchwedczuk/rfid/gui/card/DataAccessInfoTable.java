package pl.marcinchwedczuk.rfid.gui.card;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.function.Consumer;

public class DataAccessInfoTable {
    private final TableView<DataAccessInfoBean> tableView;
    private ContextMenu rowContextMenu;

    public DataAccessInfoTable(TableView<DataAccessInfoBean> tableView) {
        this.tableView = tableView;
    }

    public DataAccessInfoTable setup() {
        rowContextMenu = new ContextMenu();

        TableColumn<DataAccessInfoBean, String> accessBits = new TableColumn<>("ACCESS BITS");
        TableColumn<DataAccessInfoBean, String> c1 = dataColumn("C1", "c1");
        TableColumn<DataAccessInfoBean, String> c2 = dataColumn("C2", "c2");
        TableColumn<DataAccessInfoBean, String> c3 = dataColumn("C3", "c3");
        accessBits.getColumns().addAll(c1, c2, c3);

        TableColumn<DataAccessInfoBean, String> accessCondition = new TableColumn<>("ACCESS CONDITION FOR");
        TableColumn<DataAccessInfoBean, String> read = dataColumn("READ", "read");
        TableColumn<DataAccessInfoBean, String> write = dataColumn("WRITE", "write");
        TableColumn<DataAccessInfoBean, String> inc = dataColumn("INCREMENT", "increment");
        TableColumn<DataAccessInfoBean, String> other = dataColumn("DECREMENT\nTRANSFER\nRESTORE", "other");
        accessCondition.getColumns().addAll(read, write, inc, other);

        TableColumn<DataAccessInfoBean, String> application = dataColumn("APPLICATION", "application");

        tableView.getColumns().addAll(accessBits, accessCondition, application);

        tableView.getItems().addAll(
                new DataAccessInfoBean("0", "0", "0", "key A|B", "key A|B", "key A|B", "key A|B", "transport\nconfiguration"),
                new DataAccessInfoBean("0", "1", "0", "key A|B", "never", "never", "never", "read/write block"),
                new DataAccessInfoBean("1", "0", "0", "key A|B", "key B", "never", "never", "read/write block"),
                new DataAccessInfoBean("1", "1", "0", "key A|B", "key B", "key B", "key A|B", "value block"),
                new DataAccessInfoBean("0", "0", "1", "key A|B", "never", "never", "key A|B", "value block"),
                new DataAccessInfoBean("0", "1", "1", "key B", "key B", "never", "never", "read/write block"),
                new DataAccessInfoBean("1", "0", "1", "key B", "never", "never", "never", "read/write block"),
                new DataAccessInfoBean("1", "1", "1", "never", "never", "never", "never", "read/write block")
        );

        tableView.setContextMenu(rowContextMenu);

        return this;
    }

    public DataAccessInfoTable addRowMenuEntry(String caption, Consumer<DataAccessInfoBean> action) {
        MenuItem menuItem = new MenuItem(caption);
        menuItem.setOnAction((ActionEvent event) -> {
            DataAccessInfoBean item = tableView.getSelectionModel().getSelectedItem();
            if (item != null) {
                action.accept(item);
            }
        });

        rowContextMenu.getItems().add(menuItem);
        return this;
    }

    private TableColumn<DataAccessInfoBean, String> dataColumn(String name, String property) {
        TableColumn<DataAccessInfoBean, String> col = new TableColumn<>(name);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setSortable(false);
        col.setEditable(false);
        return col;
    }

    public static class DataAccessInfoBean {
        private final String c1;
        private final String c2;
        private final String c3;

        private final String read;
        private final String write;
        private final String increment;
        private final String other;

        private final String application;

        public DataAccessInfoBean(String c1, String c2, String c3,
                                  String read, String write, String increment, String other,
                                  String application) {
            this.c1 = c1;
            this.c2 = c2;
            this.c3 = c3;
            this.read = read;
            this.write = write;
            this.increment = increment;
            this.other = other;
            this.application = application;
        }

        public String getCBits() {
            return String.format("C%s%s%s", c1, c2, c3);
        }

        public String getC1() {
            return c1;
        }

        public String getC2() {
            return c2;
        }

        public String getC3() {
            return c3;
        }

        public String getRead() {
            return read;
        }

        public String getWrite() {
            return write;
        }

        public String getIncrement() {
            return increment;
        }

        public String getOther() {
            return other;
        }

        public String getApplication() {
            return application;
        }
    }
}
