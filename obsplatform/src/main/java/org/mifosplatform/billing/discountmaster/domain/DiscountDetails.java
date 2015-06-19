package org.mifosplatform.billing.discountmaster.domain;


import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "b_discount_details", uniqueConstraints = { @UniqueConstraint(columnNames = { "discount_id", "category_type"}, name = "discountid_with_category_uniquekey") })
public class DiscountDetails extends AbstractPersistable<Long>{

	@Column(name = "category_type")
	private String categoryType;

	@Column(name = "discount_rate")
	private BigDecimal discountRate;
	
	@Column(name = "is_deleted")
	private String isDeleted="N";
	
	@ManyToOne
    @JoinColumn(name="discount_id")
	private DiscountMaster discountMaster;

	
	public DiscountDetails(){}

	public DiscountDetails(String categoryId, BigDecimal discountRate) {
            
		this.categoryType = categoryId;
		this.discountRate = discountRate;
		
	
	}

	public void update(DiscountMaster discountMaster) {
            this.discountMaster= discountMaster;
	}

	public void delete() {

		  this.isDeleted = "Y";
		  this.categoryType = this.getId()+"_"+this.categoryType+"_Y";
		
	}

	public String getCategoryType() {
		return categoryType;
	}

	public BigDecimal getDiscountRate() {
		return discountRate;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public DiscountMaster getDiscountMaster() {
		return discountMaster;
	}

	public void setCategoryType(String categoryType) {
		this.categoryType = categoryType;
	}

	public void setDiscountRate(BigDecimal discountRate) {
		this.discountRate = discountRate;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}

	public void setDiscountMaster(DiscountMaster discountMaster) {
		this.discountMaster = discountMaster;
	}

	
	

}