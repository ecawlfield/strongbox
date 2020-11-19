package org.carlspring.strongbox.ext.jersey;

import org.glassfish.jersey.internal.AbstractRuntimeDelegate;
import org.glassfish.jersey.server.ContainerFactory;

import javax.ws.rs.core.Application;

/**
 * @author Przemyslaw Fusik
 * @see org.glassfish.jersey.server.internal.RuntimeDelegateImpl
 */
public class CustomJerseyRuntimeDelegateImpl
        extends AbstractRuntimeDelegate
{

    public CustomJerseyRuntimeDelegateImpl()
    {
        super((new CustomJerseyHeaderDelegateProviders()).getHeaderDelegateProviders());
    }

    @Override
    public <T> T createEndpoint(Application application,
                                Class<T> endpointType)
    {
        if (application == null)
        {
            throw new IllegalArgumentException("application is null.");
        }
        return ContainerFactory.createContainer(endpointType, application);
    }

}
