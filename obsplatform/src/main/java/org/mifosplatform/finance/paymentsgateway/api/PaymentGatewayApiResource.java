
package org.mifosplatform.finance.paymentsgateway.api;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.crm.clientprospect.service.SearchSqlQuery;
import org.mifosplatform.finance.payments.exception.ReceiptNoDuplicateException;
import org.mifosplatform.finance.paymentsgateway.data.PaymentGatewayData;
import org.mifosplatform.finance.paymentsgateway.data.PaymentGatewayDownloadData;
import org.mifosplatform.finance.paymentsgateway.data.RecurringPaymentTransactionTypeConstants;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGateway;
import org.mifosplatform.finance.paymentsgateway.domain.PaymentGatewayRepository;
import org.mifosplatform.finance.paymentsgateway.domain.PaypalRecurringBilling;
import org.mifosplatform.finance.paymentsgateway.domain.RecurringBillingHistory;
import org.mifosplatform.finance.paymentsgateway.domain.RecurringBillingHistoryRepository;
import org.mifosplatform.finance.paymentsgateway.service.PaymentGatewayReadPlatformService;
import org.mifosplatform.finance.paymentsgateway.service.PaymentGatewayRecurringWritePlatformService;
import org.mifosplatform.finance.paymentsgateway.service.PaymentGatewayWritePlatformService;
import org.mifosplatform.infrastructure.codes.data.CodeData;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.MediaEnumoptionData;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.itemdetails.exception.ActivePlansFoundException;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.order.domain.StatusTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

@Path("/paymentgateways")
@Component
@Scope("singleton")

/**
 * The class <code>PaymentGatewayApiResource</code> is developed for
 * Third party PaymentGateway systems.
 * Using the below API to Communicate OBS with Adapters/Third-Party servers. 
 * 
 * @author ashokreddy
 *
 */
public class PaymentGatewayApiResource {
	
	/**
	 * The set of parameters that are supported in response for {@link CodeData}
	 */
	private static final Set<String> RESPONSEPARAMETERS = new HashSet<String>(
			Arrays.asList("id","paymentId", "serialNo", "paymentDate", "receiptNo","status","phoneNo","clientName","amountPaid","remarks"));
	
	private final String resourceNameForPermissions = "PAYMENTGATEWAY";

	private final PlatformSecurityContext context;
	private final PaymentGatewayReadPlatformService readPlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final DefaultToApiJsonSerializer<PaymentGatewayData> toApiJsonSerializer;
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private String returnMessage;
	private String success;
	private String errorDesc;
	private String contentData;
	private CommandProcessingResult result;
	private JSONObject jsonData;
	private Long errorCode;
	private final PaymentGatewayWritePlatformService paymentGatewayWritePlatformService;
	private final PaymentGatewayRepository paymentGatewayRepository;
	private final PaymentGatewayRecurringWritePlatformService paymentGatewayRecurringWritePlatformService;
	private final OrderRepository orderRepository;
	private final RecurringBillingHistoryRepository recurringBillingHistoryRepository;

	@Autowired
	public PaymentGatewayApiResource(final PlatformSecurityContext context,final PaymentGatewayReadPlatformService readPlatformService,
			final DefaultToApiJsonSerializer<PaymentGatewayData> toApiJsonSerializer,final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
    		final PaymentGatewayWritePlatformService paymentGatewayWritePlatformService,
    		final PaymentGatewayRepository paymentGatewayRepository, final OrderRepository orderRepository,
    		final PaymentGatewayRecurringWritePlatformService paymentGatewayRecurringWritePlatformService,
    		final RecurringBillingHistoryRepository recurringBillingHistoryRepository) {

		this.toApiJsonSerializer = toApiJsonSerializer;
		this.context=context;
		this.readPlatformService=readPlatformService;
		this.apiRequestParameterHelper=apiRequestParameterHelper;
		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    	this.paymentGatewayWritePlatformService = paymentGatewayWritePlatformService;
    	this.paymentGatewayRepository = paymentGatewayRepository;
    	this.paymentGatewayRecurringWritePlatformService = paymentGatewayRecurringWritePlatformService;
    	this.orderRepository = orderRepository;
    	this.recurringBillingHistoryRepository = recurringBillingHistoryRepository;
    	
	}

