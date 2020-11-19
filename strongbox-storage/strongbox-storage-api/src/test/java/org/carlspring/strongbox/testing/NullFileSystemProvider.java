package org.carlspring.strongbox.testing;

import org.carlspring.strongbox.providers.layout.AbstractLayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutFileSystemProvider;

import javax.inject.Inject;
import java.nio.file.spi.FileSystemProvider;

public class NullFileSystemProvider extends LayoutFileSystemProvider
{

    @Inject
    private NullLayoutProvider nullLayoutProvider;

    public NullFileSystemProvider(FileSystemProvider storageFileSystemProvider)
    {
        super(storageFileSystemProvider);

    }
    @Override
    protected AbstractLayoutProvider getLayoutProvider()
    {
        return nullLayoutProvider;
    }

}
