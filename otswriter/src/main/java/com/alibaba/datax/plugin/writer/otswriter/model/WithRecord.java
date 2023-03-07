package net.tbsoft.datax.plugin.writer.otswriter.model;

import net.tbsoft.datax.common.element.Record;

public interface WithRecord {
    Record getRecord();

    void setRecord(Record record);
}
