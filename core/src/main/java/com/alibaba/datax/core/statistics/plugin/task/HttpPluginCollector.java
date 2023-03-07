package net.tbsoft.datax.core.statistics.plugin.task;

import net.tbsoft.datax.common.constant.PluginType;
import net.tbsoft.datax.common.element.Record;
import net.tbsoft.datax.common.util.Configuration;
import net.tbsoft.datax.core.statistics.communication.Communication;

/**
 * Created by jingxing on 14-9-9.
 */
public class HttpPluginCollector extends AbstractTaskPluginCollector {
    public HttpPluginCollector(Configuration configuration, Communication Communication,
                               PluginType type) {
        super(configuration, Communication, type);
    }

    @Override
    public void collectDirtyRecord(Record dirtyRecord, Throwable t,
                                   String errorMessage) {
        super.collectDirtyRecord(dirtyRecord, t, errorMessage);
    }

}
