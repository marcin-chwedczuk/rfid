package pl.marcinchwedczuk.rfid.gui;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.stream.Stream;

public class SectorTrailerAccessInfoTable {
    private final TableView<SectorTrailerAccessInfoBean> tableView;

    public SectorTrailerAccessInfoTable(TableView<SectorTrailerAccessInfoBean> tableView) {
        this.tableView = tableView;
    }

    public void setup() {
        TableColumn<SectorTrailerAccessInfoBean, String> accessBits = containerColumn("ACCESS BITS",
                dataColumn("C1", "c1"),
                dataColumn("C2", "c2"),
                dataColumn("C3", "c3")
        );

        TableColumn<SectorTrailerAccessInfoBean, String> accessCondition = containerColumn("ACCESS CONDITION FOR",
                containerColumn("KEY A",
                        dataColumn("READ", "keyARead"),
                        dataColumn("WRITE", "keyAWrite")),
                containerColumn("ACCESS BITS",
                        dataColumn("READ", "accessBitsRead"),
                        dataColumn("WRITE", "accessBitsWrite")),
                containerColumn("KEY B",
                        dataColumn("READ", "keyBRead"),
                        dataColumn("WRITE", "keyBWrite")));


        TableColumn<SectorTrailerAccessInfoBean, String> remark = dataColumn("REMARK", "remark");

        tableView.getColumns().addAll(accessBits, accessCondition, remark);

        tableView.getItems().addAll(
                new SectorTrailerAccessInfoBean("0", "0", "0", "never", "key A", "key A", "never", "key A", "key A", "key B may be read"),
                new SectorTrailerAccessInfoBean("0", "1", "0", "never", "never", "key A", "never", "key A", "never", "key B may be read"),
                new SectorTrailerAccessInfoBean("1", "0", "0", "never", "key B", "key A|B", "never", "never", "key B", ""),
                new SectorTrailerAccessInfoBean("1", "1", "0", "never", "never", "key A|B", "never", "never", "never", ""),
                new SectorTrailerAccessInfoBean("0", "0", "1", "never", "key A", "key A", "key A", "key A", "key A", "key B may be read\ntransport configuration"),
                new SectorTrailerAccessInfoBean("0", "1", "1", "never", "key B", "key A|B", "key B", "never", "key B", ""),
                new SectorTrailerAccessInfoBean("1", "0", "1", "never", "never", "key A|B", "key B", "never", "never", ""),
                new SectorTrailerAccessInfoBean("1", "1", "1", "never", "never", "key A|B", "never", "never", "never", "")
        );
    }

    private TableColumn<SectorTrailerAccessInfoBean, String> containerColumn(
            String name, TableColumn<SectorTrailerAccessInfoBean, String>... childColumns) {
        TableColumn<SectorTrailerAccessInfoBean, String> col = new TableColumn<>(name);
        col.getColumns().addAll(childColumns);
        return col;
    }

    private TableColumn<SectorTrailerAccessInfoBean, String> dataColumn(String name, String property) {
        TableColumn<SectorTrailerAccessInfoBean, String> col = new TableColumn<>(name);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setSortable(false);
        col.setEditable(false);
        return col;
    }

    public static class SectorTrailerAccessInfoBean {
        private final String c1;
        private final String c2;
        private final String c3;

        private final String keyARead;
        private final String keyAWrite;
        private final String accessBitsRead;
        private final String accessBitsWrite;
        private final String keyBRead;
        private final String keyBWrite;

        private final String remark;

        public SectorTrailerAccessInfoBean(String c1, String c2, String c3,
                                           String keyARead, String keyAWrite,
                                           String accessBitsRead, String accessBitsWrite,
                                           String keyBRead, String keyBWrite,
                                           String remark) {
            this.c1 = c1;
            this.c2 = c2;
            this.c3 = c3;
            this.keyARead = keyARead;
            this.keyAWrite = keyAWrite;
            this.accessBitsRead = accessBitsRead;
            this.accessBitsWrite = accessBitsWrite;
            this.keyBRead = keyBRead;
            this.keyBWrite = keyBWrite;
            this.remark = remark;
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

        public String getKeyARead() {
            return keyARead;
        }

        public String getKeyAWrite() {
            return keyAWrite;
        }

        public String getAccessBitsRead() {
            return accessBitsRead;
        }

        public String getAccessBitsWrite() {
            return accessBitsWrite;
        }

        public String getKeyBRead() {
            return keyBRead;
        }

        public String getKeyBWrite() {
            return keyBWrite;
        }

        public String getRemark() {
            return remark;
        }
    }
}
