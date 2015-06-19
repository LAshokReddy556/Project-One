package org.mifosplatform.finance.depositandrefund.exception;

import java.math.BigDecimal;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class ItemQualityAndStatusException extends AbstractPlatformDomainRuleException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ItemQualityAndStatusException(String value) {		
		super("error.msg.hardware.quality.not.good", "Hardware Quality not Good", value);
	}
	
	public ItemQualityAndStatusException() {		
		super("error.msg.hardware.unallocated", "Hardware Unallocated", "");
	}
	
	public ItemQualityAndStatusException(BigDecimal amount) {		
		super("error.msg.unable.to.refund.clientbalance.greater.than.refund.amount", "Unable to refund", "");
	}
}
