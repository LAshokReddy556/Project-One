package org.mifosplatform.billing.discountmaster.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;

/**
 * @author hugo
 * 
 */
public class DiscountMasterData {
	private Long id;
	private String discountCode;
	private String discountDescription;
	private String discountType;
	private BigDecimal discountRate;
	private LocalDate discountStartDate;
	private LocalDate discountEndDate;
	private String isDeleted;
	private Long orderPriceId;
	private Long orderDiscountId;
	private BigDecimal discountAmount;
	private BigDecimal discountedChargeAmount;
	private List<EnumOptionData> statusData;
	private Collection<MCodeData> discountTypeData;
	private String discountStatus;
	private Collection<MCodeData> clientCategoryDatas;
	private List<DiscountDetailData> discountDetailDatas;

	public DiscountMasterData(final Long id, final String discountCode,final String discountDescription, final String discountType,
			final BigDecimal discountRate, final LocalDate startDate,final String discountStatus) {
		
		this.id = id;
		this.discountCode = discountCode;
		this.discountDescription = discountDescription;
		this.discountType = discountType;
		this.discountRate = discountRate;
		this.discountStartDate = startDate;
		this.discountStatus = discountStatus;

	}
	
	public DiscountMasterData(final Long id, final String discountCode,final String discountDescription, final String discountType,
			final BigDecimal discountRate, final LocalDate startDate,final String discountStatus,final BigDecimal discountAmount) {
		this.id = id;
		this.discountCode = discountCode;
		this.discountDescription = discountDescription;
		this.discountType = discountType;
		this.discountRate = discountRate;
		this.discountStartDate = startDate;
		this.discountStatus = discountStatus;
		this.discountAmount = discountAmount;

	}

	public DiscountMasterData(final Long id, final Long orderPriceId,final Long orderDiscountId, final LocalDate discountStartDate,
			final LocalDate discountEndDate, final String discountType,final BigDecimal discountRate, final String isDeleted) {
		this.id = id;
		this.orderPriceId = orderPriceId;
		this.orderDiscountId = orderDiscountId;
		this.discountStartDate = discountStartDate;
		this.discountEndDate = discountEndDate;
		this.discountType = discountType;
		this.discountRate = discountRate;
		this.isDeleted = isDeleted;
		this.discountAmount = BigDecimal.ZERO;
		this.discountedChargeAmount = BigDecimal.ZERO;
	}

	public DiscountMasterData(final List<EnumOptionData> statusData,final Collection<MCodeData> discountTypeData, 
			Collection<MCodeData> clientCategoryDatas) {
		
		this.statusData = statusData;
		this.discountTypeData = discountTypeData;
		this.clientCategoryDatas = clientCategoryDatas;

	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getDiscountCode() {
		return discountCode;
	}

	public void setDiscountCode(final String discountCode) {
		this.discountCode = discountCode;
	}

	public String getDiscountDescription() {
		return discountDescription;
	}

	public void setDiscountDescription(final String discountDescription) {
		this.discountDescription = discountDescription;
	}

	public String getDiscountType() {
		return discountType;
	}

	public void setDiscountType(final String discountType) {
		this.discountType = discountType;
	}

	public LocalDate getDiscountStartDate() {
		return discountStartDate;
	}

	public void setDiscountStartDate(final LocalDate discountStartDate) {
		this.discountStartDate = discountStartDate;
	}

	public LocalDate getDiscountEndDate() {
		return discountEndDate;
	}

	public void setDiscountEndDate(LocalDate discountEndDate) {
		this.discountEndDate = discountEndDate;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Long getOrderPriceId() {
		return orderPriceId;
	}

	public void setOrderPriceId(final Long orderPriceId) {
		this.orderPriceId = orderPriceId;
	}

	public Long getOrderDiscountId() {
		return orderDiscountId;
	}

	public void setOrderDiscountId(final Long orderDiscountId) {
		this.orderDiscountId = orderDiscountId;
	}

	public BigDecimal getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(final BigDecimal discountAmount) {
		this.discountAmount = discountAmount;
	}

	public BigDecimal getDiscountRate() {
		return discountRate;
	}

	public void setDiscountRate(final BigDecimal discountRate) {
		this.discountRate = discountRate;
	}

	public List<EnumOptionData> getStatusData() {
		return statusData;
	}

	public void setStatusData(final List<EnumOptionData> statusData) {
		this.statusData = statusData;
	}

	public Collection<MCodeData> getDiscounTypeData() {
		return discountTypeData;
	}

	public void setDiscountTypeData(final Collection<MCodeData> discountTypeData) {
		this.discountTypeData = discountTypeData;
	}

	public String getDiscountStatus() {
		return discountStatus;
	}

	public void setDiscountStatus(final String discountStatus) {
		this.discountStatus = discountStatus;
	}

	public BigDecimal getDiscountedChargeAmount() {
		return discountedChargeAmount;
	}

	public void setDiscountedChargeAmount(BigDecimal discountedChargeAmount) {
		this.discountedChargeAmount = discountedChargeAmount;
	}

	public void setclientCategoryData(Collection<MCodeData> clientCategoryDatas) {

		this.clientCategoryDatas = clientCategoryDatas;
	}

	public void setDiscountDetailsData(List<DiscountDetailData> discountDetailDatas) {

		this.discountDetailDatas = discountDetailDatas;
	}

}
