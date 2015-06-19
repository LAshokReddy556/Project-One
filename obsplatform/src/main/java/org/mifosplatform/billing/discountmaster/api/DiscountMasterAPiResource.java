package org.mifosplatform.billing.discountmaster.api;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.billing.discountmaster.data.DiscountDetailData;
import org.mifosplatform.billing.discountmaster.data.DiscountMasterData;
import org.mifosplatform.billing.discountmaster.domain.DiscountDetails;
import org.mifosplatform.billing.discountmaster.service.DiscountReadPlatformService;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;
import org.mifosplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.mifosplatform.portfolio.plan.service.PlanReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author hugo
 * this api class used to create,update and delete diff discounts 
 */
@Path("/discount")
@Component
@Scope("singleton")
public class DiscountMasterAPiResource {

	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "discountCode", "discountDescription",
					"discountType", "discountRate", "startDate","discountStatus"));
	
	private final String resourceNameForPermissions = "DISCOUNT";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<DiscountMasterData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final DiscountReadPlatformService discountReadPlatformService;
	private final PlanReadPlatformService planReadPlatformService;
	private final MCodeReadPlatformService mCodeReadPlatformService;

	@Autowired
	public DiscountMasterAPiResource(final PlatformSecurityContext context,final DefaultToApiJsonSerializer<DiscountMasterData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final DiscountReadPlatformService discountReadPlatformService,final PlanReadPlatformService planReadPlatformService,
			final MCodeReadPlatformService codeReadPlatformService) {
		
		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.discountReadPlatformService = discountReadPlatformService;
		this.planReadPlatformService = planReadPlatformService;
		this.mCodeReadPlatformService = codeReadPlatformService;
	}

	/**
	 * @param uriInfo
	 * @return retrieved all discounts details
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllDiscountDetails(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final List<DiscountMasterData> discountMasterDatas = this.discountReadPlatformService.retrieveAllDiscounts();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings,discountMasterDatas,RESPONSE_DATA_PARAMETERS);
	}

	/**
	 * @param uriInfo
	 * @return retrieved drop down data for creating discounts
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveDiscountTemplate(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		DiscountMasterData discountMasterData = handleTemplateData();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings,discountMasterData,RESPONSE_DATA_PARAMETERS);

	}

	private DiscountMasterData handleTemplateData() {
		final List<EnumOptionData> statusData = this.planReadPlatformService.retrieveNewStatus();
		final Collection<MCodeData> discountTypeData = mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_TYPE);
		final Collection<MCodeData> clientCategoryDatas = mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_CLIENT_CATEGORY);
		return new DiscountMasterData(statusData, discountTypeData,clientCategoryDatas);
	}

	/**
	 * @param apiRequestBodyAsJson
	 * @return
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createNewDiscount(final String apiRequestBodyAsJson) {

		context.authenticatedUser();
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createDiscount().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * @param discountId
	 * @param uriInfo
	 * @return single discount details
	 */
	@GET
	@Path("{discountId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveSingleDiscountDetails(@PathParam("discountId") final Long discountId,@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		DiscountMasterData discountMasterData = this.discountReadPlatformService.retrieveSingleDiscountDetail(discountId);
		List<DiscountDetailData> discountDetailDatas = this.discountReadPlatformService.retrieveDiscountdetails(discountId);
		discountMasterData.setDiscountDetailsData(discountDetailDatas);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		if(settings.isTemplate()){
		final List<EnumOptionData> statusData = this.planReadPlatformService.retrieveNewStatus();
		final Collection<MCodeData> discountTypeData = mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_TYPE);
		final Collection<MCodeData> clientCategoryDatas = mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_CLIENT_CATEGORY);
		discountMasterData.setStatusData(statusData);
		discountMasterData.setclientCategoryData(clientCategoryDatas);
		discountMasterData.setDiscountTypeData(discountTypeData);
	    }
		return this.toApiJsonSerializer.serialize(settings,discountMasterData,RESPONSE_DATA_PARAMETERS);
	}

	/**
	 * @param discountId
	 * @param apiRequestBodyAsJson
	 * @return single discount details are update here
	 */
	@PUT
	@Path("{discountId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateDiscount(@PathParam("discountId") final Long discountId,final String apiRequestBodyAsJson) {

		context.authenticatedUser();
		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateDiscount(discountId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * @param discountId
	 * @return
	 */
	@DELETE
	@Path("{discountId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteDiscount(@PathParam("discountId") final Long discountId) {

		context.authenticatedUser();
		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteDiscount(discountId).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);

	}

}
