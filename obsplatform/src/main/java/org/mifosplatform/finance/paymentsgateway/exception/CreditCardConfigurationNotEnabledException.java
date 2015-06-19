package org.mifosplatform.finance.paymentsgateway.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

@SuppressWarnings("serial")
public class CreditCardConfigurationNotEnabledException extends AbstractPlatformDomainRuleException {

	public CreditCardConfigurationNotEnabledException() {
		super("error.msg.configuration.creditcard.configure.details.not.enable", "CreditCard Configuration details are not Enabled","");
	}
	
}
