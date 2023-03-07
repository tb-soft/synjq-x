package net.tbsoft.datax.plugin.writer.otswriter.model;

import net.tbsoft.datax.common.element.Record;

public class RowPutChangeWithRecord extends com.aliyun.openservices.ots.model.RowPutChange implements WithRecord {

    private Record record;

    public RowPutChangeWithRecord(String tableName) {
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
