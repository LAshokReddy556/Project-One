package org.mifosplatform.finance.depositandrefund.service;

import org.mifosplatform.organisation.feemaster.data.FeeMasterData;

/**
 * @author hugo
 * 
 */
public interface DepositeReadPlatformService {

	FeeMasterData retrieveDepositDetails(Long feeId, Long clientId);

}
