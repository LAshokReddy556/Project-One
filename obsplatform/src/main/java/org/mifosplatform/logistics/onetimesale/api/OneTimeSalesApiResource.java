package org.mifosplatform.logistics.onetimesale.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.billing.chargecode.data.ChargesData;
import org.mifosplatform.billing.discountmaster.data.DiscountMasterData;
import org.mifosplatform.billing.discountmaster.service.DiscountReadPlatformService;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.item.data.ItemData;
import org.mifosplatform.logistics.item.exception.NoItemRegionalPriceFound;
import org.mifosplatform.logistics.item.service.ItemReadPlatformService;
import org.mifosplatform.logistics.onetimesale.data.AllocationDetailsData;
import org.mifosplatform.logistics.onetimesale.data.OneTimeSaleData;
import org.mifosplatform.logistics.onetimesale.service.OneTimeSaleReadPlatformService;
import org.mifosplatform.logistics.onetimesale.service.OneTimeSaleWritePlatformService;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.portfolio.contract.data.SubscriptionData;
import org.mifosplatform.portfolio.contract.service.ContractPeriodReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

/**
 * @author hugo
 * 
 */
@Path("/onetimesales")
@Component
@Scope("singleton")
public class OneTimeSalesApiResource {
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("itemId", "chargedatas", "itemDatas", "units",
					"unitPrice", "saleDate", "totalprice", "quantity", "flag","allocationData", "discountMasterDatas", "id", "eventName",
					"bookedDate", "eventPrice", "chargeCode", "status","contractPeriods"));
	
	private final String resourceNameForPermissions = "ONETIMESALE";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<OneTimeSaleData> toApiJsonSerializer;
	private final DefaultToApiJsonSerializer<ItemData> defaultToApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final OneTimeSaleWritePlatformService oneTimeSaleWritePlatformService;
	private final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService;
	private final ItemReadPlatformService itemMasterReadPlatformService;
	private final DiscountReadPlatformService discountReadPlatformService;
	private final FromJsonHelper fromJsonHelper;
	private final OfficeReadPlatformService officeReadPlatformService;
	private final ContractPeriodReadPlatformService contractPeriodReadPlatformService;

	@Autowired
	public OneTimeSalesApiResource(final PlatformSecurityContext context,final DefaultToApiJsonSerializer<OneTimeSaleData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final OneTimeSaleWritePlatformService oneTimeSaleWritePlatformService,final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService,
			final ItemReadPlatformService itemReadPlatformService,final DiscountReadPlatformService discountReadPlatformService,
			final OfficeReadPlatformService officeReadPlatformService,final DefaultToApiJsonSerializer<ItemData> defaultToApiJsonSerializer,
			final FromJsonHelper fromJsonHelper,final ContractPeriodReadPlatformService contractPeriodReadPlatformService) {

		this.context = context;
		this.fromJsonHelper = fromJsonHelper;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.officeReadPlatformService = officeReadPlatformService;
		this.defaultToApiJsonSerializer = defaultToApiJsonSerializer;
		this.itemMasterReadPlatformService = itemReadPlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.discountReadPlatformService = discountReadPlatformService;
		this.oneTimeSaleReadPlatformService = oneTimeSaleReadPlatformService;
		this.oneTimeSaleWritePlatformService = oneTimeSaleWritePlatformService;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.contractPeriodReadPlatformService = contractPeriodReadPlatformService;
	}

	@POST
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createNewSale(@PathParam("clientId") final Long clientId,
			@QueryParam("devicesaleTpye") final String devicesaleTpye,final String apiRequestBodyAsJson) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createOneTimeSale(clientId,devicesaleTpye).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveItemTemplateData(@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		OneTimeSaleData data = handleTemplateRelatedData();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, data,RESPONSE_DATA_PARAMETERS);
	}

	private OneTimeSaleData handleTemplateRelatedData() {

		final List<ItemData> itemData = this.oneTimeSaleReadPlatformService.retrieveItemData();
		final Collection<OfficeData> offices = officeReadPlatformService.retrieveAllOfficesForDropdown();
		List<DiscountMasterData> discountData = this.discountReadPlatformService.retrieveAllDiscounts();
		Collection<SubscriptionData> subscriptionDatas = this.contractPeriodReadPlatformService.retrieveAllSubscription();
		return new OneTimeSaleData(itemData,discountData, offices, subscriptionDatas);

	}

	@GET
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveClientOneTimeSaleDetails(@PathParam("clientId") final Long clientId,@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<OneTimeSaleData> salesData = this.oneTimeSaleReadPlatformService.retrieveClientOneTimeSalesData(clientId);
		//final List<EventOrderData> eventOrderDatas = this.eventOrderReadplatformServie.getTheClientEventOrders(clientId);
		final OneTimeSaleData data = new OneTimeSaleData(salesData, null);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, data,RESPONSE_DATA_PARAMETERS);
	}

	@GET
	@Path("{itemId}/item")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSingleItemDetails(@PathParam("itemId") final Long itemId, @QueryParam("clientId") final Long clientId, 
			 @QueryParam("region") final String region, @Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<ItemData> itemCodeData = this.oneTimeSaleReadPlatformService.retrieveItemData();
		final List<DiscountMasterData> discountdata = this.discountReadPlatformService.retrieveAllDiscounts();
	    ItemData itemData = this.itemMasterReadPlatformService.retrieveSingleItemDetails(clientId, itemId,region,clientId != null?true:false); // If you pass clientId you can set to 'true' else 'false'

	    if(itemData == null){
	    	throw new NoItemRegionalPriceFound();
	    }
		final List<ChargesData> chargesDatas = this.itemMasterReadPlatformService.retrieveChargeCode();
		itemData = new ItemData(itemCodeData, itemData, null, null,discountdata, chargesDatas);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.defaultToApiJsonSerializer.serialize(settings, itemData,RESPONSE_DATA_PARAMETERS);
	}

	@POST
	@Path("{itemId}/totalprice")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveTotalPrice(@PathParam("itemId") final Long itemId,@Context final UriInfo uriInfo, final String apiRequestBodyAsJson) {

		final JsonElement parsedQuery = this.fromJsonHelper.parse(apiRequestBodyAsJson);
		final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson,parsedQuery, this.fromJsonHelper);
		ItemData itemData = oneTimeSaleWritePlatformService.calculatePrice(itemId, query);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.defaultToApiJsonSerializer.serialize(settings, itemData,RESPONSE_DATA_PARAMETERS);
	}

/*	@GET
	@Path("{saleId}/oneTimeSale")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSingleOneTimeSaleData(@PathParam("saleId") final Long saleId,@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		OneTimeSaleData salesData = this.oneTimeSaleReadPlatformService.retrieveSingleOneTimeSaleDetails(saleId);
		salesData = handleTemplateRelatedData(salesData);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, salesData,RESPONSE_DATA_PARAMETERS);
	}*/

	@GET
	@Path("{saleId}/allocation")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveItemAllocationDetails(@PathParam("saleId") final Long saleId,@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<AllocationDetailsData> allocationData = this.oneTimeSaleReadPlatformService.retrieveAllocationDetails(saleId);
		OneTimeSaleData salesData = new OneTimeSaleData();
		salesData.setAllocationData(allocationData);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, salesData,RESPONSE_DATA_PARAMETERS);
	}

	@DELETE
	@Path("{saleId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String cancelSale(@PathParam("saleId") final Long saleId,final String apiRequestBodyAsJson) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final CommandWrapper commandRequest = new CommandWrapperBuilder().cancelOneTimeSale(saleId).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

}