	/**
	 * This method <code>onlinePayment</code> is 
	 * used for the Both M-pesa & Tigo-pesa PaymentGateways to Pay the Money.
	 * 
	 * @param requestData
	 * 			Containg input data in the Form of Xml/Soap . 
	 * @return
	 */
	@POST
	@Consumes({ MediaType.WILDCARD })
	@Produces({ MediaType.APPLICATION_XML })
	public String onlinePayment(final String requestData)  {
		
	     try{
			final JSONObject xmlJSONObj = XML.toJSONObject(requestData);
			jsonData=this.returnJsonFromXml(xmlJSONObj);
			final CommandWrapper commandRequest = new CommandWrapperBuilder().createPaymentGateway().withJson(jsonData.toString()).build();
			result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
			success = "SUCCESS";
			errorDesc = "";
			errorCode = Long.valueOf(0);	
			contentData = "OBSTRANSACTIONID=" + result.resourceId();	
			return this.returnToServer();	
		}catch(ReceiptNoDuplicateException e){
				success="DUPLICATE_TXN";
				errorDesc="DUPLICATE";
				errorCode=Long.valueOf(1);
				contentData="TXNID ALREADY EXIST";
				return this.returnToServer();
		} catch (JSONException e) {
			    return e.getCause().toString();	 
		} catch (PlatformDataIntegrityException e) {
		        return null;
	    }   
	}

	private String returnToServer() {
		
		try {
			final String obsPaymentType = jsonData.getString("OBSPAYMENTTYPE");
		
			
			if(obsPaymentType.equalsIgnoreCase("MPesa")){
				
					     String receipt=jsonData.getString("receipt");
						 StringBuilder builder = new StringBuilder();
				            builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")
				                .append("<response>")
				                .append("<receipt>"+receipt)
				                .append("</receipt>")
				                .append("<result>"+success)
				                .append("</result>")
				                .append("</response>");
				            returnMessage= builder.toString();
					
			}else if (obsPaymentType.equalsIgnoreCase("TigoPesa")) {
				
					//String TYPE = jsonData.getString("TYPE");
					String txnId = jsonData.getString("TXNID");
					String customerReferenceNumber = jsonData.getString("CUSTOMERREFERENCEID");	
					String msisdn = jsonData.getString("MSISDN");
					
						 StringBuilder builder = new StringBuilder();
				            builder.append("<?xml version=\"1.0\"?>")
				                .append("<!DOCTYPE COMMAND PUBLIC \"-//Ocam//DTD XML Command 1.0//EN\" \"xml/command.dtd\">")
				                .append("<COMMAND>")
				                .append("<TYPE>"+"SYNC_BILLPAY_RESPONSE")
				                .append("</TYPE>")
				                .append("<TXNID>"+txnId)
				                .append("</TXNID>")
				                .append("<REFID>"+customerReferenceNumber)
				                .append("</REFID>")
				                .append("<RESULT>"+success)
				                .append("</RESULT>")
				                .append("<ERRORCODE>"+errorCode)
				                .append("</ERRORCODE>")
				                .append("<ERRORDESC>"+errorDesc)
				                .append("</ERRORDESC>")
				                .append("<MSISDN>"+msisdn)
				                .append("</MSISDN>")
				                .append("<FLAG>"+"Y")
				                .append("</FLAG>")
				                .append("<CONTENT>"+contentData)
				                .append("</CONTENT>")
				                .append("</COMMAND>");
				            
				            returnMessage= builder.toString();			 
					
		}
		return returnMessage;
		} catch (JSONException e) {
			return e.getCause().toString();	 
		}
		
	}

	public JSONObject returnJsonFromXml(JSONObject xmlJSONObj){		
		try {
			JSONObject element=null;
			boolean b=xmlJSONObj.has("COMMAND");
			
			if(b==true){
			    element = xmlJSONObj.getJSONObject("COMMAND");
			    element.put("OBSPAYMENTTYPE", "TigoPesa");
			    element.put("locale", "en");
			}else{
				element = xmlJSONObj.getJSONObject("transaction");
				element.put("OBSPAYMENTTYPE", "MPesa");
				element.put("locale", "en");
			}
			return element;
		} catch (JSONException e) { 
			return null;
		}
		
	}
	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllDetailsForPayments(@Context final UriInfo uriInfo,@QueryParam("sqlSearch") final String sqlSearch,@QueryParam("source") final String source,
			@QueryParam("limit") final Integer limit, @QueryParam("offset") final Integer offset,@QueryParam("tabType") final String type) {
		
		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		final SearchSqlQuery searchItemDetails =SearchSqlQuery.forSearch(sqlSearch, offset,limit );
		Page<PaymentGatewayData> paymentData = readPlatformService.retrievePaymentGatewayData(searchItemDetails,type,source);
		return this.toApiJsonSerializer.serialize(paymentData);

	}
	
