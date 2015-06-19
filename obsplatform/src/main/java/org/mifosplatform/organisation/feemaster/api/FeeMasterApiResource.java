package org.mifosplatform.organisation.feemaster.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.billing.chargecode.data.ChargesData;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.item.data.ItemData;
import org.mifosplatform.logistics.item.service.ItemReadPlatformService;
import org.mifosplatform.logistics.ownedhardware.service.OwnedHardwareReadPlatformService;
import org.mifosplatform.organisation.feemaster.data.FeeMasterData;
import org.mifosplatform.organisation.feemaster.service.FeeMasterReadplatformService;
import org.mifosplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;
import org.mifosplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.mifosplatform.organisation.region.data.RegionData;
import org.mifosplatform.organisation.region.service.RegionReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/feemaster")
@Component
@Scope("singleton")
public class FeeMasterApiResource {

	private  final Set<String> RESPONSE_DATA_PARAMETERS=new HashSet<String>(Arrays.asList("feeMasterData","transactionTypeDatas","chargeDatas","regionDatas"));
    private final String resourceNameForPermissions = "FEEMASTER";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<FeeMasterData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final ItemReadPlatformService itemReadPlatformService;
	private final RegionReadPlatformService regionReadPlatformService; 
	private final MCodeReadPlatformService mCodeReadPlatformService; 
	private final FeeMasterReadplatformService feeMasterReadplatformService; 
	private final OwnedHardwareReadPlatformService ownedHardwareReadPlatformService;

	@Autowired
	public FeeMasterApiResource(final PlatformSecurityContext context,final DefaultToApiJsonSerializer<FeeMasterData> toApiJsonSerializer, 
			final ApiRequestParameterHelper apiRequestParameterHelper,final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final ItemReadPlatformService itemReadPlatformService,final RegionReadPlatformService regionReadPlatformService,
			final MCodeReadPlatformService mCodeReadPlatformService,final FeeMasterReadplatformService feeMasterReadplatformService,
			final OwnedHardwareReadPlatformService ownedHardwareReadPlatformService) {
		
		        this.context = context;
		        this.toApiJsonSerializer = toApiJsonSerializer;
		        this.apiRequestParameterHelper = apiRequestParameterHelper;
		        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		        this.itemReadPlatformService=itemReadPlatformService;
		        this.regionReadPlatformService=regionReadPlatformService;
		        this.mCodeReadPlatformService=mCodeReadPlatformService;
		        this.feeMasterReadplatformService=feeMasterReadplatformService;
		        this.ownedHardwareReadPlatformService = ownedHardwareReadPlatformService;
		    }
	
	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveAllContracts(@Context final UriInfo uriInfo,@QueryParam("transactionType") String transType) {
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final Collection<FeeMasterData> feeMasterData=this.feeMasterReadplatformService.retrieveAllData(transType);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, feeMasterData, RESPONSE_DATA_PARAMETERS);
	}
	
	@GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveFeeMasterTemplateInfo(@Context final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
    final Collection<MCodeData> transactionTypeDatas = this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.TRANSACTION_TYPE);
    final List<ChargesData> chargeDatas = this.itemReadPlatformService.retrieveChargeCode();
	final List<RegionData> regionDatas = this.regionReadPlatformService.getRegionDetails();
	List<ItemData> itemCodes = this.ownedHardwareReadPlatformService.retriveTemplate();
    final FeeMasterData feeMasterData=new FeeMasterData(transactionTypeDatas,chargeDatas,regionDatas, itemCodes);
    final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
    return this.toApiJsonSerializer.serialize(settings, feeMasterData, RESPONSE_DATA_PARAMETERS);
    
    }
	
	@GET
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSingletemData(@PathParam("id") final Long id, @Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		FeeMasterData feeMasterData=this.feeMasterReadplatformService.retrieveSingleFeeMasterDetails(id); 
		final Collection<MCodeData> transactionTypeDatas = this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.TRANSACTION_TYPE);
	    final List<ChargesData> chargeDatas = this.itemReadPlatformService.retrieveChargeCode();
		final List<RegionData> regionDatas = this.regionReadPlatformService.getRegionDetails();
		final List<FeeMasterData> feeMasterRegionPricesDatas = this.feeMasterReadplatformService.retrieveRegionPrice(id);
		final List<ItemData> itemCodes = this.ownedHardwareReadPlatformService.retriveTemplate();
   		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
   		feeMasterData=new FeeMasterData(feeMasterData,transactionTypeDatas,chargeDatas,regionDatas,feeMasterRegionPricesDatas,itemCodes);
   		feeMasterData.setRegionDatas(regionDatas);
   		feeMasterData.setFeeMasterRegionPricesDatas(feeMasterRegionPricesDatas);
   		return this.toApiJsonSerializer.serialize(settings, feeMasterData, RESPONSE_DATA_PARAMETERS);
	}
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String createNewFeeMaster(final String apiRequestBodyAsJson) {
		 final CommandWrapper commandRequest = new CommandWrapperBuilder().createFeeMaster().withJson(apiRequestBodyAsJson).build();
	        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
	        return this.toApiJsonSerializer.serialize(result);
	}
	
	@PUT
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateFeeMaster(@PathParam("id") final Long id,final String jsonRequestBody) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateFeeMaster(id).withJson(jsonRequestBody).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	@DELETE
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteFeeMaster(@PathParam("id") final Long id) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteFeeMaster(id).build();
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
}
