package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.yaml.YAMLMapperFactory;
import org.carlspring.strongbox.yaml.YamlFileManager;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfigurationDto;
import org.carlspring.strongbox.yaml.repository.remote.RemoteRepositoryConfigurationDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ConfigurationFileManager
        extends YamlFileManager<MutableConfiguration>
{
    @Value("#{@propertiesPathResolver.resolve('strongbox.config.file','etc/conf/strongbox.yaml')}")
    private Resource resource;

    @Inject
    public ConfigurationFileManager(YAMLMapperFactory yamlMapperFactory)
    {
        super(yamlMapperFactory, CustomRepositoryConfigurationDto.class, RemoteRepositoryConfigurationDto.class);
    }

    @Override
    protected Resource getResource()
    {
        return resource;
    }
}
