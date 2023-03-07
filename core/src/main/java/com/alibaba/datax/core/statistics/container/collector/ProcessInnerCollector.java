package net.tbsoft.datax.core.statistics.container.collector;

import net.tbsoft.datax.core.statistics.communication.Communication;
import net.tbsoft.datax.core.statistics.communication.LocalTGCommunicationManager;

public class ProcessInnerCollector extends AbstractCollector {

    public ProcessInnerCollector(Long jobId) {
        super.setJobId(jobId);
    }

    @Override
    public Communication collectFromTaskGroup() {
        return LocalTGCommunicationManager.getJobCommunication();
    }

}
