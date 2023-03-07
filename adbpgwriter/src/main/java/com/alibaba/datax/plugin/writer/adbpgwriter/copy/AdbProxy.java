package net.tbsoft.datax.plugin.writer.adbpgwriter.copy;

import net.tbsoft.datax.common.plugin.RecordReceiver;

import java.sql.Connection;
/**
 * @author yuncheng
 */
public interface AdbProxy {
    public abstract void startWriteWithConnection(RecordReceiver recordReceiver, Connection connection);

    public void closeResource();
}
