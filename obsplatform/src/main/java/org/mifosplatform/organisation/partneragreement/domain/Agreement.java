package org.mifosplatform.organisation.partneragreement.domain;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_office_agreement")
public class Agreement extends AbstractAuditableCustom<AppUser, Long> {

	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;

	@Column(name = "partner_id")
	private Long partnerId;
	
	@Column(name = "office_id")
	private Long officeId;

	@Column(name = "agreement_status")
	private String agreementStatus;

	@Column(name = "start_date")
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "is_deleted")
	private char isDeleted;

	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "agreements", orphanRemoval = true)
	private List<AgreementDetails> details = new ArrayList<AgreementDetails>();

	public Agreement() {

	}

	public static Agreement fromJosn(final JsonCommand command,final Long officeId) {
		
		final String agreementStatus=command.stringValueOfParameterNamed("agreementStatus");
		final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
		final LocalDate endDate = command.localDateValueOfParameterNamed("endDate");
		final Long partnerId = command.entityId();
		return new Agreement(partnerId,officeId,agreementStatus,startDate,endDate);
	}
	
	public Agreement(final Long partnerId,final Long officeId, final String agreementStatus, final LocalDate startDate,final LocalDate endDate) {
		
		this.partnerId = partnerId;
		this.officeId = officeId;
		this.agreementStatus =agreementStatus;
		this.startDate = startDate.toDate();
		this.endDate =endDate.toDate();
		this.isDeleted = 'N';
	}

	public Long getPartnerId() {
		return partnerId;
	}
	
	public Long getOfficeId() {
		return officeId;
	}

	public String getAgreementStatus() {
		return agreementStatus;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public char getIsDeleted() {
		return isDeleted;
	}

	public List<AgreementDetails> getDetails() {
		return details;
	}

	public void setPartnerId(Long partnerId) {
		this.partnerId = partnerId;
	}
	
	public void setOfficeId(Long officeId) {
		this.officeId = officeId;
	}

	public void setAgreementStatus(String agreementStatus) {
		this.agreementStatus = agreementStatus;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setIsDeleted(char isDeleted) {
		this.isDeleted = isDeleted;
	}

	public void setDetails(List<AgreementDetails> details) {
		this.details = details;
	}

	public void addAgreementDetails(AgreementDetails detail) {
		detail.update(this);
		this.details.add(detail);
		
	}

	public Map<String, Object> update(JsonCommand command) {
		
		final Map<String, Object> actualChanges = new ConcurrentHashMap<String, Object>(1);
		
		final String agreementStatus = "agreementStatus";
		if (command.isChangeInStringParameterNamed(agreementStatus,this.agreementStatus)) {
			final String newValue=command.stringValueOfParameterNamed(agreementStatus);
			actualChanges.put(agreementStatus, newValue);
			this.agreementStatus = StringUtils.defaultIfEmpty(newValue, null);
		}
		
		final String startDateParamName = "startDate";
		if (command.isChangeInLocalDateParameterNamed(startDateParamName,new LocalDate(this.startDate))) {
			final LocalDate newValue = command.localDateValueOfParameterNamed(startDateParamName);
			actualChanges.put(startDateParamName, newValue);
			this.startDate = newValue.toDate();
		}
		
		final String endDateParamName = "endDate";
		if (command.isChangeInLocalDateParameterNamed(endDateParamName,new LocalDate(this.endDate))) {
			final LocalDate newValue = command.localDateValueOfParameterNamed(endDateParamName);
			actualChanges.put(endDateParamName, newValue);
			if(newValue !=null){
			this.endDate = newValue.toDate();
			}
		}
		
		return actualChanges;
	}


}
