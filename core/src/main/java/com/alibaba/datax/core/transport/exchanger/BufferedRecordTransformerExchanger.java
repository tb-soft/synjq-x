package net.tbsoft.datax.core.transport.exchanger;

import net.tbsoft.datax.common.element.Record;
import net.tbsoft.datax.common.exception.CommonErrorCode;
import net.tbsoft.datax.common.exception.DataXException;
import net.tbsoft.datax.common.plugin.RecordReceiver;
import net.tbsoft.datax.common.plugin.RecordSender;
import net.tbsoft.datax.common.plugin.TaskPluginCollector;
import net.tbsoft.datax.common.util.Configuration;
import net.tbsoft.datax.core.statistics.communication.Communication;
import net.tbsoft.datax.core.transport.channel.Channel;
import net.tbsoft.datax.core.transport.record.TerminateRecord;
import net.tbsoft.datax.core.transport.transformer.TransformerExecution;
import net.tbsoft.datax.core.util.FrameworkErrorCode;
import net.tbsoft.datax.core.util.container.CoreConstant;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BufferedRecordTransformerExchanger extends TransformerExchanger implements RecordSender, RecordReceiver {

    private final Channel channel;

    private final Configuration configuration;

    private final List<Record> buffer;

    private int bufferSize;

    protected final int byteCapacity;

    private final AtomicInteger memoryBytes = new AtomicInteger(0);

    private int bufferIndex = 0;

    private static Class<? extends Record> RECORD_CLASS;

    private volatile boolean shutdown = false;


    @SuppressWarnings("unchecked")
    public BufferedRecordTransformerExchanger(final int taskGroupId, final int taskId,
                                              final Channel channel, final Communication communication,
                                              final TaskPluginCollector pluginCollector,
                                              final List<TransformerExecution> tInfoExecs) {
        super(taskGroupId, taskId, communication, tInfoExecs, pluginCollector);
        assert null != channel;
        assert null != channel.getConfiguration();

        this.channel = channel;
        this.configuration = channel.getConfiguration();

        this.bufferSize = configuration
                .getInt(CoreConstant.DATAX_CORE_TRANSPORT_EXCHANGER_BUFFERSIZE);
        this.buffer = new ArrayList<Record>(bufferSize);

        //channel的queue默认大小为8M，原来为64M
        this.byteCapacity = configuration.getInt(
                CoreConstant.DATAX_CORE_TRANSPORT_CHANNEL_CAPACITY_BYTE, 8 * 1024 * 1024);

        try {
            BufferedRecordTransformerExchanger.RECORD_CLASS = ((Class<? extends Record>) Class
                    .forName(configuration.getString(
                            CoreConstant.DATAX_CORE_TRANSPORT_RECORD_CLASS,
                            "net.tbsoft.datax.core.transport.record.DefaultRecord")));
        } catch (Exception e) {
            throw DataXException.asDataXException(
                    FrameworkErrorCode.CONFIG_ERROR, e);
        }
    }

    @Override
    public Record createRecord() {
        try {
            return BufferedRecordTransformerExchanger.RECORD_CLASS.newInstance();
        } catch (Exception e) {
            throw DataXException.asDataXException(
                    FrameworkErrorCode.CONFIG_ERROR, e);
        }
    }

    @Override
    public void sendToWriter(Record record) {
        if (shutdown) {
            throw DataXException.asDataXException(CommonErrorCode.SHUT_DOWN_TASK, "");
        }

        Validate.notNull(record, "record不能为空.");

        record = doTransformer(record);

        if(record == null){
            return;
        }

        if (record.getMemorySize() > this.byteCapacity) {
            this.pluginCollector.collectDirtyRecord(record, new Exception(String.format("单条记录超过大小限制，当前限制为:%s", this.byteCapacity)));
            return;
        }

        boolean isFull = (this.bufferIndex >= this.bufferSize || this.memoryBytes.get() + record.getMemorySize() > this.byteCapacity);
        if (isFull) {
            flush();
        }

        this.buffer.add(record);
        this.bufferIndex++;
        memoryBytes.addAndGet(record.getMemorySize());
    }

    @Override
    public void flush() {
        if (shutdown) {
            throw DataXException.asDataXException(CommonErrorCode.SHUT_DOWN_TASK, "");
        }
        this.channel.pushAll(this.buffer);
        //和channel的统计保持同步
        doStat();
        this.buffer.clear();
        this.bufferIndex = 0;
        this.memoryBytes.set(0);
    }

    @Override
    public void terminate() {
        if (shutdown) {
            throw DataXException.asDataXException(CommonErrorCode.SHUT_DOWN_TASK, "");
        }
        flush();
        this.channel.pushTerminate(TerminateRecord.get());
    }

    @Override
    public Record getFromReader() {
        if (shutdown) {
            throw DataXException.asDataXException(CommonErrorCode.SHUT_DOWN_TASK, "");
        }
        boolean isEmpty = (this.bufferIndex >= this.buffer.size());
        if (isEmpty) {
            receive();
        }

        Record record = this.buffer.get(this.bufferIndex++);
        if (record instanceof TerminateRecord) {
            record = null;
        }
        return record;
    }

    @Override
    public void shutdown() {
        shutdown = true;
        try {
            buffer.clear();
            channel.clear();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void receive() {
        this.channel.pullAll(this.buffer);
        this.bufferIndex = 0;
        this.bufferSize = this.buffer.size();
    }
}
