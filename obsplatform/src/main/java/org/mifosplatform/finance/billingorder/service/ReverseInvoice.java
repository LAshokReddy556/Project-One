package org.mifosplatform.finance.billingorder.service;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.finance.billingorder.commands.BillingOrderCommand;
import org.mifosplatform.finance.billingorder.data.BillingOrderData;
import org.mifosplatform.finance.billingorder.domain.Invoice;
import org.mifosplatform.finance.billingorder.domain.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReverseInvoice {
	
	private final BillingOrderReadPlatformService billingOrderReadPlatformService;
	private final GenerateReverseBillingOrderService generateReverseBillingOrderService;
	private final GenerateBillingOrderService generateBillingOrderService;
	private final BillingOrderWritePlatformService billingOrderWritePlatformService;
	private final InvoiceRepository invoiceRepository;
	
	
	@Autowired
	public ReverseInvoice(final BillingOrderReadPlatformService billingOrderReadPlatformService,final GenerateBillingOrderService generateBillingOrderService,
			final GenerateReverseBillingOrderService generateReverseBillingOrderService,final BillingOrderWritePlatformService billingOrderWritePlatformService,
			final InvoiceRepository invoiceRepository){
		
		this.billingOrderReadPlatformService = billingOrderReadPlatformService;
		this.generateReverseBillingOrderService = generateReverseBillingOrderService;
		this.billingOrderWritePlatformService=billingOrderWritePlatformService;
		this.generateBillingOrderService=generateBillingOrderService;
		this.invoiceRepository = invoiceRepository;
	}
	
	 
	public BigDecimal reverseInvoiceServices(final Long orderId,final Long clientId,final LocalDate disconnectionDate){
		
	    Invoice invoice=null;
	    BigDecimal invoiceAmount=BigDecimal.ZERO;
	   
		List<BillingOrderData> billingOrderProducts = this.billingOrderReadPlatformService.getReverseBillingOrderData(clientId, disconnectionDate, orderId);
		
		List<BillingOrderCommand> billingOrderCommands = this.generateReverseBillingOrderService.generateReverseBillingOrder(billingOrderProducts,disconnectionDate);
		
		if(billingOrderCommands.size() !=0){
			
		if(billingOrderCommands.get(0).getChargeType().equalsIgnoreCase("RC")){
			 invoice = this.generateBillingOrderService. generateInvoice(billingOrderCommands);
			 invoiceAmount=invoice.getInvoiceAmount();
		}else{
			
		invoice = this.generateReverseBillingOrderService.generateNegativeInvoice(billingOrderCommands);
        invoiceAmount=invoice.getInvoiceAmount();
        /*List<Long> invoices = this.billingOrderReadPlatformService.listOfInvoices(clientId, orderId);
        if(!invoices.isEmpty() && invoiceAmount != null && invoiceAmount.intValue() != 0){
        
        	for(Long invoiceIds :invoices){
	        	Long invoiceId = invoiceIds;
	        	Invoice invoiceData = this.invoiceRepository.findOne(invoiceId);
	        	BigDecimal dueAmount = invoiceData.getDueAmount();
	        	if(dueAmount != null && dueAmount.intValue() > 0 && invoiceAmount.intValue() < dueAmount.intValue()){
	        		BigDecimal updateAmount = dueAmount.add(invoiceAmount);
	        		invoiceData.setDueAmount(updateAmount);
	        		this.invoiceRepository.saveAndFlush(invoiceData);
	        	}else if(dueAmount != null && dueAmount.intValue() > 0 && invoiceAmount.intValue() > dueAmount.intValue()){
	        		invoiceData.setDueAmount(BigDecimal.ZERO);
	        		this.invoiceRepository.saveAndFlush(invoiceData);
	        	}
	        }
        }*/
        
		}
		
		//List<ClientBalanceData> clientBalancesDatas = clientBalanceReadPlatformService.retrieveAllClientBalances(clientId);
		
		this.billingOrderWritePlatformService.updateClientBalance(invoice.getInvoiceAmount(),clientId,false);
		
		this.billingOrderWritePlatformService.updateBillingOrder(billingOrderCommands);
	    //this.billingOrderWritePlatformService.updateOrderPrice(billingOrderCommands);
		 
		return invoiceAmount;
	}else{
		return invoiceAmount;
	}
	
	}
}
	
