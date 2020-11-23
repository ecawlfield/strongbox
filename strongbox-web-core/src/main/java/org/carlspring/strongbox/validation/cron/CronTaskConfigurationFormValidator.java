package org.carlspring.strongbox.validation.cron;

import com.sun.istack.Nullable;
import org.carlspring.strongbox.cron.jobs.CronJobDefinition;
import org.carlspring.strongbox.cron.jobs.CronJobsDefinitionsRegistry;
import org.carlspring.strongbox.cron.jobs.fields.CronJobField;
import org.carlspring.strongbox.forms.cron.CronTaskConfigurationForm;
import org.carlspring.strongbox.forms.cron.CronTaskConfigurationFormField;
import org.carlspring.strongbox.validation.cron.autocomplete.CronTaskConfigurationFormFieldAutocompleteValidator;
import org.carlspring.strongbox.validation.cron.autocomplete.CronTaskConfigurationFormFieldAutocompleteValidatorsRegistry;
import org.carlspring.strongbox.validation.cron.type.CronTaskConfigurationFormFieldTypeValidator;
import org.carlspring.strongbox.validation.cron.type.CronTaskConfigurationFormFieldTypeValidatorsRegistry;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;

/**
 * @author Przemyslaw Fusik
 */
public class CronTaskConfigurationFormValidator
        implements ConstraintValidator<CronTaskConfigurationFormValid, CronTaskConfigurationForm>
{

    @Inject
    private CronJobsDefinitionsRegistry cronJobsDefinitionsRegistry;

    @Inject
    private CronTaskConfigurationFormFieldTypeValidatorsRegistry cronTaskConfigurationFormFieldTypeValidatorsRegistry;

    @Inject
    private CronTaskConfigurationFormFieldAutocompleteValidatorsRegistry cronTaskConfigurationFormFieldAutocompleteValidatorsRegistry;

    @Override
    public boolean isValid(CronTaskConfigurationForm form,
                           ConstraintValidatorContext context)
    {

        CronJobDefinition cronJobDefinition;
        try
        {
            cronJobDefinition = getCorrespondingCronJobDefinition(form, context);
        }
        catch (CronTaskDefinitionFormValidatorException ex)
        {
            return false;
        }

        boolean isValid = true;
        boolean cronExpressionIsValid = true;

        if (form.isImmediateExecution() &&
            form.isOneTimeExecution() &&
            StringUtils.isNotBlank(form.getCronExpression()))
        {
            buildConstraintViolationHelperSimple(context,
                    "Cron expression should not be provided when both immediateExecution and oneTimeExecution are set to true",
                    "cronExpression");
            isValid = false;
            cronExpressionIsValid = false;
        }

        if (cronExpressionIsValid && StringUtils.isBlank(form.getCronExpression()))
        {
            buildConstraintViolationHelperSimple(context,
                    "Cron expression is required",
                    "cronExpression");
            isValid = false;
            cronExpressionIsValid = false;
        }

        if (cronExpressionIsValid && !CronExpression.isValidExpression(form.getCronExpression()))
        {
            buildConstraintViolationHelperSimple(context,
                    "Cron expression is invalid",
                    "cronExpression");
            isValid = false;
        }

        for (CronJobField definitionField : cronJobDefinition.getFields())
        {
            String definitionFieldName = definitionField.getName();
            CronTaskConfigurationFormField correspondingFormField = null;
            int correspondingFormFieldIndex = -1;
            for (int i = 0; i < form.getFields().size(); i++)
            {
                CronTaskConfigurationFormField formField = form.getFields().get(i);

                String formFieldName = formField.getName();
                if (StringUtils.equals(definitionFieldName, formFieldName))
                {
                    correspondingFormField = formField;
                    correspondingFormFieldIndex = i;
                    break;
                }
            }
            if (correspondingFormField == null && definitionField.isRequired())
            {
                buildConstraintViolationHelperSimple(context,
                        String.format("Required field [%s] not provided", definitionFieldName),
                        "fields");
                isValid = false;
                // field is not required and is not provided
                continue;
            }

            String formFieldValue = correspondingFormField.getValue();
            isValid = isValidFormFieldValue(context, definitionField, definitionFieldName,
                    correspondingFormFieldIndex, formFieldValue) && isValid;
            // TODO SB-1393
        }

        return isValid;
    }

    private boolean isValidFormFieldValue(ConstraintValidatorContext context, CronJobField definitionField,
                            String definitionFieldName, int correspondingFormFieldIndex,
                            String formFieldValue)
    {
        if (StringUtils.isBlank(formFieldValue) && definitionField.isRequired())
        {
            buildConstraintViolationHelperComplex(context,
                    "Required field value [%s] not provided",
                    correspondingFormFieldIndex, new Object[] {definitionFieldName});
            return false;
        }

        String definitionFieldType = definitionField.getType();
        CronTaskConfigurationFormFieldTypeValidator cronTaskConfigurationFormFieldTypeValidator
                = cronTaskConfigurationFormFieldTypeValidatorsRegistry.get(definitionFieldType);
        if (!cronTaskConfigurationFormFieldTypeValidator.isValid(formFieldValue))
        {
            buildConstraintViolationHelperComplex(context,
                    "Invalid value [%s] type provided. [%s] was expected.",
                    correspondingFormFieldIndex,
                    new Object[] {escapeMessageValue(formFieldValue), definitionFieldType});
            return false;
        }

        String autocompleteValue = definitionField.getAutocompleteValue();
        if (autocompleteValue != null)
        {
            CronTaskConfigurationFormFieldAutocompleteValidator cronTaskConfigurationFormFieldAutocompleteValidator =
                    cronTaskConfigurationFormFieldAutocompleteValidatorsRegistry.get(autocompleteValue);
            if (!cronTaskConfigurationFormFieldAutocompleteValidator.isValid(formFieldValue))
            {
                buildConstraintViolationHelperComplex(context,
                        "Invalid value [%s] provided. Possible values do not contain this value.",
                        correspondingFormFieldIndex, new Object[] {escapeMessageValue(formFieldValue)});
                return false;
            }
        }
        return true;
    }

    private void buildConstraintViolationHelperComplex(ConstraintValidatorContext context,
                                                       String format,
                                                       int correspondingFormFieldIndex,
                                                       @Nullable Object[] args) {
        context.buildConstraintViolationWithTemplate(
                String.format(format, args))
                .addPropertyNode("fields")
                .addPropertyNode("value")
                .inIterable().atIndex(correspondingFormFieldIndex)
                .addConstraintViolation();
    }

    private void buildConstraintViolationHelperSimple(ConstraintValidatorContext context,
                                                      String messageTemplate,
                                                      String fields) {
        context.buildConstraintViolationWithTemplate(
                messageTemplate)
                .addPropertyNode(fields)
                .addConstraintViolation();
    }

    private String escapeMessageValue(String value)
    {
        return Arrays.stream(value.split("\\$")).collect(Collectors.joining("\\$"));
    }

    private CronJobDefinition getCorrespondingCronJobDefinition(CronTaskConfigurationForm form,
                                                                ConstraintValidatorContext context)
    {
        String id = StringUtils.trimToEmpty(form.getJobClass());
        Optional<CronJobDefinition> cronJobDefinition = cronJobsDefinitionsRegistry.get(id);
        return cronJobDefinition.orElseThrow(() ->
                                             {
                                                 buildConstraintViolationHelperSimple(context,
                                                         "Cron job not found",
                                                         "jobClass");
                                                 return new CronTaskDefinitionFormValidatorException();
                                             }
        );
    }

    static class CronTaskDefinitionFormValidatorException
            extends RuntimeException
    {

    }
}
