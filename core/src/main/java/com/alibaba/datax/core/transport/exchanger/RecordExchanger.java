/**
 *  (C) 2010-2014 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import java.util.List;

public class RecordExchanger extends TransformerExchanger implements RecordSender, RecordReceiver {

	private Channel channel;

	private Configuration configuration;

	private static Class<? extends Record> RECORD_CLASS;

	private volatile boolean shutdown = false;

	@SuppressWarnings("unchecked")
	public RecordExchanger(final int taskGroupId, final int taskId,final Channel channel, final Communication communication,List<TransformerExecution> transformerExecs, final TaskPluginCollector pluginCollector) {
		super(taskGroupId,taskId,communication,transformerExecs, pluginCollector);
		assert channel != null;
		this.channel = channel;
		this.configuration = channel.getConfiguration();
		try {
			RecordExchanger.RECORD_CLASS = (Class<? extends Record>) Class
					.forName(configuration.getString(
                            CoreConstant.DATAX_CORE_TRANSPORT_RECORD_CLASS,
                            "net.tbsoft.datax.core.transport.record.DefaultRecord"));
		} catch (ClassNotFoundException e) {
			throw DataXException.asDataXException(
					FrameworkErrorCode.CONFIG_ERROR, e);
		}
	}

	@Override
	public Record getFromReader() {
		if(shutdown){
			throw DataXException.asDataXException(CommonErrorCode.SHUT_DOWN_TASK, "");
		}
		Record record = this.channel.pull();
		return (record instanceof TerminateRecord ? null : record);
	}

	@Override
	public Record createRecord() {
		try {
			return RECORD_CLASS.newInstance();
		} catch (Exception e) {
			throw DataXException.asDataXException(
					FrameworkErrorCode.CONFIG_ERROR, e);
		}
	}

	@Override
	public void sendToWriter(Record record) {
		if(shutdown){
			throw DataXException.asDataXException(CommonErrorCode.SHUT_DOWN_TASK, "");
		}
		record = doTransformer(record);
		if (record == null) {
			return;
		}
		this.channel.push(record);
		//和channel的统计保持同步
		doStat();
	}

	@Override
	public void flush() {
	}

	@Override
	public void terminate() {
		if(shutdown){
			throw DataXException.asDataXException(CommonErrorCode.SHUT_DOWN_TASK, "");
		}
		this.channel.pushTerminate(TerminateRecord.get());
		//和channel的统计保持同步
		doStat();
	}

	@Override
	public void shutdown(){
		shutdown = true;
	}
}
