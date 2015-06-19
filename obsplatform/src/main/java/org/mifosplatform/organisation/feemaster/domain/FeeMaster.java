package org.mifosplatform.organisation.feemaster.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.logistics.item.exception.ItemNotFoundException;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "b_fee_master", uniqueConstraints = { @UniqueConstraint(columnNames = { "fee_code" }, name = "fee_code") })
public class FeeMaster extends AbstractPersistable<Long>{


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "fee_code")
	private String feeCode;

	@Column(name = "fee_description")
	private String feeDescription;
	
	@Column(name = "transaction_type")
	private String transactionType;
	
	@Column(name = "charge_code")
	private String chargeCode;
	
	@Column(name = "default_fee_amount")
	private BigDecimal defaultFeeAmount;
	
	@Column(name = "is_deleted", nullable = false)
	private char deleted = 'N';
	
	@Column(name = "item_id")
	private Long itemId;
	
	@Column(name = "is_refundable")
	private String isRefundable;
	
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "feeMaster", orphanRemoval = true)
	private List<FeeDetail> regionPrices = new ArrayList<FeeDetail>();
	
	public FeeMaster(){}
	
	public FeeMaster(final String feeCode, final String feeDescription,final String transactionType,
						final String chargeCode, final BigDecimal defaultFeeAmount, final Long itemId, final String isRefundable) {
             this.feeCode=feeCode;
             this.feeDescription=feeDescription;
             this.transactionType=transactionType;
             this.chargeCode=chargeCode;
             this.defaultFeeAmount=defaultFeeAmount;
             this.itemId = itemId;
             this.isRefundable = isRefundable;
	}

	public String getFeeCode() {
		return feeCode;
	}

	public void setFeeCode(String feeCode) {
		this.feeCode = feeCode;
	}

	public String getFeeDescription() {
		return feeDescription;
	}

	public void setFeeDescription(String feeDescription) {
		this.feeDescription = feeDescription;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getChargeCode() {
		return chargeCode;
	}

	public void setChargeCode(String chargeCode) {
		this.chargeCode = chargeCode;
	}

	public BigDecimal getDefaultFeeAmount() {
		return defaultFeeAmount;
	}

	public void setDefaultFeeAmount(BigDecimal defaultFeeAmount) {
		this.defaultFeeAmount = defaultFeeAmount;
	}

	public char getDeleted() {
		return deleted;
	}

	public void setDeleted(char deleted) {
		this.deleted = deleted;
	}

	public Map<String, Object> update(JsonCommand command){
		if("Y".equals(deleted)){
			throw new ItemNotFoundException(command.entityId().toString());
		}
		
		final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		
		final String itemCodeParamName = "feeCode";
		if(command.isChangeInStringParameterNamed(itemCodeParamName, this.feeCode)){
			final String newValue = command.stringValueOfParameterNamed(itemCodeParamName);
			actualChanges.put(itemCodeParamName, newValue);
			this.feeCode = StringUtils.defaultIfEmpty(newValue,null);
		}
		final String itemDescriptionParamName = "feeDescription";
		if(command.isChangeInStringParameterNamed(itemDescriptionParamName, this.feeDescription)){
			final String newValue = command.stringValueOfParameterNamed(itemDescriptionParamName);
			actualChanges.put(itemDescriptionParamName, newValue);
			this.feeDescription = StringUtils.defaultIfEmpty(newValue, null);
		}
		
		final String itemClassParamName = "transactionType";
		if(command.isChangeInStringParameterNamed(itemClassParamName,this.transactionType)){
			final String newValue = command.stringValueOfParameterNamed(itemClassParamName);
			actualChanges.put(itemClassParamName, newValue);
			this.transactionType =StringUtils.defaultIfEmpty(newValue,null);
		}
		
		final String chargeCodeParamName = "chargeCode";
		if(command.isChangeInStringParameterNamed(chargeCodeParamName,this.chargeCode)){
			final String newValue = command.stringValueOfParameterNamed(chargeCodeParamName);
			actualChanges.put(chargeCodeParamName, newValue);
			this.chargeCode = StringUtils.defaultIfEmpty(newValue,null);
		}
		
		final String unitPriceParamName = "defaultFeeAmount";
		if(command.isChangeInBigDecimalParameterNamed(unitPriceParamName, this.defaultFeeAmount)){
			final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(unitPriceParamName);
			actualChanges.put(unitPriceParamName,newValue);
			this.defaultFeeAmount = newValue;
		}
		
		final String itemIdParamName = "itemId";
		if(command.hasParameter(itemIdParamName)){
			if(command.isChangeInLongParameterNamed(itemIdParamName, this.itemId)){
				final Long newValue = command.longValueOfParameterNamed(itemIdParamName);
				actualChanges.put(itemIdParamName,newValue);
				this.itemId = newValue;
			}
		}else{
			this.itemId = null;
		}
		
		
		final String isRefundableParamName = "isRefundable";
		if(command.hasParameter(isRefundableParamName)){
			if(command.isChangeInStringParameterNamed(isRefundableParamName, this.isRefundable)){
				final String newValue = command.stringValueOfParameterNamed(isRefundableParamName);
				actualChanges.put(isRefundableParamName,newValue);
				this.isRefundable = newValue;
			}
		}else{
			this.isRefundable = null;
		}
		
		
		return actualChanges;
	
	}
	
	

	public void delete() {
		this.deleted='Y';
		
	}

	public static FeeMaster fromJson(JsonCommand command) {
		final String feeCode=command.stringValueOfParameterNamed("feeCode");
		final String feeDescription=command.stringValueOfParameterNamed("feeDescription");
		final String transactionType=command.stringValueOfParameterNamed("transactionType");
		final String chargeCode=command.stringValueOfParameterNamed("chargeCode");
		final BigDecimal defaultFeeAmount=command.bigDecimalValueOfParameterNamed("defaultFeeAmount");
		final Long itemId = command.longValueOfParameterNamed("itemId");
		final String isRefundable = command.stringValueOfParameterNamed("isRefundable");
		//final char isRefundable = command.booleanObjectValueOfParameterNamed("isRefundable")?'Y':'N';
		return new FeeMaster(feeCode, feeDescription, transactionType, chargeCode, defaultFeeAmount,itemId,isRefundable);
	}
	
	public void addRegionPrices(final FeeDetail feeDetail) {
		feeDetail.update(this);
        this.regionPrices.add(feeDetail);
	}

	public List<FeeDetail> getRegionPrices() {
		return regionPrices;
	}

	public void setRegionPrices(List<FeeDetail> feeDetail) {
		this.regionPrices = feeDetail;
	}
	
	

}
