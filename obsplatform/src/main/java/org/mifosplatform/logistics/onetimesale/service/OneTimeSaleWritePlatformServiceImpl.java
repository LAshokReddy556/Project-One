package org.mifosplatform.logistics.onetimesale.service;

import java.math.BigDecimal;
import java.util.List;

import org.mifosplatform.billing.chargecode.data.ChargesData;
import org.mifosplatform.billing.chargecode.domain.ChargeCodeMaster;
import org.mifosplatform.billing.chargecode.domain.ChargeCodeRepository;
import org.mifosplatform.billing.discountmaster.data.DiscountMasterData;
import org.mifosplatform.billing.discountmaster.service.DiscountReadPlatformService;
import org.mifosplatform.finance.billingorder.domain.BillingOrder;
import org.mifosplatform.finance.billingorder.domain.Invoice;
import org.mifosplatform.finance.billingorder.domain.InvoiceRepository;
import org.mifosplatform.finance.billingorder.service.BillingOrderWritePlatformService;
import org.mifosplatform.finance.clientbalance.domain.ClientBalance;
import org.mifosplatform.finance.clientbalance.domain.ClientBalanceRepository;
import org.mifosplatform.finance.depositandrefund.domain.DepositAndRefund;
import org.mifosplatform.finance.depositandrefund.domain.DepositAndRefundRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.item.data.ItemData;
import org.mifosplatform.logistics.item.domain.ItemMaster;
import org.mifosplatform.logistics.item.domain.ItemRepository;
import org.mifosplatform.logistics.item.domain.UnitEnumType;
import org.mifosplatform.logistics.item.service.ItemReadPlatformService;
import org.mifosplatform.logistics.itemdetails.service.ItemDetailsWritePlatformService;
import org.mifosplatform.logistics.onetimesale.data.OneTimeSaleData;
import org.mifosplatform.logistics.onetimesale.domain.OneTimeSale;
import org.mifosplatform.logistics.onetimesale.domain.OneTimeSaleRepository;
import org.mifosplatform.logistics.onetimesale.exception.DeviceSaleNotFoundException;
import org.mifosplatform.logistics.onetimesale.serialization.OneTimesaleCommandFromApiJsonDeserializer;
import org.mifosplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.mifosplatform.useradministration.domain.AppUser;
import org.mifosplatform.workflow.eventvalidation.service.EventValidationReadPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author hugo
 *
 */
@Service
public class OneTimeSaleWritePlatformServiceImpl implements OneTimeSaleWritePlatformService {
	
	
	private final static Logger LOGGER = LoggerFactory.getLogger(OneTimeSaleWritePlatformServiceImpl.class);
	private final FromJsonHelper fromJsonHelper;
	private final PlatformSecurityContext context;
	private final ItemRepository itemMasterRepository;
	private final OneTimesaleCommandFromApiJsonDeserializer apiJsonDeserializer;
	private final InvoiceOneTimeSale invoiceOneTimeSale;
	private final OneTimeSaleRepository oneTimeSaleRepository;
	private final ItemReadPlatformService itemReadPlatformService;
	private final DiscountReadPlatformService discountReadPlatformService;
	private final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService;
	private final ItemDetailsWritePlatformService inventoryItemDetailsWritePlatformService;
	private final EventValidationReadPlatformService eventValidationReadPlatformService;
	private final ChargeCodeRepository chargeCodeRepository;
	private final InvoiceRepository invoiceRepository;
	private final DepositAndRefundRepository depositAndRefundRepository;
	private final BillingOrderWritePlatformService billingOrderWritePlatformService;

	@Autowired
	public OneTimeSaleWritePlatformServiceImpl(final PlatformSecurityContext context,final OneTimeSaleRepository oneTimeSaleRepository,
			final ItemRepository itemMasterRepository,final OneTimesaleCommandFromApiJsonDeserializer apiJsonDeserializer,
			final InvoiceOneTimeSale invoiceOneTimeSale,final ItemReadPlatformService itemReadPlatformService,
			final FromJsonHelper fromJsonHelper,final OneTimeSaleReadPlatformService oneTimeSaleReadPlatformService,
			final ItemDetailsWritePlatformService inventoryItemDetailsWritePlatformService,
			final EventValidationReadPlatformService eventValidationReadPlatformService,
			final DiscountReadPlatformService discountReadPlatformService,
			final ChargeCodeRepository chargeCodeRepository,
			final InvoiceRepository invoiceRepository,
			final DepositAndRefundRepository depositAndRefundRepository,
			final BillingOrderWritePlatformService billingOrderWritePlatformService) {

		
		this.context = context;
		this.fromJsonHelper = fromJsonHelper;
		this.invoiceOneTimeSale = invoiceOneTimeSale;
		this.apiJsonDeserializer = apiJsonDeserializer;
		this.itemMasterRepository = itemMasterRepository;
		this.oneTimeSaleRepository = oneTimeSaleRepository;
		this.itemReadPlatformService = itemReadPlatformService;
		this.discountReadPlatformService = discountReadPlatformService;
		this.oneTimeSaleReadPlatformService = oneTimeSaleReadPlatformService;
		this.inventoryItemDetailsWritePlatformService = inventoryItemDetailsWritePlatformService;
		this.eventValidationReadPlatformService = eventValidationReadPlatformService;
		this.chargeCodeRepository = chargeCodeRepository;
		this.invoiceRepository = invoiceRepository;
		this.depositAndRefundRepository = depositAndRefundRepository;
		this.billingOrderWritePlatformService = billingOrderWritePlatformService;
	}

