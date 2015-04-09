/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.vendoragreement.api;

import java.io.File;
import java.io.IOException;
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
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiConstants;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;
import org.mifosplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.mifosplatform.organisation.priceregion.data.PriceRegionData;
import org.mifosplatform.organisation.priceregion.service.RegionalPriceReadplatformService;
import org.mifosplatform.portfolio.order.service.OrderReadPlatformService;
import org.mifosplatform.portfolio.plan.data.PlanCodeData;
import org.mifosplatform.portfolio.plan.data.ServiceData;
import org.mifosplatform.portfolio.plan.service.PlanReadPlatformService;
import org.mifosplatform.portfolio.service.service.ServiceMasterReadPlatformService;
import org.mifosplatform.useradministration.data.AppUserData;
import org.mifosplatform.vendoragreement.data.VendorAgreementData;
import org.mifosplatform.vendoragreement.exception.VendorNotFoundException;
import org.mifosplatform.vendoragreement.service.VendorAgreementReadPlatformService;
import org.mifosplatform.vendoragreement.service.VendorAgreementWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

@Path("/vendoragreement")
@Component
@Scope("singleton")
public class VendorAgreementApiResource {

    /**
     * The set of parameters that are supported in response for
     * {@link AppUserData}.
     */
    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id"));

    private static final String RESOURCENAMEFORPERMISSIONS = "VENDORAGREEMENT";
    private final PlatformSecurityContext context;
    private final VendorAgreementReadPlatformService readPlatformService;
    private final RegionalPriceReadplatformService regionalPriceReadplatformService;
    private final DefaultToApiJsonSerializer<VendorAgreementData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlanReadPlatformService planReadPlatformService;
    private final ServiceMasterReadPlatformService serviceMasterReadPlatformService;
    private final OrderReadPlatformService orderReadPlatformService;
    private final MCodeReadPlatformService mCodeReadPlatformService;
    public InputStream inputStreamObject;
    private final VendorAgreementWritePlatformService vendorAgreementWritePlatformService;
    
