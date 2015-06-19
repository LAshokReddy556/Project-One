package org.mifosplatform.portfolio.planmapping.execption;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;


public class PlanMappingNotExist extends AbstractPlatformDomainRuleException {


	public PlanMappingNotExist(String planCode) {
		 super("error.msg.plan.mapping.does.not.exist", "Plan Mapping does not exist",planCode);
		 
	}
}
