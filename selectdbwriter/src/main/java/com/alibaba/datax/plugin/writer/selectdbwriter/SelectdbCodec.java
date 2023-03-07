package net.tbsoft.datax.plugin.writer.selectdbwriter;

import net.tbsoft.datax.common.element.Record;

import java.io.Serializable;

public interface SelectdbCodec extends Serializable {

    String codec( Record row);
}
