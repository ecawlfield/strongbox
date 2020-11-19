package org.carlspring.strongbox.storage.metadata.maven;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RefreshMetadataExpirationStrategy
        implements MetadataExpirationStrategy
{

    @Override
    public Decision decide(RepositoryPath repositoryPath)
            throws IOException
    {
        return Decision.EXPIRED;
    }
}
