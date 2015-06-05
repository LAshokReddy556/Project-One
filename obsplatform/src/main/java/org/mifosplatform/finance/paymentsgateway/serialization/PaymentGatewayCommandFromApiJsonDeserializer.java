package org.mifosplatform.finance.paymentsgateway.serialization;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.finance.paymentsgateway.data.RecurringPaymentTransactionTypeConstants;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
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
 * 
 * @author ashokreddy
 *
 */
@Component
public class PaymentGatewayCommandFromApiJsonDeserializer {

	/**
	 * The parameters supported for this command.
	 */
	private final Set<String> supportedParameters = new HashSet<String>(Arrays.asList("amount","timestamp", "msisdn","name", 
				"service", "receipt", "reference","transaction","dateFormat","locale","remarks","status","CUSTOMERREFERENCEID",
				"TXNID","COMPANYNAME","STATUS","AMOUNT","MSISDN","TYPE","OBSPAYMENTTYPE"));
	
	private final Set<String> onlinePaymentSupportedParameters = new HashSet<String>(Arrays.asList("total_amount", 
			"clientId", "emailId", "transactionId", "source", "otherData", "device", "currency","dateFormat","locale",
			"paytermCode","planCode","contractPeriod","value","verificationCode","screenName",
			"renewalPeriod", "description","cardType","cardNumber","status","error"));
	
	private final Set<String> cardSupportedParameters = new HashSet<String>(Arrays.asList("cardNumber", "cardType", 
			"expiryDate", "cardCVV", "nameOnCard","source","custom","clientId", "priceId", "finalAmount", 
			"billingCycles", "billingPeriod", "billingFrequency", "description", "currencyCode", "locale"));
	
    private final FromJsonHelper fromApiJsonHelper;
    
    @Autowired
    public PaymentGatewayCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public void validateForCreate(final String json) {

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("paymentgateway");

		final JsonElement element = fromApiJsonHelper.parse(json);
	
		final String OBSPAYMENTTYPE = fromApiJsonHelper.extractStringNamed("OBSPAYMENTTYPE", element);
		
		if(OBSPAYMENTTYPE.equalsIgnoreCase("MPesa")){
			
			final String reference = fromApiJsonHelper.extractStringNamed("reference", element);
			baseDataValidator.reset().parameter("reference").value(reference).notBlank().notExceedingLengthOf(30);
			final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
			baseDataValidator.reset().parameter("amount").value(amount).notBlank();
			final String receipt = fromApiJsonHelper.extractStringNamed("receipt", element);
			baseDataValidator.reset().parameter("receipt").value(receipt).notBlank();
			
		}else if (OBSPAYMENTTYPE.equalsIgnoreCase("TigoPesa")) {
			
			final String keyId = fromApiJsonHelper.extractStringNamed("CUSTOMERREFERENCEID", element);
			baseDataValidator.reset().parameter("CUSTOMERREFERENCEID").value(keyId).notBlank().notExceedingLengthOf(30);
			final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("AMOUNT", element);
			baseDataValidator.reset().parameter("AMOUNT").value(amount).notBlank();
			final String transactionId = fromApiJsonHelper.extractStringNamed("TXNID", element);
			baseDataValidator.reset().parameter("TXNID").value(transactionId).notBlank();
			
		}
		
		
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}
    
