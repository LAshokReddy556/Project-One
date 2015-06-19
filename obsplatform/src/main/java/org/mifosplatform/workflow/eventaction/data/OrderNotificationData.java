package org.mifosplatform.workflow.eventaction.data;

import org.joda.time.LocalDate;

public class OrderNotificationData {

	private final String firstName;
	private final String lastName;
	private final String planName;
	private final String emailId;
	private final String officeName;
	private final String officeEmail;
	private final String officePhoneNo;
	private final LocalDate activationDate;
	private final LocalDate startDate;
	private final LocalDate endDate;
	private final String clientPhone;
	
	public OrderNotificationData(String firstName, String lastName, String planName, 
			String emailId, String officeName, String officeEmail, String officePhoneNo,
			LocalDate activationDate, LocalDate startDate, LocalDate endDate, String clientPhone) {
		
		// TODO Auto-generated constructor stub
		this.firstName = firstName;
		this.lastName = lastName;
		this.planName = planName;
		this.emailId = emailId;
		this.officeName = officeName;
		this.officeEmail = officeEmail;
		this.officePhoneNo = officePhoneNo;
		this.activationDate = activationDate;
		this.startDate = startDate;
		this.endDate = endDate;
		this.clientPhone = clientPhone;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getPlanName() {
		return planName;
	}

	public String getOfficeName() {
		return officeName;
	}

	public String getOfficeEmail() {
		return officeEmail;
	}

	public String getOfficePhoneNo() {
		return officePhoneNo;
	}

	public LocalDate getActivationDate() {
		return activationDate;
	}

	public String getEmailId() {
		return emailId;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public String getClientPhone() {
		return clientPhone;
	}

}
