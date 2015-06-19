package org.mifosplatform.billing.chargecode.api;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.billing.chargecode.data.BillFrequencyCodeData;
import org.mifosplatform.billing.chargecode.data.ChargeCodeData;
import org.mifosplatform.billing.chargecode.data.ChargeTypeData;
import org.mifosplatform.billing.chargecode.data.DurationTypeData;
import org.mifosplatform.billing.chargecode.service.ChargeCodeReadPlatformService;
import org.mifosplatform.billing.chargecode.service.ChargeCodeWritePlatformService;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hugo
 * 
 */
@Path("/chargecode")
@Component
@Scope("singleton")
public class ChargeCodeApiResource {


	private final Set<String> RESPONSE_PARAMETERS = new HashSet<String>(Arrays.asList("id", "chargeCode", "chargeDescription","chargeType",
			"chargeDurtion", "durationType","taxInclusive", "billFrequencyCode"));
	

	private final String resourceNameForPermissions = "CHARGECODE";
	private final PlatformSecurityContext context;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final DefaultToApiJsonSerializer<ChargeCodeData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final ChargeCodeReadPlatformService chargeCodeReadPlatformService;
	private final ChargeCodeWritePlatformService chargeCodeWritePlatformService;

	@Autowired
	public ChargeCodeApiResource(final PlatformSecurityContext context,final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final DefaultToApiJsonSerializer<ChargeCodeData> toApiJsonSerializer,final ApiRequestParameterHelper apiRequestParameterHelper,
			final ChargeCodeReadPlatformService chargeCodeReadPlatformService,final ChargeCodeWritePlatformService chargeCodeWritePlatformService) {
		this.context = context;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.chargeCodeReadPlatformService = chargeCodeReadPlatformService;
		this.chargeCodeWritePlatformService = chargeCodeWritePlatformService;
	}

	/**
	 * @param uriInfo
	 * @return retrieved list of all chargecodes details
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllChargeCodes(@Context final UriInfo uriInfo) {
		
	    context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<ChargeCodeData> chargeCodes = this.chargeCodeReadPlatformService.retrieveAllChargeCodes();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, chargeCodes,RESPONSE_PARAMETERS);

	}

	/**
	 * @param uriInfo
	 * @return retrieved template data for creating charge codes
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveChargeCodeTemplateData(@Context final UriInfo uriInfo) {

	   
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<ChargeTypeData> chargeTypeData = this.chargeCodeReadPlatformService.getChargeType();
		final List<DurationTypeData> durationTypeData = this.chargeCodeReadPlatformService.getDurationType();
		final List<BillFrequencyCodeData> billFrequencyData = this.chargeCodeReadPlatformService.getBillFrequency();
		final ChargeCodeData chargeCodeData = new ChargeCodeData(null,chargeTypeData,durationTypeData,billFrequencyData);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, chargeCodeData,RESPONSE_PARAMETERS);

	}

	/**
	 * @param uriInfo
	 * @param apiRequestBodyAsJson
	 * @return
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })

	public String createChargeCode(final String apiRequestBodyAsJson,@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createChargeCode().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	
	}

	/**
	 * @param chargeCodeId
	 * @param uriInfo
	 * @return retrieved single charge code details
	 */
	@GET
	@Path("{chargeCodeId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })

	public String retrieveSingleChargeCodeDetails(@PathParam("chargeCodeId") final Long chargeCodeId,@Context final UriInfo uriInfo) {
	   
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		ChargeCodeData chargeCodeData = chargeCodeReadPlatformService.retrieveSingleChargeCodeDetails(chargeCodeId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		if(settings.isTemplate()){
		chargeCodeData.setChargeTypeData(this.chargeCodeReadPlatformService.getChargeType());
		chargeCodeData.setDurationTypeData(this.chargeCodeReadPlatformService.getDurationType());
		chargeCodeData.setBillFrequencyCodeData(this.chargeCodeReadPlatformService.getBillFrequency());
		}
		return this.toApiJsonSerializer.serialize(settings,chargeCodeData,RESPONSE_PARAMETERS);
	}

	/**
	 * @param chargeCodeId
	 * @param apiRequestBodyAsJson
	 * @return update charge code here
	 */
	@PUT
	@Path("{chargeCodeId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })

	public String updateSingleChargeCode(@PathParam("chargeCodeId") final Long chargeCodeId,final String apiRequestBodyAsJson) {
	   
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateChargeCode(chargeCodeId).withJson(apiRequestBodyAsJson)	.build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	/**
	 * @param uriInfo
	 * @return retrieved template data for creating charge codes
	 */
	@GET
	@Path("{priceId}/{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveChargeCodeTemplateData(@PathParam("priceId") final Long priceId, 
			@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {

		//context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final ChargeCodeData chargeCodeData = this.chargeCodeReadPlatformService.retrieveChargeCodeForRecurring(priceId);
		final BigDecimal finalAmount=this.chargeCodeWritePlatformService.calculateFinalAmount(chargeCodeData,clientId,priceId);
		chargeCodeData.setPlanfinalAmount(finalAmount);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, chargeCodeData, RESPONSE_PARAMETERS);

	}

}
