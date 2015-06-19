package org.mifosplatform.portfolio.order.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.LocalDate;
import org.mifosplatform.billing.chargecode.domain.ChargeCodeMaster;
import org.mifosplatform.billing.chargecode.domain.ChargeCodeRepository;
import org.mifosplatform.billing.planprice.domain.Price;
import org.mifosplatform.billing.planprice.domain.PriceRepository;
import org.mifosplatform.billing.planprice.exceptions.PriceNotFoundException;
import org.mifosplatform.billing.promotioncodes.domain.PromotionCodeMaster;
import org.mifosplatform.billing.promotioncodes.domain.PromotionCodeRepository;
import org.mifosplatform.billing.promotioncodes.exception.PromotionCodeNotFoundException;
import org.mifosplatform.cms.eventorder.service.PrepareRequestWriteplatformService;
import org.mifosplatform.finance.billingorder.domain.Invoice;
import org.mifosplatform.finance.billingorder.service.InvoiceClient;
import org.mifosplatform.finance.billingorder.service.ReverseInvoice;
import org.mifosplatform.finance.paymentsgateway.domain.PaypalRecurringBilling;
import org.mifosplatform.finance.paymentsgateway.domain.PaypalRecurringBillingRepository;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepository;
import org.mifosplatform.infrastructure.configuration.domain.Configuration;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.mifosplatform.infrastructure.configuration.domain.EnumDomainService;
import org.mifosplatform.infrastructure.configuration.domain.EnumDomainServiceRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.onetimesale.data.AllocationDetailsData;
import org.mifosplatform.portfolio.allocation.domain.HardwareAssociationRepository;
import org.mifosplatform.portfolio.allocation.service.AllocationReadPlatformService;
import org.mifosplatform.portfolio.association.data.AssociationData;
import org.mifosplatform.portfolio.association.domain.HardwareAssociation;
import org.mifosplatform.portfolio.association.exception.HardwareDetailsNotFoundException;
import org.mifosplatform.portfolio.association.service.HardwareAssociationReadplatformService;
import org.mifosplatform.portfolio.association.service.HardwareAssociationWriteplatformService;
import org.mifosplatform.portfolio.client.domain.AccountNumberGenerator;
import org.mifosplatform.portfolio.client.domain.AccountNumberGeneratorFactory;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.domain.ClientStatus;
import org.mifosplatform.portfolio.contract.data.SubscriptionData;
import org.mifosplatform.portfolio.contract.domain.Contract;
import org.mifosplatform.portfolio.contract.domain.ContractRepository;
import org.mifosplatform.portfolio.contract.service.ContractPeriodReadPlatformService;
import org.mifosplatform.portfolio.order.data.OrderStatusEnumaration;
import org.mifosplatform.portfolio.order.data.UserActionStatusEnumaration;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderDiscount;
import org.mifosplatform.portfolio.order.domain.OrderDiscountRepository;
import org.mifosplatform.portfolio.order.domain.OrderHistory;
import org.mifosplatform.portfolio.order.domain.OrderHistoryRepository;
import org.mifosplatform.portfolio.order.domain.OrderLine;
import org.mifosplatform.portfolio.order.domain.OrderPrice;
import org.mifosplatform.portfolio.order.domain.OrderPriceRepository;
import org.mifosplatform.portfolio.order.domain.OrderRepository;
import org.mifosplatform.portfolio.order.domain.PaymentFollowup;
import org.mifosplatform.portfolio.order.domain.PaymentFollowupRepository;
import org.mifosplatform.portfolio.order.domain.StatusTypeEnum;
import org.mifosplatform.portfolio.order.domain.UserActionStatusTypeEnum;
import org.mifosplatform.portfolio.order.exceptions.NoOrdersFoundException;
import org.mifosplatform.portfolio.order.exceptions.OrderNotFoundException;
import org.mifosplatform.portfolio.order.exceptions.SchedulerOrderFoundException;
import org.mifosplatform.portfolio.order.serialization.OrderCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.plan.domain.Plan;
import org.mifosplatform.portfolio.plan.domain.PlanDetails;
import org.mifosplatform.portfolio.plan.domain.PlanRepository;
import org.mifosplatform.portfolio.service.domain.ServiceMaster;
import org.mifosplatform.portfolio.service.domain.ServiceMasterRepository;
import org.mifosplatform.provisioning.preparerequest.domain.PrepareRequest;
import org.mifosplatform.provisioning.preparerequest.domain.PrepareRequsetRepository;
import org.mifosplatform.provisioning.preparerequest.exception.PrepareRequestActivationException;
import org.mifosplatform.provisioning.preparerequest.service.PrepareRequestReadplatformService;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.mifosplatform.provisioning.provisioning.service.ProvisioningWritePlatformService;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.workflow.eventaction.data.ActionDetaislData;
import org.mifosplatform.workflow.eventaction.domain.EventAction;
import org.mifosplatform.workflow.eventaction.domain.EventActionRepository;
import org.mifosplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.mifosplatform.workflow.eventaction.service.ActiondetailsWritePlatformService;
import org.mifosplatform.workflow.eventaction.service.EventActionConstants;
import org.mifosplatform.workflow.eventvalidation.service.EventValidationReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Service
public class OrderWritePlatformServiceImpl implements OrderWritePlatformService {

	private final ActionDetailsReadPlatformService actionDetailsReadPlatformService; 
	private final ProvisioningWritePlatformService provisioningWritePlatformService;
	private final PlanRepository planRepository;
	private final ReverseInvoice reverseInvoice;
	private final PlatformSecurityContext context;
	private final OrderRepository orderRepository;
	private final PriceRepository  priceRepository;
	private final OrderAssembler  orderAssembler;
	private final ClientRepository clientRepository;
	private final EventValidationReadPlatformService eventValidationReadPlatformService;
	private final PrepareRequestReadplatformService prepareRequestReadplatformService;
	private final ActiondetailsWritePlatformService actiondetailsWritePlatformService;
	private final ContractPeriodReadPlatformService contractPeriodReadPlatformService;
	private final PrepareRequestWriteplatformService prepareRequestWriteplatformService;
	private final HardwareAssociationWriteplatformService associationWriteplatformService;
	private final OrderReadPlatformService orderReadPlatformService;
	private final ServiceMasterRepository serviceMasterRepository;
	private final PrepareRequsetRepository prepareRequsetRepository;
	private final PaymentFollowupRepository paymentFollowupRepository;
	private final CodeValueRepository codeValueRepository;
	private final HardwareAssociationRepository associationRepository;
	private final EnumDomainServiceRepository enumDomainServiceRepository;
	private final AllocationReadPlatformService allocationReadPlatformService; 
	private final OrderCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final ContractRepository subscriptionRepository;
	private final ConfigurationRepository configurationRepository;
	private final OrderDiscountRepository orderDiscountRepository;
	private final PromotionCodeRepository promotionCodeRepository;
	private final HardwareAssociationReadplatformService hardwareAssociationReadplatformService;
	private final ChargeCodeRepository chargeCodeRepository;
	private final OrderPriceRepository OrderPriceRepository;
	private final EventActionRepository eventActionRepository;
	private final OrderHistoryRepository orderHistoryRepository;
	private final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory;
	private final PaypalRecurringBillingRepository paypalRecurringBillingRepository;
	private final InvoiceClient invoiceClient;
	
   
    

