package org.carlspring.strongbox.storage.repository.remote.heartbeat.monitor;

import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class RemoteRepositoryHeartbeatMonitorStrategyRegistry
{

    @Inject
    private RemoteRepositoryHeartbeatMonitorStrategy httpGetRemoteRepositoryCheckStrategy;


    public RemoteRepositoryHeartbeatMonitorStrategy of(boolean allowsDirectoryBrowsing)
    {
        return allowsDirectoryBrowsing ? httpGetRemoteRepositoryCheckStrategy :
               PingRemoteRepositoryUrlStrategy.INSTANCE;
    }

}
