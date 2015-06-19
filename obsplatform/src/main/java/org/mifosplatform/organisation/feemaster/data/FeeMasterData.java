package org.mifosplatform.organisation.feemaster.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.mifosplatform.billing.chargecode.data.ChargesData;
import org.mifosplatform.logistics.item.data.ItemData;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;
import org.mifosplatform.organisation.region.data.RegionData;

public class FeeMasterData {
	
	private Long id;
	private String feeCode;
	private String feeDescription;
	private String transactionType;
	private String chargeCode;
	private BigDecimal defaultFeeAmount;
	private List<ChargesData> chargeDatas;
	private List<RegionData> regionDatas;
	private Long feeId;
	private Long regionId;
	private BigDecimal amount;
	private FeeMasterData feeMasterData;
	private Collection<MCodeData> transactionTypeDatas;
	private List<FeeMasterData> feeMasterRegionPricesDatas;
	private List<ItemData> itemCodes;
	private Long itemId; 
	private String isRefundable;
	
	public FeeMasterData(Long id,String feeCode,String feeDescription,String transactionType,String chargeCode,
			BigDecimal defaultFeeAmount, Long itemId, String isRefundable) {
     
		this.id=id;
		this.feeCode=feeCode;
		this.feeDescription=feeDescription;
		this.transactionType=transactionType;
		this.chargeCode=chargeCode;
		this.defaultFeeAmount=defaultFeeAmount;
		this.itemId = itemId;
		this.isRefundable = isRefundable;
		
	}



	public FeeMasterData(Collection<MCodeData> transactionTypeDatas,List<ChargesData> chargeDatas, List<RegionData> regionDatas,
			List<ItemData> itemCodes) {

		this.transactionTypeDatas = transactionTypeDatas;
		this.chargeDatas = chargeDatas;
		this.regionDatas = regionDatas;
		this.itemCodes = itemCodes;
	}



	public FeeMasterData(Long id, Long feeId, Long regionId, BigDecimal amount) {
		
		this.id = id;
		this.feeId = feeId;
		this.regionId = regionId;
		this.amount = amount;
	}



	public FeeMasterData(FeeMasterData feeMasterData, Collection<MCodeData> transactionTypeDatas, 
			List<ChargesData> chargeDatas, List<RegionData> regionDatas, List<FeeMasterData> feeMasterRegionPricesDatas,
			List<ItemData> itemCodes) {
		
		this.feeMasterData = feeMasterData;
		this.transactionTypeDatas = transactionTypeDatas;
		this.chargeDatas = chargeDatas;
		this.regionDatas = regionDatas;
		this.feeMasterRegionPricesDatas = feeMasterRegionPricesDatas;
		this.itemCodes = itemCodes;
	}



	public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
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
	

	public List<ChargesData> getChargeDatas() {
		return chargeDatas;
	}



	public void setChargeDatas(List<ChargesData> chargeDatas) {
		this.chargeDatas = chargeDatas;
	}



	public List<RegionData> getRegionDatas() {
		return regionDatas;
	}



	public void setRegionDatas(List<RegionData> regionDatas) {
		this.regionDatas = regionDatas;
	}



	public Long getFeeId() {
		return feeId;
	}



	public void setFeeId(Long feeId) {
		this.feeId = feeId;
	}



	public Long getRegionId() {
		return regionId;
	}



	public void setRegionId(Long regionId) {
		this.regionId = regionId;
	}



	public BigDecimal getAmount() {
		return amount;
	}



	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}



	public FeeMasterData getFeeMasterData() {
		return feeMasterData;
	}



	public void setFeeMasterData(FeeMasterData feeMasterData) {
		this.feeMasterData = feeMasterData;
	}



	public Collection<MCodeData> getTransactionTypeDatas() {
		return transactionTypeDatas;
	}



	public void setTransactionTypeDatas(Collection<MCodeData> transactionTypeDatas) {
		this.transactionTypeDatas = transactionTypeDatas;
	}



	public List<FeeMasterData> getFeeMasterRegionPricesDatas() {
		return feeMasterRegionPricesDatas;
	}



	public void setFeeMasterRegionPricesDatas(
			List<FeeMasterData> feeMasterRegionPricesDatas) {
		this.feeMasterRegionPricesDatas = feeMasterRegionPricesDatas;
	}




}
