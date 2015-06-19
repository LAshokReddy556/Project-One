package org.mifosplatform.finance.depositandrefund.handler;
import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.finance.depositandrefund.service.DepositeWritePlatformService;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateDepositeCommandHandler implements NewCommandSourceHandler {

	private final DepositeWritePlatformService depositewritePlatformService;

	@Autowired
	public CreateDepositeCommandHandler(
			final DepositeWritePlatformService depositewritePlatformService) {
		this.depositewritePlatformService = depositewritePlatformService;
	}

	@Transactional
	@Override
	public CommandProcessingResult processCommand(final JsonCommand command) {

		return this.depositewritePlatformService.createDeposite(command);
	}
	
	
}

