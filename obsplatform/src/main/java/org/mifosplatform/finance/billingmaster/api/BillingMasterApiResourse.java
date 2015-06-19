package org.mifosplatform.finance.billingmaster.api;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.finance.billingmaster.domain.BillMaster;
import org.mifosplatform.finance.billingmaster.domain.BillMasterRepository;
import org.mifosplatform.finance.billingmaster.service.BillMasterReadPlatformService;
import org.mifosplatform.finance.billingmaster.service.BillMasterWritePlatformService;
import org.mifosplatform.finance.billingmaster.service.BillWritePlatformService;
import org.mifosplatform.finance.billingorder.data.BillDetailsData;
import org.mifosplatform.finance.billingorder.exceptions.BillingOrderNoRecordsFoundException;
import org.mifosplatform.finance.financialtransaction.data.FinancialTransactionsData;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Path("/billmaster")
@Component
@Scope("singleton")
public class BillingMasterApiResourse {
	
	    private  final Set<String> RESPONSE_DATA_PARAMETERS=new HashSet<String>(Arrays.asList("transactionId", "transactionDate", "transactionType", "amount", "orderId",
			"invoiceId", "chrageAmount", "taxAmount", "chargeType", "amount", "billDate", "dueDate", "id", "transaction", "chargeStartDate", "chargeEndDate"));
	    
        private static final String RESOURCENAMEFORPERMISSIONS = "BILLMASTER";
	    private final PlatformSecurityContext context;
	    private final DefaultToApiJsonSerializer<FinancialTransactionsData> toApiJsonSerializer;
	    private final DefaultToApiJsonSerializer<BillDetailsData> ApiJsonSerializer;
	    private final ApiRequestParameterHelper apiRequestParameterHelper;
	    private final BillMasterReadPlatformService billMasterReadPlatformService;
		private final BillMasterRepository billMasterRepository;
		private final BillWritePlatformService billWritePlatformService;
	    private final FromJsonHelper fromApiJsonHelper;
	    private final BillMasterWritePlatformService billMasterWritePlatformService;
	    private final PortfolioCommandSourceWritePlatformService commandSourceWritePlatformService;
		
		 @Autowired
	    public BillingMasterApiResourse(final PlatformSecurityContext context, final FromJsonHelper fromJsonHelper,final BillWritePlatformService billWritePlatformService,
	    final DefaultToApiJsonSerializer<FinancialTransactionsData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
	    final BillMasterReadPlatformService billMasterReadPlatformService,final BillMasterRepository billMasterRepository,
	    final BillMasterWritePlatformService billMasterWritePlatformService,final DefaultToApiJsonSerializer<BillDetailsData> ApiJsonSerializer,
	    final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
		        
			 this.context = context;
		     this.toApiJsonSerializer = toApiJsonSerializer;
		     this.apiRequestParameterHelper = apiRequestParameterHelper;
		     this.billMasterReadPlatformService = billMasterReadPlatformService;
		     this.billMasterRepository = billMasterRepository;
		     this.fromApiJsonHelper = fromJsonHelper;
		     this.billMasterWritePlatformService = billMasterWritePlatformService;
		     this.billWritePlatformService = billWritePlatformService;
		     this.ApiJsonSerializer = ApiJsonSerializer;
		     this.commandSourceWritePlatformService = commandsSourceWritePlatformService;
		     
		    }		
		

	@POST
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String generateBillStatement(@PathParam("clientId") final Long clientId, final String apiRequestBodyAsJson) {
		
		final JsonElement parsedCommand = this.fromApiJsonHelper.parse(apiRequestBodyAsJson);
        final JsonCommand command = JsonCommand.from(apiRequestBodyAsJson, parsedCommand,this.fromApiJsonHelper,
        		 						"BILLMASTER", clientId, null, null, clientId, null, null, null, null, null, null, null);
       	final CommandProcessingResult result = this.billMasterWritePlatformService.createBillMaster(command, command.entityId());
	    return this.toApiJsonSerializer.serialize(result);
	}
	
