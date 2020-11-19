package org.carlspring.strongbox.cron.jobs;

import com.google.common.collect.ImmutableSet;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Przemyslaw Fusik
 */
@Component
class CronJobsRegistry
        implements Supplier<Set<Class<? extends AbstractCronJob>>>
{

    private final Set<Class<? extends AbstractCronJob>> cronJobs;

    CronJobsRegistry()
    {
        cronJobs = ImmutableSet.copyOf(new Reflections("org.carlspring.strongbox.cron.jobs")
                                               .getSubTypesOf(AbstractCronJob.class)
                                               .stream()
                                               .filter(c -> !Modifier.isAbstract(c.getModifiers()) &&
                                                            !c.isInterface()).collect(
                        Collectors.toSet()));
    }

    @Override
    public Set<Class<? extends AbstractCronJob>> get()
    {
        return cronJobs;
    }
}
