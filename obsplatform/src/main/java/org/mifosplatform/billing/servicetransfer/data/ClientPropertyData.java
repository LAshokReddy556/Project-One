package org.mifosplatform.billing.servicetransfer.data;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.organisation.feemaster.data.FeeMasterData;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;
import org.mifosplatform.portfolio.property.data.PropertyDefinationData;

public class ClientPropertyData {

	private String clientId;
	private Long id;
	private String firstname;
	private String lastname;
	private String displayName;
	private String email;
	private String phone;
	private String addressNo;
	private String street;
	private String city;
	private String state;
	private String country;
	private String zip;
	private Long propertyTypeId;
	private String propertyType;
	private String propertyCode;
	private String unitCode;
	private String floor;
	private String buildingCode;
	private String parcel;
	private String precinct;
	private String status;
	private String categoryType;
	private String addressKey;
	private List<PropertyDefinationData> propertyCodesData;
	private FeeMasterData feeMasterData;
	private Collection<MCodeData> propertyTypes;
	private String floorDesc;
	private String parcelDesc;

	public ClientPropertyData(final Long id, final Long propertyTypeId,final String propertyType, final String propertyCode,
			final String unitCode, final String floor, final String floorDesc,final String buildingCode,final String parcel,
            final String parcelDesc,final String precinct, final String street,final String zip, final String state, final String country,
	        final String status, final String clientId, final String firstName,final String lastName, String displayName, 
	        final String email, final String addressNo,final String addressKey, final String categoryType) {

		this.id = id;
		this.propertyTypeId = propertyTypeId;
		this.propertyType = propertyType;
		this.propertyCode = propertyCode;
		this.unitCode = unitCode;
		this.floor = floor;
		this.floorDesc =floorDesc;
		this.buildingCode = buildingCode;
		this.parcel = parcel;
		this.parcelDesc = parcelDesc;
		this.precinct = precinct;
		this.street = street;
		this.zip = zip;
		this.state = state;
		this.country = country;
		this.status = status;
		this.clientId = clientId;
		this.firstname = StringUtils.defaultIfEmpty(firstName, null);
		this.lastname = StringUtils.defaultIfEmpty(lastName, null);
		this.displayName = StringUtils.defaultIfEmpty(displayName, null);
		this.email = email;
		this.addressNo = addressNo;
		this.addressKey = addressKey;
		this.categoryType = categoryType;

	}

	public ClientPropertyData() {
		
	}

	public String getClientId() {
		return clientId;
	}

	public Long getId() {
		return id;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getEmail() {
		return email;
	}

	public String getPhone() {
		return phone;
	}

	public String getAddressNo() {
		return addressNo;
	}

	public String getStreet() {
		return street;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getCountry() {
		return country;
	}

	public String getZip() {
		return zip;
	}

	public Long getPropertyTypeId() {
		return propertyTypeId;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public String getPropertyCode() {
		return propertyCode;
	}

	public String getUnitCode() {
		return unitCode;
	}

	public String getFloor() {
		return floor;
	}

	public String getBuildingCode() {
		return buildingCode;
	}

	public String getParcel() {
		return parcel;
	}

	public String getPrecinct() {
		return precinct;
	}

	public String getStatus() {
		return status;
	}

	public String getCategoryType() {
		return categoryType;
	}

	public String getAddressKey() {
		return addressKey;
	}
	
	public String getFloorDesc() {
		return floorDesc;
	}

	public String getParcelDesc() {
		return parcelDesc;
	}

	public void setPropertyCodes(List<PropertyDefinationData> propertyCodesData) {

		this.propertyCodesData = propertyCodesData;

	}

	public List<PropertyDefinationData> getPropertyCodesData() {
		return propertyCodesData;
	}

	public FeeMasterData getFeeMasterData() {
		return feeMasterData;
	}

	public void setFeeMasterData(FeeMasterData feeMasterData) {
		this.feeMasterData = feeMasterData;
	}

	public void setPropertyTypes(Collection<MCodeData> propertyTypes) {

		this.propertyTypes = propertyTypes;
	}

	public Collection<MCodeData> getPropertyTypes() {
		return propertyTypes;
	}
	

}
