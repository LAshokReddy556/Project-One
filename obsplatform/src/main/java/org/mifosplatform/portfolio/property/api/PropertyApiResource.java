package org.mifosplatform.portfolio.property.api;

import java.io.File;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.crm.clientprospect.service.SearchSqlQuery;
import org.mifosplatform.infrastructure.codes.data.CodeData;
import org.mifosplatform.infrastructure.core.api.ApiConstants;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.address.data.CityDetailsData;
import org.mifosplatform.organisation.address.service.AddressReadPlatformService;
import org.mifosplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;
import org.mifosplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.mifosplatform.portfolio.property.data.PropertyDefinationData;
import org.mifosplatform.portfolio.property.exceptions.PropertyCodeAllocatedException;
import org.mifosplatform.portfolio.property.service.PropertyReadPlatformService;
import org.mifosplatform.scheduledjobs.dataupload.command.DataUploadCommand;
import org.mifosplatform.scheduledjobs.dataupload.service.DataUploadWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

/**
 * @author ranjith
 * this api class used to create,update and delete different property's 
 * Date: 12/04/2015
 */
@Path("/property")
@Component
@Scope("singleton")
public class PropertyApiResource {

	/**
	 * The set of parameters that are supported in response for {@link CodeData}
	 */
	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "propertycode", "propertyTypeId", "unitCode",
					"floor", "buildingCode", "parcel", "street", "status","precinct", "poBox"));

	public InputStream inputStreamObject;
	private final static String RESOURCENAMEFORPERMISSIONS = "PROPERTY";
	private final PlatformSecurityContext context;
	private final DefaultToApiJsonSerializer<PropertyDefinationData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
	private final PropertyReadPlatformService propertyReadPlatformService;
	private final MCodeReadPlatformService mCodeReadPlatformService;
	private final AddressReadPlatformService addressReadPlatformService;
	private final DataUploadWritePlatformService dataUploadWritePlatformService;

	@Autowired
	public PropertyApiResource(final PlatformSecurityContext context,final DefaultToApiJsonSerializer<PropertyDefinationData> toApiJsonSerializer,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService,
			final PropertyReadPlatformService propertyReadPlatformService,
			final MCodeReadPlatformService mCodeReadPlatformService,
			final AddressReadPlatformService addressReadPlatformService,
			final DataUploadWritePlatformService dataUploadWritePlatformService) {

		this.context = context;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.commandSourceWritePlatformService = commandSourceWritePlatformService;
		this.propertyReadPlatformService = propertyReadPlatformService;
		this.mCodeReadPlatformService = mCodeReadPlatformService;
		this.addressReadPlatformService = addressReadPlatformService;
		this.dataUploadWritePlatformService = dataUploadWritePlatformService;

	}

	/**
	 * @param uriInfo
	 * @return retrieved drop down data for creating properties
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrievePropertyTemplate(@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		final PropertyDefinationData propertyTypeData = hadleTemplateData();
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, propertyTypeData,RESPONSE_DATA_PARAMETERS);

	}

	private PropertyDefinationData hadleTemplateData() {

		final Collection<MCodeData> propertyTypes = this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_PROPERTY_TYPE);
		final List<CityDetailsData> citiesData = this.addressReadPlatformService.retrieveCitywithCodeDetails();
		return new PropertyDefinationData(propertyTypes, citiesData);
	}

	/**
	 * @param uriInfo
	 * @return retrieved all property details
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllPropertiesDetails(@Context final UriInfo uriInfo,@QueryParam("sqlSearch") final String sqlSearch,
			@QueryParam("limit") final Integer limit,@QueryParam("offset") final Integer offset) {

		this.context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		final SearchSqlQuery searchPropertyDetails = SearchSqlQuery.forSearch(sqlSearch, offset, limit);
		final Page<PropertyDefinationData> propertyDefinationData = this.propertyReadPlatformService.retrieveAllProperties(searchPropertyDetails);
		return this.toApiJsonSerializer.serialize(propertyDefinationData);
	}

	/**
	 * using this method posting property data
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createNewProperty(final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createProperty().withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * using this method posting property data from uploaded file
	 */
	@POST
	@Path("/documents")
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response createNewPropertyUpload(@HeaderParam("Content-Length") final Long fileSize,@FormDataParam("file") final InputStream inputStream,
			@FormDataParam("file") final FormDataContentDisposition fileDetails,@FormDataParam("file") final FormDataBodyPart bodyPart) {

		String name = "Property Data";
		FileUtils.validateFileSizeWithinPermissibleRange(fileSize, name,ApiConstants.MAX_FILE_UPLOAD_SIZE_IN_MB);
		inputStreamObject = inputStream;
		DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
		final Date date = DateUtils.getDateOfTenant();
		final DateTimeFormatter dtf = DateTimeFormat.forPattern("dd MMMM yyyy");
		final LocalDate localdate = dtf.parseLocalDate(dateFormat.format(date));
		final String fileUploadLocation = FileUtils.generateXlsFileDirectory();
		final String fileName = fileDetails.getFileName();
		if (!new File(fileUploadLocation).isDirectory()) {
			new File(fileUploadLocation).mkdirs();
		}
		final DataUploadCommand uploadStatusCommand = new DataUploadCommand(name, null, localdate, "", null, null, null, "", fileName,inputStream, fileUploadLocation);
		CommandProcessingResult result = this.dataUploadWritePlatformService.addItem(uploadStatusCommand);
		if (result != null) {
			this.dataUploadWritePlatformService.processDatauploadFile(result.resourceId());
		}
		return Response.ok().entity(result.toString()).build();

	}

	/**
	 * using this method ,we search property codes for fill client address
	 */
	@GET
	@Path("/propertycode")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllPropertiesForSearch(@Context final UriInfo uriInfo,@QueryParam("query") final String propertyCode) {

		context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		final List<PropertyDefinationData> propertyCodesData = this.propertyReadPlatformService.retrieveAllPropertiesForSearch(propertyCode);
		if(propertyCode.isEmpty()){
			throw new PropertyCodeAllocatedException();
		}
		return this.toApiJsonSerializer.serialize(propertyCodesData);
	}

	/**
	 * using this method get single property code details
	 */
	@GET
	@Path("{propertyId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveOneProperty(@PathParam("propertyId") final Long propertyId,@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		final PropertyDefinationData propertyCodeData = this.propertyReadPlatformService.retrievePropertyDetails(propertyId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		if (settings.isTemplate()) {
			final Collection<MCodeData> propertyTypes = this.mCodeReadPlatformService.getCodeValue(CodeNameConstants.CODE_PROPERTY_TYPE);
			final List<CityDetailsData> citiesData = this.addressReadPlatformService.retrieveCitywithCodeDetails();
			propertyCodeData.setPropertyTypes(propertyTypes);
			propertyCodeData.setCitiesData(citiesData);
		}
		return this.toApiJsonSerializer.serialize(settings, propertyCodeData,RESPONSE_DATA_PARAMETERS);
	}
	
	/**
	 * @param propertyId
	 * @param apiRequestBodyAsJson
	 * @return single property details are update here
	 */
	@PUT
	@Path("{propertyId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateProperty(@PathParam("propertyId") final Long propertyId,final String apiRequestBodyAsJson) {

		context.authenticatedUser();
		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateProperty(propertyId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}

	/**
	 * using this method we can remove single property code details
	 */
	@DELETE
	@Path("{propertyId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String deleteProperty(@PathParam("propertyId") final Long propertyId) {

		this.context.authenticatedUser();
		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteProperty(propertyId).build();
		final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);

	}
	
	
	/**
	 * using this method ,we track property code history
	 */
	@GET
	@Path("/history")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrievePropertyHistory(@Context final UriInfo uriInfo,@QueryParam("sqlSearch") final String sqlSearch,
			@QueryParam("limit") final Integer limit,@QueryParam("offset") final Integer offset) {

		this.context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		final SearchSqlQuery searchPropertyDetails = SearchSqlQuery.forSearch(sqlSearch, offset, limit);
		final Page<PropertyDefinationData> propertyDefinationData = this.propertyReadPlatformService.retrievePropertyHistory(searchPropertyDetails);
		return this.toApiJsonSerializer.serialize(propertyDefinationData);
	}

}
