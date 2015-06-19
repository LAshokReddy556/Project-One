package org.mifosplatform.finance.depositandrefund.serialization;

import java.lang.reflect.Type;
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

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;

/**
 * @author hugo Deserializer for code JSON to validate API request.
 */
@Component
public class DepositeCommandFromApiJsonDeserializer {

	/**
	 * The parameters supported for this command.
	 */
	final private Set<String> supportedParameters = new HashSet<String>(Arrays.asList("clientId", "feeId"));
	private final FromJsonHelper fromApiJsonHelper;

	@Autowired
	public DepositeCommandFromApiJsonDeserializer(
			final FromJsonHelper fromApiJsonHelper) {
		this.fromApiJsonHelper = fromApiJsonHelper;
	}

	/**
	 * @param json
	 * check validation for create charge codes
	 */

	public void validaForCreate(String json) {
		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
			private static final long serialVersionUID = 1L;
		}.getType();

		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json,
				supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("deposite");

		final JsonElement element = fromApiJsonHelper.parse(json);

		final String clientId = fromApiJsonHelper.extractStringNamed("clientId", element);
		baseDataValidator.reset().parameter("clientId").value(clientId).notBlank();

		final String feeId = fromApiJsonHelper.extractStringNamed("feeId", element);
		baseDataValidator.reset().parameter("feeId").value(feeId).notBlank();

		throwExceptionIfValidationWarningsExist(dataValidationErrors);

	}

	private void throwExceptionIfValidationWarningsExist(
			final List<ApiParameterError> dataValidationErrors) {
		if (!dataValidationErrors.isEmpty()) {
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}
}
