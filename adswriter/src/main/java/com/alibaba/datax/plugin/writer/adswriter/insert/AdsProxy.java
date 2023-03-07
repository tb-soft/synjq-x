package net.tbsoft.datax.plugin.writer.adswriter.insert;

import net.tbsoft.datax.common.plugin.RecordReceiver;

import java.sql.Connection;

public interface AdsProxy {
    public abstract void startWriteWithConnection(RecordReceiver recordReceiver, Connection connection,
                                                  int columnNumber);

    public void closeResource();
}
