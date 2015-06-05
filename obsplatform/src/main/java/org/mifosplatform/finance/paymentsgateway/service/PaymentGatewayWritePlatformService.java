package org.mifosplatform.finance.paymentsgateway.service;

import java.io.IOException;

import org.json.JSONException;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface PaymentGatewayWritePlatformService {

	CommandProcessingResult createPaymentGateway(JsonCommand command);

	CommandProcessingResult updatePaymentGateway(JsonCommand command);

	CommandProcessingResult onlinePaymentGateway(JsonCommand command);
	
	String payment(Long clientId, Long id, String txnId, String amount, String errorDesc) throws JSONException;
	void emailSending(Long clientId, String result, String description,String txnId, String amount, String cardType, String cardNumber) throws JSONException;

	String globalPayProcessing(String transactionId, String remarks) throws JSONException, IOException;
	
}
