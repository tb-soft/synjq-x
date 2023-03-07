package net.tbsoft.datax.core.statistics.container.communicator.taskgroup;

import net.tbsoft.datax.common.util.Configuration;
import net.tbsoft.datax.core.statistics.container.report.ProcessInnerReporter;
import net.tbsoft.datax.core.statistics.communication.Communication;

public class StandaloneTGContainerCommunicator extends AbstractTGContainerCommunicator {

    public StandaloneTGContainerCommunicator(Configuration configuration) {
        super(configuration);
        super.setReporter(new ProcessInnerReporter());
    }

    @Override
    public void report(Communication communication) {
        super.getReporter().reportTGCommunication(super.taskGroupId, communication);
    }

}