    @Autowired
	public OrderWritePlatformServiceImpl(final PlatformSecurityContext context,final OrderRepository orderRepository,
			final PlanRepository planRepository,final OrderPriceRepository OrderPriceRepository,final CodeValueRepository codeRepository,
            final ServiceMasterRepository serviceMasterRepository,final EnumDomainServiceRepository enumDomainServiceRepository,
			final ContractRepository subscriptionRepository,final OrderCommandFromApiJsonDeserializer fromApiJsonDeserializer,final ReverseInvoice reverseInvoice,
			final PrepareRequestWriteplatformService prepareRequestWriteplatformService,final OrderHistoryRepository orderHistoryRepository,
			final  ConfigurationRepository configurationRepository,final AllocationReadPlatformService allocationReadPlatformService,
			final HardwareAssociationWriteplatformService associationWriteplatformService,final PrepareRequestReadplatformService prepareRequestReadplatformService,
			final OrderReadPlatformService orderReadPlatformService,
		    final ProcessRequestRepository processRequestRepository,final HardwareAssociationReadplatformService hardwareAssociationReadplatformService,
		    final PrepareRequsetRepository prepareRequsetRepository,final PromotionCodeRepository promotionCodeRepository,
		    final OrderDiscountRepository orderDiscountRepository, final OrderAssembler orderAssembler,
		    final ClientRepository clientRepository,final ActionDetailsReadPlatformService actionDetailsReadPlatformService,
		    final ActiondetailsWritePlatformService actiondetailsWritePlatformService,final EventValidationReadPlatformService eventValidationReadPlatformService,
		    final EventActionRepository eventActionRepository,final ContractPeriodReadPlatformService contractPeriodReadPlatformService,
		    final HardwareAssociationRepository associationRepository,final ProvisioningWritePlatformService provisioningWritePlatformService,
		    final PaymentFollowupRepository paymentFollowupRepository,final PriceRepository priceRepository,final ChargeCodeRepository chargeCodeRepository,
		    final AccountNumberGeneratorFactory accountIdentifierGeneratorFactory,
		    final PaypalRecurringBillingRepository paypalRecurringBillingRepository,
		    final InvoiceClient invoiceClient) {

		this.context = context;
		this.reverseInvoice=reverseInvoice;
		this.subscriptionRepository = subscriptionRepository;
		this.serviceMasterRepository=serviceMasterRepository;
		this.fromApiJsonDeserializer=fromApiJsonDeserializer;
		this.configurationRepository=configurationRepository;
		this.orderDiscountRepository=orderDiscountRepository;
		this.prepareRequsetRepository=prepareRequsetRepository;
		this.orderReadPlatformService = orderReadPlatformService;
		this.paymentFollowupRepository=paymentFollowupRepository;
		this.enumDomainServiceRepository=enumDomainServiceRepository;
		this.allocationReadPlatformService=allocationReadPlatformService;
		this.orderAssembler=orderAssembler;
		this.eventValidationReadPlatformService=eventValidationReadPlatformService;
		this.priceRepository=priceRepository;
		this.planRepository = planRepository;
		this.orderRepository = orderRepository;
		this.clientRepository=clientRepository;
		this.codeValueRepository=codeRepository;
		this.promotionCodeRepository=promotionCodeRepository;
		this.provisioningWritePlatformService=provisioningWritePlatformService;
		this.prepareRequestReadplatformService=prepareRequestReadplatformService;
		this.actiondetailsWritePlatformService=actiondetailsWritePlatformService;
		this.contractPeriodReadPlatformService=contractPeriodReadPlatformService;
		this.prepareRequestWriteplatformService=prepareRequestWriteplatformService;
		this.hardwareAssociationReadplatformService=hardwareAssociationReadplatformService;
		this.chargeCodeRepository=chargeCodeRepository;
		this.OrderPriceRepository = OrderPriceRepository;
		this.eventActionRepository=eventActionRepository;
		this.associationRepository=associationRepository;
		this.orderHistoryRepository=orderHistoryRepository;
		this.associationWriteplatformService=associationWriteplatformService;
		this.actionDetailsReadPlatformService=actionDetailsReadPlatformService;
		this.accountIdentifierGeneratorFactory=accountIdentifierGeneratorFactory;
		this.paypalRecurringBillingRepository = paypalRecurringBillingRepository;
		this.invoiceClient = invoiceClient;
		

	}
	
@Override
public CommandProcessingResult createOrder(Long clientId,JsonCommand command) {
	
try{
	this.fromApiJsonDeserializer.validateForCreate(command.json());
	final Long userId=getUserId();
	
	//Check for Custome_Validation
	this.eventValidationReadPlatformService.checkForCustomValidations(clientId,EventActionConstants.EVENT_CREATE_ORDER,command.json(),userId);
	
	Plan plan = this.planRepository.findOne(command.longValueOfParameterNamed("planCode"));
	Order order=this.orderAssembler.assembleOrderDetails(command,clientId,plan);
	this.orderRepository.save(order);
	
	boolean isNewPlan=command.booleanPrimitiveValueOfParameterNamed("isNewplan");
	String requstStatus =UserActionStatusTypeEnum.ACTIVATION.toString();
	
	if(isNewPlan){
		
		final AccountNumberGenerator orderNoGenerator = this.accountIdentifierGeneratorFactory.determineClientAccountNoGenerator(order.getId());
		order.updateOrderNum(orderNoGenerator.generate());
		Set<PlanDetails> planDetails=plan.getDetails();
		ServiceMaster service=this.serviceMasterRepository.findOneByServiceCode(planDetails.iterator().next().getServiceCode());
		Long commandId=Long.valueOf(0);
		
		if(service != null && service.isAuto() == 'Y' && !plan.getProvisionSystem().equalsIgnoreCase("None")){
				
			CommandProcessingResult processingResult=this.prepareRequestWriteplatformService.prepareNewRequest(order,plan,requstStatus);
			commandId=processingResult.commandId();
		}
		
		//	For Order History
		OrderHistory orderHistory=new OrderHistory(order.getId(),DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(),commandId,requstStatus,userId,null);
		this.orderHistoryRepository.save(orderHistory);
	}
	
	//For Plan And HardWare Association
	Configuration configurationProperty=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_IMPLICIT_ASSOCIATION);
	
	if(configurationProperty.isEnabled()){
		
		if(plan.isHardwareReq() == 'Y'){
			List<AllocationDetailsData> allocationDetailsDatas=this.allocationReadPlatformService.retrieveHardWareDetailsByItemCode(clientId,plan.getPlanCode());
			if(allocationDetailsDatas.size() == 1 ){
				this.associationWriteplatformService.createNewHardwareAssociation(clientId,plan.getId(),allocationDetailsDatas.get(0).getSerialNo(),
						order.getId(),allocationDetailsDatas.get(0).getAllocationType());
			}
		}
	}
	
	if(plan.getProvisionSystem().equalsIgnoreCase("None")){
		
		Client client=this.clientRepository.findOne(clientId);
		client.setStatus(ClientStatus.ACTIVE.getValue());
		this.clientRepository.save(client);
		List<ActionDetaislData> actionDetaislDatas=this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_CREATE_ORDER);
		
		if(actionDetaislDatas.size() != 0){
			this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas,clientId, order.getId().toString(),null);
		}
	}
	
	/*processNotifyMessages(EventActionConstants.EVENT_ACTIVE_ORDER, clientId, order.getId().toString());*/
	
	return new CommandProcessingResult(order.getId(),order.getClientId());	
	}catch (DataIntegrityViolationException dve) {
		handleCodeDataIntegrityIssues(command, dve);
		return new CommandProcessingResult(Long.valueOf(-1));
		
			}
	}	
		
	@Override
	public void processNotifyMessages(String eventName, Long clientId, String orderId) {

		List<ActionDetaislData> actionDetaislDatas=this.actionDetailsReadPlatformService.retrieveActionDetails(eventName);
		
		if(actionDetaislDatas.size() != 0){
			this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas, clientId, orderId, null);
		}
	}

	private void handleCodeDataIntegrityIssues(JsonCommand command,DataIntegrityViolationException dve) {
				//   	Throwable realCause = dve.getMostSpecificCause();
				throw new PlatformDataIntegrityException("error.msg.office.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
		}
    
	@Transactional
	@Override
	public CommandProcessingResult updateOrderPrice(Long orderId,JsonCommand command) {
	try{
		final Long userId=context.authenticatedUser().getId();
		final Order order = retrieveOrderById(orderId);
		
		Long orderPriceId=command.longValueOfParameterNamed("priceId");
		BigDecimal price=command.bigDecimalValueOfParameterNamed("price");
		OrderPrice orderPrice=this.OrderPriceRepository.findOne(orderPriceId);
		orderPrice.setPrice(price);
		this.OrderPriceRepository.save(orderPrice);
		
		//For Order History
		OrderHistory orderHistory=new OrderHistory(order.getId(),DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(),null,"UPDATE PRICE",userId,null);
		this.orderHistoryRepository.save(orderHistory);
		return new CommandProcessingResultBuilder() //
		.withCommandId(command.commandId()) //
		.withEntityId(order.getId()) //
		.with(null) //
		.withClientId(order.getClientId())
		.build();
	} catch (DataIntegrityViolationException dve) {
		handleCodeDataIntegrityIssues(command, dve);
		return new CommandProcessingResult(Long.valueOf(-1));
	}
}
	private Order retrieveOrderById(Long orderId) {
		 final Order order= this.orderRepository.findOne(orderId);
	        if (order == null) { throw new NoOrdersFoundException(orderId.toString()); }
	        return order;
	}

	@Transactional
	@Override
	public CommandProcessingResult deleteOrder(Long orderId, JsonCommand command) {
		
	final Long userId=this.context.authenticatedUser().getId();
	Order order = this.orderRepository.findOne(orderId);
	List<OrderLine> orderline = order.getServices();
	List<OrderPrice> orderPrices=order.getPrice();
	Plan plan=this.planRepository.findOne(order.getPlanId());
		if(!plan.getProvisionSystem().equalsIgnoreCase("None")){
			List<Long> prepareIds=this.prepareRequestReadplatformService.getPrepareRequestDetails(orderId);
			if(prepareIds.isEmpty()){
				throw new PrepareRequestActivationException();	
			}
			for(Long id:prepareIds){
				PrepareRequest prepareRequest=this.prepareRequsetRepository.findOne(id);
				prepareRequest.setCancelStatus("CANCEL");
				this.prepareRequsetRepository.save(prepareRequest);
			}
		}
	for(OrderPrice price:orderPrices){
		price.delete();
	}
	for (OrderLine orderData : orderline) {
		orderData.delete();
	}
	order.delete();
	this.orderRepository.save(order);
	
	//For Order History
	OrderHistory orderHistory=new OrderHistory(order.getId(),DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(),null,"CANCELLED",userId,null);
	this.orderHistoryRepository.save(orderHistory);
	return new CommandProcessingResult(order.getId(),order.getClientId());
}
	
	@Override
	public CommandProcessingResult disconnectOrder(final JsonCommand command, final Long orderId ) {
		
    try {
    	this.fromApiJsonDeserializer.validateForDisconnectOrder(command.json());
    	Order order = this.orderRepository.findOne(orderId);
    	
    	final LocalDate disconnectionDate = command.localDateValueOfParameterNamed("disconnectionDate");
    	LocalDate currentDate = DateUtils.getLocalDateOfTenant();
    	currentDate.toDate();
    	final Configuration configurationProperty = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_DISCONNECT);
    	List<OrderPrice> orderPrices = order.getPrice();
    	for(OrderPrice price:orderPrices){
    		price.updateDates(disconnectionDate);
    	}
    	final Plan plan = this.planRepository.findOne(order.getPlanId());
    	Long orderStatus = null;
    		if("None".equalsIgnoreCase(plan.getProvisionSystem())){
    			orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.DISCONNECTED).getId();
    		}else{
    			orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId();
    		}
			if(configurationProperty.isEnabled() && plan.isPrepaid() == 'N'){
				if(plan.getBillRule() != 400 && plan.getBillRule() != 300){ 
					this.reverseInvoice.reverseInvoiceServices(orderId, order.getClientId(), disconnectionDate);
				}
			}
			order.update(command, orderStatus);
			order.setuserAction(UserActionStatusTypeEnum.DISCONNECTION.toString());
			this.orderRepository.saveAndFlush(order);
			
			final String requstStatus = UserActionStatusTypeEnum.DISCONNECTION.toString();
			Long processingResultId = Long.valueOf(0);
			
			//Update Client Status
			if("None".equalsIgnoreCase(plan.getProvisionSystem())){
				final Long activeOrders = this.orderReadPlatformService.retrieveClientActiveOrderDetails(order.getClientId(), null);
				if(activeOrders == 0){
					Client client = this.clientRepository.findOne(order.getClientId());
					client.setStatus(ClientStatus.DEACTIVE.getValue());
					this.clientRepository.saveAndFlush(client);
				}
			}else{
				
				CommandProcessingResult processingResult=this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order, plan.getPlanCode(), UserActionStatusTypeEnum.DISCONNECTION.toString(),
						processingResultId, null, null, order.getId(),plan.getProvisionSystem(),null);
				processingResultId = processingResult.commandId();
				
			}		
	
			// checking for Paypal Recurring DisConnection
			processPaypalRecurringActions(orderId, EventActionConstants.EVENT_PAYPAL_RECURRING_TERMINATE_ORDER);
			processNotifyMessages(EventActionConstants.EVENT_DISCONNECTION_ORDER, order.getClientId(), order.getId().toString());
			
		//For Order History
		final OrderHistory orderHistory = new OrderHistory(order.getId(), DateUtils.getLocalDateOfTenant(), DateUtils.getLocalDateOfTenant(), processingResultId, requstStatus, getUserId(), null);
		this.orderHistoryRepository.save(orderHistory);
 
		  return new CommandProcessingResult(Long.valueOf(order.getId()),order.getClientId());	
		}catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null,dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}
  
