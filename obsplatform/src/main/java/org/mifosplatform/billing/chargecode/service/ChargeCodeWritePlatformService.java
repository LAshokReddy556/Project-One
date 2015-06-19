package org.mifosplatform.billing.chargecode.service;

import java.math.BigDecimal;

import org.mifosplatform.billing.chargecode.data.ChargeCodeData;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

/**
 * @author hugo
 *
 */
public interface ChargeCodeWritePlatformService {

	 CommandProcessingResult createChargeCode(JsonCommand command);
	
	 CommandProcessingResult updateChargeCode(JsonCommand command,Long chargeCodeId);
	
	 BigDecimal calculateFinalAmount(ChargeCodeData chargeCodeData,Long clientId, Long priceId);
}
