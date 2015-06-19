package org.mifosplatform.logistics.itemdetails.api;

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

import org.mifosplatform.billing.servicetransfer.service.ServiceTransferReadPlatformService;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.crm.clientprospect.service.SearchSqlQuery;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.grn.service.GrnReadPlatformService;
import org.mifosplatform.logistics.item.data.ItemData;
import org.mifosplatform.logistics.itemdetails.data.InventoryGrnData;
import org.mifosplatform.logistics.itemdetails.data.ItemDetailsData;
import org.mifosplatform.logistics.itemdetails.data.ItemSerialNumberData;
import org.mifosplatform.logistics.itemdetails.domain.ItemDetailsAllocation;
import org.mifosplatform.logistics.itemdetails.service.ItemDetailsReadPlatformService;
import org.mifosplatform.logistics.onetimesale.service.OneTimeSaleReadPlatformService;
import org.mifosplatform.organisation.feemaster.data.FeeMasterData;
import org.mifosplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;
import org.mifosplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/itemdetails")
@Component
@Scope("singleton")
public class ItemDetailsApiResource {
	
	private final Set<String> RESPONSE_DATA_SERIAL_NUMBER_PARAMETERS = new HashSet<String>(Arrays.asList("serialNumber"));
	private final Set<String> RESPONSE_DATA_GRN_IDS_PARAMETERS = new HashSet<String>(Arrays.asList("id"));
	private final Set<String> RESPONSE_ITEM_DETAILS_PARAMETERS = new HashSet<String>(Arrays.asList("id", "purchaseDate", "supplierId",
            "itemMasterId","orderdQuantity", "receivedQuantity","id", "itemMasterId", "serialNumber", "grnId","provisioningSerialNumber",
            "quality", "status","warranty", "remarks"));
	private final Set<String> RESPONSE_ITEM_MASTER_DETAILS_PARAMETERS = new HashSet<String>(Arrays.asList("id", "itemCode", "itemDescription",
																							"chargeCode","unitPrice"));

	private final String resourceNameForPermissions = "INVENTORY";
    private final String resourceNameForGrnPermissions = "GRN";
    private final String resourceNameForPermissionsAllocation = "ALLOCATION";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<ItemDetailsData> toApiJsonSerializerForItem;
	private final DefaultToApiJsonSerializer<ItemSerialNumberData> toApiJsonSerializerForAllocationHardware;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final GrnReadPlatformService inventoryGrnReadPlatformService;
	private final ItemDetailsReadPlatformService itemDetailsReadPlatformService;
	private final DefaultToApiJsonSerializer<ItemDetailsAllocation> toApiJsonSerializerForItemAllocation;
	private final MCodeReadPlatformService mCodeReadPlatformService;
	private final DefaultToApiJsonSerializer<ItemData> toApiJsonSerializerForItemData;
	private final OfficeReadPlatformService officeReadPlatformService;
	private final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService;
	private final ServiceTransferReadPlatformService serviceTransferReadPlatformService;
    
	@Autowired
	public ItemDetailsApiResource(final PlatformSecurityContext context,final DefaultToApiJsonSerializer<ItemDetailsData> toApiJsonSerializerForItem,
			ApiRequestParameterHelper apiRequestParameterHelper,PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final GrnReadPlatformService inventoryGrnReadPlatformService,final MCodeReadPlatformService mCodeReadPlatformService,
			final DefaultToApiJsonSerializer<ItemDetailsAllocation> toApiJsonSerializerForItemAllocation,
			final DefaultToApiJsonSerializer<ItemSerialNumberData> toApiJsonSerializerForAllocationHardware,
			final ItemDetailsReadPlatformService itemDetailsReadPlatformService,
			final DefaultToApiJsonSerializer<ItemData> toApiJsonSerializerForItemData,final OfficeReadPlatformService officeReadPlatformService,
			final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService,
			final ServiceTransferReadPlatformService serviceTransferReadPlatformService) {
		
		this.context=context;
		this.mCodeReadPlatformService=mCodeReadPlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
	    this.toApiJsonSerializerForItem = toApiJsonSerializerForItem;
	    this.itemDetailsReadPlatformService = itemDetailsReadPlatformService;
	    this.inventoryGrnReadPlatformService = inventoryGrnReadPlatformService;
	    this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
	    this.toApiJsonSerializerForItemAllocation = toApiJsonSerializerForItemAllocation;
	    this.toApiJsonSerializerForAllocationHardware = toApiJsonSerializerForAllocationHardware;
	    this.toApiJsonSerializerForItemData = toApiJsonSerializerForItemData;
	    this.officeReadPlatformService = officeReadPlatformService;
	    this.oneTimeSaleReadPlatformService = oneTimeSaleReadPlatformService;
	    this.serviceTransferReadPlatformService = serviceTransferReadPlatformService;
	}

	/*
	 * for storing item details into b_item_detail table
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String addItemDetails(final String jsonRequestBody) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createInventoryItem(null).withJson(jsonRequestBody).build();
		 final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
	     return this.toApiJsonSerializerForItem.serialize(result);
	}

	@POST
	@Path("allocation")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })/*@QueryParam("id") final Long id,*/
	public String allocateHardware(final String jsonRequestBody) {
		CommandWrapper command = new CommandWrapperBuilder().allocateHardware().withJson(jsonRequestBody).build();
		final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(command);
		return this.toApiJsonSerializerForItemAllocation.serialize(result);
	}
	
