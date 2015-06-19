package org.mifosplatform.finance.depositandrefund.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;

public interface DepositeWritePlatformService {


	CommandProcessingResult createDeposite(JsonCommand command);


}
