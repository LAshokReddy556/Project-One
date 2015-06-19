package org.mifosplatform.billing.discountmaster.data;

import java.math.BigDecimal;

public class DiscountDetailData {
	
	private final  Long id;
	private final String categoryType;
	private final Long categoryId;
	private final BigDecimal discountRate;

	public DiscountDetailData(Long id, String categoryType,Long categoryTypeId, BigDecimal discountRate) {
             
		this.id=id;
		this.categoryType = categoryType;
		this.categoryId = categoryTypeId;
		this.discountRate = discountRate;
	
	}

	public Long getId() {
		return id;
	}

	public String getCategoryType() {
		return categoryType;
	}

	public Long getCategoryTypeId() {
		return categoryId;
	}

	public BigDecimal getDiscountRate() {
		return discountRate;
	}
	
	

}
