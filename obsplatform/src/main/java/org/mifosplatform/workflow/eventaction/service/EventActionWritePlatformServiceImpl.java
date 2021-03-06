package org.mifosplatform.workflow.eventaction.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import net.java.dev.obs.beesmart.AddExternalBeesmartMethod;

import org.codehaus.jettison.json.JSONObject;
import org.mifosplatform.cms.eventmaster.domain.EventMaster;
import org.mifosplatform.cms.eventmaster.domain.EventMasterRepository;
import org.mifosplatform.cms.eventorder.domain.EventOrder;
import org.mifosplatform.cms.eventorder.domain.EventOrderRepository;
import org.mifosplatform.cms.eventorder.domain.EventOrderdetials;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.crm.ticketmaster.data.TicketMasterData;
import org.mifosplatform.crm.ticketmaster.domain.TicketMaster;
import org.mifosplatform.crm.ticketmaster.domain.TicketMasterRepository;
import org.mifosplatform.crm.ticketmaster.service.TicketMasterReadPlatformService;
import org.mifosplatform.finance.billingorder.api.BillingOrderApiResourse;
import org.mifosplatform.finance.paymentsgateway.domain.PaypalRecurringBilling;
import org.mifosplatform.finance.paymentsgateway.domain.PaypalRecurringBillingRepository;
import org.mifosplatform.finance.paymentsgateway.service.PaymentGatewayRecurringWritePlatformService;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.organisation.message.domain.BillingMessage;
import org.mifosplatform.organisation.message.domain.BillingMessageRepository;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplate;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateConstants;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.mifosplatform.organisation.message.exception.BillingMessageTemplateNotFoundException;
import org.mifosplatform.organisation.message.exception.EmailNotFoundException;
import org.mifosplatform.portfolio.association.data.AssociationData;
import org.mifosplatform.portfolio.association.exception.HardwareDetailsNotFoundException;
import org.mifosplatform.portfolio.association.service.HardwareAssociationReadplatformService;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.contract.data.SubscriptionData;
import org.mifosplatform.portfolio.contract.service.ContractPeriodReadPlatformService;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.plan.domain.Plan;
import org.mifosplatform.portfolio.plan.domain.PlanRepository;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequest;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestDetails;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.mifosplatform.provisioning.provisioning.api.ProvisioningApiConstants;
import org.mifosplatform.useradministration.data.AppUserData;
import org.mifosplatform.useradministration.service.AppUserReadPlatformService;
import org.mifosplatform.workflow.eventaction.data.ActionDetaislData;
import org.mifosplatform.workflow.eventaction.data.EventActionProcedureData;
import org.mifosplatform.workflow.eventaction.data.OrderNotificationData;
import org.mifosplatform.workflow.eventaction.domain.EventAction;
import org.mifosplatform.workflow.eventaction.domain.EventActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import urn.ebay.apis.eBLBaseComponents.StatusChangeActionType;

import com.google.gson.JsonObject;

@Service
public class EventActionWritePlatformServiceImpl implements ActiondetailsWritePlatformService{
	
	
	
	private final OrderRepository orderRepository;
	private final TicketMasterRepository repository;
	private final ClientRepository clientRepository;
	private final EventOrderRepository eventOrderRepository;
	private final EventMasterRepository eventMasterRepository;
	private final EventActionRepository eventActionRepository;
	private final BillingMessageRepository messageDataRepository;
	private final AppUserReadPlatformService readPlatformService;
	private final BillingOrderApiResourse billingOrderApiResourse;
	private final ProcessRequestRepository processRequestRepository;
	private final BillingMessageTemplateRepository messageTemplateRepository;
	private final TicketMasterReadPlatformService ticketMasterReadPlatformService ;
    private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;	
    private final ContractPeriodReadPlatformService contractPeriodReadPlatformService;
    private final HardwareAssociationReadplatformService hardwareAssociationReadplatformService;
    private final PaymentGatewayRecurringWritePlatformService paymentGatewayRecurringWritePlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PaypalRecurringBillingRepository paypalRecurringBillingRepository;
    private final EventActionReadPlatformService eventActionReadPlatformService;
    
    private BillingMessageTemplate activationTemplate;
    private BillingMessageTemplate reConnectionTemplate;
    private BillingMessageTemplate disConnectionTemplate;
    private BillingMessageTemplate paymentTemplate;


