package net.tbsoft.datax.plugin.writer.doriswriter;

import net.tbsoft.datax.common.element.Record;

import java.io.Serializable;

public interface DorisCodec extends Serializable {

    String codec( Record row);
}