	@Path("download")
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response retriveDataForDownload(@Context final UriInfo uriInfo, @QueryParam("source") final String source, @QueryParam("status") final String status,
			@QueryParam("fromDate") final Long start, @QueryParam("toDate") final Long end) throws IOException {
		
		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		/**
		 * have to convert from and to date to format like 2014-06-15
		 * 
		 */
		
		Date fDate = new Date(start);
		Date tDate = new Date(end);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		String fromDate = df.format(fDate);
		String toDate = df.format(tDate);
		
		List<PaymentGatewayDownloadData> paymentData = readPlatformService.retriveDataForDownload(source,fromDate,toDate,status);
		
		/**
		 * 
		 * receiptNo serialNumber paymentDate amountPaid PhoneMSISDN Remarks  status 
		 */
		
		boolean statusSuccess = false;
		if(status.equalsIgnoreCase("Success"))
			statusSuccess = true;
		
		StringBuilder builder = new StringBuilder();
		if(statusSuccess){
			builder.append("Receipt No, Serial No, Payment Date, Amount Paid, Payment Id, Phone MSISDN, Remarks, Status \n");
		}else{
			builder.append("Receipt No, Serial No, Payment Date, Amount Paid, Phone MSISDN, Remarks, Status \n");
		}
		
		
		for(PaymentGatewayDownloadData data: paymentData){
			builder.append(data.getReceiptNo()+",");
			builder.append(data.getSerialNo()+",");
			builder.append(data.getPaymendDate()+",");
			builder.append(data.getAmountPaid()+",");
			if(statusSuccess){
				builder.append(data.getPaymentId()+",");
			}
			builder.append(data.getPhoneMSISDN()+",");
			builder.append(data.getRemarks()+",");
			builder.append(data.getStatus());
			builder.append("\n");
		}
		statusSuccess = false;
		String fileLocation = System.getProperty("java.io.tmpdir")+File.separator + "billing"+File.separator+""+source+""+System.currentTimeMillis()+status+".csv";
		
		String dirLocation = System.getProperty("java.io.tmpdir")+File.separator + "billing";
		File dir = new File(dirLocation);
		if(!dir.exists()){
			dir.mkdir();
		}
		
		File file = new File(fileLocation);
		if(!file.exists()){
			file.createNewFile();
		}
		FileUtils.writeStringToFile(file, builder.toString());
		
        final ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
        response.header("Content-Type", "application/csv");
        
        return response.build();
		
		/*String toJson = gson.toJson(paymentData);
		JSONArray arry  = null;
		try {
			arry = new JSONArray(toJson);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		System.out.println(arry);
		String json = this.toApiJsonSerializer.serialize(paymentData);
		
		File file=new File("/home/rakesh/Desktop/demo.csv");
	    String csv = null;
		try {
			csv = CDL.toString(arry);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    FileUtils.writeStringToFile(file, csv);*/
	     
		//return this.toApiJsonSerializer.serialize(paymentData);

	}	
	
