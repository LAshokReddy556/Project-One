package org.mifosplatform.billing.servicetransfer.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.billing.servicetransfer.data.ClientPropertyData;
import org.mifosplatform.billing.servicetransfer.exception.NoFeeMasterRegionalPriceFound;
import org.mifosplatform.billing.servicetransfer.service.ServiceTransferReadPlatformService;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.feemaster.data.FeeMasterData;
import org.mifosplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;
import org.mifosplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.mifosplatform.portfolio.property.service.PropertyReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author ranjith
 * this api class used to service transfer with property's 
 * Date: 14/04/2015
 */
@Path("/servicetransfer")
@Component
@Scope("singleton")
public class ServiceTransferApiResource {

	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("clientId", "propertyCode"));

	private final String resourceNameForPermissions = "SERVICETRANSFER";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<ClientPropertyData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final PropertyReadPlatformService propertyReadPlatformService;
	private final ServiceTransferReadPlatformService serviceTransferReadPlatformService;
	private final MCodeReadPlatformService mCodeReadPlatformService;

	@Autowired
	public ServiceTransferApiResource(final PlatformSecurityContext context,final DefaultToApiJsonSerializer<ClientPropertyData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final PropertyReadPlatformService propertyReadPlatformService,final ServiceTransferReadPlatformService serviceTransferReadPlatformService,
			final MCodeReadPlatformService mCodeReadPlatformService) {
		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.propertyReadPlatformService = propertyReadPlatformService;
		this.serviceTransferReadPlatformService = serviceTransferReadPlatformService;
		this.mCodeReadPlatformService = mCodeReadPlatformService;

	}

	/**
	 * @param uriInfo
	 * @return retrieved client property details
	 */
	@GET
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveClientPropertyData(@PathParam("clientId") final Long clientId,@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		ClientPropertyData clientPropertyData;
		clientPropertyData = this.propertyReadPlatformService.retrieveClientPropertyDetails(clientId);
		if(clientPropertyData !=null){
			serviceTransferGetData(clientPropertyData,clientId);
			/*final List<FeeMasterData> feeMasterData = this.serviceTransferReadPlatformService.retrieveSingleFeeDetails(clientId,"Service Transfer");
			final Collection<MCodeData> propertyTypes = this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_PROPERTY_TYPE);
			if(!feeMasterData.isEmpty()){
		    	clientPropertyData.setFeeMasterData(feeMasterData.get(0));
			    clientPropertyData.setPropertyTypes(propertyTypes);
			 }else{
				throw new NoFeeMasterRegionalPriceFound();
			}*/
		}else{
			clientPropertyData = new ClientPropertyData();
			serviceTransferGetData(clientPropertyData,clientId);
		}
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, clientPropertyData,RESPONSE_DATA_PARAMETERS);
	}

	/**
	 * @param apiRequestBodyAsJson
	 * @return
	 */
	@POST
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String serviceTransferProperty(@PathParam("clientId") final Long clientId,final String apiRequestBodyAsJson) {

		context.authenticatedUser();
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createServiceTransfer(clientId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	public void serviceTransferGetData(ClientPropertyData clientPropertyData, Long clientId){
		final List<FeeMasterData> feeMasterData = this.serviceTransferReadPlatformService.retrieveSingleFeeDetails(clientId,"Service Transfer");
		final Collection<MCodeData> propertyTypes = this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_PROPERTY_TYPE);
		if(!feeMasterData.isEmpty()){
	    	clientPropertyData.setFeeMasterData(feeMasterData.get(0));
		    clientPropertyData.setPropertyTypes(propertyTypes);
		 }else{
			throw new NoFeeMasterRegionalPriceFound();
		}
	}

}
