package org.mifosplatform.finance.billingorder.service;

import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.billing.discountmaster.data.DiscountMasterData;
import org.mifosplatform.billing.taxmaster.data.TaxMappingRateData;
import org.mifosplatform.finance.billingorder.data.BillingOrderData;
import org.mifosplatform.organisation.partneragreement.data.AgreementData;

public interface BillingOrderReadPlatformService {

	//List<OrderPriceData> retrieveInvoiceTillDate(Long clientOrderId);
	 
	//List<GenerateInvoiceData> retrieveClientsWithOrders(LocalDate processDate);
	
	List<BillingOrderData> retrieveOrderIds(Long clientId, LocalDate processDate);

	List<BillingOrderData> retrieveBillingOrderData(Long clientId,LocalDate localDate, Long planId);

	List<DiscountMasterData> retrieveDiscountOrders(Long orderId,Long orderPriceId);
	
	List<TaxMappingRateData> retrieveTaxMappingData(Long clientId, String chargeCode);

	List<TaxMappingRateData> retrieveDefaultTaxMappingData(Long clientId,String chargeCode);

	List<BillingOrderData> getReverseBillingOrderData(Long clientId,LocalDate disconnectionDate, Long orderId);

	TaxMappingRateData retriveExemptionTaxDetails(Long clientId);

	AgreementData retriveClientOfficeDetails(Long clientId);

	AgreementData retrieveOfficeChargesCommission(Long id);

	List<Long> listOfInvoices(Long clientId, Long orderId);

}
