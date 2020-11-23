package org.carlspring.strongbox.forms.users;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.converters.users.AccessModelFormToUserAccessModelDtoConverter;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.AccessModelDto;
import org.carlspring.strongbox.users.dto.PathPrivilegesDto;
import org.carlspring.strongbox.users.dto.RepositoryPrivilegesDto;
import org.carlspring.strongbox.users.dto.StoragePrivilegesDto;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@IntegrationTest
public class AccessModelFormTest
        extends RestAssuredBaseTest
{

    private static final String STORAGE_ID_VALID = "storage0";

    private static final String REPOSITORY_ID_VALID = "releases";

    private Collection<String> privileges;

    @Inject
    private Validator validator;


    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

        privileges = Lists.newArrayList(Privileges.r());
    }

    @Test
    public void shouldProperlyMapToDto()
    {

        AccessModelForm developer01AccessModel = new AccessModelForm();

        formCreationHelper(developer01AccessModel, "releases",
                "com/carlspring/foo", Privileges.r(), true);
        formCreationHelper(developer01AccessModel, "releases",
                "org/carlspring/foo", Privileges.rw(), true);
        formCreationHelper(developer01AccessModel, "releases",
                "com/apache/foo", Privileges.r(), false);
        formCreationHelper(developer01AccessModel, "releases",
                "org/apache/foo", Privileges.rw(), false);
        formCreationHelper(developer01AccessModel, "releases",
                "", ImmutableSet.of("ARTIFACTS_RESOLVE", "ARTIFACTS_DEPLOY"), false);
        formCreationHelper(developer01AccessModel, "snapshots",
                "", ImmutableSet.of("ARTIFACTS_DEPLOY"), false);

        AccessModelDto userAccessModel = AccessModelFormToUserAccessModelDtoConverter.INSTANCE.convert(
                developer01AccessModel);
        assertThat(userAccessModel).isNotNull();

        Set<StoragePrivilegesDto> userStorages = userAccessModel.getStorageAuthorities();
        assertThat(userStorages).isNotNull();
        assertThat(userStorages).hasSize(1);

        StoragePrivilegesDto userStorage = userStorages.iterator().next();
        assertThat(userStorage).isNotNull();

        assertThat(userStorage.getStorageId()).isEqualTo("storage0");

        Set<RepositoryPrivilegesDto> userRepositories = userStorage.getRepositoryPrivileges();
        assertThat(userRepositories).isNotNull();
        assertThat(userRepositories).hasSize(2);

        for (RepositoryPrivilegesDto userRepository : userRepositories)
        {
            assertThat(userRepository.getRepositoryId()).isIn("releases", "snapshots");

            Set<Privileges> repositoryPrivileges = userRepository.getRepositoryPrivileges();
            Set<PathPrivilegesDto> pathPrivileges = userRepository.getPathPrivileges();

            if ("releases".equals(userRepository.getRepositoryId()))
            {
                releasePathPrivilegesCheckHelper(repositoryPrivileges, pathPrivileges);
            }
            if ("snapshots".equals(userRepository.getRepositoryId()))
            {
                assertThat(pathPrivileges).isEmpty();
                assertThat(repositoryPrivileges).hasSize(1);
                assertThat(repositoryPrivileges).contains(Privileges.ARTIFACTS_DEPLOY);
            }
        }
    }

    private void releasePathPrivilegesCheckHelper(Set<Privileges> repositoryPrivileges,
                                                  Set<PathPrivilegesDto> pathPrivileges)
    {
        assertThat(pathPrivileges).isNotNull();
        assertThat(pathPrivileges).hasSize(4);

        for (PathPrivilegesDto pathPrivilege : pathPrivileges)
        {
            assertThat(pathPrivilege.getPath())
                    .isIn("com/apache/foo",
                          "org/apache/foo",
                          "com/carlspring/foo",
                          "org/carlspring/foo");
            if (pathPrivilege.getPath().startsWith("org"))
            {
                assertThat(pathPrivilege.getPrivileges()).hasSize(5);
            }
            else
            {
                assertThat(pathPrivilege.getPrivileges()).hasSize(2);
            }

            if (pathPrivilege.getPath().contains("carlspring"))
            {
                assertThat(pathPrivilege.isWildcard()).isTrue();
            }
            else
            {
                assertThat(pathPrivilege.isWildcard()).isFalse();
            }
        }

        assertThat(repositoryPrivileges).hasSize(2);
        assertThat(repositoryPrivileges).contains(Privileges.ARTIFACTS_RESOLVE, Privileges.ARTIFACTS_DEPLOY);
    }

    private void formCreationHelper(AccessModelForm model, String repoId,
                                                         String filepath, Collection<String> privileges, Boolean wc)
    {
        RepositoryAccessModelForm form = new RepositoryAccessModelForm();
        form.setStorageId("storage0");
        form.setRepositoryId(repoId);
        form.setPath(filepath);
        form.setPrivileges(privileges);
        if (wc) { form.setWildcard(true); }
        model.addRepositoryAccess(form);
    }

    @Test
    void testAccessModelFormValid()
    {
        // given
        AccessModelForm accessModelForm = new AccessModelForm();
        RepositoryAccessModelForm repositoryAccessModelForm = new RepositoryAccessModelForm();
        repositoryAccessModelForm.setStorageId(STORAGE_ID_VALID);
        repositoryAccessModelForm.setRepositoryId(REPOSITORY_ID_VALID);
        repositoryAccessModelForm.setPrivileges(privileges);
        List<RepositoryAccessModelForm> repositories = Lists.newArrayList(repositoryAccessModelForm);
        accessModelForm.setRepositoriesAccess(repositories);

        // when
        Set<ConstraintViolation<RepositoryAccessModelForm>> violations = validator.validate(repositoryAccessModelForm);

        // then
        assertThat(violations).as("Violations are not empty!").isEmpty();
    }

    @Test
    void testAccessModelFormInvalidEmptyStorageId()
    {
        // given
        RepositoryAccessModelForm repositoryAccessModelForm = new RepositoryAccessModelForm();
        repositoryAccessModelForm.setStorageId(StringUtils.EMPTY);
        repositoryAccessModelForm.setRepositoryId(REPOSITORY_ID_VALID);
        repositoryAccessModelForm.setPrivileges(privileges);

        // when
        Set<ConstraintViolation<RepositoryAccessModelForm>> violations = validator.validate(repositoryAccessModelForm);

        // then
        assertThat(violations).as("Violations are empty!").isNotEmpty();
        assertThat(violations).hasSize(1);
        assertThat(violations).extracting("message").containsAnyOf("A storage id must be specified.");
    }

}