    @Autowired
    public VendorAgreementApiResource(final PlatformSecurityContext context, final VendorAgreementReadPlatformService readPlatformService,
    		final RegionalPriceReadplatformService regionalPriceReadplatformService, final DefaultToApiJsonSerializer<VendorAgreementData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final PlanReadPlatformService planReadPlatformService, final ServiceMasterReadPlatformService serviceMasterReadPlatformService,
            final OrderReadPlatformService orderReadPlatformService,
            final MCodeReadPlatformService mCodeReadPlatformService,final VendorAgreementWritePlatformService vendorAgreementWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.regionalPriceReadplatformService = regionalPriceReadplatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.planReadPlatformService = planReadPlatformService;
        this.serviceMasterReadPlatformService = serviceMasterReadPlatformService;
        this.orderReadPlatformService = orderReadPlatformService;
        this.mCodeReadPlatformService = mCodeReadPlatformService;
        this.vendorAgreementWritePlatformService = vendorAgreementWritePlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String vendorAgreementTemplateDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
        VendorAgreementData vendor=handleTemplateData();
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, vendor, RESPONSE_DATA_PARAMETERS);
    }
    
    private VendorAgreementData handleTemplateData() {
		
    	final List<PriceRegionData> priceRegionData = this.regionalPriceReadplatformService.getPriceRegionsDetails();
        //final List<EnumOptionData> statusData = this.planReadPlatformService.retrieveNewStatus();
    	final Collection<MCodeData> agreementTypes = this.mCodeReadPlatformService.getCodeValue("Agreement Type");
        final List<ServiceData> servicesData = this.serviceMasterReadPlatformService.retrieveAllServices("N");
        final List<PlanCodeData> planDatas = this.orderReadPlatformService.retrieveAllPlatformData((long)0, null);
		 
		return new VendorAgreementData(priceRegionData, agreementTypes, servicesData,
					planDatas);
			
	}
    
    @POST
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createUploadFile(@HeaderParam("Content-Length") final Long fileSize, @FormDataParam("file") final InputStream inputStream,
            @FormDataParam("file") final FormDataContentDisposition fileDetails, @FormDataParam("file") final FormDataBodyPart bodyPart,
            @FormDataParam("jsonData") final String jsonData) throws JSONException, IOException {

        FileUtils.validateFileSizeWithinPermissibleRange(fileSize, jsonData, ApiConstants.MAX_FILE_UPLOAD_SIZE_IN_MB);
        inputStreamObject=inputStream;
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        final Date date = DateUtils.getDateOfTenant();
        final DateTimeFormatter dtf = DateTimeFormat.forPattern("dd MMMM yyyy");
        final LocalDate localdate = dtf.parseLocalDate(dateFormat.format(date));
        JSONObject object = new JSONObject(jsonData);
        
        if(fileDetails != null){
        final String fileUploadLocation = FileUtils.generateXlsFileDirectory();
        final String fileName = fileDetails.getFileName();
        	if (!new File(fileUploadLocation).isDirectory()) {
        		new File(fileUploadLocation).mkdirs();
        	}
        
        String fileLocation=null;
        fileLocation = FileUtils.saveToFileSystem(inputStream, fileUploadLocation, fileName);
        object.put("fileLocation", fileLocation);
        
        }
        
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
        .createVendorAgreement() //
        .withJson(object.toString()) //
        .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
 }
    
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveVendorAgreements(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);

        final List<VendorAgreementData> vendor = this.readPlatformService.retrieveAllVendorAgreements();

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, vendor, RESPONSE_DATA_PARAMETERS);
    }
    
    @GET
	@Path("{vendorId}") /** vendorId*/
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveVendorAgreementData(	@PathParam("vendorId") final Long vendorId,@Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		List<VendorAgreementData> agreementData = this.readPlatformService.retrieveRespectiveAgreementData(vendorId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, agreementData,RESPONSE_DATA_PARAMETERS);
	}
    
    @GET
    @Path("{vendorAgreementId}/details")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveSingleVendorAgreement(@PathParam("vendorAgreementId") final Long vendorAgreementId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        VendorAgreementData vendorAgreeData = this.readPlatformService.retrieveVendorAgreement(vendorAgreementId);
        if(vendorAgreeData == null){
        	throw new VendorNotFoundException(vendorAgreementId.toString());
        }
        List<VendorAgreementData> vendorAgreementDetailsData = this.readPlatformService.retrieveVendorAgreementDetails(vendorAgreementId);
        vendorAgreeData.setVendorAgreementDetailsData(vendorAgreementDetailsData);
        
        if (settings.isTemplate()) {
        	final List<PriceRegionData> priceRegionData = this.regionalPriceReadplatformService.getPriceRegionsDetails();
        	final Collection<MCodeData> agreementTypes = this.mCodeReadPlatformService.getCodeValue("Agreement Type");
            final List<ServiceData> servicesData = this.serviceMasterReadPlatformService.retrieveAllServices("N");
            final List<PlanCodeData> planDatas = this.orderReadPlatformService.retrieveAllPlatformData((long)0, null);
            vendorAgreeData.setPriceRegionData(priceRegionData);
            vendorAgreeData.setPlanDatas(planDatas);
            vendorAgreeData.setAgreementTypes(agreementTypes);
            vendorAgreeData.setServicesData(servicesData);
        }

        return this.toApiJsonSerializer.serialize(settings, vendorAgreeData, RESPONSE_DATA_PARAMETERS);
    }
    
    @POST
    @Path("{vendorAgreementId}")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateVendorAgreement(@HeaderParam("Content-Length") final Long fileSize, @FormDataParam("file") final InputStream inputStream,
            @FormDataParam("file") final FormDataContentDisposition fileDetails, @FormDataParam("file") final FormDataBodyPart bodyPart,
            @FormDataParam("jsonData") final String jsonData,@PathParam("vendorAgreementId") final Long vendorAgreementId) throws JSONException, IOException {

        FileUtils.validateFileSizeWithinPermissibleRange(fileSize, jsonData, ApiConstants.MAX_FILE_UPLOAD_SIZE_IN_MB);
        inputStreamObject=inputStream;
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        final Date date = DateUtils.getDateOfTenant();
        final DateTimeFormatter dtf = DateTimeFormat.forPattern("dd MMMM yyyy");
        final LocalDate localdate = dtf.parseLocalDate(dateFormat.format(date));
        JSONObject object = new JSONObject(jsonData);
        
        if(fileDetails != null){
        final String fileUploadLocation = FileUtils.generateXlsFileDirectory();
        final String fileName=fileDetails.getFileName();
        	if (!new File(fileUploadLocation).isDirectory()) {
        		new File(fileUploadLocation).mkdirs();
        	}
        
        String fileLocation=null;
        fileLocation = FileUtils.saveToFileSystem(inputStream, fileUploadLocation, fileName);
        object.put("fileLocation", fileLocation);
        }
        
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
        .updateVendorAgreement(vendorAgreementId) //
        .withJson(object.toString()) //
        .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
 }

}