@Override
public CommandProcessingResult renewalClientOrder(JsonCommand command,Long orderId) {
		
  try{
	  LocalDate newStartdate=null;
	  String requstStatus=null;
	  String requestStatusForProv=null;
	  final Long userId=getUserId();
	  this.fromApiJsonDeserializer.validateForRenewalOrder(command.json());
	  Order orderDetails=retrieveOrderById(orderId);
	  this.eventValidationReadPlatformService.checkForCustomValidations(orderDetails.getClientId(),EventActionConstants.EVENT_ORDER_RENEWAL,command.json(),userId);	
	  List<OrderPrice>  orderPrices=orderDetails.getPrice();
	  final Long contractPeriod = command.longValueOfParameterNamed("renewalPeriod");
	  final String description=command.stringValueOfParameterNamed("description");
	  
	  Contract contractDetails=this.subscriptionRepository.findOne(contractPeriod);
	  Plan plan=this.planRepository.findOne(orderDetails.getPlanId());
	  
	  if(orderDetails.getStatus().equals(StatusTypeEnum.ACTIVE.getValue().longValue())){
		  
		  newStartdate=new LocalDate(orderDetails.getEndDate()).plusDays(1);
		  requstStatus=UserActionStatusEnumaration.OrderStatusType(UserActionStatusTypeEnum.RENEWAL_BEFORE_AUTOEXIPIRY).getValue();
					
	  } else if(orderDetails.getStatus().equals(StatusTypeEnum.DISCONNECTED.getValue().longValue())){
		  newStartdate = DateUtils.getLocalDateOfTenant(); 
		  requstStatus=UserActionStatusEnumaration.OrderStatusType(UserActionStatusTypeEnum.RENEWAL_AFTER_AUTOEXIPIRY).getValue();
		  if(!plan.getProvisionSystem().equalsIgnoreCase("None")){
			  orderDetails.setStatus(StatusTypeEnum.PENDING.getValue().longValue());
		   }else{
			   orderDetails.setStatus(StatusTypeEnum.ACTIVE.getValue().longValue());
			   Client client = this.clientRepository.findOne(orderDetails.getClientId());
				client.setStatus(ClientStatus.ACTIVE.getValue());
				this.clientRepository.saveAndFlush(client);
			}
		  requestStatusForProv="RENEWAL_AE";//UserActionStatusTypeEnum.ACTIVATION.toString();
		  orderDetails.setNextBillableDay(null);
	  }
	  LocalDate renewalEndDate=this.orderAssembler.calculateEndDate(newStartdate,contractDetails.getSubscriptionType(),contractDetails.getUnits());
	  
		
     Configuration configuration = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_ALIGN_BIILING_CYCLE);
		
		if(configuration != null && plan.isPrepaid() == 'N'){
			orderDetails.setBillingAlign(configuration.isEnabled()?'Y':'N');
			if(configuration.isEnabled() && renewalEndDate != null){
				orderDetails.setEndDate(renewalEndDate.dayOfMonth().withMaximumValue());
			}else{
				orderDetails.setEndDate(renewalEndDate);
			}
		}else{
			orderDetails.setEndDate(renewalEndDate);
		}
	  //orderDetails.setEndDate(renewalEndDate);
		 orderDetails.setuserAction(requstStatus);
	  for(OrderPrice orderprice:orderPrices){
		  if(plan.isPrepaid() == 'Y'){
			  final Long priceId = command.longValueOfParameterNamed("priceId");
			 // ServiceMaster service=this.serviceMasterRepository.findOne(orderprice.getServiceId()); 
			    Price price=this.priceRepository.findOne(priceId);
			 /* Price price=this.priceRepository.findOneByPlanAndService(plan.getId(), service.getServiceCode(),
					  contractDetails.getSubscriptionPeriod(),orderprice.getChargeCode());*/
				if(price != null){
					ChargeCodeMaster chargeCode=this.chargeCodeRepository.findOneByChargeCode(price.getChargeCode());
					orderprice.setChargeCode(chargeCode.getChargeCode());
					orderprice.setChargeDuration(chargeCode.getChargeDuration().toString());
					orderprice.setChargeType(chargeCode.getChargeType());
					orderprice.setChargeDurationType(chargeCode.getDurationType());
					orderprice.setPrice(price.getPrice());
				}else{
					throw new PriceNotFoundException(priceId);
				}
		  	}
		  orderprice.setDatesOnOrderStatus(newStartdate,new LocalDate(orderDetails.getEndDate()),orderDetails.getUserAction());//setBillEndDate(renewalEndDate);
		  //this.OrderPriceRepository.save(orderprice);
		  orderDetails.setNextBillableDay(null);
	  }
	
	  orderDetails.setContractPeriod(contractDetails.getId());
	 
	  orderDetails.setRenewalDate(newStartdate.toDate());
	  this.orderRepository.saveAndFlush(orderDetails);

	//  Set<PlanDetails> planDetails=plan.getDetails();
	 // ServiceMaster serviceMaster=this.serviceMasterRepository.findOneByServiceCode(planDetails.iterator().next().getServiceCode());
	  Long resourceId=Long.valueOf(0);
	  	if(!plan.getProvisionSystem().equalsIgnoreCase("None")){ 
		    	
			  //Prepare Provisioning Req
			  CodeValue codeValue=this.codeValueRepository.findOneByCodeValue(plan.getProvisionSystem());
			  
			  if(codeValue.position() == 1 && orderDetails.getStatus().equals(StatusTypeEnum.ACTIVE.getValue().longValue())){
				  requestStatusForProv="RENEWAL_BE";
			  
			  }
			  if(requestStatusForProv != null){
			  CommandProcessingResult commandProcessingResult=this.provisioningWritePlatformService.postOrderDetailsForProvisioning(orderDetails,plan.getPlanCode(),requestStatusForProv,
					  Long.valueOf(0),null,null,orderDetails.getId(),plan.getProvisionSystem(),null);
		      resourceId=commandProcessingResult.resourceId();
		    	 }

		     }

		     //For Order History
   			OrderHistory orderHistory=new OrderHistory(orderDetails.getId(),DateUtils.getLocalDateOfTenant(),newStartdate,resourceId,requstStatus,userId,description);
   			this.orderHistoryRepository.saveAndFlush(orderHistory);
   			
   			//Auto renewal with invoice process  for Topup  orders
   			
   		  if(plan.isPrepaid() == 'Y' && orderDetails.getStatus().equals(StatusTypeEnum.ACTIVE.getValue().longValue())){
   			  
   		     Invoice invoice=this.invoiceClient.onTopUpAutoRenewalInvoice(orderDetails.getId(),orderDetails.getClientId(),newStartdate.plusDays(1));
   		    
   		  }
   			processNotifyMessages(EventActionConstants.EVENT_RECONNECTION_ORDER, orderDetails.getClientId(), orderId.toString());
   			return new CommandProcessingResult(Long.valueOf(orderDetails.getClientId()),orderDetails.getClientId());
		
			
		}catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null,dve);
			  return new CommandProcessingResult(Long.valueOf(-1));
			}
	}
   
	private Long getUserId() {
		
		Long userId=null;
		SecurityContext context = SecurityContextHolder.getContext();
			if(context.getAuthentication() != null){
				AppUser appUser=this.context.authenticatedUser();
				userId=appUser.getId();
			}else {
				userId=new Long(0);
			}
			
			return userId;
	}

	/* (non-Javadoc)
	 * @see #reconnectOrder(java.lang.Long)
	 */
	@Transactional
    @Override
	public CommandProcessingResult reconnectOrder(final Long orderId) {
	  
	try{
		Order order=this.orderRepository.findOne(orderId);
		if(order == null){
			throw new NoOrdersFoundException(orderId);
		}
		final LocalDate startDate = DateUtils.getLocalDateOfTenant();
		List<SubscriptionData> subscriptionDatas=this.contractPeriodReadPlatformService.retrieveSubscriptionDatabyOrder(orderId);
		Contract contractPeriod=this.subscriptionRepository.findOne(subscriptionDatas.get(0).getId());
		LocalDate EndDate=this.orderAssembler.calculateEndDate(startDate,contractPeriod.getSubscriptionType(),contractPeriod.getUnits());
		
		order.setStartDate(startDate);
		order.setEndDate(EndDate);
		order.setNextBillableDay(null);
		final List<OrderPrice> orderPrices=order.getPrice();
		
		for(OrderPrice price:orderPrices){
			
			price.setBillStartDate(startDate);
			price.setBillEndDate(EndDate);
			price.setNextBillableDay(null);
			price.setInvoiceTillDate(null);
		}
		
		Plan plan=this.planRepository.findOne(order.getPlanId());
		String requstStatus = UserActionStatusTypeEnum.RECONNECTION.toString().toString();
		Long processingResultId=Long.valueOf(0);
		
		if(plan.getProvisionSystem().equalsIgnoreCase("None")){
			order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
			Client client=this.clientRepository.findOne(order.getClientId());
			client.setStatus(ClientStatus.ACTIVE.getValue());
			this.clientRepository.save(client);
			
		}else{
			
			order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId());
			CommandProcessingResult processingResult =this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order,plan.getPlanCode(), requstStatus,
					Long.valueOf(0),null,null,order.getId(),plan.getProvisionSystem(),null);
			processingResultId=processingResult.commandId();
		
		}
		order.setuserAction(UserActionStatusTypeEnum.RECONNECTION.toString());
		this.orderRepository.save(order);
		
		//For Order History
		OrderHistory orderHistory=new OrderHistory(order.getId(),DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(),processingResultId,requstStatus,getUserId(),null);
		this.orderHistoryRepository.save(orderHistory);
		
		processNotifyMessages(EventActionConstants.EVENT_RECONNECTION_ORDER, order.getClientId(), order.getId().toString());
		return new CommandProcessingResult(order.getId(),order.getClientId());
	
	}catch(final DataIntegrityViolationException dve){
		handleCodeDataIntegrityIssues(null, dve);
		return new CommandProcessingResult(Long.valueOf(-1));
	  }
	}

	@Override
	public CommandProcessingResult retrackOsdMessage(final JsonCommand command) {
		try {
			this.context.authenticatedUser();
			this.fromApiJsonDeserializer.validateForRetrack(command.json());
			String requstStatus = null;
			String message = null;
			final String commandName = command.stringValueOfParameterNamed("commandName");
			final Order order = this.orderRepository.findOne(command.entityId());
				if (order == null) {
					throw new NoOrdersFoundException(command.entityId());
				}
				if (commandName.equalsIgnoreCase("RETRACK")) {
					final String restrict = orderReadPlatformService.checkRetrackInterval(command.entityId());
						if (restrict != null && restrict.equalsIgnoreCase("yes")) {	
							requstStatus = UserActionStatusTypeEnum.RETRACK.toString();					 
						} else {
							throw new PlatformDataIntegrityException("retrack.already.done", "retrack.already.done","retrack.already.done");
						}
				} else if(commandName.equalsIgnoreCase("OSM")) {
					requstStatus = UserActionStatusTypeEnum.MESSAGE.toString();
					message = command.stringValueOfParameterNamed("message");
				}
				final Plan plan = this.planRepository.findOne(order.getPlanId());
				if (plan == null) {
					throw new NoOrdersFoundException(command.entityId());
				}
				Long resourceId = Long.valueOf(0);
				if (requstStatus != null && plan!=null) {
					
					 CommandProcessingResult commandProcessingResult=this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order,plan.getPlanCode(),requstStatus,
		    				 Long.valueOf(0),null,null,order.getId(),plan.getProvisionSystem(),null);

		    		 resourceId=commandProcessingResult.resourceId();
					
					/*final AllocationDetailsData detailsData = this.allocationReadPlatformService.getTheHardwareItemDetails(command.entityId());
					final ProcessRequest processRequest=new ProcessRequest(Long.valueOf(0),order.getClientId(),order.getId(),plan.getProvisionSystem(),requstStatus
							,'N','N');
				  processRequest.setNotify();
				  final List<OrderLine> orderLineData = order.getServices();
				  	for (OrderLine orderLine : orderLineData) {
				  		String hardWareId = null;
				  			if (detailsData != null) {
				  				hardWareId = detailsData.getSerialNo();
				  			}
				  			final List<ServiceMapping> provisionServiceDetails = this.provisionServiceDetailsRepository.findOneByServiceId(orderLine.getServiceId());
				  			final ServiceMaster service = this.serviceMasterRepository.findOne(orderLine.getServiceId());
				  			if (!provisionServiceDetails.isEmpty()) {
				  				if (message == null) {
				  					message = provisionServiceDetails.get(0).getServiceIdentification();
				  				}
				  				final ProcessRequestDetails processRequestDetails = new ProcessRequestDetails(orderLine.getId(), orderLine.getServiceId(),message, "Recieved",
								hardWareId,order.getStartDate(), order.getEndDate(), null,null, 'N',requstStatus,service.getServiceType());

				  				processRequest.add(processRequestDetails);
				  			}
				  	}
				this.processRequestRepository.save(processRequest);*/

				this.orderRepository.save(order);
				final OrderHistory orderHistory = new OrderHistory(order.getId(),DateUtils.getLocalDateOfTenant(), DateUtils.getLocalDateOfTenant(), resourceId,
						requstStatus, getUserId(),null);
				this.orderHistoryRepository.save(orderHistory);
				
			}
			return new CommandProcessingResult(order.getId(),order.getClientId());
		} catch (EmptyResultDataAccessException dve) {
			throw new PlatformDataIntegrityException("retrack.already.done", "retrack.already.done", "retrack.already.done");
		} catch (DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
	

@Override
public CommandProcessingResult changePlan(JsonCommand command, Long entityId) {
		
 try{
	 Long userId=this.context.authenticatedUser().getId();
	 Order order=this.orderRepository.findOne(entityId);
	 order.updateDisconnectionstate();
	 Date billEndDate=order.getPrice().get(0).getBillEndDate();
			
	 Date invoicetillDate=order.getPrice().get(0).getInvoiceTillDate();
	 this.orderRepository.save(order);
	 
	 Configuration property = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_CHANGE_PLAN_ALIGN_DATES);
	 if(!property.isEnabled()){
		 Configuration dcConfiguration= this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_DISCONNECT);
		 if(dcConfiguration.isEnabled()){
			 this.reverseInvoice.reverseInvoiceServices(order.getId(), order.getClientId(),DateUtils.getLocalDateOfTenant());
		 }
	 }	
	 
	 CommandProcessingResult result=this.createOrder(order.getClientId(), command);
	 Order newOrder=this.orderRepository.findOne(result.resourceId());
	 
	 newOrder.updateOrderNum(order.getOrderNo());
	 newOrder.updateActivationDate(order.getActiveDate());
	
	 if(property.isEnabled()){
		 List<OrderPrice> orderPrices=newOrder.getPrice();
		 for(OrderPrice orderPrice:orderPrices){
			 if(billEndDate == null){
				// orderPrice.setBillEndDate(null);	
				 
			 }else{
				// orderPrice.setBillEndDate(new LocalDate(billEndDate));
			 }
			 orderPrice.setInvoiceTillDate(invoicetillDate);
			 orderPrice.setNextBillableDay(order.getPrice().get(0).getNextBillableDay());
					}
			}
				
	 newOrder.setuserAction(UserActionStatusTypeEnum.CHANGE_PLAN.toString());
	 this.orderRepository.save(newOrder);
	 
	Plan plan=this.planRepository.findOne(newOrder.getPlanId());
	HardwareAssociation association=this.associationRepository.findOneByOrderAndClient(order.getId(),order.getClientId());
		
		if(association != null){
			association.delete();
			this.associationRepository.save(association);
		}
		Long processResuiltId=new Long(0);
		
			if(!plan.getProvisionSystem().equalsIgnoreCase("None")){
				CommandProcessingResult processingResult=this.provisioningWritePlatformService.postOrderDetailsForProvisioning(newOrder, plan.getCode(),
						UserActionStatusTypeEnum.CHANGE_PLAN.toString(),new Long(0), null, null,newOrder.getId(),plan.getProvisionSystem(),null);

				processResuiltId=processingResult.commandId();
			}
		     
    	// For Order History
			OrderHistory orderHistory=new OrderHistory(order.getId(),DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(),processResuiltId,
					UserActionStatusTypeEnum.CHANGE_PLAN.toString(),userId,null);
			this.orderHistoryRepository.save(orderHistory);
			
			return new CommandProcessingResult(result.resourceId(),order.getClientId());
		
 	}catch(DataIntegrityViolationException exception){
	 handleCodeDataIntegrityIssues(command, exception);
	 return new CommandProcessingResult(new Long(-1));
 	}
 
}
    
	
	@Override
 public CommandProcessingResult applyPromo(JsonCommand command) {
	try{
		this.context.authenticatedUser().getUsername();
		this.fromApiJsonDeserializer.validateForPromo(command.json());			
		final Long promoId=command.longValueOfParameterNamed("promoId");
		final LocalDate startDate=command.localDateValueOfParameterNamed("startDate");
		PromotionCodeMaster promotion=this.promotionCodeRepository.findOne(promoId);
		
		if(promotion == null){
			throw new PromotionCodeNotFoundException(promoId.toString());
		}
		Order order=this.orderRepository.findOne(command.entityId());
		List<OrderDiscount> orderDiscounts=order.getOrderDiscount();
		LocalDate enddate=this.orderAssembler.calculateEndDate(startDate,promotion.getDurationType(),promotion.getDuration());
		
		for(OrderDiscount orderDiscount:orderDiscounts){
			orderDiscount.updateDates(promotion.getDiscountRate(),promotion.getDiscountType(),enddate);
			this.orderDiscountRepository.save(orderDiscount);
		}
		this.orderRepository.save(order);
		return new CommandProcessingResult(command.entityId(),order.getClientId());
		
		
	}catch(DataIntegrityViolationException dve){
		handleCodeDataIntegrityIssues(command, dve);
		return null;
	}
	}

	@Transactional
	@Override
