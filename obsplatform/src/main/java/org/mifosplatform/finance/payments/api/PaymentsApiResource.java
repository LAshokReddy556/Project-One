package org.mifosplatform.finance.payments.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.json.JSONObject;
import org.mifosplatform.billing.selfcare.domain.SelfCareTemporary;
import org.mifosplatform.billing.selfcare.domain.SelfCareTemporaryRepository;
import org.mifosplatform.billing.selfcare.exception.SelfCareTemporaryAlreadyExistException;
import org.mifosplatform.billing.selfcare.exception.SelfCareTemporaryEmailIdNotFoundException;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.finance.payments.data.McodeData;
import org.mifosplatform.finance.payments.data.PaymentData;
import org.mifosplatform.finance.payments.exception.DalpayRequestFailureException;
import org.mifosplatform.finance.payments.service.PaymentReadPlatformService;
import org.mifosplatform.infrastructure.codes.data.CodeData;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

@Path("/payments")
@Component
@Scope("singleton")
public class PaymentsApiResource {

	/**
	 * The set of parameters that are supported in response for {@link CodeData}
	 */
	private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(
			Arrays.asList("id", "clientId", "paymentDate", "paymentCode",
					"amountPaid", "statmentId", "externalId", "Remarks"));
	private final static String RESOURCENAMEFORPERMISSIONS = "PAYMENT";

	private final PlatformSecurityContext context;
	private final PaymentReadPlatformService readPlatformService;
	private final DefaultToApiJsonSerializer<PaymentData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService writePlatformService;
	private final SelfCareTemporaryRepository selfCareTemporaryRepository;

	@Autowired
	public PaymentsApiResource(final PlatformSecurityContext context,final PaymentReadPlatformService readPlatformService,
			final DefaultToApiJsonSerializer<PaymentData> toApiJsonSerializer,final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService writePlatformService, final SelfCareTemporaryRepository selfCareTemporaryRepository) {
		
		this.context = context;
		this.readPlatformService = readPlatformService;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.writePlatformService = writePlatformService;
		this.selfCareTemporaryRepository = selfCareTemporaryRepository;
	}

	/**
	 * This method is using for posting data to create payment
	 */
	@POST
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createPayment(@PathParam("clientId") final Long clientId,	final String apiRequestBodyAsJson) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createPayment(clientId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
    
	/**
	 * This method is using for getting template data to create payment
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveDetailsForPayments(@QueryParam("clientId") final Long clientId,@Context final UriInfo uriInfo) {
		context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		final Collection<McodeData> data = this.readPlatformService.retrievemCodeDetails("Payment Mode");
		final List<PaymentData> depositDatas = this.readPlatformService.retrieveDepositDetails(clientId);
		final PaymentData paymentData=new PaymentData(data, depositDatas);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, paymentData,RESPONSE_DATA_PARAMETERS);

	}
	
	@GET
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllDetailsForPayments(@PathParam("clientId") final Long clientId,@Context final UriInfo uriInfo) {
		context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		final List<PaymentData> paymentData = readPlatformService.retrivePaymentsData(clientId);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, paymentData,RESPONSE_DATA_PARAMETERS);

	}


	 /**
	 * This method is using for cancelling payment with payment id
	 */
	 @PUT
	 @Path("cancelpayment/{paymentId}")
	 @Consumes({ MediaType.APPLICATION_JSON })
	 @Produces({ MediaType.APPLICATION_JSON })
	 public String cancelPayment(@PathParam("paymentId") final Long paymentId,final String apiRequestBodyAsJson) {
		 
			final CommandWrapper commandRequest = new CommandWrapperBuilder().cancelPayment(paymentId).withJson(apiRequestBodyAsJson).build();
			final CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);
			return this.toApiJsonSerializer.serialize(result);
		}	
	 
	 
	 /**
	 * This method is using for posting data to create payment using dalpay
	 */
	 @POST
	 @Path("dalpay")
	 @Consumes({ MediaType.APPLICATION_JSON })
	 @Produces({ MediaType.TEXT_HTML})
	 public String dalpayCheckout(final String apiRequestBodyAsJson){

		   try {
			   	  final JSONObject json= new JSONObject(apiRequestBodyAsJson);
			   	  final String orderNumber = json.getString("order_num");
			   	  final Long clientId = json.getLong("user1");
			   	  String returnUrl = json.getString("user2");
			   	  final String screenName = json.getString("user3");
			   	  final String EmailId = json.getString("cust_email");
			   	  final String amount = json.getString("total_amount");
			   	  final BigDecimal totalAmount = new BigDecimal(amount);
			   	returnUrl = returnUrl.replace("index.html", "index.html#/"+screenName);
			   	  
				if (clientId !=null && clientId > 0) {
					final String date = new SimpleDateFormat("dd MMMM yyyy").format(DateUtils.getDateOfTenant());
					final JsonObject object = new JsonObject();
					object.addProperty("txn_id", orderNumber);
					object.addProperty("dateFormat", "dd MMMM yyyy");
					object.addProperty("locale", "en");
					object.addProperty("paymentDate", date);
					object.addProperty("amountPaid", totalAmount);
					object.addProperty("isChequeSelected", "no");
					object.addProperty("receiptNo", orderNumber);
					object.addProperty("remarks", "Updated with Dalpay");
					object.addProperty("paymentCode", 27);

					final CommandWrapper commandRequest = new CommandWrapperBuilder().createPayment(clientId).withJson(object.toString()).build();
					this.writePlatformService.logCommandSource(commandRequest);
					
				}else if(clientId !=null && clientId == 0){
					
					final SelfCareTemporary selfCareTemporary = this.selfCareTemporaryRepository.findOneByEmailId(EmailId);
					if(selfCareTemporary != null && selfCareTemporary.getPaymentStatus().equalsIgnoreCase("INACTIVE")){
						
						selfCareTemporary.setPaymentData(json.toString());
						selfCareTemporary.setPaymentStatus("PENDING");
						this.selfCareTemporaryRepository.save(selfCareTemporary);											
						
					}else if(selfCareTemporary != null){				
						throw new SelfCareTemporaryAlreadyExistException(EmailId);					
					}else{					
						throw new SelfCareTemporaryEmailIdNotFoundException(EmailId);					
					}		
				}else{		
					throw new DalpayRequestFailureException(clientId);				
				}
				
				return "<!-- success--> <span>Order Accepted Successfully</span>"
				+ "<br>"
				+ "<a href='"+ returnUrl +"'>"
				+ "<strong>CLICK HERE</strong> to return to your account</a>";
			   	  
				  
		  } 
		   catch(Exception e){
		    return e.getMessage();
		   }
		 
	 }
	 
	@POST
	@Path("paypalEnquirey/{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String paypalEnquireyPayment(@PathParam("clientId") final Long clientId, final String apiRequestBodyAsJson) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().paypalEnquireyPayment(clientId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
}