	@GET
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllDetailsForPayments(@PathParam("id") final Long id,@Context final UriInfo uriInfo) {
		this.context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
		PaymentGatewayData paymentData = readPlatformService.retrievePaymentGatewayIdData(id);
		List<MediaEnumoptionData> data=readPlatformService.retrieveTemplateData();
		paymentData.setStatusData(data);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, paymentData,RESPONSEPARAMETERS);

	}
	
	@PUT
	@Path("{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateData(@PathParam("id") final Long id,final String apiRequestBodyAsJson) {
		 final CommandWrapper commandRequest = new CommandWrapperBuilder().updatePaymentGateway(id).withJson(apiRequestBodyAsJson).build();
		 final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		 return this.toApiJsonSerializer.serialize(result);

	}

	/**
	 * This method is used for Online Payment 
	 * Systems like Paypal,Dalpay,Korta etc...
	 * 
	 * Storing these payment details in 2 tables.
	 * 1) b_paymentgateway and 
	 * 2) b_payment.
	 */

	@PUT
	@Path("onlinepayment")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_HTML})
	public String OnlinePaymentMethod(final String apiRequestBodyAsJson){
		
		try{
			
			final CommandWrapper commandRequest = new CommandWrapperBuilder().OnlinePaymentGateway().withJson(apiRequestBodyAsJson).build();
			final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
			
			Map<String, Object> output = result.getChanges();
			
			String status = String.valueOf(output.get("status"));
			String client = String.valueOf(output.get("clientId"));
			String txnId = String.valueOf(output.get("txnId"));
			String amount = String.valueOf(output.get("amount"));
			String currency = String.valueOf(output.get("currency"));

			String error = String.valueOf(output.get("error"));
			String cardType = null;
			String cardNumber = null;
			
			JSONObject apiObject = new JSONObject(apiRequestBodyAsJson);
			
			if(apiObject.has("cardType")){
				cardType = apiObject.getString("cardType");
			}
			
			if(apiObject.has("cardNumber")){
				cardNumber = apiObject.getString("cardNumber");
			}
			
			String totalAmount =  amount + " " + currency;
			
			Long clientId = Long.valueOf(client);
			
			if(status.equalsIgnoreCase("Success") || status.equalsIgnoreCase("Pending")){
				
				Long pgId = Long.valueOf(String.valueOf(output.get("pgId")));
				String OutputData = this.paymentGatewayWritePlatformService.payment(clientId, pgId, txnId, amount, error);
				
				JSONObject object = new JSONObject(OutputData);
				
				this.paymentGatewayWritePlatformService.emailSending(clientId, object.getString("Result"), 
						object.getString("Description"), txnId, totalAmount, cardType, cardNumber);
				
				return object.toString();
				
			} else{
				
				JSONObject object = new JSONObject();
				object.put("Result", status.toUpperCase());
				object.put("Description", error);
				object.put("Amount", totalAmount);
				object.put("ObsPaymentId", "");
				object.put("TransactionId", txnId);
				
				this.paymentGatewayWritePlatformService.emailSending(clientId, status, error, txnId, totalAmount, cardType, cardNumber);
				
				return object.toString();
			}
		
				
		} catch (JSONException e) {
			String output = "{\"Result\":\"FAILURE\", \"Description\":\"JSONException = \""    + e.getMessage() + "}";
			return output;
		}	
	}
	
	/**
	 * This method is using for posting data to create payment using paypal
	 */
	 @POST
	 @Path("paypal")
	 @Consumes("application/x-www-form-urlencoded")
	 @Produces("text/html")
	 public String paypalOnlinePayment(@FormParam("txn_id") final String txnId,@FormParam("payment_date") final Date paymentDate,
			 @FormParam("mc_gross") final BigDecimal amount,
			 @FormParam("address_name") final String name,@FormParam("payer_email") final String payerEmail,
			 @FormParam("custom") final String customData, @FormParam("mc_currency") final String currency,
			 @FormParam("receiver_email") final String receiverEmail, @FormParam("payer_status") final String payerStatus,
			 @FormParam("payment_status") final String paymentStatus, @FormParam("pending_reason") final String pendingReason){
		 
		String returnUrl = null;
		
		try {
			 //in customData you should get the Parameters are clientId,locale,plancode,paytermcode,contractPeriod,returnUrl.
			
			final JSONObject jsonCustomData = new JSONObject(customData);
			
			final String dateFormat = "dd MMMM yyyy";

			final SimpleDateFormat daformat = new SimpleDateFormat(dateFormat);

			final String date = daformat.format(paymentDate);
			
			returnUrl = jsonCustomData.getString("returnUrl");
			
			final Long clientId = jsonCustomData.getLong("clientId");

			JsonObject jsonObj = new JsonObject();
			jsonObj.addProperty("paymentDate", date);
			jsonObj.addProperty("payerEmail", payerEmail);
			jsonObj.addProperty("address_name", name);
			jsonObj.addProperty("receiverEmail", receiverEmail);
			jsonObj.addProperty("payerStatus", payerStatus);
			jsonObj.addProperty("currency", currency);
			jsonObj.addProperty("paymentStatus", paymentStatus);
			jsonObj.addProperty("pendingReason", pendingReason);

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("source", RecurringPaymentTransactionTypeConstants.PAYPAL);
			jsonObject.put("transactionId", txnId);
			jsonObject.put("currency", currency);
			jsonObject.put("clientId", clientId);
			jsonObject.put("total_amount", amount);
			jsonObject.put("locale", "en");
			jsonObject.put("dateFormat", dateFormat);
			jsonObject.put("otherData", jsonObj.toString());
			jsonObject.put("status", paymentStatus);
			jsonObject.put("error", pendingReason);
			
			String data = OnlinePaymentMethod(jsonObject.toString());
			
			JSONObject resultJsonObject = new JSONObject(data);
			
			String Result = resultJsonObject.getString("Result");
			String Description = resultJsonObject.getString("Description");
			
			
			String paymentStatus1 = null;
			
			if(Result.equalsIgnoreCase("SUCCESS")){
				
				 jsonCustomData.remove("clientId");
				 jsonCustomData.remove("returnUrl");
				 String pgId = resultJsonObject.getString("pgId");
				 try{
					 paymentStatus1 = orderBooking(customData, date, clientId);
				 } catch (Exception e){
					 PaymentGateway  paymentGateway= this.paymentGatewayRepository.findOne(new Long(pgId));
					
					 if(e.getCause() !=null && e.getCause().getMessage() != null){
						 paymentGateway.setRemarks(e.getCause().getMessage());
					 } else if (e.getMessage() !=null) {
						 paymentGateway.setRemarks(e.getMessage());
					 } else{
						 StringWriter errors = new StringWriter();
						 e.printStackTrace(new PrintWriter(errors));
						 paymentGateway.setRemarks(errors.toString());	 	
					 }
					 this.paymentGatewayRepository.save(paymentGateway);
					 paymentStatus1 = "Payment Failed, Please Contact to Your Service Provider.  ";	
				 }
				 
				
			} else if (Result.equalsIgnoreCase("PENDING")) {
				paymentStatus1 = " Payment Pending, Please Contact to Your Service Provider, Reason="+Description;
			
			} else {
				paymentStatus1 = " Payment Failed, Please Contact to Your Service Provider, Reason="+Description;
			}
			
			String htmlData = "<a href=\""+returnUrl+"\"> Click On Me. </a>" + "<strong>"+ paymentStatus1 + "</Strong>";
			
			return htmlData;

		} 
	   catch(Exception e){
		   String paymentStatus1 = "Payment Failed, Please Contact to Your Service Provider.  ";
		   String htmlData = "<a href=\""+returnUrl+"\"> Click On Me </a>" + "<strong>"+ paymentStatus1 + "</Strong>";
		   return htmlData;   
	   }
	 }

	public String orderBooking(String jsonObject, String date, Long clientId) {
		
		try {
			
			JSONObject jsonCustomData = new JSONObject(jsonObject);

			final String dateFormat = "dd MMMM yyyy";
			String screenName = jsonCustomData.getString("screenName");
			Long orderId = null;
			String eventDataStr = null;

			if (jsonCustomData.has("clientId"))
				jsonCustomData.remove("clientId");
			if (jsonCustomData.has("returnUrl"))
				jsonCustomData.remove("returnUrl");

			if (jsonCustomData.has("screenName"))
				jsonCustomData.remove("screenName");
			
			if(jsonCustomData.has("orderId")){
				orderId = Long.valueOf(jsonCustomData.getString("orderId"));
				jsonCustomData.remove("orderId");
			}
			
			if (jsonCustomData.has("eventData")){
				eventDataStr = jsonCustomData.getString("eventData");
				jsonCustomData.remove("eventData");
			}
				
			if (screenName.equalsIgnoreCase("vod")) {
			
				CommandProcessingResult resultEvents = null;
				JSONArray eventDataArray = new JSONArray(eventDataStr);
				
				for(int i=0;i<eventDataArray.length();i++){
					JSONObject item = eventDataArray.getJSONObject(i);
					jsonCustomData.put("clientId", clientId);
					jsonCustomData.put("dateFormat", dateFormat);
					jsonCustomData.put("eventBookedDate", date);
					jsonCustomData.put("locale", "en");
					jsonCustomData.put("deviceId", jsonCustomData.getString("deviceId"));
					jsonCustomData.put("eventId", item.getLong("eventId"));
					jsonCustomData.put("formatType", item.getString("formatType"));
					jsonCustomData.put("optType", item.getString("optType"));
					
					CommandWrapper commandRequest = new CommandWrapperBuilder().createEventOrder(clientId).withJson(jsonCustomData.toString()).build();
					resultEvents = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
					if(resultEvents == null){
						break;
					}
				}
				
			    if (resultEvents == null) {
					return "failure : Payment Done and Event(s) Booking Failed";
				} else {
					return "Payment Done and Event(s) Booked Successfully. ";
				}
				
			} else if (screenName.equalsIgnoreCase("additionalOrders")) {

				jsonCustomData.put("billAlign", false);
				jsonCustomData.put("isNewplan", true);
				jsonCustomData.put("dateFormat", dateFormat);
				jsonCustomData.put("start_date", date);

				CommandWrapper commandRequest = new CommandWrapperBuilder().createOrder(clientId).withJson(jsonCustomData.toString()).build();
				CommandProcessingResult resultOrder = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

				if (resultOrder == null) {
					return "failure : Payment Done and Plan Booking Failed";
				} else {
					return "Payment Done and Plan Booked Successfully. ";
				}

			} else if (screenName.equalsIgnoreCase("renewalorder")) {
				
				Long renewalOrder = Long.valueOf(jsonCustomData.getString("renewalPeriod"));
				
				JSONObject object = new JSONObject();
				object.put("renewalPeriod", renewalOrder);
				object.put("description", jsonCustomData.getString("description"));

				final CommandWrapper commandRequest = new CommandWrapperBuilder().renewalOrder(orderId).withJson(object.toString()).build();
				final CommandProcessingResult resultOrder = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
				
				if (resultOrder == null) {
					return "failure : Payment Done and renewal Plan Booking Failed";
				} else {
					return "Payment Done and renewal Plan Booked Successfully. ";
				}
				
			} else if (screenName.equalsIgnoreCase("changeorder")) {
				
				jsonCustomData.put("billAlign", false);
				jsonCustomData.put("isNewplan", false);
				jsonCustomData.put("dateFormat", dateFormat);
				jsonCustomData.put("start_date", date);
				jsonCustomData.put("disconnectionDate", date);
				jsonCustomData.put("disconnectReason", "Not Interested");

				final CommandWrapper commandRequest = new CommandWrapperBuilder().changePlan(orderId).withJson(jsonCustomData.toString()).build();
				final CommandProcessingResult resultOrder = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
				
				if (resultOrder == null) {
					return "failure : Payment Done and Plan Changing Failed";
				} else {
					return "Payment Done and Plan Changing Successfully. ";
				}
				
			}else {
				return "Payment Done Successfully.";
			}
			
		} catch (JSONException e) {
			return "failure : Payment Done and Plan Booking Failed with throwing JSONException";
		} catch (ActivePlansFoundException e) {
			return "failure : Payment Done and Plan Booking Failed with throwing ActivePlansFoundException";
		}
		
	}
	 
	 
	/**
	 * This method is using for posting data to create payment using Neteller
	 */
	@POST
	@Path("neteller")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String netellerOnlinePayment(final String apiRequestBodyAsJson) {

		try {

			String data = OnlinePaymentMethod(apiRequestBodyAsJson.toString());

			JSONObject resultJsonObject = new JSONObject(data);
			
			JSONObject apiJson = new JSONObject(apiRequestBodyAsJson);

			String Result = resultJsonObject.getString("Result");

			String paymentStatus = null;

			if (Result.equalsIgnoreCase("SUCCESS")) {
               
				Long clientId = apiJson.getLong("clientId");
				SimpleDateFormat daformat = new SimpleDateFormat("dd MMMM yyyy");
				String date = daformat.format(DateUtils.getDateOfTenant());
				
				apiJson.remove("currency");
				apiJson.remove("total_amount");
				apiJson.remove("value");
				apiJson.remove("source");
				apiJson.remove("transactionId");
				apiJson.remove("verificationCode");
				
				paymentStatus = orderBooking(apiJson.toString(), date, clientId);
				
				if(paymentStatus.equalsIgnoreCase("failure : Payment Done and Plan Booking Failed") || paymentStatus.contains("failure :")){
					resultJsonObject.remove("Result");
					resultJsonObject.remove("Description");
					
					resultJsonObject.put("Result", "FAILURE");
					resultJsonObject.put("Description", paymentStatus);
				}
			}

			return resultJsonObject.toString();

		} catch (JSONException e) {
			String output = "{\"Result\":\"FAILURE\", \"Description\":\"JsonException\"" + e.getMessage() + "}";
			return output;
		}
	}	 
	
	/**
	 * This method is using for Handling Paypal IPN Requests. 
	 * 
	 * i) We have to Verify the Paypal IPN Request by Re-Sending the Received Parameters to Paypal IPN Server. 
	 * 
	 * ii) Paypal IPN Server Checks Whether IPN Server Sending Request Parameters and 
	 * Received Parameters(Which are Sending by OBS on (i)). 
	 * 
	 * iii) If Both Request Parameters are Match, Then Only Paypal Server Send "VERIFIED" as Response
	 * 
	 * iv) If Both are mis-match, Then Send "INVALID" as Response.
	 */
	
	@POST
	@Path("ipnhandler")
	@Consumes({ MediaType.APPLICATION_FORM_URLENCODED })
	@Produces({ MediaType.TEXT_HTML })
	public void paypalRecurringPayment(final @Context HttpServletRequest request) {

		RecurringBillingHistory recurringBillingHistory = new RecurringBillingHistory();
		
		try {
			String verifiyMessage = this.paymentGatewayRecurringWritePlatformService.paypalRecurringVerification(request);
			String txnType = request.getParameter(RecurringPaymentTransactionTypeConstants.RECURRING_TXNTYPE);
			
			System.out.println("Transaction Type :" +txnType+ "Result:" + verifiyMessage);
			
			String requestParameters = this.paymentGatewayRecurringWritePlatformService.getRequestParameters(request);
			
			recurringBillingHistory.setTransactionData(requestParameters);
			recurringBillingHistory.setTransactionDate(new Date());
			recurringBillingHistory.setSource(RecurringPaymentTransactionTypeConstants.PAYPAL);
			recurringBillingHistory.setTransactionStatus(verifiyMessage);
			recurringBillingHistory.setTransactionCategory(txnType);
			
			if(request.getParameterMap().containsKey("txn_id")){
				recurringBillingHistory.setTransactionId(request.getParameter("txn_id"));
			}
			
			if (RecurringPaymentTransactionTypeConstants.RECURRING_VERIFIED.equals(verifiyMessage)) {
				
				switch (txnType) {

				case RecurringPaymentTransactionTypeConstants.SUBSCR_SIGNUP:
				case RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_PROFILE_CREATED:
					
					this.paymentGatewayRecurringWritePlatformService.recurringSubscriberSignUp(request, recurringBillingHistory);
			
					break;
					
				case RecurringPaymentTransactionTypeConstants.SUBSCR_PAYMENT:
				case RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT:
					
					PaypalRecurringBilling paypalRecurringBilling = this.paymentGatewayRecurringWritePlatformService.recurringSubscriberSignUp(request, recurringBillingHistory);
					
					if(null != paypalRecurringBilling){
						
						String jsonObject = this.paymentGatewayRecurringWritePlatformService.createJsonForOnlineMethod(request);
						
						String data = OnlinePaymentMethod(jsonObject);
						
						JSONObject resultJsonObject = new JSONObject(data);
						
						String result = resultJsonObject.getString("Result");
						String description = resultJsonObject.getString("Description");
						
						if(result.equalsIgnoreCase(RecurringPaymentTransactionTypeConstants.SUCCESS)){
							
							this.paymentGatewayRecurringWritePlatformService.recurringEventUpdate(request, recurringBillingHistory);				
						
						} else{
							recurringBillingHistory.setClientId(paypalRecurringBilling.getClientId());
							recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
							recurringBillingHistory.setObsDescription("Payment Failed in OBS, Reason: "+ description);
							this.recurringBillingHistoryRepository.save(recurringBillingHistory);
						}
					}	
					
					break;
					
				case RecurringPaymentTransactionTypeConstants.SUBSCR_EOT:
				case RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_EXPIRED:
				case RecurringPaymentTransactionTypeConstants.SUBSCR_CANCELLED:
					
					String profileId = request.getParameter(RecurringPaymentTransactionTypeConstants.SUBSCRID);
					PaypalRecurringBilling billing = this.paymentGatewayRecurringWritePlatformService.getRecurringBillingObject(profileId);
					
					if(null == billing || null == billing.getOrderId()){
						
						recurringBillingHistory.setClientId(0L);
						recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
						recurringBillingHistory.setObsDescription("OrderId Not Found with this SubscriberId:" + profileId);
						
					} else{
						
						String status = this.paymentGatewayRecurringWritePlatformService.getOrderStatus(billing.getOrderId());
						
						this.paymentGatewayRecurringWritePlatformService.updateRecurringBillingTable(profileId);
						
						if (null != status && status.equalsIgnoreCase(StatusTypeEnum.ACTIVE.toString())) {		
							final CommandWrapper commandRequest = new CommandWrapperBuilder().terminateOrder(billing.getOrderId()).build();
							CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
							recurringBillingHistory.setClientId(billing.getClientId());
							
							if (null == result || result.resourceId() <= 0) {
								recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
								recurringBillingHistory.setObsDescription("order Terminate Action Failed...");
							}else{
								recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SUCCESS);
								recurringBillingHistory.setObsDescription("Order Termination Completed...");
							}
						}
					}
					
					this.recurringBillingHistoryRepository.save(recurringBillingHistory);
					
					break;
					
				case RecurringPaymentTransactionTypeConstants.SUBSCR_FAILED:
				case RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILED:
					
			
					break;
					
				case RecurringPaymentTransactionTypeConstants.SUBSCR_MODIFY:
					
					break;
				
				case RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SKIPPED:
					
					break;
				
				case RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SUSPENDED:
				case RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_SUSPENDED_DUE_TO_MAX_FAILED_PAYMENT:
					
					String profileId1 = request.getParameter(RecurringPaymentTransactionTypeConstants.SUBSCRID);
					this.paymentGatewayRecurringWritePlatformService.disConnectOrder(profileId1, recurringBillingHistory);
					
					break;
				default:
					
					break;
				}

			} else {
				recurringBillingHistory.setClientId(0L);
				recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
				recurringBillingHistory.setObsDescription("Paypal Verification Failed...");
				this.recurringBillingHistoryRepository.save(recurringBillingHistory);
			}

		} catch (JSONException e) {
			recurringBillingHistory.setClientId(0L);
			recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			recurringBillingHistory.setObsDescription("JSONException throwing.." + stackTraceToString(e));
			this.recurringBillingHistoryRepository.save(recurringBillingHistory);
		} catch (UnsupportedEncodingException e) {
			recurringBillingHistory.setClientId(0L);
			recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			recurringBillingHistory.setObsDescription("UnsupportedEncodingException throwing.." + stackTraceToString(e));
			this.recurringBillingHistoryRepository.save(recurringBillingHistory);
		} catch (IllegalStateException e) {
			recurringBillingHistory.setClientId(0L);
			recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			recurringBillingHistory.setObsDescription("IllegalStateException throwing.." + stackTraceToString(e));
			this.recurringBillingHistoryRepository.save(recurringBillingHistory);
		} catch (ClientProtocolException e) {
			recurringBillingHistory.setClientId(0L);
			recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			recurringBillingHistory.setObsDescription("ClientProtocolException throwing.." + stackTraceToString(e));
			this.recurringBillingHistoryRepository.save(recurringBillingHistory);
		} catch (IOException e) {
			recurringBillingHistory.setClientId(0L);
			recurringBillingHistory.setObsStatus(RecurringPaymentTransactionTypeConstants.RECURRING_PAYMENT_FAILURE);
			recurringBillingHistory.setObsDescription("IOException throwing.." + stackTraceToString(e));
			this.recurringBillingHistoryRepository.save(recurringBillingHistory);
		}

	}
	
	private String stackTraceToString(Throwable e) {
		
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement element : e.getStackTrace()) {
			sb.append(element.toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * This method is using for posting data to create payment using Neteller
	 */
	@POST
	@Path("cardPayment")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String cardPaymentProcessing(final String apiRequestBodyAsJson) {
		
		try {
			
			final CommandWrapper commandRequest = new CommandWrapperBuilder().cardPayment().withJson(apiRequestBodyAsJson).build();
			final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
			
			Map<String, Object> output = result.getChanges();
			
		} catch (Exception e) {
			
		}
		return "Success";
	}	 
	 
}

