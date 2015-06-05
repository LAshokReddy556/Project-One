package org.mifosplatform.finance.paymentsgateway.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.mifosplatform.finance.paymentsgateway.domain.PaypalRecurringBilling;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface PaymentGatewayRecurringWritePlatformService {

	public String paypalRecurringVerification(HttpServletRequest request) 
			throws UnsupportedEncodingException, IllegalStateException, ClientProtocolException, IOException, JSONException;

	//public String getAccessToken(String data);

	public void recurringEventUpdate(HttpServletRequest request) throws JSONException;

	public PaypalRecurringBilling recurringSubscriberSignUp(HttpServletRequest request);

	public String createJsonForOnlineMethod(HttpServletRequest request) throws JSONException;
	
	public CommandProcessingResult updatePaypalRecurring(JsonCommand command);

	public CommandProcessingResult updatePaypalProfileStatus(JsonCommand command);

	public void disConnectOrder(String profileId);

	public PaypalRecurringBilling getRecurringBillingObject(String profileId);

	public String getOrderStatus(Long orderId);

	public Long updatePaypalRecurringBilling(String profileId);

	public CommandProcessingResult deleteRecurringBilling(JsonCommand command);

	public CommandProcessingResult cardPayment(JsonCommand command);

}
