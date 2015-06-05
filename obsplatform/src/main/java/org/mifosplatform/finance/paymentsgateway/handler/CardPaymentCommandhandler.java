package org.mifosplatform.finance.paymentsgateway.handler;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.finance.paymentsgateway.service.PaymentGatewayRecurringWritePlatformService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author ashokreddy
 * 
 */
@Service
public class CardPaymentCommandhandler implements NewCommandSourceHandler{


	private final PaymentGatewayRecurringWritePlatformService paymentGatewayRecurringWritePlatformService;
	
	@Autowired
	public CardPaymentCommandhandler(PaymentGatewayRecurringWritePlatformService paymentGatewayRecurringWritePlatformService){
		
		this.paymentGatewayRecurringWritePlatformService = paymentGatewayRecurringWritePlatformService;
	}
	
	@Override
	public CommandProcessingResult processCommand(JsonCommand command) {
		
		return this.paymentGatewayRecurringWritePlatformService.cardPayment(command);
	}

	

}
