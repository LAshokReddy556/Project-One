package org.mifosplatform.finance.payments.service;

import java.util.Collection;
import java.util.List;

import org.mifosplatform.finance.payments.data.McodeData;
import org.mifosplatform.finance.payments.data.PaymentData;
import org.mifosplatform.infrastructure.core.api.JsonCommand;

public interface PaymentReadPlatformService {

	McodeData retrieveSinglePaymode(Long paymodeId);

	List<PaymentData> retrieveClientPaymentDetails(Long clientId);

	McodeData retrievePaymodeCode(JsonCommand command);

	Collection<McodeData> retrievemCodeDetails(String codeName);

	List<PaymentData> retrivePaymentsData(Long clientId);
	
	Long getOnlinePaymode(String paymentMode);

	List<PaymentData> retrieveDepositDetails(Long clientId);


}
