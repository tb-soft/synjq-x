package com.starrocks.connector.datax.plugin.writer.starrockswriter.row;

import java.io.Serializable;

import net.tbsoft.datax.common.element.Record;

public interface StarRocksISerializer extends Serializable {

    String serialize(Record row);
    
}
