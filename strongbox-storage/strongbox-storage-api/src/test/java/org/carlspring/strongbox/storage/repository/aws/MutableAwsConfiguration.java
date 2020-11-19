package org.carlspring.strongbox.storage.repository.aws;

import com.fasterxml.jackson.annotation.JsonTypeName;
import org.carlspring.strongbox.storage.repository.CustomConfiguration;
import org.carlspring.strongbox.storage.repository.MutableCustomConfiguration;
import org.carlspring.strongbox.yaml.CustomTag;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@JsonTypeName("awsConfiguration")
public class MutableAwsConfiguration
        extends MutableCustomConfiguration
        implements CustomTag
{

    private String bucket;

    private String key;


    public String getBucket()
    {
        return bucket;
    }

    public void setBucket(String bucket)
    {
        this.bucket = bucket;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    @Override
    public CustomConfiguration getImmutable()
    {
        return new AwsConfiguration(this);
    }
}
