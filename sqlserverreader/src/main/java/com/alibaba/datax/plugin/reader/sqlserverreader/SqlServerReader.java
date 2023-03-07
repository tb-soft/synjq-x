package net.tbsoft.datax.plugin.reader.sqlserverreader;

import net.tbsoft.datax.common.exception.DataXException;
import net.tbsoft.datax.common.plugin.RecordSender;
import net.tbsoft.datax.common.spi.Reader;
import net.tbsoft.datax.common.util.Configuration;
import net.tbsoft.datax.plugin.rdbms.reader.CommonRdbmsReader;
import net.tbsoft.datax.plugin.rdbms.util.DBUtilErrorCode;
import net.tbsoft.datax.plugin.rdbms.util.DataBaseType;

import java.util.List;

public class SqlServerReader extends Reader {

	private static final DataBaseType DATABASE_TYPE = DataBaseType.SQLServer;

	public static class Job extends Reader.Job {

		private Configuration originalConfig = null;
		private CommonRdbmsReader.Job commonRdbmsReaderJob;

		@Override
		public void init() {
			this.originalConfig = super.getPluginJobConf();
			int fetchSize = this.originalConfig.getInt(
					net.tbsoft.datax.plugin.rdbms.reader.Constant.FETCH_SIZE,
					Constant.DEFAULT_FETCH_SIZE);
			if (fetchSize < 1) {
				throw DataXException
						.asDataXException(DBUtilErrorCode.REQUIRED_VALUE,
								String.format("您配置的fetchSize有误，根据DataX的设计，fetchSize : [%d] 设置值不能小于 1.",
										fetchSize));
			}
			this.originalConfig.set(
					net.tbsoft.datax.plugin.rdbms.reader.Constant.FETCH_SIZE,
					fetchSize);

			this.commonRdbmsReaderJob = new CommonRdbmsReader.Job(
					DATABASE_TYPE);
			this.commonRdbmsReaderJob.init(this.originalConfig);
		}

		@Override
		public List<Configuration> split(int adviceNumber) {
			return this.commonRdbmsReaderJob.split(this.originalConfig,
					adviceNumber);
		}

		@Override
		public void post() {
			this.commonRdbmsReaderJob.post(this.originalConfig);
		}

		@Override
		public void destroy() {
			this.commonRdbmsReaderJob.destroy(this.originalConfig);
		}

	}

	public static class Task extends Reader.Task {

		private Configuration readerSliceConfig;
		private CommonRdbmsReader.Task commonRdbmsReaderTask;

		@Override
		public void init() {
			this.readerSliceConfig = super.getPluginJobConf();
			this.commonRdbmsReaderTask = new CommonRdbmsReader.Task(
					DATABASE_TYPE ,super.getTaskGroupId(), super.getTaskId());
			this.commonRdbmsReaderTask.init(this.readerSliceConfig);
		}

		@Override
		public void startRead(RecordSender recordSender) {
			int fetchSize = this.readerSliceConfig
					.getInt(net.tbsoft.datax.plugin.rdbms.reader.Constant.FETCH_SIZE);

			this.commonRdbmsReaderTask.startRead(this.readerSliceConfig,
					recordSender, super.getTaskPluginCollector(), fetchSize);
		}

		@Override
		public void post() {
			this.commonRdbmsReaderTask.post(this.readerSliceConfig);
		}

		@Override
		public void destroy() {
			this.commonRdbmsReaderTask.destroy(this.readerSliceConfig);
		}

	}

}
