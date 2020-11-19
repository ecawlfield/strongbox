package org.carlspring.strongbox.storage;

import org.carlspring.strongbox.storage.repository.Repository;

import java.util.Map;

public interface Storage
{

    Repository getRepository(String repositoryId);

    String getId();

    String getBasedir();

    Map<String, ? extends Repository> getRepositories();

    boolean containsRepository(String repositoryId);

}