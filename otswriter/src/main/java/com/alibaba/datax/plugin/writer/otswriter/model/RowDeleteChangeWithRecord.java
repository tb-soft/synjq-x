package net.tbsoft.datax.plugin.writer.otswriter.model;

import net.tbsoft.datax.common.element.Record;

public class RowDeleteChangeWithRecord extends com.aliyun.openservices.ots.model.RowDeleteChange implements WithRecord {

    private Record record;

    public RowDeleteChangeWithRecord(String tableName) {
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
