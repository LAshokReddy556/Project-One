package org.mifosplatform.finance.paymentsgateway.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class PaypalBillingPeriodTypeMisMatchException extends AbstractPlatformDomainRuleException{

	public PaypalBillingPeriodTypeMisMatchException(String message) {
		
		super("error.msg.paypal.billingPeriodType.mismatch", "BillingPrepaidType MisMatch with "+ message ,"");
	}

	

}
