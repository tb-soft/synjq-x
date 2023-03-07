package net.tbsoft.datax.plugin.writer.tdenginewriter;

import net.tbsoft.datax.common.plugin.RecordReceiver;
import net.tbsoft.datax.common.plugin.TaskPluginCollector;

public interface DataHandler {
    int handle(RecordReceiver lineReceiver, TaskPluginCollector collector);
}
