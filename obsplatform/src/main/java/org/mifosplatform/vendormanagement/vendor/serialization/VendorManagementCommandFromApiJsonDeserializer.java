package org.mifosplatform.vendormanagement.vendor.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Deserializer for code JSON to validate API request.
 */
@Component
public final class VendorManagementCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("id", "vendorCode", "vendorName",
    		  "vendorEmailId", "contactName", "vendormobileNo", "vendorLandlineNo", "vendorAddress", "vendorCountry", "vendorCurrency",
    		  "dateFormat", "locale"));
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public VendorManagementCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("vendormanagement");

        final JsonElement element = fromApiJsonHelper.parse(json);

        final String vendorCode = fromApiJsonHelper.extractStringNamed("vendorCode", element);
        baseDataValidator.reset().parameter("vendorCode").value(vendorCode).notBlank().notExceedingLengthOf(10);
        
        final String vendorName = fromApiJsonHelper.extractStringNamed("vendorName", element);
        baseDataValidator.reset().parameter("vendorName").value(vendorName).notBlank().notExceedingLengthOf(100);
        
        final String vendorEmailId = fromApiJsonHelper.extractStringNamed("vendorEmailId", element);
        baseDataValidator.reset().parameter("vendorEmailId").value(vendorEmailId).notBlank().notExceedingLengthOf(100);
        
        final String contactName = fromApiJsonHelper.extractStringNamed("contactName", element);
        baseDataValidator.reset().parameter("contactName").value(contactName).notBlank().notExceedingLengthOf(100);
        
        final String vendormobileNo = fromApiJsonHelper.extractStringNamed("vendormobileNo", element);
        baseDataValidator.reset().parameter("vendormobileNo").value(vendormobileNo).notBlank().notExceedingLengthOf(13);
        
        final String vendorLandlineNo = fromApiJsonHelper.extractStringNamed("vendorLandlineNo", element);
        baseDataValidator.reset().parameter("vendorLandlineNo").value(vendorLandlineNo).notBlank().notExceedingLengthOf(15);
        
        final String vendorAddress = fromApiJsonHelper.extractStringNamed("vendorAddress", element);
        baseDataValidator.reset().parameter("vendorAddress").value(vendorAddress).notBlank().notExceedingLengthOf(200);
        
        final Long vendorCountry = fromApiJsonHelper.extractLongNamed("vendorCountry", element);
        baseDataValidator.reset().parameter("vendorCountry").value(vendorCountry).notBlank().notExceedingLengthOf(10);
        
        final String vendorCurrency = fromApiJsonHelper.extractStringNamed("vendorCurrency", element);
        baseDataValidator.reset().parameter("vendorCurrency").value(vendorCurrency).notBlank().notExceedingLengthOf(20);
               
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
        
    }

    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }

}
