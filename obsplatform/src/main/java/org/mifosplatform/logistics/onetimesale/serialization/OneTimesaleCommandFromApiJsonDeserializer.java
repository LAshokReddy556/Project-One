package org.mifosplatform.logistics.onetimesale.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.ApiParameterError;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.exception.PlatformApiDataValidationException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.logistics.item.domain.UnitEnumType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Deserializer for code JSON to validate API request.
 */
@Component
public final class OneTimesaleCommandFromApiJsonDeserializer {

    /**
     * The parameters supported for this command.
     */
    private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("itemId","locale","dateFormat","units","chargeCode","unitPrice",
    		"quantity","totalPrice","saleDate","discountId","serialNumber","orderId","clientId","status","itemMasterId","isNewHw","saleType","officeId",
    		"contractPeriod", "amount","addDeposit"));
    
    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public OneTimesaleCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    /**
     * @param json
     * check validation for create one time sale
     */
    public void validateForCreate(final String json) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("onetimesale");

        final JsonElement element = fromApiJsonHelper.parse(json);
        
        final String saleType = fromApiJsonHelper.extractStringNamed("saleType", element);
        baseDataValidator.reset().parameter("saleType").value(saleType).notNull();
        
        final LocalDate saleDate = fromApiJsonHelper.extractLocalDateNamed("saleDate", element);
        baseDataValidator.reset().parameter("saleDate").value(saleDate).notBlank();
        
        final Long itemId = fromApiJsonHelper.extractLongNamed("itemId", element);
        baseDataValidator.reset().parameter("itemId").value(itemId).notNull();
        
        final String quantity = fromApiJsonHelper.extractStringNamed("quantity", element);
        baseDataValidator.reset().parameter("quantity").value(quantity).notBlank().notExceedingLengthOf(50).positiveAmount(); 
        
        final Long discountId = fromApiJsonHelper.extractLongNamed("discountId", element);
        baseDataValidator.reset().parameter("discountId").value(discountId).notNull();
        
        final String chargeCode = fromApiJsonHelper.extractStringNamed("chargeCode", element);
        baseDataValidator.reset().parameter("chargeCode").value(chargeCode).notBlank().notExceedingLengthOf(10);
        
        final BigDecimal totalPrice=fromApiJsonHelper.extractBigDecimalWithLocaleNamed("totalPrice", element);
        baseDataValidator.reset().parameter("totalPrice").value(totalPrice).notNull();
        
        final BigDecimal unitPrice=fromApiJsonHelper.extractBigDecimalWithLocaleNamed("unitPrice", element);
        baseDataValidator.reset().parameter("unitPrice").value(unitPrice).notNull();
        
        final Long officeId = fromApiJsonHelper.extractLongNamed("officeId", element);
        baseDataValidator.reset().parameter("officeId").value(officeId).notNull();
        
        	if(saleType.equalsIgnoreCase("DEVICERENTAL")){
        		final String contractPeriod = fromApiJsonHelper.extractStringNamed("contractPeriod", element);
                baseDataValidator.reset().parameter("contractPeriod").value(contractPeriod).notBlank();
        		
        	}
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
        
    }  
    
    /**
     * @param jsonElement
     * check validation for calculate total price of item sale
     */
    public void validateForPrice(final JsonElement jsonElement) {
        if (StringUtils.isBlank(jsonElement.toString())) { throw new InvalidJsonException(); }

        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, jsonElement.toString(), supportedParameters);
      
        final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("onetimesale");
        
        if(UnitEnumType.PIECES.toString().equalsIgnoreCase(fromApiJsonHelper.extractStringNamed("units", jsonElement))){
        	final Integer totalPrice=fromApiJsonHelper.extractIntegerWithLocaleNamed("quantity", jsonElement);
            baseDataValidator.reset().parameter("quantity").value(totalPrice).notNull().integerGreaterThanZero();
        }else{
        	final String quntity=fromApiJsonHelper.extractStringNamed("quantity", jsonElement);
            baseDataValidator.reset().parameter("quantity").value(quntity).notNull();
        }
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
    }
   
    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) {
        	throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}