	@Autowired
	public EventActionWritePlatformServiceImpl(final ActionDetailsReadPlatformService actionDetailsReadPlatformService,final EventActionRepository eventActionRepository,
			final HardwareAssociationReadplatformService hardwareAssociationReadplatformService,final ContractPeriodReadPlatformService contractPeriodReadPlatformService,
			final OrderRepository orderRepository,final TicketMasterRepository repository,final ProcessRequestRepository processRequestRepository,
			final BillingOrderApiResourse billingOrderApiResourse,final BillingMessageRepository messageDataRepository,final ClientRepository clientRepository,
			final BillingMessageTemplateRepository messageTemplateRepository,final EventMasterRepository eventMasterRepository,final EventOrderRepository eventOrderRepository,
			final TicketMasterReadPlatformService ticketMasterReadPlatformService,final AppUserReadPlatformService readPlatformService,
			final PaymentGatewayRecurringWritePlatformService paymentGatewayRecurringWritePlatformService, final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final PaypalRecurringBillingRepository paypalRecurringBillingRepository, final EventActionReadPlatformService eventActionReadPlatformService)
	{
		this.repository=repository;
		this.orderRepository=orderRepository;
		this.clientRepository=clientRepository;
		this.readPlatformService=readPlatformService;
		this.eventOrderRepository=eventOrderRepository;
		this.eventActionRepository=eventActionRepository;
		this.eventMasterRepository=eventMasterRepository;
		this.messageDataRepository=messageDataRepository;
		this.billingOrderApiResourse=billingOrderApiResourse;
		this.processRequestRepository=processRequestRepository;
		this.messageTemplateRepository=messageTemplateRepository;
		this.ticketMasterReadPlatformService=ticketMasterReadPlatformService;
        this.actionDetailsReadPlatformService=actionDetailsReadPlatformService;
        this.contractPeriodReadPlatformService=contractPeriodReadPlatformService;
        this.hardwareAssociationReadplatformService=hardwareAssociationReadplatformService;
        this.paymentGatewayRecurringWritePlatformService = paymentGatewayRecurringWritePlatformService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.paypalRecurringBillingRepository = paypalRecurringBillingRepository;
        this.eventActionReadPlatformService = eventActionReadPlatformService;
	}
	
	
	
