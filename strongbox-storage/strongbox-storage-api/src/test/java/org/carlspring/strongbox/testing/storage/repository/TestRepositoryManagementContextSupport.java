package org.carlspring.strongbox.testing.storage.repository;

import org.junit.jupiter.api.extension.*;

import java.lang.annotation.Annotation;

/**
 * @author sbespalov
 *
 * @param <T>
 */
public abstract class TestRepositoryManagementContextSupport<T extends Annotation>
        implements ParameterResolver, BeforeTestExecutionCallback, AfterTestExecutionCallback
{

    private final Class<T> type;

    public TestRepositoryManagementContextSupport(Class<T> type)
    {
        this.type = type;
    }

    protected TestRepositoryManagementContext getTestRepositoryManagementContext()
    {
        return TestRepositoryManagementApplicationContext.getInstance();
    }

    @Override
    public void beforeTestExecution(ExtensionContext context)
        throws Exception
    {
        TestRepositoryManagementApplicationContext.registerExtension(type, context);
    }

    @Override
    public void afterTestExecution(ExtensionContext context)
        throws Exception
    {
        TestRepositoryManagementApplicationContext.closeExtension(type, context);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        TestRepositoryManagementContext testRepositoryManagementContext = getTestRepositoryManagementContext();
        if (testRepositoryManagementContext == null)
        {
            return false;
        }

        return testRepositoryManagementContext.tryToApply(type, parameterContext);
    }

}