	@GET
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveBillStatements(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		final List<FinancialTransactionsData> data = this.billMasterReadPlatformService.retrieveStatments(clientId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, data, RESPONSE_DATA_PARAMETERS);
	}
	
	@GET
	@Path("{billId}/billdetails")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String getBillDetails(@PathParam("billId") final Long billId, @Context final UriInfo uriInfo) {
		
		context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		final List<BillDetailsData> data = this.billMasterReadPlatformService.retrievegetStatementDetails(billId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.ApiJsonSerializer.serialize(settings, data, RESPONSE_DATA_PARAMETERS);
	}

	@DELETE
	@Path("{billId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String cancelBillStatement(@PathParam("billId") final Long billId) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().cancelBillStatement(billId).build();
        final CommandProcessingResult result = this.commandSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
	}

	@GET
	@Path("{billId}/print")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response printStatement(@PathParam("billId") final Long billId) {
		
		final BillMaster billMaster = this.billMasterRepository.findOne(billId);
		final String fileName = billMaster.getFileName();
		    
		if("invoice".equalsIgnoreCase(fileName)){
			try {
			      this.billWritePlatformService.generateStatementPdf(billId);
			} catch (SQLException e) {  e.printStackTrace();}
		 }
		 final BillMaster updatedBillMaster = this.billMasterRepository.findOne(billId);
		 final String printFileName = updatedBillMaster.getFileName();
		 final File file = new File(printFileName);
		 final ResponseBuilder response = Response.ok(file);
		 response.header("Content-Disposition", "attachment; filename=\"" + printFileName + "\"");
		 response.header("Content-Type", "application/pdf");
		 return response.build();
	}
	
	@PUT
	@Path("/email/{billId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String sendBillPathToMsg(@PathParam("billId") final Long billId) {
		
		final BillMaster billMaster = this.billMasterRepository.findOne(billId);
		final String fileName = billMaster.getFileName();	
		if("invoice".equalsIgnoreCase(fileName)){
			final String msg = "No Generated Pdf file For This Statement";
			throw new BillingOrderNoRecordsFoundException(msg, billId);
		}
	     this.billWritePlatformService.sendPdfToEmail(fileName,billMaster.getClientId(),BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATEMENT);
	    return this.toApiJsonSerializer.serialize(CommandProcessingResult.resourceResult(billId, null));
	}
	
	@GET
	@Path("/print/{clientId}/{invoiceId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response printInvoice(@PathParam("invoiceId") final Long invoiceId, @PathParam("clientId") final Long clientId) {
		
		 String printFileName=this.billWritePlatformService.generateInovicePdf(invoiceId);
		 final File file = new File(printFileName);
		 this.billWritePlatformService.sendPdfToEmail(printFileName,clientId,BillingMessageTemplateConstants.MESSAGE_TEMPLATE_INVOICE);
		 final ResponseBuilder response = Response.ok(file);
		 response.header("Content-Disposition", "attachment; filename=\"" + printFileName + "\"");
		 response.header("Content-Type", "application/pdf");
		 return response.build();
	}
	
	@GET
	@Path("/payment/{clientId}/{paymentId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response printPayment(@PathParam("paymentId") final Long paymentId, @PathParam("clientId") final Long clientId) {
		
		 String printFileName=this.billWritePlatformService.generatePaymentPdf(paymentId);
		 final File file = new File(printFileName);
		 this.billWritePlatformService.sendPdfToEmail(printFileName,clientId,BillingMessageTemplateConstants.MESSAGE_TEMPLATE_PAYMENT);
		 final ResponseBuilder response = Response.ok(file);
		 response.header("Content-Disposition", "attachment; filename=\"" + printFileName + "\"");
		 response.header("Content-Type", "application/pdf");
		 return response.build();
	}
	
}