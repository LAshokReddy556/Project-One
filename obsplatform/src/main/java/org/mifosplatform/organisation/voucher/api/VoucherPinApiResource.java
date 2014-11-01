package org.mifosplatform.organisation.voucher.api;

import java.util.Arrays;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.codes.data.CodeData;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.voucher.data.VoucherData;
import org.mifosplatform.organisation.voucher.service.VoucherReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

/**
 * The class <code>VoucherPinApiResource</code> is developed for
 * Generating the Vouchers. 
 * Using this voucher Subscriber/client can 
 * Pay his due amount (or) pay money for Pre-paid Plans.
 * <p>A <code>VoucherPinApiResource</code> includes methods for 
 * Generating the Vouchers and Downloading the Vouchers List.
 * @author  ashokreddy
 */

@Path("/vouchers")
@Component
@Scope("singleton")
public class VoucherPinApiResource {

	/**
	 * The set of parameters that are supported in response for {@link CodeData}
	 */
	private static final Set<String> RESPONSE_PARAMETERS = new HashSet<String>(
			Arrays.asList("id", "batchName", "batchDescription", "length",
					"beginWith", "pinCategory", "pinType", "quantity",
					"serialNo", "expiryDate", "dateFormat", "pinValue",
					"pinNO", "locale", "pinExtention"));

	/** The value is used for Create Permission Checking. */
	private static String resourceNameForPermissions = "VOUCHER";

	/** The value is used for Download Permission Checking. */
	private static String resourceNameFordownloadFilePermissions = "DOWNLOAD_FILE";

	/** The Object is used for Authentication Checking. */
	private PlatformSecurityContext context;
	
	/** The Below Objects are used for Program. */
	private VoucherReadPlatformService readPlatformService;
	private DefaultToApiJsonSerializer<VoucherData> toApiJsonSerializer;
	private ApiRequestParameterHelper apiRequestParameterHelper;
	private PortfolioCommandSourceWritePlatformService writePlatformService;

	@Autowired
	public VoucherPinApiResource(
			final PlatformSecurityContext context,
			final VoucherReadPlatformService readPlatformService,
			final DefaultToApiJsonSerializer<VoucherData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService writePlatformService) {

		this.context = context;
		this.readPlatformService = readPlatformService;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.writePlatformService = writePlatformService;

	}

	/**
	 * This method <code>createVoucherBatch</code> is 
	 * Used for Creating a Batch/Group with specify the characteristic. Like Name/Description of Group  and length of the VoucherPins in Group, 
	 * Category of the VoucherPin(Numeric/Alphabetic/AlphaNumeric) in Group, Starting String of VoucherPin in Group, 
	 * Quantity of VoucherPins in Group, Expire Date of Vouchers in the Group , Value of VoucherPin etc..
	 * 
	 * Note: using this method we didn't Generate VoucherPins.
	 *  
	 * @param requestData 
	 * 			Containg input data in the Form of JsonObject.
	 * @return
	 */
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createVoucherBatch(final String requestData) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createVoucherGroup().withJson(requestData).build();
		
		final CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);
		
		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * This method <code>retrieveTemplate</code> 
	 * used for Retrieving the all mandatory/necessary data
	 * For creating a VoucherPin Group/Batch.
	 * 
	 * @param uriInfo
	 * 			Containing Url information 
	 * @return
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveTemplate(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		
		final List<EnumOptionData> pinCategoryData = this.readPlatformService.pinCategory();
		
		final List<EnumOptionData> pinTypeData = this.readPlatformService.pinType();	
		
		final VoucherData voucherData = new VoucherData(pinCategoryData, pinTypeData);
		
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		
		return this.toApiJsonSerializer.serialize(settings, voucherData, RESPONSE_PARAMETERS);
	}

	
	/**
	 * This method <code>retrieveVoucherGroups</code> 
	 * used for Retrieving the All Voucherpin Groups/Batch Data.
	 * 
	 * @param uriInfo
	 * 			Containing Url information 
	 * @return
	 */	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveVoucherGroups(@Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		
		final List<VoucherData> randomGenerator = this.readPlatformService.getAllData();
		
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		
		return this.toApiJsonSerializer.serialize(settings, randomGenerator, RESPONSE_PARAMETERS);
	}
	
	/**
	 * This method <code>retrieveVoucherPinList</code> 
	 * Used to retrieve the VoucherPins list of a Voucher Group/Batch 
	 * based on batchId.
	 * We can get the Data in the Format of .csv(comma separated value).
	 * 
	 * @param batchId
	 * 			Voucher Group/Batch id value.
	 * @param uriInfo
	 * 			Containing Url information 
	 * @return
	 */

	@GET
	@Path("{batchId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON, "application/x-msdownload", "application/vnd.ms-excel", "application/pdf", "text/html" })
	public Response retrieveVoucherPinList(@PathParam("batchId") final Long batchId, @Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(resourceNameFordownloadFilePermissions);
		
		final StreamingOutput result = this.readPlatformService.retrieveVocherDetailsCsv(batchId);
		
		return Response.ok().entity(result).type("application/x-msdownload")
				.header("Content-Disposition", "attachment;filename=" + "Vochers_" + batchId + ".csv")
				.build();
	}
	
	/**
	 * This method <code>generateVoucherPins</code> Used for Generating VoucherPins.
	 * We are passing Group/Batch Id as Parameter, Based on this batchId we can get the 
	 * Details of a Batch/Group. like quantity,length,type etc...
	 * 
	 * @param batchId
	 * 			Voucher Group/Batch id value.
	 * @param uriInfo
	 * 			Containing Url information 
	 * @return
	 */

	@POST
	@Path("{batchId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String generateVoucherPins(@PathParam("batchId") final Long batchId, @Context final UriInfo uriInfo) {
		
		final JsonObject object = new JsonObject();
		object.addProperty("batchId", batchId);
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().generateVoucherPin(batchId)
				.withJson(object.toString()).build();
		
		final CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);
		
		return this.toApiJsonSerializer.serialize(result);
	}

}