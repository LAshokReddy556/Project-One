package org.mifosplatform.finance.paymentsgateway.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


@SuppressWarnings("serial")
public class PaypalStatusChangeActionTypeMisMatchException extends AbstractPlatformDomainRuleException{

	public PaypalStatusChangeActionTypeMisMatchException(String message) {
		
		super("error.msg.paypal.StatusChangeActionType.mismatch", "StatusChangeActionType MisMatch with "+ message ,"");
	}

	
}
