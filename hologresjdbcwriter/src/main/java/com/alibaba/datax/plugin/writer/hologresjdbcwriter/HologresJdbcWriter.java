package net.tbsoft.datax.plugin.writer.hologresjdbcwriter;

import net.tbsoft.datax.common.plugin.RecordReceiver;
import net.tbsoft.datax.common.spi.Writer;
import net.tbsoft.datax.common.util.Configuration;
import net.tbsoft.datax.plugin.rdbms.util.DataBaseType;

import java.util.List;

public class HologresJdbcWriter extends Writer {
	private static final DataBaseType DATABASE_TYPE = DataBaseType.PostgreSQL;

	public static class Job extends Writer.Job {
		private Configuration originalConfig = null;
		private BaseWriter.Job baseWriterMaster;

		@Override
		public void init() {
			this.originalConfig = super.getPluginJobConf();
			this.baseWriterMaster = new BaseWriter.Job(DATABASE_TYPE);
			this.baseWriterMaster.init(this.originalConfig);
		}

		@Override
		public void prepare() {
			this.baseWriterMaster.prepare(this.originalConfig);
		}

		@Override
		public List<Configuration> split(int mandatoryNumber) {
			return this.baseWriterMaster.split(this.originalConfig, mandatoryNumber);
		}

		@Override
		public void post() {
			this.baseWriterMaster.post(this.originalConfig);
		}

		@Override
		public void destroy() {
			this.baseWriterMaster.destroy(this.originalConfig);
		}

	}

	public static class Task extends Writer.Task {
		private Configuration writerSliceConfig;
		private BaseWriter.Task baseWriterSlave;

		@Override
		public void init() {
			this.writerSliceConfig = super.getPluginJobConf();
			this.baseWriterSlave = new BaseWriter.Task(DATABASE_TYPE);
			this.baseWriterSlave.init(this.writerSliceConfig);
		}

		@Override
		public void prepare() {
			this.baseWriterSlave.prepare(this.writerSliceConfig);
		}

		public void startWrite(RecordReceiver recordReceiver) {
			this.baseWriterSlave.startWrite(recordReceiver, super.getTaskPluginCollector());
		}

		@Override
		public void post() {
			this.baseWriterSlave.post(this.writerSliceConfig);
		}

		@Override
		public void destroy() {
			this.baseWriterSlave.destroy(this.writerSliceConfig);
		}

	}

}
