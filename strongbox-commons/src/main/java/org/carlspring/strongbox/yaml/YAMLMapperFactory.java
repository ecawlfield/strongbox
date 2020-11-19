package org.carlspring.strongbox.yaml;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
public interface YAMLMapperFactory
{
    YAMLMapper create(@Nonnull final Set<Class<?>> contextClasses);
}
