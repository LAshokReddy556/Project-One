package org.mifosplatform.finance.depositandrefund.service;

import net.sf.ehcache.transaction.xa.commands.Command;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface RefundWritePlatformService {

	CommandProcessingResult createRefund(JsonCommand command, Long depositId);

}
