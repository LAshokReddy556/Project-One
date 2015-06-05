package org.mifosplatform.portfolio.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.loader.custom.Return;
import org.joda.time.LocalDate;
import org.mifosplatform.billing.discountmaster.domain.DiscountMaster;
import org.mifosplatform.billing.discountmaster.domain.DiscountMasterRepository;
import org.mifosplatform.billing.discountmaster.exception.DiscountMasterNotFoundException;
import org.mifosplatform.billing.planprice.data.PriceData;
import org.mifosplatform.infrastructure.configuration.domain.Configuration;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.portfolio.contract.domain.Contract;
import org.mifosplatform.portfolio.contract.domain.ContractRepository;
import org.mifosplatform.portfolio.order.data.OrderStatusEnumaration;
import org.mifosplatform.portfolio.order.domain.Order;
import org.mifosplatform.portfolio.order.domain.OrderDiscount;
import org.mifosplatform.portfolio.order.domain.OrderLine;
import org.mifosplatform.portfolio.order.domain.OrderPrice;
import org.mifosplatform.portfolio.order.domain.StatusTypeEnum;
import org.mifosplatform.portfolio.order.domain.UserActionStatusTypeEnum;
import org.mifosplatform.portfolio.order.exceptions.NoRegionalPriceFound;
import org.mifosplatform.portfolio.plan.data.ServiceData;
import org.mifosplatform.portfolio.plan.domain.Plan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderAssembler {
	
private final OrderDetailsReadPlatformServices orderDetailsReadPlatformServices;
private final ContractRepository contractRepository;
private final ConfigurationRepository configurationRepository;
private final DiscountMasterRepository discountMasterRepository;


@Autowired
public OrderAssembler(final OrderDetailsReadPlatformServices orderDetailsReadPlatformServices,final ContractRepository contractRepository,
		   final DiscountMasterRepository discountMasterRepository,final ConfigurationRepository configurationRepository){
	
	this.orderDetailsReadPlatformServices=orderDetailsReadPlatformServices;
	this.contractRepository=contractRepository;
	this.discountMasterRepository=discountMasterRepository;
	this.configurationRepository = configurationRepository;
	
}

	public Order assembleOrderDetails(JsonCommand command, Long clientId, Plan plan) {
		
		List<OrderLine> serviceDetails = new ArrayList<OrderLine>();
		List<OrderPrice> orderprice = new ArrayList<OrderPrice>();
		List<PriceData> datas = new ArrayList<PriceData>();
		Long orderStatus=null;
		LocalDate endDate = null;
        Order order=Order.fromJson(clientId, command);
			List<ServiceData> details =this.orderDetailsReadPlatformServices.retrieveAllServices(order.getPlanId());
			datas=this.orderDetailsReadPlatformServices.retrieveAllPrices(order.getPlanId(),order.getBillingFrequency(),clientId);
			/*if(datas.isEmpty()){
				datas=this.orderDetailsReadPlatformServices.retrieveDefaultPrices(order.getPlanId(),order.getBillingFrequency(),clientId);
			}*/
			if(datas.isEmpty()){
				throw new NoRegionalPriceFound();
			}
			
			
			Contract contractData = this.contractRepository.findOne(order.getContarctPeriod());
			LocalDate startDate=new LocalDate(order.getStartDate());
			
			if(plan.getProvisionSystem().equalsIgnoreCase("None")){
				orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.ACTIVE).getId();

			}else{
			orderStatus = OrderStatusEnumaration.OrderStatusType(StatusTypeEnum.PENDING).getId();
			}

			//Calculate EndDate
			endDate = calculateEndDate(startDate,contractData.getSubscriptionType(),contractData.getUnits());
			
			order=new Order(order.getClientId(),order.getPlanId(),orderStatus,null,order.getBillingFrequency(),startDate, endDate,
					 order.getContarctPeriod(), serviceDetails, orderprice,order.getbillAlign(),
					 UserActionStatusTypeEnum.ACTIVATION.toString(),plan.isPrepaid(),order.isAutoRenewal());
			

	Configuration configuration = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_ALIGN_BIILING_CYCLE);

			
			if(configuration != null && plan.isPrepaid() == 'N'){
				order.setBillingAlign(configuration.isEnabled()?'Y':'N');
				if(configuration.isEnabled() && endDate != null){
				order.setEndDate(endDate.dayOfMonth().withMaximumValue());
				}
			}
			BigDecimal priceforHistory=BigDecimal.ZERO;

			for (PriceData data : datas) {
				LocalDate billstartDate = startDate;
				LocalDate billEndDate = null;

				//end date is null for rc
				if (data.getChagreType().equalsIgnoreCase("RC")	&& endDate != null) {
					billEndDate = new LocalDate(order.getEndDate());
				} else if(data.getChagreType().equalsIgnoreCase("NRC")) {
					billEndDate = billstartDate;
				}
				
				final DiscountMaster discountMaster=this.discountMasterRepository.findOne(data.getDiscountId());
				if(discountMaster == null){
					throw new DiscountMasterNotFoundException();
				}
				
				//	If serviceId Not Exist
				OrderPrice price = new OrderPrice(data.getServiceId(),data.getChargeCode(), data.getChargingVariant(),data.getPrice(), 
						null, data.getChagreType(),
			    data.getChargeDuration(), data.getDurationType(),billstartDate.toDate(), billEndDate,data.isTaxInclusive());
				order.addOrderDeatils(price);
				priceforHistory=priceforHistory.add(data.getPrice());
				
				//discount Order
				OrderDiscount orderDiscount=new OrderDiscount(order,price,discountMaster.getId(),discountMaster.getStartDate(),null,discountMaster.getDiscountType(),
						discountMaster.getDiscountRate());
				//price.addOrderDiscount(orderDiscount);
				order.addOrderDiscount(orderDiscount);
			}
			
			for (ServiceData data : details) {
				OrderLine orderdetails = new OrderLine(data.getPlanId(),data.getServiceType(), plan.getStatus(), 'n');
				order.addServiceDeatils(orderdetails);
			}
			
		  return order;
	
	}
	

    //Calculate EndDate
	public LocalDate calculateEndDate(LocalDate startDate,String durationType,Long duration) {

			LocalDate contractEndDate = null;
			 		if (durationType.equalsIgnoreCase("DAY(s)")) {
			 			contractEndDate = startDate.plusDays(duration.intValue() - 1);
			 		} else if (durationType.equalsIgnoreCase("MONTH(s)")) {
			 			contractEndDate = startDate.plusMonths(duration.intValue()).minusDays(1);
			 		} else if (durationType.equalsIgnoreCase("YEAR(s)")) {
			 		contractEndDate = startDate.plusYears(duration.intValue()).minusDays(1);
			 		} else if (durationType.equalsIgnoreCase("week(s)")) {
			 		contractEndDate = startDate.plusWeeks(duration.intValue()).minusDays(1);
			 		}
			 	return contractEndDate;
			}

	
	public Order setDatesOnOrderActivation(Order order, LocalDate startDate) {
		
		Contract contract = this.contractRepository.findOne(order.getContarctPeriod());
	    LocalDate endDate = this.calculateEndDate(startDate, contract.getSubscriptionType(), contract.getUnits());
	    order.setStartDate(startDate);
	    if(order.getbillAlign() == 'Y'){
	    	order.setEndDate(endDate.dayOfMonth().withMaximumValue());
		}else{
			order.setEndDate(endDate);
		}

			for (OrderPrice orderPrice: order.getPrice()) {
				LocalDate billstartDate = startDate;
				
				orderPrice.setBillStartDate(billstartDate);
				//end date is null for rc
				if (orderPrice.getChargeType().equalsIgnoreCase("RC")	&& endDate != null) {
					orderPrice.setBillEndDate(new LocalDate(order.getEndDate()));
				} else if(orderPrice.getChargeType().equalsIgnoreCase("NRC")) {
					orderPrice.setBillEndDate(billstartDate);
				}
	}
			return order;
	}
}
