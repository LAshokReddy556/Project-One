package org.mifosplatform.finance.billingorder.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.billing.discountmaster.domain.DiscountMaster;
import org.mifosplatform.billing.discountmaster.data.DiscountMasterData;
import org.mifosplatform.billing.discountmaster.domain.DiscountMasterRepository;
import org.mifosplatform.billing.taxmaster.data.TaxMappingRateData;
import org.mifosplatform.finance.billingorder.commands.BillingOrderCommand;
import org.mifosplatform.finance.billingorder.commands.InvoiceTaxCommand;
import org.mifosplatform.finance.billingorder.data.BillingOrderData;
import org.mifosplatform.finance.billingorder.domain.BillingOrder;
import org.mifosplatform.finance.billingorder.domain.Invoice;
import org.mifosplatform.finance.billingorder.domain.InvoiceRepository;
import org.mifosplatform.finance.billingorder.domain.InvoiceTax;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hugo
 *
 */
@Service
public class GenerateReverseBillingOrderServiceImp implements GenerateReverseBillingOrderService {

	private final BillingOrderReadPlatformService billingOrderReadPlatformService;
	private final GenerateDisconnectionBill generateDisconnectionBill;
	private final InvoiceRepository invoiceRepository;
	private final DiscountMasterRepository discountMasterRepository;

	
	@Autowired
	public GenerateReverseBillingOrderServiceImp(final BillingOrderReadPlatformService billingOrderReadPlatformService,
			final GenerateDisconnectionBill generateDisconnectionBill,final InvoiceRepository invoiceRepository,
			final DiscountMasterRepository discountMasterRepository) {

		this.billingOrderReadPlatformService = billingOrderReadPlatformService;
		this.generateDisconnectionBill = generateDisconnectionBill;
		this.invoiceRepository = invoiceRepository;
		this.discountMasterRepository = discountMasterRepository;
	}

	@Override
	public List<BillingOrderCommand> generateReverseBillingOrder(final List<BillingOrderData> billingOrderProducts,final LocalDate disconnectDate) {

		BillingOrderCommand billingOrderCommand = null;
		List<BillingOrderCommand> billingOrderCommands = new ArrayList<BillingOrderCommand>();
		
		if (billingOrderProducts.size() != 0) {

			for (BillingOrderData billingOrderData : billingOrderProducts) {

				DiscountMasterData discountMasterData = null;

		      List<DiscountMasterData> discountMasterDatas = billingOrderReadPlatformService.retrieveDiscountOrders(billingOrderData.getClientOrderId(),
		    		                                               billingOrderData.getOderPriceId());
				if (discountMasterDatas.size() != 0) {
					discountMasterData = discountMasterDatas.get(0);
				}

				if (generateDisconnectionBill.isChargeTypeRC(billingOrderData)) {

					System.out.println("---- RC ----");

					// monthly
					if (billingOrderData.getDurationType().equalsIgnoreCase("month(s)")) {

						billingOrderCommand = generateDisconnectionBill.getReverseMonthyBill(billingOrderData,discountMasterData, disconnectDate);
						billingOrderCommands.add(billingOrderCommand);

					}
					// weekly	
					else if (billingOrderData.getDurationType().equalsIgnoreCase("week(s)")) {

						billingOrderCommand = generateDisconnectionBill.getReverseWeeklyBill(billingOrderData,discountMasterData,disconnectDate);
						billingOrderCommands.add(billingOrderCommand);
				 }
				}
			}

		}

		return billingOrderCommands;
	}

