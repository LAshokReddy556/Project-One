package org.mifosplatform.finance.paymentsgateway.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

@SuppressWarnings("serial")
public class PaypalCustomParameterDataNotFoundException extends AbstractPlatformDomainRuleException {
	
	public PaypalCustomParameterDataNotFoundException() {
		
		super("error.msg.paypal.custom.parameter.data.not.found", "Paypal Custom Parameter Data Not Found" ,"");
	}

}