	@Override
	public String AddNewActions(List<ActionDetaislData> actionDetaislDatas,final Long clientId,final String resourceId,String ticketURL) {
    
  try{
    	
	if(actionDetaislDatas!=null){
	   EventAction eventAction=null;
			
	   	for(ActionDetaislData detailsData:actionDetaislDatas){
	   		 
		      EventActionProcedureData actionProcedureData=this.actionDetailsReadPlatformService.checkCustomeValidationForEvents(clientId, detailsData.getEventName(),detailsData.getActionName(),resourceId);
			  JSONObject jsonObject=new JSONObject();
			  SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
			  	if(actionProcedureData.isCheck()){
				   
				    List<SubscriptionData> subscriptionDatas=this.contractPeriodReadPlatformService.retrieveSubscriptionDatabyContractType("Month(s)",1);
				   
				    switch(detailsData.getActionName()){
				  
				    case EventActionConstants.ACTION_SEND_EMAIL :
				    	

				    	   
				          TicketMasterData data = this.ticketMasterReadPlatformService.retrieveTicket(clientId,new Long(resourceId));
				          TicketMaster ticketMaster=this.repository.findOne(new Long(resourceId));
				          AppUserData user = this.readPlatformService.retrieveUser(new Long(data.getUserId()));
				          BillingMessageTemplate billingMessageTemplate = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_TICKET_TEMPLATE);
				          if(billingMessageTemplate !=null){
				          String value=ticketURL+""+resourceId;
				          String removeUrl="<br/><b>URL : </b>"+"<a href="+value+">View Ticket</a>";
				         // removeUrl.replaceAll("(PARAMURL)", ticketURL+""+resourceId); 	
				        	if(detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_CREATE_TICKET)){
				        	  	if(!user.getEmail().isEmpty()){
				        	  		BillingMessage billingMessage = new BillingMessage("CREATE TICKET", data.getProblemDescription()+"<br/>"
				        	  	    +ticketMaster.getDescription()+"\n"+removeUrl, "", user.getEmail(), user.getEmail(),
									 "Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
									 BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				        	  		this.messageDataRepository.save(billingMessage);
				        	  	}else{
				        	  	   if(actionProcedureData.getEmailId().isEmpty()){
				        	  		   
				        	  			throw new EmailNotFoundException(new Long(data.getUserId()));
				        	  		}else{
				        	  			
				        	  			BillingMessage billingMessage = new BillingMessage("CREATE TICKET", data.getProblemDescription()+"<br/>"
				        	  		    +ticketMaster.getDescription()+"\n"+removeUrl, "", actionProcedureData.getEmailId(), actionProcedureData.getEmailId(),
										"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
										BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				        	  			this.messageDataRepository.save(billingMessage);
				        	  		}
				        	  	}
				        	
				        	}else if(detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_EDIT_TICKET)){
				        	  		
				        	    if(!user.getEmail().isEmpty()){
				        	    	
				        	  		BillingMessage billingMessage = new BillingMessage("ADD COMMENT", data.getProblemDescription()+"<br/>"
				        	        +ticketMaster.getDescription()+"<br/>"+"COMMENT: "+data.getLastComment()+"<br/>"+removeUrl, "", user.getEmail(), user.getEmail(),
									"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				        	  		this.messageDataRepository.save(billingMessage);
				        	  	
				        	    }else{
				        	  		
				        	  		if(actionProcedureData.getEmailId().isEmpty()){
					        	  			throw new EmailNotFoundException(new Long(data.getUserId()));	
					        	  	}else{
					        	  		BillingMessage billingMessage = new BillingMessage("ADD COMMENT", data.getProblemDescription()+"<br/>"
					        	  	     +ticketMaster.getDescription()+"<br/>"+"COMMENT: \t"+data.getLastComment()+"<br/>"+removeUrl, "", actionProcedureData.getEmailId(),
					        	  	     actionProcedureData.getEmailId(),"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
					        	  	     BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
						        	  		this.messageDataRepository.save(billingMessage);
					        	  	}
				        	  	}
				        	
				        	}else if(detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_CLOSE_TICKET)){
				        		
				        	  	if(!user.getEmail().isEmpty()){
				        	  			BillingMessage billingMessage = new BillingMessage("CLOSED TICKET", data.getProblemDescription()+"<br/>"
				        	  			+ticketMaster.getDescription()+"<br/>"+"RESOLUTION: \t"+ticketMaster.getResolutionDescription()+"<br/>"+removeUrl, "", user.getEmail(), user.getEmail(),
										"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
										BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				        	  			this.messageDataRepository.save(billingMessage);
				        	  	}else{
				        	  		if(actionProcedureData.getEmailId().isEmpty()){
					        	  		throw new EmailNotFoundException(new Long(data.getUserId()));	
					        	  	}else{
					        	  		     BillingMessage billingMessage = new BillingMessage("CLOSED TICKET", data.getProblemDescription()+"<br/>"
					        	  		    +ticketMaster.getDescription()+"<br/>"+"RESOLUTION: \t"+ticketMaster.getResolutionDescription()+"<br/>"+removeUrl, "", actionProcedureData.getEmailId(),
					        	  	         actionProcedureData.getEmailId(),"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
					        	  	       BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
						        	        this.messageDataRepository.save(billingMessage);
					        	  }
				        	  	}
				        	  }
				        	}else{
				        		  throw new BillingMessageTemplateNotFoundException(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_TICKET_TEMPLATE);
				          }
				       break;
				       
				    case EventActionConstants.ACTION_ACTIVE : 
				    	
				          AssociationData associationData=this.hardwareAssociationReadplatformService.retrieveSingleDetails(actionProcedureData.getOrderId());
				          		if(associationData ==null){
				          			throw new HardwareDetailsNotFoundException(actionProcedureData.getOrderId().toString());
				          		}
				          		jsonObject.put("renewalPeriod",subscriptionDatas.get(0).getId());	
				          		jsonObject.put("description","Order Renewal By Scheduler");
				          		eventAction=new EventAction(DateUtils.getDateOfTenant(), "CREATE", "PAYMENT",EventActionConstants.ACTION_RENEWAL.toString(),"/orders/renewal", 
			        			 Long.parseLong(resourceId), jsonObject.toString(),actionProcedureData.getOrderId(),clientId);
				          		this.eventActionRepository.save(eventAction);
				         break; 		

				    case EventActionConstants.ACTION_NEW :

				    	jsonObject.put("billAlign","false");
				    	jsonObject.put("contractPeriod",subscriptionDatas.get(0).getId());
				    	jsonObject.put("dateFormat","dd MMMM yyyy");
                        jsonObject.put("locale","en");
                        jsonObject.put("paytermCode","Monthly");
                        jsonObject.put("planCode",actionProcedureData.getPlanId());
                        jsonObject.put("isNewplan","true");
                        jsonObject.put("start_date",dateFormat.format(DateUtils.getDateOfTenant()));
                        eventAction=new EventAction(DateUtils.getDateOfTenant(), "CREATE", "PAYMENT",actionProcedureData.getActionName(),"/orders/"+clientId, 
                        		Long.parseLong(resourceId), jsonObject.toString(),null,clientId);
			        	this.eventActionRepository.save(eventAction);
			        	   
				    	break;
				    	
				    case EventActionConstants.ACTION_DISCONNECT :

			        	   eventAction=new EventAction(DateUtils.getDateOfTenant(), "CREATE", "PAYMENT",EventActionConstants.ACTION_ACTIVE.toString(),"/orders/reconnect/"+clientId, 
			        	   Long.parseLong(resourceId), jsonObject.toString(),actionProcedureData.getOrderId(),clientId);
			        	   this.eventActionRepository.save(eventAction);

			        	   break; 
			        	   
			        	  
					default:
						break;
				    }
			  	}
				    
				    switch(detailsData.getActionName()){
				 
				    case EventActionConstants.ACTION_PROVISION_IT : 

				    	Client client=this.clientRepository.findOne(clientId);
			  	    	EventOrder eventOrder=this.eventOrderRepository.findOne(Long.valueOf(resourceId));
			  	    	EventMaster eventMaster=this.eventMasterRepository.findOne(eventOrder.getEventId());
			  	    	String response= AddExternalBeesmartMethod.addVodPackage(client.getOffice().getExternalId().toString(),client.getAccountNo(),
			  	    			eventMaster.getEventName());

			  	    	ProcessRequest processRequest=new ProcessRequest(Long.valueOf(0), eventOrder.getClientId(),eventOrder.getId(),ProvisioningApiConstants.PROV_BEENIUS,
								ProvisioningApiConstants.REQUEST_ACTIVATION_VOD,'Y','Y');
						List<EventOrderdetials> eventDetails=eventOrder.getEventOrderdetials();
						//EventMaster eventMaster=this.eventMasterRepository.findOne(eventOrder.getEventId());
						//JSONObject jsonObject=new JSONObject();
						jsonObject.put("officeUid",client.getOffice().getExternalId());
						jsonObject.put("subscriberUid",client.getAccountNo());
						jsonObject.put("vodUid",eventMaster.getEventName());
								
							for(EventOrderdetials details:eventDetails){
								ProcessRequestDetails processRequestDetails=new ProcessRequestDetails(details.getId(),details.getEventDetails().getId(),jsonObject.toString(),
										response,null,eventMaster.getEventStartDate(), eventMaster.getEventEndDate(),DateUtils.getDateOfTenant(),DateUtils.getDateOfTenant(),'N',
										ProvisioningApiConstants.REQUEST_ACTIVATION_VOD,null);
								processRequest.add(processRequestDetails);
							}
						this.processRequestRepository.save(processRequest);

						break;
						
				    case EventActionConstants.ACTION_SEND_PROVISION : 

				    	eventAction=new EventAction(DateUtils.getDateOfTenant(), "CLOSE", "Client",EventActionConstants.ACTION_SEND_PROVISION.toString(),"/processrequest/"+clientId, 
				    	Long.parseLong(resourceId),jsonObject.toString(),clientId,clientId);
				    	this.eventActionRepository.save(eventAction);
				  	
			        	break;
			        	
				    case EventActionConstants.ACTION_ACTIVE_LIVE_EVENT :
				    	 eventMaster=this.eventMasterRepository.findOne(Long.valueOf(resourceId));
				    	 
				    	 eventAction=new EventAction(eventMaster.getEventStartDate(),"Create","Live Event",EventActionConstants.ACTION_ACTIVE_LIVE_EVENT.toString(),
				    			 "/eventmaster",Long.parseLong(resourceId),jsonObject.toString(),Long.valueOf(0),Long.valueOf(0));
				    	 this.eventActionRepository.saveAndFlush(eventAction);
				    	 
				    	 eventAction=new EventAction(eventMaster.getEventEndDate(),"Disconnect","Live Event",EventActionConstants.ACTION_INACTIVE_LIVE_EVENT.toString(),
				    			 "/eventmaster",Long.parseLong(resourceId),jsonObject.toString(),Long.valueOf(0),Long.valueOf(0));
				    	 this.eventActionRepository.saveAndFlush(eventAction);
			      
				    	break; 	
				    	
				    case EventActionConstants.ACTION_INVOICE : 
				    	Order order=this.orderRepository.findOne(new Long(resourceId));
			        	  jsonObject.put("dateFormat","dd MMMM yyyy");
			        	  jsonObject.put("locale","en");
			        	  jsonObject.put("systemDate",dateFormat.format(order.getStartDate()));
			        	  	if(detailsData.IsSynchronous().equalsIgnoreCase("N")){
			        	  		eventAction=new EventAction(DateUtils.getDateOfTenant(), "CREATE",EventActionConstants.EVENT_ACTIVE_ORDER.toString(),
			        	  		EventActionConstants.ACTION_INVOICE.toString(),"/billingorder/"+clientId,Long.parseLong(resourceId),
			        	  		jsonObject.toString(),Long.parseLong(resourceId),clientId);
					        	this.eventActionRepository.save(eventAction);
			        	  	
			        	  	}else{
			            	 
			        	  		
			        	  		jsonObject.put("dateFormat","dd MMMM yyyy");
			        	  		jsonObject.put("locale","en");
			        	  		jsonObject.put("systemDate",dateFormat.format(order.getStartDate()));
			        	  		this.billingOrderApiResourse.retrieveBillingProducts(order.getClientId(),jsonObject.toString());
			        	  	}
			        	  break;
				    	
				    case EventActionConstants.ACTION_RECURRING_DISCONNECT : 
				    	
				    	JsonObject apiRequestBodyAsJson = new JsonObject();
				    	apiRequestBodyAsJson.addProperty("orderId", Long.parseLong(resourceId));
				    	apiRequestBodyAsJson.addProperty("recurringStatus", "SUSPEND");
				    	
				    	final CommandWrapper commandRequest = new CommandWrapperBuilder().updatePaypalProfileStatus().withJson(apiRequestBodyAsJson.toString()).build();
						final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
					
						Map<String,Object> resultMap = result.getChanges();
						
						JsonObject resultJson = new JsonObject();
						resultJson.addProperty("result", resultMap.get("result").toString());
						resultJson.addProperty("acknoledgement", resultMap.get("acknoledgement").toString());
						resultJson.addProperty("error", resultMap.get("error").toString());
						
						
						EventAction eventAction1=new EventAction(new Date(),"Recurring Disconnect","Recurring Disconnect",EventActionConstants.ACTION_RECURRING_DISCONNECT.toString(),
				    			 "/eventmaster",Long.parseLong(resourceId),resultJson.toString(),Long.valueOf(0),Long.valueOf(0));
						
						eventAction1.updateStatus('Y');
						this.eventActionRepository.saveAndFlush(eventAction1);
						
			        	break;
			        	
			        	
				    case EventActionConstants.ACTION_RECURRING_RECONNECTION : 
				    	
				    	JsonObject JsonString = new JsonObject();
				    	JsonString.addProperty("orderId", Long.parseLong(resourceId));
				    	JsonString.addProperty("recurringStatus", "REACTIVATE");
				    	
				    	final CommandWrapper commandRequestForReconn = new CommandWrapperBuilder().updatePaypalProfileStatus().withJson(JsonString.toString()).build();
						final CommandProcessingResult commandResult = this.commandsSourceWritePlatformService.logCommandSource(commandRequestForReconn);
					
						Map<String,Object> resultMapObj = commandResult.getChanges();
						
						JsonObject resultJsonObj = new JsonObject();
						resultJsonObj.addProperty("result", resultMapObj.get("result").toString());
						resultJsonObj.addProperty("acknoledgement", resultMapObj.get("acknoledgement").toString());
						resultJsonObj.addProperty("error", resultMapObj.get("error").toString());
						
						
						EventAction eventActionObj=new EventAction(new Date(),"Recurring Reconnection","Recurring Reconnection",EventActionConstants.ACTION_RECURRING_RECONNECTION.toString(),
				    			 "/eventmaster",Long.parseLong(resourceId),resultJsonObj.toString(),Long.valueOf(0),Long.valueOf(0));
						
						eventActionObj.updateStatus('Y');
						this.eventActionRepository.saveAndFlush(eventActionObj);
				  	
			        	break;
			        	
				    case EventActionConstants.ACTION_RECURRING_TERMINATION : 
				    	
				    	Long orderId = Long.parseLong(resourceId);
				    	
				    	PaypalRecurringBilling billing = this.paypalRecurringBillingRepository.findOneByOrderId(orderId);
				    	
				    	if(billing.getDeleted() == 'N'){
				    		JsonObject terminationObj = new JsonObject();
					    	terminationObj.addProperty("orderId", orderId);
					    	terminationObj.addProperty("recurringStatus", "CANCEL");
					    	
					    	final CommandWrapper terminateCommandRequest = new CommandWrapperBuilder().updatePaypalProfileStatus().withJson(terminationObj.toString()).build();
							final CommandProcessingResult terminateResult = this.commandsSourceWritePlatformService.logCommandSource(terminateCommandRequest);
						
							Map<String,Object> resultMapForTerminate = terminateResult.getChanges();
							
							JsonObject resultJsonObject = new JsonObject();
							resultJsonObject.addProperty("result", resultMapForTerminate.get("result").toString());
							resultJsonObject.addProperty("acknoledgement", resultMapForTerminate.get("acknoledgement").toString());
							resultJsonObject.addProperty("error", resultMapForTerminate.get("error").toString());
							
							
							EventAction eventActionTermination=new EventAction(new Date(),"Cancel Recurring","Cancel Recurring Profile",EventActionConstants.ACTION_RECURRING_TERMINATION.toString(),
					    			 "/eventmaster",Long.parseLong(resourceId),resultJsonObject.toString(),Long.valueOf(0),Long.valueOf(0));
							
							eventActionTermination.updateStatus('Y');
							this.eventActionRepository.saveAndFlush(eventActionTermination);
				    	}	
						
			        	break;
			        	
				    case EventActionConstants.ACTION_NOTIFY_ACTIVATION : 
				    	
				    	OrderNotificationData data = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, new Long(resourceId));
				    					 
				    	SimpleDateFormat dateFormatObj = new SimpleDateFormat("dd MMMM yyyy");			    	
				    	
				    	BillingMessageTemplate template = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_ACTIVATION);
				    	
				    	String headerMessage = template.getHeader().replaceAll("<CustomerName>", data.getFirstName() + " " + data.getLastName());
				    	String bodyMessage = template.getBody().replaceAll("<Service name>", data.getPlanName());
				    	bodyMessage = bodyMessage.replaceAll("<Activation Date>", dateFormatObj.format(data.getActivationDate().toDate()));
				    	
				    	String footerMessage = template.getFooter().replaceAll("<Reseller Name>", data.getOfficeName());
				    	footerMessage = footerMessage.replaceAll("<Contact Name>", data.getOfficeEmail());
				    	footerMessage = footerMessage.replaceAll("<Number>", data.getOfficePhoneNo());
				    	
				    	BillingMessage billingMessage = new BillingMessage(headerMessage, bodyMessage, footerMessage, 
				    			data.getOfficeEmail(), data.getEmailId(), template.getSubject(), BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, 
				    			template, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null );
				    		
				    	this.messageDataRepository.save(billingMessage);
			        	  		
				    	break;
				    
				    case EventActionConstants.ACTION_NOTIFY_DISCONNECTION : 
				    	
				    	OrderNotificationData disConnectData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, new Long(resourceId));
				    	
				    	SimpleDateFormat disDateFormatObj = new SimpleDateFormat("dd MMMM yyyy");
				    	
				    	BillingMessageTemplate disConnectionTemplate = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_DISCONNECTION);
				    	
				    	String disHeaderMessage = disConnectionTemplate.getHeader().replaceAll("<CustomerName>", disConnectData.getFirstName() + " " + disConnectData.getLastName());
				    	String disBodyMessage = disConnectionTemplate.getBody().replaceAll("<Service name>", disConnectData.getPlanName());
				    	disBodyMessage = disBodyMessage.replaceAll("<Disconnection Date>", disDateFormatObj.format(disConnectData.getEndDate().toDate()));
				    	
				    	String disFooterMessage = disConnectionTemplate.getFooter().replaceAll("<Reseller Name>", disConnectData.getOfficeName());
				    	disFooterMessage = disFooterMessage.replaceAll("<Contact Name>", disConnectData.getOfficeEmail());
				    	disFooterMessage = disFooterMessage.replaceAll("<Number>", disConnectData.getOfficePhoneNo());
				    	
				    	BillingMessage disBillingMessage = new BillingMessage(disHeaderMessage, disBodyMessage, disFooterMessage, 
				    			disConnectData.getOfficeEmail(), disConnectData.getEmailId(), disConnectionTemplate.getSubject(), BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, 
				    			disConnectionTemplate, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null );
				    		
				    	this.messageDataRepository.save(disBillingMessage);
				    	
				    	break;
				    	
				    case EventActionConstants.ACTION_NOTIFY_RECONNECTION : 
				    	
				    	OrderNotificationData reConnectData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, new Long(resourceId));
				    	
				    	SimpleDateFormat reDateFormatObj = new SimpleDateFormat("dd MMMM yyyy");
				    	
				    	BillingMessageTemplate reConnectionTemplate = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_RECONNECTION);
				    	
				    	String reHeaderMessage = reConnectionTemplate.getHeader().replaceAll("<CustomerName>", reConnectData.getFirstName() + " " + reConnectData.getLastName());
				    	String reBodyMessage = reConnectionTemplate.getBody().replaceAll("<Service name>", reConnectData.getPlanName());
				    	reBodyMessage = reBodyMessage.replaceAll("<Reconnection Date>", reDateFormatObj.format(reConnectData.getStartDate().toDate()));
				    	
				    	String reFooterMessage = reConnectionTemplate.getFooter().replaceAll("<Reseller Name>", reConnectData.getOfficeName());
				    	reFooterMessage = reFooterMessage.replaceAll("<Contact Name>", reConnectData.getOfficeEmail());
				    	reFooterMessage = reFooterMessage.replaceAll("<Number>", reConnectData.getOfficePhoneNo());
				    	
				    	BillingMessage reBillingMessage = new BillingMessage(reHeaderMessage, reBodyMessage, reFooterMessage, 
				    			reConnectData.getOfficeEmail(), reConnectData.getEmailId(), reConnectionTemplate.getSubject(), BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, 
				    			reConnectionTemplate, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null );
				    		
				    	this.messageDataRepository.save(reBillingMessage);
				    	
				    	break;
				    	
				    case EventActionConstants.ACTION_NOTIFY_PAYMENT : 
				    	
				    	OrderNotificationData paymentConnectData = this.eventActionReadPlatformService.retrieveNotifyDetails(clientId, null);
				    	
				    	SimpleDateFormat paymentDateFormatObj = new SimpleDateFormat("dd MMMM yyyy");
				    	
				    	BillingMessageTemplate paymentTemplate = getTemplate(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_PAYMENT);
				    	
				    	String paymentHeader = paymentTemplate.getHeader().replaceAll("<CustomerName>", paymentConnectData.getFirstName() + " " + paymentConnectData.getLastName());
				    	String paymentBodyMessage = paymentTemplate.getBody().replaceAll("<Amount>", resourceId);
				    	paymentBodyMessage = paymentBodyMessage.replaceAll("<Payment Date>", paymentDateFormatObj.format(new Date()));
				    	
				    	String paymentFooterMessage = paymentTemplate.getFooter().replaceAll("<Reseller Name>", paymentConnectData.getOfficeName());
				    	paymentFooterMessage = paymentFooterMessage.replaceAll("<Contact Name>", paymentConnectData.getOfficeEmail());
				    	paymentFooterMessage = paymentFooterMessage.replaceAll("<Number>", paymentConnectData.getOfficePhoneNo());
				    	
				    	BillingMessage paymentBillingMessage = new BillingMessage(paymentHeader, paymentBodyMessage, paymentFooterMessage, 
				    			paymentConnectData.getOfficeEmail(), paymentConnectData.getEmailId(), paymentTemplate.getSubject(), BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, 
				    			paymentTemplate, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null );
				    		
				    	this.messageDataRepository.save(paymentBillingMessage);
				    	
				    	break;
				    	
				    default:
				    	
						break;
				    }
				    
				    
				    	 	
