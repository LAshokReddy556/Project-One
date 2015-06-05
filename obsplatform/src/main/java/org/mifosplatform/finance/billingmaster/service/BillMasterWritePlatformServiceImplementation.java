package org.mifosplatform.finance.billingmaster.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.finance.adjustment.domain.Adjustment;
import org.mifosplatform.finance.adjustment.domain.AdjustmentRepository;
import org.mifosplatform.finance.billingmaster.domain.BillDetail;
import org.mifosplatform.finance.billingmaster.domain.BillMaster;
import org.mifosplatform.finance.billingmaster.domain.BillMasterRepository;
import org.mifosplatform.finance.billingmaster.serialize.BillMasterCommandFromApiJsonDeserializer;
import org.mifosplatform.finance.billingorder.domain.BillingOrder;
import org.mifosplatform.finance.billingorder.domain.BillingOrderRepository;
import org.mifosplatform.finance.billingorder.domain.Invoice;
import org.mifosplatform.finance.billingorder.domain.InvoiceRepository;
import org.mifosplatform.finance.billingorder.domain.InvoiceTax;
import org.mifosplatform.finance.billingorder.domain.InvoiceTaxRepository;
import org.mifosplatform.finance.billingorder.exceptions.BillingOrderNoRecordsFoundException;
import org.mifosplatform.finance.financialtransaction.data.FinancialTransactionsData;
import org.mifosplatform.finance.payments.domain.Payment;
import org.mifosplatform.finance.payments.domain.PaymentRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.onetimesale.domain.OneTimeSale;
import org.mifosplatform.logistics.onetimesale.domain.OneTimeSaleRepository;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillMasterWritePlatformServiceImplementation implements BillMasterWritePlatformService {
	
	 private final static Logger LOGGER = LoggerFactory.getLogger(BillMasterWritePlatformServiceImplementation.class);
		private final PlatformSecurityContext context;
		private final BillMasterRepository billMasterRepository;
		private final BillMasterReadPlatformService billMasterReadPlatformService;
		private final BillWritePlatformService billWritePlatformService;
	    private final BillMasterCommandFromApiJsonDeserializer  apiJsonDeserializer;
	    private final ClientRepository clientRepository;
		private final BillingOrderRepository billingOrderRepository;
		private final InvoiceTaxRepository invoiceTaxRepository;
		private final InvoiceRepository invoiceRepository;
		private final PaymentRepository paymentRepository;
		private final AdjustmentRepository adjustmentRepository;
		private final OneTimeSaleRepository oneTimeSaleRepository;
		 
	@Autowired
	 public BillMasterWritePlatformServiceImplementation(final PlatformSecurityContext context,final BillMasterRepository billMasterRepository,
				final BillMasterReadPlatformService billMasterReadPlatformService,final BillWritePlatformService billWritePlatformService,
				final BillMasterCommandFromApiJsonDeserializer apiJsonDeserializer,final ClientRepository clientRepository,
				final BillingOrderRepository billingOrderRepository, final InvoiceTaxRepository invoiceTaxRepository,
		        final InvoiceRepository invoiceRepository, final PaymentRepository paymentRepository,
		        final AdjustmentRepository adjustmentRepository,final OneTimeSaleRepository oneTimeSaleRepository){
		
		    this.context = context;
			this.billMasterRepository = billMasterRepository;
			this.clientRepository = clientRepository;
			this.billMasterReadPlatformService = billMasterReadPlatformService;
			this.billWritePlatformService = billWritePlatformService;
			this.apiJsonDeserializer = apiJsonDeserializer;
			this.billingOrderRepository = billingOrderRepository;
			this.invoiceRepository = invoiceRepository;
			this.invoiceTaxRepository = invoiceTaxRepository;
			this.adjustmentRepository = adjustmentRepository;
			this.paymentRepository = paymentRepository;
			this.oneTimeSaleRepository = oneTimeSaleRepository;
			
	}
	
	@Transactional
	@Override
	public CommandProcessingResult createBillMaster(final JsonCommand command, final Long clientId) {
		try{
			Long parentId=null;
			List<FinancialTransactionsData> financialTransactionsDatas = new ArrayList<FinancialTransactionsData>();
			this.apiJsonDeserializer.validateForCreate(command.json());
			financialTransactionsDatas = billMasterReadPlatformService.retrieveFinancialData(clientId);
			if (financialTransactionsDatas.size() == 0) {
				final String msg = "no Bills to Generate";
				throw new BillingOrderNoRecordsFoundException(msg);
			}
			final Client client = this.clientRepository.findOne(clientId);
			if(client.getParentId() != null){
				parentId = client.getParentId();
			}else{
				parentId = clientId;
			}
		
		final BigDecimal previousBal = this.billMasterReadPlatformService.retrieveClientBalance(clientId);
		
		final LocalDate billDate = DateUtils.getLocalDateOfTenant();
		final BigDecimal previousBalance = BigDecimal.ZERO;
		final BigDecimal chargeAmount = BigDecimal.ZERO;
		final BigDecimal adjustmentAmount = BigDecimal.ZERO;
		final BigDecimal taxAmount = BigDecimal.ZERO;
		final BigDecimal paidAmount = BigDecimal.ZERO;
		final BigDecimal dueAmount = BigDecimal.ZERO;
		final LocalDate dueDate = command.localDateValueOfParameterNamed("dueDate");
		final String message = command.stringValueOfParameterNamed("message");
		BillMaster  billMaster = new BillMaster(clientId, clientId,billDate.toDate(), null, null, dueDate.toDate(),previousBalance, 
				                    chargeAmount, adjustmentAmount, taxAmount, paidAmount, dueAmount,null, message, parentId);
		
		List<BillDetail> listOfBillingDetail = new ArrayList<BillDetail>();
		
		for (final FinancialTransactionsData financialTransactionsData : financialTransactionsDatas) {
			
			final BillDetail billDetail = new BillDetail(null, financialTransactionsData.getTransactionId(),financialTransactionsData.getTransDate().toDate(),
					financialTransactionsData.getTransactionType(),financialTransactionsData.getDebitAmount(),financialTransactionsData.getPlanCode(),financialTransactionsData.getDescription());
			
			listOfBillingDetail.add(billDetail);
		    billMaster.addBillDetails(billDetail);
		
		}
	
		billMaster = this.billMasterRepository.saveAndFlush(billMaster);
	
		this.billWritePlatformService.updateBillMaster(listOfBillingDetail, billMaster, previousBal);
		this.updateBillId(financialTransactionsDatas, billMaster.getId());
		
        return new CommandProcessingResultBuilder().withCommandId(command.commandId())
        		     .withClientId(clientId).withEntityId(billMaster.getId()).build();
	}   catch (DataIntegrityViolationException dve) {
		LOGGER.error(dve.getLocalizedMessage());
		 handleCodeDataIntegrityIssues(command, dve);
		return  CommandProcessingResult.empty();
	}
}
	
	public void updateBillId(final List<FinancialTransactionsData> financialTransactionsDatas, final Long billId) {
		
		try{

			for (final FinancialTransactionsData transIds : financialTransactionsDatas) {
				if ("ADJUSTMENT".equalsIgnoreCase(transIds.getTransactionType())) {
					Adjustment adjustment = this.adjustmentRepository.findOne(transIds.getTransactionId());
					adjustment.updateBillId(billId);
					this.adjustmentRepository.save(adjustment);
				}
				else if ("TAXES".equalsIgnoreCase(transIds.getTransactionType())) {
					InvoiceTax tax = this.invoiceTaxRepository.findOne(transIds.getTransactionId());
					tax.updateBillId(billId);
					this.invoiceTaxRepository.save(tax);
				}
				else if (transIds.getTransactionType().contains("PAYMENT")) {
					Payment payment = this.paymentRepository.findOne(transIds.getTransactionId());
					payment.updateBillId(billId);
					this.paymentRepository.save(payment);
				}
				else if ("SERVICE_CHARGES".equalsIgnoreCase(transIds.getTransactionType())) {
					BillingOrder billingOrder = this.billingOrderRepository.findOne(transIds.getTransactionId());
					billingOrder.updateBillId(billId);
					this.billingOrderRepository.save(billingOrder);
					Invoice invoice = this.invoiceRepository.findOne(billingOrder.getInvoice().getId());
					invoice.updateBillId(billId);
					this.invoiceRepository.save(invoice);
				}
				else if ("INVOICE".equalsIgnoreCase(transIds.getTransactionType())) {
					Invoice invoice = this.invoiceRepository.findOne(transIds.getTransactionId());
					invoice.updateBillId(billId);
					this.invoiceRepository.save(invoice);
				}
				else if ("ONETIME_CHARGES".equalsIgnoreCase(transIds.getTransactionType())) {
					BillingOrder billingOrder = this.billingOrderRepository.findOne(transIds.getTransactionId());
					billingOrder.updateBillId(billId);
					this.billingOrderRepository.save(billingOrder);
					Invoice invoice = this.invoiceRepository.findOne(billingOrder.getInvoice().getId());
					invoice.updateBillId(billId);
					this.invoiceRepository.save(invoice);
					OneTimeSale oneTimeSale=this.oneTimeSaleRepository.findOne(billingOrder.getOrderId());
					oneTimeSale.updateBillId(billId);
					this.oneTimeSaleRepository.save(oneTimeSale);
				
				}
				else if ("SERVICE_TRANSFER".equalsIgnoreCase(transIds.getTransactionType())) {
					BillingOrder billingOrder = this.billingOrderRepository.findOne(transIds.getTransactionId());
					billingOrder.updateBillId(billId);
					this.billingOrderRepository.save(billingOrder);
					Invoice invoice = this.invoiceRepository.findOne(billingOrder.getInvoice().getId());
					invoice.updateBillId(billId);
					this.invoiceRepository.save(invoice);
				}

			}
		}catch(Exception ex){
			 LOGGER.error(ex.getLocalizedMessage());
		}
	}

	@Override
	public CommandProcessingResult cancelBillMaster(final Long billId) {
		try{
			context.authenticatedUser();
			List<BillDetail> billingDetails = new ArrayList<BillDetail>();
		
			final BillMaster billMaster = this.billMasterRepository.findOne(billId);
			if(billMaster == null){
				throw  new	BillingOrderNoRecordsFoundException();
			}//Get all billdetails for that billId 
			billingDetails = billMaster.getBillDetails();
			for(final BillDetail billDetail:billingDetails){  
				
				if ("SERVICE_CHARGES".equalsIgnoreCase(billDetail.getTransactionType())) {
					BillingOrder billingOrder = this.billingOrderRepository.findOne(billDetail.getTransactionId());
					billingOrder.updateBillId(null);
					this.billingOrderRepository.save(billingOrder);
					Invoice invoice = this.invoiceRepository.findOne(billingOrder.getInvoice().getId());
					invoice.updateBillId(null);
					this.invoiceRepository.save(invoice);
				} 
				else if("TAXES".equalsIgnoreCase(billDetail.getTransactionType())){
					InvoiceTax tax = this.invoiceTaxRepository.findOne(billDetail.getTransactionId());
					tax.updateBillId(null);
					this.invoiceTaxRepository.save(tax);
				}
				else if("ADJUSTMENT".equalsIgnoreCase(billDetail.getTransactionType())){
					Adjustment adjustment = this.adjustmentRepository.findOne(billDetail.getTransactionId());
					adjustment.updateBillId(null);
					this.adjustmentRepository.save(adjustment);
				}
				else if(billDetail.getTransactionType().contains("PAYMENT")) {
					Payment payment = this.paymentRepository.findOne(billDetail.getTransactionId());
					payment.updateBillId(null);
					this.paymentRepository.save(payment);
				}
				else if ("ONETIME_CHARGES".equalsIgnoreCase(billDetail.getTransactionType())) {
					BillingOrder billingOrder = this.billingOrderRepository.findOne(billDetail.getTransactionId());
					billingOrder.updateBillId(null);
					this.billingOrderRepository.save(billingOrder);
					Invoice invoice = this.invoiceRepository.findOne(billingOrder.getInvoice().getId());
					invoice.updateBillId(null);
					this.invoiceRepository.save(invoice);
					OneTimeSale oneTimeSale=this.oneTimeSaleRepository.findOne(billingOrder.getOrderId());
					oneTimeSale.updateBillId(null);
					this.oneTimeSaleRepository.save(oneTimeSale);
				}
				else if ("SERVICE_TRANSFER".equalsIgnoreCase(billDetail.getTransactionType())) {
					BillingOrder billingOrder = this.billingOrderRepository.findOne(billDetail.getTransactionId());
					billingOrder.updateBillId(null);
					this.billingOrderRepository.save(billingOrder);
					Invoice invoice = this.invoiceRepository.findOne(billingOrder.getInvoice().getId());
					invoice.updateBillId(null);
					this.invoiceRepository.save(invoice);

				}
			}
			
		billMaster.delete();
        this.billMasterRepository.save(billMaster);
		return new CommandProcessingResult(billMaster.getId(), billMaster.getClientId());
	   }catch(DataIntegrityViolationException dve) {
		   LOGGER.error(dve.getLocalizedMessage());
		   return  CommandProcessingResult.empty();
	   }
		
   }
	
	private void handleCodeDataIntegrityIssues(final JsonCommand command,final DataIntegrityViolationException dve) {
		final Throwable realCause = dve.getMostSpecificCause(); 
		if(realCause.getMessage().contains("plan_code"))
		throw new PlatformDataIntegrityException("error.msg.data.truncation.issue",
                "Data truncation: Data too long for column 'plan_code'");
		
	}
	
}	