package org.mifosplatform.portfolio.servicemapping.data;

import java.util.Collection;
import java.util.List;

import org.mifosplatform.finance.payments.data.McodeData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.dataqueries.data.ReportParameterData;
import org.mifosplatform.organisation.mcodevalues.data.MCodeData;

public class ServiceMappingData {

	
	private Long id;
	private Long serviceId;
	private String serviceCode;
	private String serviceIdentification;
	private String status;
	private String image;
	private String category;
	private String subCategory;
	private List<ServiceMappingData> serviceMappingData;
	private List<ServiceCodeData> serviceCodeData;
	private List<EnumOptionData> statusData;
	private Collection<ReportParameterData> serviceParameters;
	private Collection<McodeData> categories;
	private Collection<McodeData> subCategories;
	private Collection<MCodeData> provisionSysData;
	private String provisionSystem;
	private String sortBy;
	
	public Collection<McodeData> getCategories() {
		return categories;
	}

	public void setCategories(Collection<McodeData> categories) {
		this.categories = categories;
	}

	public Collection<McodeData> getSubCategories() {
		return subCategories;
	}

	public void setSubCategories(Collection<McodeData> subCategories) {
		this.subCategories = subCategories;
	}




	

	public ServiceMappingData(final Long id, final String serviceCode,final String serviceIndentification, final String status,
			final String image, final String category, final String subCategory,final String sortBy, String provisionSystem) {
		
		this.id=id;
		this.serviceCode=serviceCode;
		this.serviceIdentification=serviceIndentification;
		this.status=status;
		this.image=image;
		this.category=category;
		this.provisionSystem=provisionSystem;
		this.subCategory=subCategory;
		this.sortBy = sortBy;
	}
	
	public ServiceMappingData( final List<ServiceMappingData> serviceMappingData,	
			final List<ServiceCodeData> serviceCodeData, 
			final List<EnumOptionData> status, 
			final Collection<ReportParameterData> serviceParameters, 
			final Collection<McodeData> categories, final Collection<McodeData> subCategories,final Collection<MCodeData> provisionSysData) {

		this.serviceMappingData=serviceMappingData;
		this.serviceCodeData=serviceCodeData;
		this.statusData=status;
		this.serviceParameters=serviceParameters;
		this.categories=categories;
		this.provisionSysData=provisionSysData;
		this.subCategories=subCategories;
	}
	
	public List<ServiceCodeData> getServiceCodeData() {
		return serviceCodeData;
	}

	public void setServiceCodeData(List<ServiceCodeData> serviceCodeData) {
		this.serviceCodeData = serviceCodeData;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public List<ServiceMappingData> getServiceMappingData() {
		return serviceMappingData;
	}


	public void setServiceMappingData(List<ServiceMappingData> serviceMappingData) {
		this.serviceMappingData = serviceMappingData;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}
	
	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getServiceIndentification() {
		return serviceIdentification;
	}

	public void setServiceIndentification(String serviceIndentification) {
		this.serviceIdentification = serviceIndentification;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public void setStatusData(List<EnumOptionData> status) {
		this.statusData=status;
		
	}

	public void setProvisionSysData(Collection<MCodeData> provisionSysData) {
		this.provisionSysData=provisionSysData;
		
	}
	
	
	
	

	
	
}
