package org.mifosplatform.billing.servicetransfer.service;

import java.util.List;

import org.mifosplatform.organisation.feemaster.data.FeeMasterData;

public interface ServiceTransferReadPlatformService {
	
	List<FeeMasterData> retrieveSingleFeeDetails(Long clientId, String transationType);
}
