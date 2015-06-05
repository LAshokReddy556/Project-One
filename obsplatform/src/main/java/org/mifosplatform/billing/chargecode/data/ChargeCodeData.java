package org.mifosplatform.billing.chargecode.data;

import java.math.BigDecimal;
import java.util.List;


public class ChargeCodeData {

	private Long id;
	private String chargeCode;
	private String chargeDescription;
	private String chargeType;
	private Integer chargeDuration;
	private String durationType;
	private Integer taxInclusive;
	private String billFrequencyCode;
	private String contractType;
	private Integer units;

	private List<ChargeCodeData> chargeCodeData;
	private List<ChargeTypeData> chargeTypeData;
	private List<DurationTypeData> durationTypeData;
	private List<BillFrequencyCodeData> billFrequencyCodeData;
	
	private BigDecimal price;
	private Integer contractDuration;
	private BigDecimal finalAmount;


	public ChargeCodeData() {
	}

	public ChargeCodeData(final List<ChargeCodeData> chargeCodeData,final List<ChargeTypeData> chargeType,final List<DurationTypeData> durationType,
			final List<BillFrequencyCodeData> billFrequencyCodeData) {
		
		this.chargeCodeData = chargeCodeData;
		this.chargeTypeData = chargeType;
		this.durationTypeData = durationType;
		this.billFrequencyCodeData = billFrequencyCodeData;

	}

	public ChargeCodeData(final String chargeCode, final String chargeDescription) {
		this.chargeCode = chargeCode;
		this.chargeDescription = chargeDescription;
	}

	public ChargeCodeData(Long id, String chargeCode, String chargeDescription,
			String chargeType, Integer chargeDuration, String durationType,
			Integer taxInclusive, String billFrequencyCode) {
		this.id = id;
		this.chargeCode = chargeCode;
		this.chargeDescription = chargeDescription;
		this.chargeType = chargeType;
		this.chargeDuration = chargeDuration;
		this.durationType = durationType;
		this.taxInclusive = taxInclusive;
		this.billFrequencyCode = billFrequencyCode;
	}
	
	public ChargeCodeData(Long id, String chargeCode, String chargeDescription,
			String chargeType, Integer chargeDuration, String durationType,
			Integer taxInclusive, String billFrequencyCode,String contractType ,Integer units ) {
		this.id = id;
		this.chargeCode = chargeCode;
		this.chargeDescription = chargeDescription;
		this.chargeType = chargeType;
		this.chargeDuration = chargeDuration;
		this.durationType = durationType;
		this.taxInclusive = taxInclusive;
		this.billFrequencyCode = billFrequencyCode;
		this.contractType = contractType;
		this.units = units;
	}

	public ChargeCodeData(Long id, String contractType,Integer contractDuration, String chargeType,Integer chargeDuration, BigDecimal price) {
             
		this.id = id;
		this.contractType = contractType;
		this.contractDuration = contractDuration;
		this.chargeType = chargeType;
		this.chargeDuration = chargeDuration;
		this.price = price;
	
	}

	/**
	 * @return the chargeCode
	 */
	public String getChargeCode() {
		return chargeCode;
	}

	public void setChargeCode(String chargeCode) {
		this.chargeCode = chargeCode;
	}

	/**
	 * @return the chargeDescription
	 */
	public String getChargeDescription() {
		return chargeDescription;
	}

	public void setChargeDescription(String chargeDescription) {
		this.chargeDescription = chargeDescription;
	}

	/**
	 * @return the chargeType
	 */
	public String getChargeType() {
		return chargeType;
	}

	public void setChargeType(String chargeType) {
		this.chargeType = chargeType;
	}

	/**
	 * @return the chargeDuration
	 */
	public Integer getchargeDuration() {
		return chargeDuration;
	}

	public void setchargeDuration(Integer chargeDuration) {
		this.chargeDuration = chargeDuration;
	}

	/**
	 * @return the durationType
	 */
	public String getDurationType() {
		return durationType;
	}
	
	public void setDurationType(String durationType) {
		this.durationType = durationType;
	}

	/**
	 * @return the taxInclusive
	 */
	public Integer getTaxInclusive() {
		return taxInclusive;
	}

	public void setTaxInclusive(Integer taxInclusive) {
		this.taxInclusive = taxInclusive;
	}

	/**
	 * @return the billFrequencyCode
	 */
	public String getBillFrequencyCode() {
		return billFrequencyCode;
	}

	public void setBillFrequencyCode(String billFrequencyCode) {
		this.billFrequencyCode = billFrequencyCode;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the chargeDuration
	 */
	public Integer getChargeDuration() {
		return chargeDuration;
	}

	public void setChargeDuration(Integer chargeDuration) {
		this.chargeDuration = chargeDuration;
	}

	/**
	 * @return the chargeCodeData
	 */
	public List<ChargeCodeData> getChargeCodeData() {
		return chargeCodeData;
	}

	public void setChargeCodeData(List<ChargeCodeData> chargeCodeData) {
		this.chargeCodeData = chargeCodeData;
	}

	/**
	 * @return the chargeTypeData
	 */
	public List<ChargeTypeData> getChargeTypeData() {
		return chargeTypeData;
	}


	public void setChargeTypeData(final List<ChargeTypeData> chargeTypeData) {
		this.chargeTypeData = chargeTypeData;
	}

	/**
	 * @return the durationTypeData
	 */
	public List<DurationTypeData> getDurationTypeData() {
		return durationTypeData;
	}

	public void setDurationTypeData(final List<DurationTypeData> durationTypeData) {
		this.durationTypeData = durationTypeData;
	}

	/**
	 * @return the billFrequencyData
	 */
	public List<BillFrequencyCodeData> getBillFrequencyCodeData() {
		return billFrequencyCodeData;
	}


	public void setBillFrequencyCodeData(final List<BillFrequencyCodeData> billFrequencyCodeData) {
		this.billFrequencyCodeData = billFrequencyCodeData;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public void setPlanfinalAmount(BigDecimal finalAmount) {
		this.finalAmount=finalAmount;		
	}
	
	

}
