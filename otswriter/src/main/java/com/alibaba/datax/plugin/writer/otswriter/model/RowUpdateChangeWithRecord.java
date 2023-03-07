package net.tbsoft.datax.plugin.writer.otswriter.model;

import net.tbsoft.datax.common.element.Record;

public class RowUpdateChangeWithRecord extends com.aliyun.openservices.ots.model.RowUpdateChange implements WithRecord {

    private Record record;

    public RowUpdateChangeWithRecord(String tableName) {
        super(tableName);
    }

    @Override
    public Record getRecord() {
        return record;
    }

    @Override
    public void setRecord(Record record) {
        this.record = record;
    }
}
