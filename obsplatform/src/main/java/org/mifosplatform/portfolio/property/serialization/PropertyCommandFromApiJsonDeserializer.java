package org.mifosplatform.portfolio.property.serialization;

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
public final class PropertyCommandFromApiJsonDeserializer {

	/**
	 * The parameters supported for this command.
	 */
	private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("propertyCode", "propertyType", "unitCode",
					"floor", "buildingCode", "parcel", "street", "status","precinct", "poBox", "state", "country"));
	
	private final Set<String> supportedParametersForServiceTransfer = new HashSet<String>(Arrays.asList("oldPropertyCode", "newPropertyCode", "shiftChargeAmount","locale","unitCode",
			           "clientId","chargeCode"));
	
    private final Set<String> supportedParametersForMaster = new HashSet<String>(Arrays.asList("propertyCodeType","code","description","referenceValue"));
	
   private final FromJsonHelper fromApiJsonHelper;

	@Autowired
	public PropertyCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {

		this.fromApiJsonHelper = fromApiJsonHelper;

	}

	/**
	 * @param json check validation for create property
	 */
	public void validateForCreate(final String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("property");

		final JsonElement element = fromApiJsonHelper.parse(json);
		
		final String precinct = fromApiJsonHelper.extractStringNamed("precinct", element);
		baseDataValidator.reset().parameter("precinct").value(precinct).notBlank();
		
		final String parcel = fromApiJsonHelper.extractStringNamed("parcel", element);
		baseDataValidator.reset().parameter("parcel").value(parcel).notBlank();
		
		final Long propertyType = fromApiJsonHelper.extractLongNamed("propertyType", element);
		baseDataValidator.reset().parameter("propertyType").value(propertyType).notBlank();
		
		final String  buildingCode = fromApiJsonHelper.extractStringNamed("buildingCode", element);
		baseDataValidator.reset().parameter("buildingCode").value(buildingCode).notBlank();
		
		final String floor = fromApiJsonHelper.extractStringNamed("floor", element);
		baseDataValidator.reset().parameter("floor").value(floor).notBlank();
		
		final String unitCode = fromApiJsonHelper.extractStringNamed("unitCode", element);
		baseDataValidator.reset().parameter("unitCode").value(unitCode).notBlank();

		final String propertyCode = fromApiJsonHelper.extractStringNamed("propertyCode", element);
		baseDataValidator.reset().parameter("propertyCode").value(propertyCode).notBlank().notExceedingLengthOf(14);

		/*final Long poBox = fromApiJsonHelper.extractLongNamed("poBox", element);
		baseDataValidator.reset().parameter("poBox").value(poBox).notBlank();*/
     
		final String state = fromApiJsonHelper.extractStringNamed("state", element);
		baseDataValidator.reset().parameter("state").value(state).notBlank().notExceedingLengthOf(100);
		
		final String country = fromApiJsonHelper.extractStringNamed("country", element);
		baseDataValidator.reset().parameter("country").value(country).notBlank().notExceedingLengthOf(100);


		throwExceptionIfValidationWarningsExist(dataValidationErrors);

	}


	public void validateForServiceTransfer(final String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,supportedParametersForServiceTransfer);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("serviceTransfer");
		
		final JsonElement element = fromApiJsonHelper.parse(json);
		
		final String oldPropertyCode = fromApiJsonHelper.extractStringNamed("oldPropertyCode", element);
		baseDataValidator.reset().parameter("oldPropertyCode").value(oldPropertyCode).notExceedingLengthOf(14);
		
		final String newPropertyCode = fromApiJsonHelper.extractStringNamed("newPropertyCode", element);
		baseDataValidator.reset().parameter("newPropertyCode").value(newPropertyCode).notBlank().notExceedingLengthOf(14);
		
		final BigDecimal shiftChargeAmount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("shiftChargeAmount", element);
		baseDataValidator.reset().parameter("shiftChargeAmount").value(shiftChargeAmount).notBlank().notExceedingLengthOf(10);
		
		final String chargeCode = fromApiJsonHelper.extractStringNamed("chargeCode", element);
		baseDataValidator.reset().parameter("chargeCode").value(chargeCode).notBlank();
		
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
		
	}
	
	private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist","Validation errors exist.", dataValidationErrors);
		}
	}

	public void validateForCreatePropertyMaster(final String json) {
		
	        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

	        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
	        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParametersForMaster);

	        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
	        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("propertycodemaster");

	        final JsonElement element = fromApiJsonHelper.parse(json);

	        final String propertyCodeType = fromApiJsonHelper.extractStringNamed("propertyCodeType", element);
	        baseDataValidator.reset().parameter("propertyCodeType").value(propertyCodeType).notBlank().notExceedingLengthOf(40);
	        
	        final String code = fromApiJsonHelper.extractStringNamed("code", element);
	        baseDataValidator.reset().parameter("code").value(code).notBlank().notExceedingLengthOf(5);
	        
	        final String description = fromApiJsonHelper.extractStringNamed("description", element);
	        baseDataValidator.reset().parameter("description").value(description).notBlank().notExceedingLengthOf(200);

	        throwExceptionIfValidationWarningsExist(dataValidationErrors);
	    }
		
	}