public CommandProcessingResult scheduleOrderCreation(Long clientId,JsonCommand command) {

  try{
	  String actionType = command.stringValueOfParameterNamed("actionType");
	  if(!actionType.equalsIgnoreCase("renewalorder")){
		  this.fromApiJsonDeserializer.validateForCreate(command.json());
	  }
	  LocalDate startDate=command.localDateValueOfParameterNamed("start_date");
	  
	  char status = 'N';
	  if(command.hasParameter("status")){
		  status = command.stringValueOfParameterNamed("status").trim().charAt(0);
	  }
	 
	
		EventAction  eventAction=null;
		JSONObject jsonObject=new JSONObject();
		Long userId=getUserId();
		
		if(actionType.equalsIgnoreCase("renewalorder")){
			
			//Check for Custome_Validation
			this.eventValidationReadPlatformService.checkForCustomValidations(clientId,EventActionConstants.EVENT_ORDER_RENEWAL,command.json(),userId);
		    	  
		    	  	jsonObject.put("renewalPeriod",command.longValueOfParameterNamed("renewalPeriod"));
		    	  	jsonObject.put("description",command.stringValueOfParameterNamed("description"));
	        	   
	        	    eventAction=new EventAction(DateUtils.getLocalDateOfTenant().toDate(), "RENEWAL", "ORDER",EventActionConstants.ACTION_RENEWAL,"/orders/renewalorder/"+clientId, 
	        			  clientId,command.json(),null,clientId);
	        	    
	        	    
	        	  
		}else if(actionType.equalsIgnoreCase("changeorder")){
			
			//Check for Custome_Validation
			this.eventValidationReadPlatformService.checkForCustomValidations(clientId,EventActionConstants.EVENT_CHANGE_ORDER,command.json(),userId);
				
		    	  	jsonObject.put("billAlign",command.booleanPrimitiveValueOfParameterNamed("billAlign"));
		    	  	jsonObject.put("contractPeriod",command.longValueOfParameterNamed("contractPeriod"));
		    	  	jsonObject.put("dateFormat",command.booleanPrimitiveValueOfParameterNamed("dateFormat"));
		    	  	jsonObject.put("locale",command.booleanPrimitiveValueOfParameterNamed("locale"));
		    	  	jsonObject.put("isNewPlan",command.booleanPrimitiveValueOfParameterNamed("isNewPlan"));
		    	  	jsonObject.put("paytermCode",command.stringValueOfParameterNamed("paytermCode"));
		    	  	jsonObject.put("planCode",command.longValueOfParameterNamed("planCode"));
		    	  	jsonObject.put("start_date",command.stringValueOfParameterNamed("start_date"));
		    	  	jsonObject.put("disconnectionDate",command.stringValueOfParameterNamed("disconnectionDate"));
		    	  	jsonObject.put("disconnectReason",command.stringValueOfParameterNamed("disconnectReason"));
	        	   
	        	    eventAction=new EventAction(startDate.toDate(), "CHANGEPLAN", "ORDER",EventActionConstants.ACTION_CHNAGE_PLAN,"/orders/changPlan/"+clientId, 
	        			  clientId,command.json(),null,clientId);
			
		}else{
		
		//Check for Custome_Validation
		this.eventValidationReadPlatformService.checkForCustomValidations(clientId,EventActionConstants.EVENT_CREATE_ORDER,command.json(),userId);
			
	    	  	//Check for Active Orders	
	    	/*  Long activeorderId=this.orderReadPlatformService.retrieveClientActiveOrderDetails(clientId,null);
	    	  	if(activeorderId !=null && activeorderId !=0){
	    	  		Order order=this.orderRepository.findOne(activeorderId);
				   		if(order.getEndDate() == null || !startDate.isAfter(new LocalDate(order.getEndDate()))){
				   			throw new SchedulerOrderFoundException(activeorderId);				   
				   		}
	    	  	}*/
	    	  
	    	  	jsonObject.put("billAlign",command.booleanPrimitiveValueOfParameterNamed("billAlign"));
	    	  	jsonObject.put("contractPeriod",command.longValueOfParameterNamed("contractPeriod"));
	    	  	jsonObject.put("dateFormat","dd MMMM yyyy");
	    	  	jsonObject.put("locale","en");
	    	  	jsonObject.put("isNewPlan","true");
	    	  	jsonObject.put("paytermCode",command.stringValueOfParameterNamed("paytermCode"));
	    	  	jsonObject.put("planCode",command.longValueOfParameterNamed("planCode"));
	    	  	jsonObject.put("start_date",startDate.toDate());
        	   
        	    eventAction=new EventAction(startDate.toDate(), "CREATE", "ORDER",EventActionConstants.ACTION_NEW,"/orders/"+clientId, 
        			  clientId,command.json(),null,clientId);
        	    
         }
		
			  eventAction.updateStatus(status);
			  this.eventActionRepository.save(eventAction);
        	  return  new CommandProcessingResult(command.entityId(),clientId);
	
  }catch(DataIntegrityViolationException dve){
		handleCodeDataIntegrityIssues(command, null);
		return new CommandProcessingResult(Long.valueOf(-1));
	}catch(JSONException dve){
		
		return new CommandProcessingResult(Long.valueOf(-1));
		}
		
     }


	@Override
	public CommandProcessingResult deleteSchedulingOrder(Long entityId,JsonCommand command) {
		
		try{
			this.context.authenticatedUser();
			EventAction eventAction=this.eventActionRepository.findOne(entityId);
				if(eventAction.IsProcessed() == 'Y'){
					throw new PrepareRequestActivationException();	
				}else{
					eventAction.updateStatus('C');
					this.eventActionRepository.saveAndFlush(eventAction);
				}
			return new CommandProcessingResult(Long.valueOf(entityId),eventAction.getClientId());
		}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

    @Transactional
	@Override
	public CommandProcessingResult orderExtension(JsonCommand command,Long entityId) {
		
		try{
			
			Long userId=this.context.authenticatedUser().getId();
			Order order=this.orderRepository.findOne(entityId);
			String extensionperiod=command.stringValueOfParameterNamed("extensionPeriod");
			String extensionReason=command.stringValueOfParameterNamed("extensionReason");
			LocalDate newStartdate=new LocalDate(order.getEndDate());
			newStartdate=newStartdate.plusDays(1);
			String[] periodData=extensionperiod.split(" ");
			LocalDate endDate=this.orderAssembler.calculateEndDate(newStartdate,periodData[1], new Long(periodData[0]));
			List<OrderPrice>  orderPrices=order.getPrice();
			Plan plan=this.planRepository.findOne(order.getPlanId());
					if(order.getStatus().intValue() == StatusTypeEnum.ACTIVE.getValue()){
						order.setEndDate(endDate);
							for(OrderPrice orderprice:orderPrices){
								orderprice.setBillEndDate(endDate);
								orderprice.setInvoiceTillDate(endDate.toDate());
								orderprice.setNextBillableDay(endDate.toDate());
								this.OrderPriceRepository.save(orderprice);
							}
					}else if(order.getStatus().intValue() == StatusTypeEnum.DISCONNECTED.getValue()){
						for(OrderPrice orderprice:orderPrices){
							orderprice.setBillStartDate(newStartdate);
				    		orderprice.setBillEndDate(endDate);
				    		orderprice.setNextBillableDay(null);
				    		orderprice.setInvoiceTillDate(null);
				    		this.OrderPriceRepository.save(orderprice);
						}
				if(plan.getProvisionSystem().equalsIgnoreCase("None")){
					order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId());
					Client client=this.clientRepository.findOne(order.getClientId());
					client.setStatus(ClientStatus.ACTIVE.getValue());
					this.clientRepository.save(client);
				}else{
					 //Check For HardwareAssociation
					  AssociationData associationData=this.hardwareAssociationReadplatformService.retrieveSingleDetails(entityId);
					  	if(associationData ==null){
					  		throw new HardwareDetailsNotFoundException(entityId.toString());
					  	}
					  	order.setStatus(OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId());
					}
					}		   
		      order.setEndDate(endDate);
		      order.setuserAction(UserActionStatusTypeEnum.RECONNECTION.toString());
		      this.orderRepository.save(order);

		      //for Prepare Request
			String requstStatus = UserActionStatusTypeEnum.RECONNECTION.toString().toString();
            this.prepareRequestWriteplatformService.prepareNewRequest(order,plan,requstStatus);

            //For Order History
			SecurityContext context = SecurityContextHolder.getContext();
	        	if (context.getAuthentication() != null) {
	        		AppUser appUser=this.context.authenticatedUser();
	        		userId=appUser.getId();
	        	}else{
	        		userId=new Long(0);
	        	}

	        	//For Order History
			OrderHistory orderHistory=new OrderHistory(order.getId(),DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(),entityId,
		    UserActionStatusTypeEnum.EXTENSION.toString(),userId,extensionReason);
			this.orderHistoryRepository.save(orderHistory);
			return new CommandProcessingResult(entityId,order.getClientId());

		}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(new Long(-1));
			
		}
	}

	@Override
	public CommandProcessingResult orderTermination(JsonCommand command,Long orderId) {
		
		try{
			AppUser appUser=this.context.authenticatedUser();
			Order order=this.orderRepository.findOne(orderId);
			Long resourceId=Long.valueOf(0);
			
			if(order == null){
				throw new OrderNotFoundException(orderId);
			}
			
			Long orderStatus=null;
			Plan plan=this.planRepository.findOne(order.getPlanId());

			if(plan.getProvisionSystem().equalsIgnoreCase("None")){
				orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.TERMINATED).getId();
			
			}else{
				orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId();
				CommandProcessingResult processingResult =this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order, plan.getCode(), 
						UserActionStatusTypeEnum.TERMINATION.toString(),resourceId, null, null,order.getId(),plan.getProvisionSystem(),null);

				resourceId=processingResult.resourceId();
			}
			order.setStatus(orderStatus);
			order.setuserAction(UserActionStatusTypeEnum.TERMINATION.toString());
			this.orderRepository.saveAndFlush(order);
			
			
			OrderHistory orderHistory=new OrderHistory(order.getId(),DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(),resourceId,
					UserActionStatusTypeEnum.TERMINATION.toString(),appUser.getId(),null);
			
			// checking for Paypal Recurring DisConnection
			processPaypalRecurringActions(orderId, EventActionConstants.EVENT_PAYPAL_RECURRING_TERMINATE_ORDER);
			
			this.orderHistoryRepository.save(orderHistory);	
		    return new CommandProcessingResult(orderId,order.getClientId());
		
		}catch(DataIntegrityViolationException exception){
			handleCodeDataIntegrityIssues(command, exception);
			return new CommandProcessingResult(new Long(-1));
		}
	}

	@Transactional
	@Override
	public CommandProcessingResult orderSuspention(final JsonCommand command, final Long entityId) {
		
		try{
				final AppUser appUser= this.context.authenticatedUser();
				this.fromApiJsonDeserializer.validateForOrderSuspension(command.json());
				final Order order=this.orderRepository.findOne(entityId);
				 Long resourceId=Long.valueOf(0);
					if(order == null){
						throw new OrderNotFoundException(entityId);
					}
					
					final EnumDomainService enumDomainService=this.enumDomainServiceRepository.findOneByEnumMessageProperty(StatusTypeEnum.SUSPENDED.toString());
	    		  	order.setStatus(enumDomainService.getEnumId());
					
					
				    final Plan plan=this.planRepository.findOne(order.getPlanId());
				     if(!plan.getProvisionSystem().equalsIgnoreCase("None")){
				    	 final Long pendingId=this.enumDomainServiceRepository.findOneByEnumMessageProperty(StatusTypeEnum.PENDING.toString()).getEnumId();	
				    		order.setStatus(pendingId);
				    	
				    			CommandProcessingResult commandProcessingResult=this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order, plan.getCode(), UserActionStatusTypeEnum.SUSPENTATION.toString(), 
										resourceId, null, null,order.getId(),plan.getProvisionSystem(),null);

				    			resourceId =commandProcessingResult.resourceId();
				    	  //}
				    		  
					}
					order.setuserAction(UserActionStatusTypeEnum.SUSPENTATION.toString());
				
					
					//Post Details in Payment followup
					final PaymentFollowup paymentFollowup=PaymentFollowup.fromJson(command,order.getClientId(),order.getId(),
							StatusTypeEnum.ACTIVE.toString(),StatusTypeEnum.SUSPENDED.toString());
					this.paymentFollowupRepository.save(paymentFollowup);
					this.orderRepository.save(order);
					
					// checking for Paypal Recurring DisConnection
					processPaypalRecurringActions(entityId, EventActionConstants.EVENT_PAYPAL_RECURRING_DISCONNECT_ORDER);
					
					final OrderHistory orderHistory=new OrderHistory(order.getId(),DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(),resourceId,UserActionStatusTypeEnum.TERMINATION.toString(),
							appUser.getId(),null);
                     this.orderHistoryRepository.save(orderHistory);	
				    
			return new CommandProcessingResult(entityId,order.getClientId());			
		}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}

  @Override
 public CommandProcessingResult reactiveOrder(final JsonCommand command, final Long entityId) {

	 try{
		 final AppUser appUser = this.context.authenticatedUser();
		 Order order = this.orderRepository.findOne(entityId);
		 Long resourceId = Long.valueOf(0);
		 
		 if(order == null){
			 throw new OrderNotFoundException(entityId);
		 }
		 
		 final Long pendingId = this.enumDomainServiceRepository.findOneByEnumMessageProperty(StatusTypeEnum.PENDING.toString()).getEnumId();	
		 final Plan plan = this.planRepository.findOne(order.getPlanId());

		 if(!"None".equalsIgnoreCase(plan.getProvisionSystem())){
		    	order.setStatus(pendingId);
				CommandProcessingResult commandProcessingResult=this.provisioningWritePlatformService.postOrderDetailsForProvisioning(order, plan.getCode(), UserActionStatusTypeEnum.REACTIVATION.toString(), 
									resourceId, null, null, order.getId(),plan.getProvisionSystem(),null);
				resourceId=commandProcessingResult.resourceId();
		 
		 }else{
			 EnumDomainService enumDomainService = this.enumDomainServiceRepository.findOneByEnumMessageProperty(StatusTypeEnum.ACTIVE.toString());
			 order.setStatus(enumDomainService.getEnumId());
		}
		
		 order.setuserAction(UserActionStatusTypeEnum.REACTIVATION.toString());
		 PaymentFollowup paymentFollowup = this.paymentFollowupRepository.findOneByorderId(order.getId());
		
		 if(paymentFollowup != null){
			 paymentFollowup.setReactiveDate(DateUtils.getDateOfTenant());
			 this.paymentFollowupRepository.save(paymentFollowup);
		 }	
			
		 this.orderRepository.save(order);
		 
		// checking for Paypal Recurring Reconnection 				
		 processPaypalRecurringActions(entityId, EventActionConstants.EVENT_PAYPAL_RECURRING_RECONNECTION_ORDER);
	 		
		 final OrderHistory orderHistory = new OrderHistory(order.getId(),DateUtils.getLocalDateOfTenant(),DateUtils.getLocalDateOfTenant(),resourceId,UserActionStatusTypeEnum.REACTIVATION.toString(),
				 appUser.getId(),null);
            this.orderHistoryRepository.save(orderHistory);	
		    
		    return new CommandProcessingResult(entityId, order.getClientId());	
			
		}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}
  
  private void processPaypalRecurringActions(Long orderId, String eventActionName){
	  
	// checking for Paypal Recurring DisConnection			
			PaypalRecurringBilling billing = this.paypalRecurringBillingRepository.findOneByOrderId(orderId);
								
			if(null != billing && null != billing.getSubscriberId()){
							
				List<ActionDetaislData> actionDetaislDatas = this.actionDetailsReadPlatformService.retrieveActionDetails(eventActionName);
										
				if(actionDetaislDatas.size() != 0){
					this.actiondetailsWritePlatformService.AddNewActions(actionDetaislDatas,billing.getClientId(), orderId.toString(),null);			
				}		
			}  
  }

 }