				      /* if(detailsData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_SEND_EMAIL)){
				    	   
				          TicketMasterData data = this.ticketMasterReadPlatformService.retrieveTicket(clientId,new Long(resourceId));
				          TicketMaster ticketMaster=this.repository.findOne(new Long(resourceId));
				          AppUserData user = this.readPlatformService.retrieveUser(new Long(data.getUserId()));
				          BillingMessageTemplate billingMessageTemplate = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_TICKET_TEMPLATE);
				          String value=ticketURL+""+resourceId;
				          String removeUrl="<br/><b>URL : </b>"+"<a href="+value+">View Ticket</a>";
				         // removeUrl.replaceAll("(PARAMURL)", ticketURL+""+resourceId); 	
				        	if(detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_CREATE_TICKET)){
				        	  	if(!user.getEmail().isEmpty()){
				        	  		BillingMessage billingMessage = new BillingMessage("CREATE TICKET", data.getProblemDescription()+"<br/>"
				        	  	    +ticketMaster.getDescription()+"\n"+removeUrl, "", user.getEmail(), user.getEmail(),
									 "Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
									 BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				        	  		this.messageDataRepository.save(billingMessage);
				        	  	}else{
				        	  	   if(actionProcedureData.getEmailId().isEmpty()){
				        	  		   
				        	  			throw new EmailNotFoundException(new Long(data.getUserId()));
				        	  		}else{
				        	  			BillingMessage billingMessage = new BillingMessage("CREATE TICKET", data.getProblemDescription()+"<br/>"
				        	  		    +ticketMaster.getDescription()+"\n"+removeUrl, "", actionProcedureData.getEmailId(), actionProcedureData.getEmailId(),
										"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
										BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				        	  			this.messageDataRepository.save(billingMessage);
				        	  		}
				        	  	}
				        	
				        	}else if(detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_EDIT_TICKET)){
				        	  		
				        	    if(!user.getEmail().isEmpty()){
				        	  		BillingMessage billingMessage = new BillingMessage("ADD COMMENT", data.getProblemDescription()+"<br/>"
				        	        +ticketMaster.getDescription()+"<br/>"+"COMMENT: "+data.getLastComment()+"<br/>"+removeUrl, "", user.getEmail(), user.getEmail(),
									"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
									BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				        	  		this.messageDataRepository.save(billingMessage);
				        	  	}else{
				        	  		if(actionProcedureData.getEmailId().isEmpty()){
					        	  			throw new EmailNotFoundException(new Long(data.getUserId()));	
					        	  	}else{
					        	  		BillingMessage billingMessage = new BillingMessage("ADD COMMENT", data.getProblemDescription()+"<br/>"
					        	  	     +ticketMaster.getDescription()+"<br/>"+"COMMENT: \t"+data.getLastComment()+"<br/>"+removeUrl, "", actionProcedureData.getEmailId(),
					        	  	     actionProcedureData.getEmailId(),"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
					        	  	     BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
						        	  		this.messageDataRepository.save(billingMessage);
					        	  	}
				        	  	}
				        	
				        	}else if(detailsData.getEventName().equalsIgnoreCase(EventActionConstants.EVENT_CLOSE_TICKET)){
				        	  	if(!user.getEmail().isEmpty()){
				        	  			BillingMessage billingMessage = new BillingMessage("CLOSED TICKET", data.getProblemDescription()+"<br/>"
				        	  			+ticketMaster.getDescription()+"<br/>"+"RESOLUTION: \t"+ticketMaster.getResolutionDescription()+"<br/>"+removeUrl, "", user.getEmail(), user.getEmail(),
										"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
										BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				        	  			this.messageDataRepository.save(billingMessage);
				        	  	}else{
				        	  		if(actionProcedureData.getEmailId().isEmpty()){
					        	  		throw new EmailNotFoundException(new Long(data.getUserId()));	
					        	  	}else{
					        	  		     BillingMessage billingMessage = new BillingMessage("CLOSED TICKET", data.getProblemDescription()+"<br/>"
					        	  		    +ticketMaster.getDescription()+"<br/>"+"RESOLUTION: \t"+ticketMaster.getResolutionDescription()+"<br/>"+removeUrl, "", actionProcedureData.getEmailId(),
					        	  	         actionProcedureData.getEmailId(),"Ticket:"+resourceId, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, billingMessageTemplate,
					        	  	       BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
						        	        this.messageDataRepository.save(billingMessage);
					        	  }
				        	  	}
				        	  }
				      
				       }else if(actionProcedureData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_ACTIVE)){
				    	   
					          AssociationData associationData=this.hardwareAssociationReadplatformService.retrieveSingleDetails(actionProcedureData.getOrderId());
					          		if(associationData ==null){
					          			throw new HardwareDetailsNotFoundException(actionProcedureData.getOrderId().toString());
					          		}
					          		jsonObject.put("renewalPeriod",subscriptionDatas.get(0).getId());	
					          		jsonObject.put("description","Order Renewal By Scheduler");
					          		eventAction=new EventAction(new Date(), "CREATE", "PAYMENT",EventActionConstants.ACTION_RENEWAL.toString(),"/orders/renewal", 
				        			 Long.parseLong(resourceId), jsonObject.toString(),actionProcedureData.getOrderId(),clientId);
					          		this.eventActionRepository.save(eventAction);
				         
				       }else if(actionProcedureData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_NEW)){
				        	  
				        	   jsonObject.put("billAlign","false");
				        	   jsonObject.put("contractPeriod",subscriptionDatas.get(0).getId());
				        	   jsonObject.put("dateFormat","dd MMMM yyyy");
                               jsonObject.put("locale","en");
				        	   jsonObject.put("paytermCode","Monthly");
				        	   jsonObject.put("planCode",actionProcedureData.getPlanId());
				        	   jsonObject.put("isNewplan","true");
				        	   jsonObject.put("start_date",dateFormat.format(new Date()));
				        	   eventAction=new EventAction(new Date(), "CREATE", "PAYMENT",actionProcedureData.getActionName(),"/orders/"+clientId, 
					        			 Long.parseLong(resourceId), jsonObject.toString(),null,clientId);
				        	   this.eventActionRepository.save(eventAction);
				        	   
				      }else if(actionProcedureData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_DISCONNECT)){
				        	   
				        	   eventAction=new EventAction(new Date(), "CREATE", "PAYMENT",EventActionConstants.ACTION_ACTIVE.toString(),"/orders/reconnect/"+clientId, 
				        	   Long.parseLong(resourceId), jsonObject.toString(),actionProcedureData.getOrderId(),clientId);
				        	   this.eventActionRepository.save(eventAction);
				        	   	
				      }else if(detailsData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_INVOICE)){
				    	  
				        	  jsonObject.put("dateFormat","dd MMMM yyyy");
                              jsonObject.put("locale","en");
				        	  jsonObject.put("systemDate",dateFormat.format(new Date()));
				        	  	
				        	  if(detailsData.IsSynchronous().equalsIgnoreCase("N")){
				        		  
				        	  		eventAction=new EventAction(new Date(), "CREATE",EventActionConstants.EVENT_ACTIVE_ORDER.toString(),
				        	  		EventActionConstants.ACTION_INVOICE.toString(),"/billingorder/"+clientId,Long.parseLong(resourceId),
				        	  		jsonObject.toString(),Long.parseLong(resourceId),clientId);
						        	this.eventActionRepository.save(eventAction);
				        	  	
				        	  	}else{
				            	 
				        	  		Order order=this.orderRepository.findOne(new Long(resourceId));
				        	  		jsonObject.put("dateFormat","dd MMMM yyyy");
				        	  		jsonObject.put("locale","en");
				        	  		jsonObject.put("systemDate",dateFormat.format(order.getStartDate()));
				        	  		this.billingOrderApiResourse.retrieveBillingProducts(order.getClientId(),jsonObject.toString());
				        	  	}
				     
				      }else if(detailsData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_PROVISION_IT)){
			  	    	
			  	    	Client client=this.clientRepository.findOne(clientId);
			  	    	EventOrder eventOrder=this.eventOrderRepository.findOne(Long.valueOf(resourceId));
			  	    	EventMaster eventMaster=this.eventMasterRepository.findOne(eventOrder.getEventId());
			  	    	
			  	    	String response= AddExternalBeesmartMethod.addVodPackage(client.getOffice().getExternalId().toString(),client.getAccountNo(),
			  	    			eventMaster.getEventName());
			  	   
						ProcessRequest processRequest=new ProcessRequest(Long.valueOf(0), eventOrder.getClientId(),eventOrder.getId(),ProvisioningApiConstants.PROV_BEENIUS,
								ProvisioningApiConstants.REQUEST_ACTIVATION_VOD,'Y','Y');
						List<EventOrderdetials> eventDetails=eventOrder.getEventOrderdetials();
						//EventMaster eventMaster=this.eventMasterRepository.findOne(eventOrder.getEventId());
						//JSONObject jsonObject=new JSONObject();
						jsonObject.put("officeUid",client.getOffice().getExternalId());
						jsonObject.put("subscriberUid",client.getAccountNo());
						jsonObject.put("vodUid",eventMaster.getEventName());
								
							for(EventOrderdetials details:eventDetails){
								ProcessRequestDetails processRequestDetails=new ProcessRequestDetails(details.getId(),details.getEventDetails().getId(),jsonObject.toString(),
										response,null,eventMaster.getEventStartDate(), eventMaster.getEventEndDate(),new Date(),new Date(),'N',
										ProvisioningApiConstants.REQUEST_ACTIVATION_VOD,null);
								processRequest.add(processRequestDetails);
							}
						this.processRequestRepository.save(processRequest);
					
			  	    	
				      }
			  	    }if(detailsData.getActionName().equalsIgnoreCase(EventActionConstants.ACTION_SEND_PROVISION)){
		        	   
		        	   eventAction=new EventAction(new Date(), "CLOSE", "Client",EventActionConstants.ACTION_SEND_PROVISION.toString(),"/processrequest/"+clientId, 
		        	   Long.parseLong(resourceId),jsonObject.toString(),clientId,clientId);
		        	   this.eventActionRepository.save(eventAction);
			  	}*/
			
		}
	}
	     return null;
    }catch(Exception exception){
    	exception.printStackTrace();
    	return null;
    }

	}
	
	private BillingMessageTemplate getTemplate(String templateName){
		
		if(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_ACTIVATION.equalsIgnoreCase(templateName)){
			
			if(null == activationTemplate){
				activationTemplate = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_ACTIVATION);
			}
			return activationTemplate;
			
		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_DISCONNECTION.equalsIgnoreCase(templateName)) {
			
			if(null == disConnectionTemplate){
				disConnectionTemplate = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_DISCONNECTION);
			}
			return disConnectionTemplate;
			
		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_RECONNECTION.equalsIgnoreCase(templateName)) {
			
			if(null == reConnectionTemplate){
				reConnectionTemplate = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_RECONNECTION);
			}
			return reConnectionTemplate;
			
		} else if (BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_PAYMENT.equalsIgnoreCase(templateName)) {
			
			if(null == paymentTemplate){
				paymentTemplate = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NOTIFY_PAYMENT);
			}
			return paymentTemplate;
			
		} else {
			throw new BillingMessageTemplateNotFoundException(templateName);
		}
		
	}
}