	@Override
	public Invoice generateNegativeInvoice(final List<BillingOrderCommand> billingOrderCommands) {
		
		BigDecimal invoiceAmount = BigDecimal.ZERO;
		BigDecimal totalChargeAmount = BigDecimal.ZERO;
		BigDecimal netTaxAmount = BigDecimal.ZERO;
		
		
		TaxMappingRateData tax=this.billingOrderReadPlatformService.retriveExemptionTaxDetails(billingOrderCommands.get(0).getClientId());
		
		Invoice invoice = new Invoice(billingOrderCommands.get(0).getClientId(), DateUtils.getLocalDateOfTenant().toDate(), invoiceAmount, invoiceAmount, 
				                         netTaxAmount, "active");
		
		for (BillingOrderCommand billingOrderCommand : billingOrderCommands) {
			
			BigDecimal netChargeTaxAmount = BigDecimal.ZERO;
			String discountCode="None";
			BigDecimal discountAmount = BigDecimal.ZERO;
		    BigDecimal netChargeAmount = billingOrderCommand.getPrice();
			DiscountMaster discountMaster = null;
			if(billingOrderCommand.getDiscountMasterData()!= null){
				discountMaster = this.discountMasterRepository.findOne(billingOrderCommand.getDiscountMasterData().getId());
				 discountCode=discountMaster.getDiscountCode();
				 discountAmount = billingOrderCommand.getDiscountMasterData().getDiscountAmount();
				 if(billingOrderCommand.getChargeType().equalsIgnoreCase("NRC")){
				  netChargeAmount = billingOrderCommand.getPrice().subtract(discountAmount);
				 }

		
			}
			
			List<InvoiceTaxCommand> invoiceTaxCommands = billingOrderCommand.getListOfTax();

			BillingOrder charge = new BillingOrder(billingOrderCommand.getClientId(), billingOrderCommand.getClientOrderId(), billingOrderCommand.getOrderPriceId(),

					billingOrderCommand.getChargeCode(),billingOrderCommand.getChargeType(),discountCode, billingOrderCommand.getPrice().negate(), discountAmount.negate(),
					netChargeAmount.negate(), billingOrderCommand.getStartDate(), billingOrderCommand.getEndDate());

			//client taxExemption
			if(tax.getTaxExemption().equalsIgnoreCase("N") && (invoiceTaxCommands !=null && !invoiceTaxCommands.isEmpty())){
			
			     for(InvoiceTaxCommand invoiceTaxCommand : invoiceTaxCommands){
				
				     netChargeTaxAmount = netChargeTaxAmount.add(invoiceTaxCommand.getTaxAmount());
				
				     InvoiceTax invoiceTax = new InvoiceTax(invoice, charge, invoiceTaxCommand.getTaxCode(),invoiceTaxCommand.getTaxValue(), 
						                  invoiceTaxCommand.getTaxPercentage(), invoiceTaxCommand.getTaxAmount().negate());
				      charge.addChargeTaxes(invoiceTax);
			     }


			     if (billingOrderCommand.getTaxInclusive() != null) {
						
						if (isTaxInclusive(billingOrderCommand.getTaxInclusive())&&invoiceTaxCommands.get(0).getTaxAmount().compareTo(BigDecimal.ZERO) > 0) {
							netChargeAmount = netChargeAmount.subtract(netChargeTaxAmount);
							charge.setNetChargeAmount(netChargeAmount.negate());
							charge.setChargeAmount(netChargeAmount.negate());
						}
					}

			}
			netTaxAmount = netTaxAmount.add(netChargeTaxAmount);
			totalChargeAmount = totalChargeAmount.add(netChargeAmount);
			invoice.addCharges(charge);		
			
		 }

		invoiceAmount = totalChargeAmount.add(netTaxAmount);
		invoice.setNetChargeAmount(totalChargeAmount.negate());
		invoice.setTaxAmount(netTaxAmount.negate());
		invoice.setInvoiceAmount(invoiceAmount.negate());
		return this.invoiceRepository.save(invoice);
	}
	
	public Boolean isTaxInclusive(Integer taxInclusive){
		
		Boolean isTaxInclusive = false;
		if(taxInclusive == 1){ isTaxInclusive = true;}
		return isTaxInclusive;
	}
}
