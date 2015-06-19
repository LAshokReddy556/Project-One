package org.mifosplatform.billing.discountmaster.service;

import java.util.List;

import org.mifosplatform.billing.discountmaster.data.DiscountDetailData;
import org.mifosplatform.billing.discountmaster.data.DiscountMasterData;

/**
 * @author hugo
 * 
 */
public interface DiscountReadPlatformService {

	List<DiscountMasterData> retrieveAllDiscounts();

	DiscountMasterData retrieveSingleDiscountDetail(Long discountId);

	List<DiscountDetailData> retrieveDiscountdetails(Long discountId);

}