  @GET
  @Path("singleitem/{itemId}")
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON })
  public String retriveSingleItemDetail(@Context final UriInfo uriInfo, @PathParam("itemId") final Long itemId) {
     context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
    final ItemDetailsData clientDatafinal = this.itemDetailsReadPlatformService.retriveSingleItemDetail(itemId);
     final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
   return this.toApiJsonSerializerForItem.serialize(settings,clientDatafinal,RESPONSE_ITEM_DETAILS_PARAMETERS);
}
	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retriveItemDetails(@Context final UriInfo uriInfo,@QueryParam("sqlSearch") final String sqlSearch, 
			         @QueryParam("limit") final Integer limit, @QueryParam("offset") final Integer offset,
			         @QueryParam("officeName") final String officeName, @QueryParam("itemCode") final String itemCode) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final SearchSqlQuery searchItemDetails =SearchSqlQuery.forSearch(sqlSearch, offset,limit );
		final Page<ItemDetailsData> clientDatafinal = this.itemDetailsReadPlatformService.retriveAllItemDetails(searchItemDetails,officeName,itemCode);
		return this.toApiJsonSerializerForItem.serialize(clientDatafinal);
	}

	@PUT
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateEventAction(@PathParam("id") final Long id,final String apiRequestBodyAsJson) {
		 final CommandWrapper commandRequest = new CommandWrapperBuilder().updateInventoryItem(id).withJson(apiRequestBodyAsJson).build();
		 final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		  return this.toApiJsonSerializerForItem.serialize(result);
	}
	
	@DELETE
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteItemDetail(@PathParam("id") final Long id,final String apiRequestBodyAsJson) {
		 final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteInventoryItem(id).withJson(apiRequestBodyAsJson).build();
		 final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		  return this.toApiJsonSerializerForItem.serialize(result);
	}
	

	@GET
	@Path("{itemmasterId}/{officeId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retriveItemSerialNumbers(@PathParam("itemmasterId") final Long itemmasterId,@PathParam("officeId") final Long officeId,
			@QueryParam("query") final String query, @Context final UriInfo uriInfo){
			
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissionsAllocation);
		List<String> itemSerialNumbers = this.itemDetailsReadPlatformService.retriveSerialNumbersOnKeyStroke(itemmasterId,query,officeId);
		ItemSerialNumberData InventoryItemSerialNumberData = new ItemSerialNumberData(itemSerialNumbers);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializerForAllocationHardware.serialize(settings, InventoryItemSerialNumberData, RESPONSE_DATA_SERIAL_NUMBER_PARAMETERS);
	}
	
	
	@GET
	@Path("template")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retriveGrnIds(@Context final UriInfo uriInfo){
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForGrnPermissions);
		Collection<InventoryGrnData> inventoryGrnData = this.inventoryGrnReadPlatformService.retriveGrnIds();
		Collection<MCodeData> qualityDatas=this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_ITEM_QUALITY);
		Collection<MCodeData> statusDatas=this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_ITEM_STATUS);
		ItemDetailsData itemDetailsData=new ItemDetailsData(inventoryGrnData,qualityDatas,statusDatas,null,null);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializerForItem.serialize(settings,itemDetailsData,RESPONSE_DATA_GRN_IDS_PARAMETERS);
	}
	
	@PUT
	@Path("deallocate/{allocationId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deAllocateHardware(@PathParam("allocationId") final Long id,final String apiRequestBodyAsJson) {
		
		 final CommandWrapper commandRequest = new CommandWrapperBuilder().deAllocate(id).withJson(apiRequestBodyAsJson).build();
		 final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		  return this.toApiJsonSerializerForItem.serialize(result);
	}
	
	/**
	 * This is for selfcare MacId's and serialNumbers
	 * */
	@GET
	@Path("searchserialnum")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retriveItemSerialNumbersBasedOnSerialAndProvisionalSerialNum(@QueryParam("query") final String query,@Context final UriInfo uriInfo){
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissionsAllocation);
		List<ItemDetailsData> itemSerialNumbers = null;
		ApiRequestJsonSerializationSettings settings = null;
		if(query != null && query.length()>0){
			itemSerialNumbers = this.itemDetailsReadPlatformService.retriveSerialNumbersOnKeyStroke(query);
			settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
			
		}
		return this.toApiJsonSerializerForItem.serialize(settings, itemSerialNumbers, RESPONSE_DATA_SERIAL_NUMBER_PARAMETERS);
	}
	
	/**
	 * This is for getting item code,charge code and prices using serialNumber
	 * */
	@GET
	@Path("serialnum")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retriveItemDetailsBySerialNum(@QueryParam("query") final String query,@QueryParam("clientId") final Long clientId,@Context final UriInfo uriInfo){
		
			 context.authenticatedUser().validateHasReadPermission(resourceNameForPermissionsAllocation);
			 final ItemData itemMasterData = this.itemDetailsReadPlatformService.retriveItemDetailsDataBySerialNum(query,clientId);
			 final List<FeeMasterData> feeMasterData = this.serviceTransferReadPlatformService.retrieveSingleFeeDetails(clientId,"Deposit");
			 itemMasterData.setFeeMasterData(feeMasterData);
			 ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
			
		
		return this.toApiJsonSerializerForItemData.serialize(settings, itemMasterData, RESPONSE_ITEM_MASTER_DETAILS_PARAMETERS);
	}
	
	@GET
	@Path("template/dropdown")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retriveDropdownValues(@Context final UriInfo uriInfo){
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForGrnPermissions);
		final Collection<OfficeData> officeData = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
		final Collection<ItemData> itemMasterData = this.oneTimeSaleReadPlatformService.retrieveItemData();
		ItemDetailsData itemDetailsData=new ItemDetailsData(officeData,itemMasterData);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializerForItem.serialize(settings,itemDetailsData,RESPONSE_DATA_GRN_IDS_PARAMETERS);
	}

}
