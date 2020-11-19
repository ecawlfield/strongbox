package org.carlspring.strongbox.yaml.configuration.repository.remote;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.yaml.repository.remote.CustomRemoteRepositoryConfigurationData;
import org.carlspring.strongbox.yaml.repository.remote.RemoteRepositoryConfigurationDto;

/**
 * @author Pablo Tirado
 */
@JsonTypeName(Maven2LayoutProvider.ALIAS)
public class MavenRemoteRepositoryConfigurationDto
        extends RemoteRepositoryConfigurationDto
{

    @Override
    public CustomRemoteRepositoryConfigurationData getImmutable()
    {
        return new MavenRemoteRepositoryConfiguration();
    }

}
