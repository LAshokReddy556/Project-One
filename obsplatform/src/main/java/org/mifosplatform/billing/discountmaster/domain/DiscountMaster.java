package org.mifosplatform.billing.discountmaster.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.logistics.item.domain.ItemPrice;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * @author hugo
 * 
 */
@Entity
@Table(name = "b_discount_master", uniqueConstraints = @UniqueConstraint(name = "discountcode", columnNames = { "discount_code" }))
public class DiscountMaster extends AbstractPersistable<Long> {

	private static final long serialVersionUID = 1L;

	@Column(name = "discount_code")
	private String discountCode;

	@Column(name = "discount_description")
	private String discountDescription;

	@Column(name = "discount_type")
	private String discountType;

	@Column(name = "start_date")
	private Date startDate;

	@Column(name = "discount_rate")
	private BigDecimal discountRate;

	@Column(name = "discount_status")
	private String discountStatus;

	@Column(name = "is_delete")
	private char isDelete = 'N';
	
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "discountMaster", orphanRemoval = true)
	private List<DiscountDetails> discountDetails = new ArrayList<DiscountDetails>();

	public DiscountMaster() {
		// TODO Auto-generated constructor stub

	}

	public DiscountMaster(final String discountCode,
			final String discountDescription, final String discountType,
			final BigDecimal discountRate, final LocalDate startDate,
			final String status) {

		this.discountCode = discountCode;
		this.discountDescription = discountDescription;
		this.discountType = discountType;
		this.discountRate = discountRate;
		this.startDate = startDate.toDate();
		this.discountStatus = status;

	}

	/**
	 * @return the discountCode
	 */
	public String getDiscountCode() {
		return discountCode;
	}

	public void setDiscountCode(final String discountCode) {
		this.discountCode = discountCode;
	}

	/**
	 * @return the discountDescription
	 */
	public String getDiscountDescription() {
		return discountDescription;
	}

	public void setDiscountDescription(final String discountDescription) {
		this.discountDescription = discountDescription;
	}

	/**
	 * @return the discountType
	 */
	public String getDiscountType() {
		return discountType;
	}

	public void setDiscountType(final String discountType) {
		this.discountType = discountType;
	}

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(final Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the discountRate
	 */
	public BigDecimal getDiscountRate() {
		return discountRate;
	}

	public void setDiscountRate(final BigDecimal discountRate) {
		this.discountRate = discountRate;
	}

	/**
	 * @return the discountStatus
	 */
	public String getDiscountStatus() {
		return discountStatus;
	}

	public void setDiscountStatus(final String discountStatus) {
		this.discountStatus = discountStatus;
	}

	/**
	 * @param command
	 * @return DiscountMaster
	 */
	public static DiscountMaster fromJson(final JsonCommand command) {
		
		final String discountCode = command.stringValueOfParameterNamed("discountCode");
		final String discountDescription = command.stringValueOfParameterNamed("discountDescription");
		final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
		final String discountType = command.stringValueOfParameterNamed("discountType");
		final BigDecimal discountRate = command.bigDecimalValueOfParameterNamed("discountRate");
		final String discountStatus = command.stringValueOfParameterNamed("discountStatus");

		return new DiscountMaster(discountCode, discountDescription,discountType, discountRate, startDate, discountStatus);
	}

	/**
	 * @param command
	 * @return changes of discountmaster object
	 */
	public Map<String, Object> update(final JsonCommand command) {
		final Map<String, Object> actualChanges = new ConcurrentHashMap<String, Object>(
				1);
		final String discountCodeParamName = "discountCode";
		if (command.isChangeInStringParameterNamed(discountCodeParamName,this.discountCode)) {
			final String newValue = command.stringValueOfParameterNamed(discountCodeParamName);
			actualChanges.put(discountCodeParamName, newValue);
			this.discountCode = StringUtils.defaultIfEmpty(newValue, null);
		}

		final String descriptionParamName = "discountDescription";
		if (command.isChangeInStringParameterNamed(descriptionParamName,this.discountDescription)) {
			final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
			actualChanges.put(descriptionParamName, newValue);
			this.discountDescription = StringUtils.defaultIfEmpty(newValue,null);
		}

		final String discountTypeParamName = "discountType";
		if (command.isChangeInStringParameterNamed(discountTypeParamName,this.discountType)) {
			final String newValue = command.stringValueOfParameterNamed(discountTypeParamName);
			actualChanges.put(discountTypeParamName, newValue);
			this.discountType = StringUtils.defaultIfEmpty(newValue, null);
		}

		final String startDateParamName = "startDate";
		if (command.isChangeInLocalDateParameterNamed(startDateParamName,new LocalDate(this.startDate))) {
			final LocalDate newValue = command.localDateValueOfParameterNamed(startDateParamName);
			actualChanges.put(startDateParamName, newValue);
			this.startDate = newValue.toDate();
		}

		final String discountRateParamName = "discountRate";
		if (command.isChangeInBigDecimalParameterNamed(discountRateParamName,this.discountRate)) {
			final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(discountRateParamName);
			actualChanges.put(discountRateParamName, newValue);
			this.discountRate = newValue;
		}

		final String statusParamName = "discountStatus";
		if (command.isChangeInStringParameterNamed(statusParamName, this.discountStatus)) {
			final String newValue = command.stringValueOfParameterNamed(statusParamName);
			actualChanges.put(statusParamName, newValue);
			this.discountStatus = newValue;
		}

		return actualChanges;

	}

	/**
	 * updating column 'is_deleted' with 'Y' for delete of discount
	 */
	public void delete() {

		if (this.isDelete == 'N') {
			this.isDelete = 'Y';
			this.discountCode = this.discountCode+"_"+this.getId();
		}
		
		for(DiscountDetails discountDetails: this.discountDetails ){
			discountDetails.delete();
		}

	}

	public void addDetails(DiscountDetails discountDetail) {
         discountDetail.update(this);
         this.discountDetails.add(discountDetail);
		
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public char getIsDelete() {
		return isDelete;
	}

	public List<DiscountDetails> getDiscountDetails() {
		return discountDetails;
	}

	
	
	

}
