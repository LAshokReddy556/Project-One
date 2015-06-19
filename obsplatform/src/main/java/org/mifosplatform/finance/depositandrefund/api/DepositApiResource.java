package org.mifosplatform.finance.depositandrefund.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.billing.chargecode.data.ChargeCodeData;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.feemaster.data.FeeMasterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hugo
 * 
 */
@Path("/deposit")
@Component
@Scope("singleton")
public class DepositApiResource {
	
	private final String resourceNameForPermissions = "DEPOSIT";
	private final PlatformSecurityContext context;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final DefaultToApiJsonSerializer<FeeMasterData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;

	@Autowired
	public DepositApiResource(final PlatformSecurityContext context,final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final DefaultToApiJsonSerializer<FeeMasterData> toApiJsonSerializer,final ApiRequestParameterHelper apiRequestParameterHelper) {
		
		this.context = context;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		
	}

	/**
	 * @param uriInfo
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })

	public String createDepositAmount(final String apiRequestBodyAsJson,@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createDeposite().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	
	}

}