	/* (non-Javadoc)
	 * @see #createOneTimeSale(org.mifosplatform.infrastructure.core.api.JsonCommand, java.lang.Long)
	 */
	@Transactional
	@Override
	public CommandProcessingResult createOneTimeSale(final JsonCommand command,final Long clientId) {

		try {
			
			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForCreate(command.json());
			final JsonElement element = fromJsonHelper.parse(command.json());
			final Long itemId = command.longValueOfParameterNamed("itemId");
			ItemMaster item = this.itemMasterRepository.findOne(itemId);

			// Check for Custome_Validation
			this.eventValidationReadPlatformService.checkForCustomValidations(clientId, "Rental",command.json(),getUserId());
			final OneTimeSale oneTimeSale = OneTimeSale.fromJson(clientId, command,item);

			this.oneTimeSaleRepository.saveAndFlush(oneTimeSale);
			final List<OneTimeSaleData> oneTimeSaleDatas = this.oneTimeSaleReadPlatformService.retrieveOnetimeSalesForInvoice(clientId);
			JsonObject jsonObject = new JsonObject();
			final String saleType = command.stringValueOfParameterNamed("saleType");
			if (saleType.equalsIgnoreCase("NEWSALE")) {
				for (OneTimeSaleData oneTimeSaleData : oneTimeSaleDatas) {
					CommandProcessingResult invoice=this.invoiceOneTimeSale.invoiceOneTimeSale(clientId,oneTimeSaleData,false);
					updateOneTimeSale(oneTimeSaleData,invoice);
				}
			}
			
			/** Deposit&Refund table */
			if(command.hasParameter("addDeposit")){
				if(command.booleanObjectValueOfParameterNamed("addDeposit")){
					final BigDecimal amount = command.bigDecimalValueOfParameterNamed("amount");
					final DepositAndRefund depositAndRefund = DepositAndRefund.fromJson(clientId, command);
					depositAndRefund.setRefId(oneTimeSale.getId());
					this.depositAndRefundRepository.saveAndFlush(depositAndRefund);
					
					// Update Client Balance
					this.billingOrderWritePlatformService.updateClientBalance(amount, clientId, false);
					
					
				}
				
			}
			
			/**	Call if Item units is PIECES */
			if(UnitEnumType.PIECES.toString().equalsIgnoreCase(item.getUnits())){
				
				JsonArray serialData = fromJsonHelper.extractJsonArrayNamed("serialNumber", element);
				for (JsonElement je : serialData) {
					JsonObject serialNumber = je.getAsJsonObject();
					serialNumber.addProperty("clientId", oneTimeSale.getClientId());
					serialNumber.addProperty("orderId", oneTimeSale.getId());
				}
				jsonObject.addProperty("itemId", oneTimeSale.getItemId());
				jsonObject.addProperty("quantity", oneTimeSale.getQuantity());
				jsonObject.add("serialNumber", serialData);
				JsonCommand jsonCommand = new JsonCommand(null,jsonObject.toString(), element, fromJsonHelper, null, null,
						null, null, null, null, null, null, null, null, null, null);
				this.inventoryItemDetailsWritePlatformService.allocateHardware(jsonCommand);
			}
			
			return new CommandProcessingResult(Long.valueOf(oneTimeSale.getId()), clientId);
		} catch (final DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(command, dve);
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

	private void handleCodeDataIntegrityIssues(final JsonCommand command,
			final DataIntegrityViolationException dve) {
		
		LOGGER.error(dve.getMessage(), dve);
		final Throwable realCause=dve.getMostSpecificCause();
		throw new PlatformDataIntegrityException(
				"error.msg.could.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: "
						+ realCause.getMessage());
		

	}

	public void updateOneTimeSale(final OneTimeSaleData oneTimeSaleData,final CommandProcessingResult invoice) {

		OneTimeSale oneTimeSale = oneTimeSaleRepository.findOne(oneTimeSaleData.getId());
		oneTimeSale.setIsInvoiced('Y');
		oneTimeSale.setInvoiceId(invoice.resourceId());
		oneTimeSaleRepository.save(oneTimeSale);

	}

	/* (non-Javadoc)
	 * @see #calculatePrice(java.lang.Long, org.mifosplatform.infrastructure.core.api.JsonQuery)
	 */
	@Override
	public ItemData calculatePrice(final Long itemId, final JsonQuery query) {

		try {
			
			this.context.authenticatedUser();
			this.apiJsonDeserializer.validateForPrice(query.parsedJson());
			final BigDecimal unitprice = fromJsonHelper.extractBigDecimalWithLocaleNamed("unitPrice",query.parsedJson());
			final String units = fromJsonHelper.extractStringNamed("units", query.parsedJson());
			BigDecimal itemprice = null;
			BigDecimal totalPrice = null;
			ItemMaster itemMaster = this.itemMasterRepository.findOne(itemId);
		
		    if (unitprice != null) {
				itemprice = unitprice;
			} else {
				itemprice = itemMaster.getUnitPrice();
			}
		    List<ItemData> itemCodeData = this.oneTimeSaleReadPlatformService.retrieveItemData();
			List<DiscountMasterData> discountdata = this.discountReadPlatformService.retrieveAllDiscounts();
			ItemData itemData = this.itemReadPlatformService.retrieveSingleItemDetails(null, itemId,null,false);
			itemData.setUnitPrice(itemprice);
			List<ChargesData> chargesDatas = this.itemReadPlatformService.retrieveChargeCode();

		    if(UnitEnumType.PIECES.toString().equalsIgnoreCase(units)){
		    	final Integer quantity = fromJsonHelper.extractIntegerWithLocaleNamed("quantity",query.parsedJson());
		    	totalPrice = itemprice.multiply(new BigDecimal(quantity));
		    	return new ItemData(itemCodeData, itemData, totalPrice, quantity.toString(), discountdata, chargesDatas, null);
		    }else{
		    	final String quantityValue = fromJsonHelper.extractStringNamed("quantity",query.parsedJson());
		    	totalPrice = itemprice.multiply(new BigDecimal(quantityValue));
		    	return new ItemData(itemCodeData, itemData, totalPrice, quantityValue, discountdata, chargesDatas, null);
		    }
			
			
		} catch (final DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
			return null;

		}
	}

	/* (non-Javadoc)
	 * @see #deleteOneTimeSale(org.mifosplatform.infrastructure.core.api.JsonCommand, java.lang.Long)
	 */
	@Transactional
	@Override
	public CommandProcessingResult deleteOneTimeSale(final Long entityId) {

		OneTimeSale oneTimeSale = null;
		try {
			oneTimeSale = this.findOneById(entityId);
		    if(oneTimeSale.getDeviceMode().equalsIgnoreCase("NEWSALE")&&oneTimeSale.getIsInvoiced()=='Y'){
				ChargeCodeMaster chargeCode=this.chargeCodeRepository.findOneByChargeCode(oneTimeSale.getChargeCode());
				if(oneTimeSale.getInvoiceId()!=null){//check for old onetimesale's
				   Invoice oldInvoice=this.invoiceRepository.findOne(oneTimeSale.getInvoiceId());
				   List<BillingOrder> charge=oldInvoice.getCharges();
				   BigDecimal discountAmount=charge.get(0).getDiscountAmount();
				   //cancel sale calling 
				   OneTimeSale cancelDeviceSale= new OneTimeSale(oneTimeSale.getClientId(), oneTimeSale.getItemId(), oneTimeSale.getUnits(),oneTimeSale.getQuantity(),oneTimeSale.getChargeCode(), oneTimeSale.getUnitPrice(), oneTimeSale.getTotalPrice(), 
			        		                           DateUtils.getLocalDateOfTenant(),oneTimeSale.getDiscountId(),oneTimeSale.getOfficeId(),CodeNameConstants.CODE_CANCEL_SALE,null);
				   this.oneTimeSaleRepository.saveAndFlush(cancelDeviceSale);
				   OneTimeSaleData oneTimeSaleData = new OneTimeSaleData(cancelDeviceSale.getId(),oneTimeSale.getClientId(), oneTimeSale.getUnits(), oneTimeSale.getChargeCode(), 
						                             chargeCode.getChargeType(),oneTimeSale.getUnitPrice(),oneTimeSale.getQuantity(), oneTimeSale.getTotalPrice(), "Y",
						                             oneTimeSale.getItemId(),oneTimeSale.getDiscountId(),chargeCode.getTaxInclusive());
				   CommandProcessingResult invoice=this.invoiceOneTimeSale.reverseInvoiceForOneTimeSale(oneTimeSale.getClientId(),oneTimeSaleData,discountAmount,false);
				   cancelDeviceSale.setIsDeleted('Y');
				   cancelDeviceSale.setInvoiceId(invoice.resourceId());
				   cancelDeviceSale.setIsInvoiced('Y');
				   this.oneTimeSaleRepository.save(cancelDeviceSale);
				  }
			 }
			oneTimeSale.setIsDeleted('Y');
			this.oneTimeSaleRepository.save(oneTimeSale);

		} catch (final DataIntegrityViolationException dve) {
			handleCodeDataIntegrityIssues(null, dve);
		}
		return new CommandProcessingResult(Long.valueOf(oneTimeSale.getId()),
				oneTimeSale.getClientId());
	}

	private OneTimeSale findOneById(final Long saleId) {
	
		try{
			OneTimeSale oneTimeSale=this.oneTimeSaleRepository.findOne(saleId);
			return oneTimeSale;
		}catch(Exception e){
			throw new DeviceSaleNotFoundException(saleId.toString());
		}
	}
}