    public void validateForOnlinePayment(final String json) {

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, onlinePaymentSupportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("paymentgateway");

		final JsonElement element = fromApiJsonHelper.parse(json);
	
		final String source = fromApiJsonHelper.extractStringNamed("source", element);
		baseDataValidator.reset().parameter("source").value(source).notNull().notBlank();
		
		if(null != source && source.equalsIgnoreCase(ConfigurationConstants.GLOBALPAY_PAYMENTGATEWAY)){
			
			final String transactionId = fromApiJsonHelper.extractStringNamed("transactionId", element);
			baseDataValidator.reset().parameter("transactionId").value(transactionId).notBlank().notExceedingLengthOf(30);
			
		} else {
			
			if (null != source && source.equalsIgnoreCase(ConfigurationConstants.NETELLER_PAYMENTGATEWAY)) {		
				final String verificationCode = fromApiJsonHelper.extractStringNamed("verificationCode", element);
				baseDataValidator.reset().parameter("verificationCode").value(verificationCode).notNull();
				
				if (verificationCode != null && verificationCode.toString().trim().length() < 6) {
			            StringBuilder validationErrorCode = new StringBuilder("validation.msg.").append("paymentgateway").append(".").append("verificationCode")
			                    .append(".length.error");
			         
			            StringBuilder defaultEnglishMessage = new StringBuilder("The parameter ").append("verificationCode ").append(verificationCode).
			            append(" is Greater than OR Equal to the minimum size of 6 characters");
			            
			            ApiParameterError error = ApiParameterError.parameterError(validationErrorCode.toString(), defaultEnglishMessage.toString(),
			            		"verificationCode");
			            dataValidationErrors.add(error);
			        }
			      
			    
				
				final String value = fromApiJsonHelper.extractStringNamed("value", element);
				baseDataValidator.reset().parameter("value").value(value).notBlank().notExceedingLengthOf(100);
			}
			
			final String transactionId = fromApiJsonHelper.extractStringNamed("transactionId", element);
			baseDataValidator.reset().parameter("transactionId").value(transactionId).notBlank().notExceedingLengthOf(30);
			
			final String currency = fromApiJsonHelper.extractStringNamed("currency", element);
			baseDataValidator.reset().parameter("currency").value(currency).notBlank().notExceedingLengthOf(30);
			
			final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("total_amount", element);
			baseDataValidator.reset().parameter("total_amount").value(amount).notBlank();
			
			final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
			baseDataValidator.reset().parameter("clientId").value(clientId).notBlank();
			
		}
		
		
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

	public void validateForUpdate(final String json) {

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, supportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors).resource("paymentgateway");

		final JsonElement element = fromApiJsonHelper.parse(json);
	
		final String status = fromApiJsonHelper.extractStringNamed("status", element);
		baseDataValidator.reset().parameter("status").value(status).notBlank();
		
		final String remarks = fromApiJsonHelper.extractStringNamed("remarks", element);
		baseDataValidator.reset().parameter("remarks").value(remarks).notBlank().notExceedingLengthOf(500);
		
		
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
	}

	public void validateForCardPayment(String json) {
		

		if (StringUtils.isBlank(json)) {
			throw new InvalidJsonException();
		}

		final Type typeOfMap = new TypeToken<Map<String, Object>>() {
		}.getType();
		fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, cardSupportedParameters);

		final List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
		final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(
				dataValidationErrors).resource("cardPayment");

		final JsonElement element = fromApiJsonHelper.parse(json);
	
		final String cardNumber = fromApiJsonHelper.extractStringNamed(RecurringPaymentTransactionTypeConstants.CARD_NUMBER, element);
		baseDataValidator.reset().parameter("cardNumber").value(cardNumber).notBlank().notExceedingLengthOf(100);
		
		final String cardType = fromApiJsonHelper.extractStringNamed(RecurringPaymentTransactionTypeConstants.CARD_TYPE, element);
		baseDataValidator.reset().parameter("cardType").value(cardType).notBlank().notExceedingLengthOf(100);
		
		final String cardExpiryDate = fromApiJsonHelper.extractStringNamed(RecurringPaymentTransactionTypeConstants.CARD_EXPIRY_DATE, element);
		baseDataValidator.reset().parameter("cardExpiryDate").value(cardExpiryDate).notBlank().notExceedingLengthOf(100);
		
		final String cardCVV = fromApiJsonHelper.extractStringNamed(RecurringPaymentTransactionTypeConstants.CARD_CVV, element);
		baseDataValidator.reset().parameter("cardCVV").value(cardCVV).notBlank().notExceedingLengthOf(100);
		
		final String nameOnCard = fromApiJsonHelper.extractStringNamed(RecurringPaymentTransactionTypeConstants.NAME_ON_CARD, element);
		baseDataValidator.reset().parameter("nameOnCard").value(nameOnCard).notBlank().notExceedingLengthOf(100);
		
		final String source = fromApiJsonHelper.extractStringNamed("source", element);
		baseDataValidator.reset().parameter("source").value(source).notBlank().notExceedingLengthOf(100);
		
		final Long clientId = fromApiJsonHelper.extractLongNamed("clientId", element);
		baseDataValidator.reset().parameter("clientId").value(clientId).notBlank();
		
		final Long billingFrequency = fromApiJsonHelper.extractLongNamed("billingFrequency", element);
		baseDataValidator.reset().parameter("billingFrequency").value(billingFrequency).notBlank().notExceedingLengthOf(2);
		
		final String billingPeriod = fromApiJsonHelper.extractStringNamed("billingPeriod", element);
		baseDataValidator.reset().parameter("billingPeriod").value(billingPeriod).notBlank();
		
		throwExceptionIfValidationWarningsExist(dataValidationErrors);
			
	}

}
