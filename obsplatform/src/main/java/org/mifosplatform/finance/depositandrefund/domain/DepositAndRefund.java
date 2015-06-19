package org.mifosplatform.finance.depositandrefund.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "b_deposit_refund")
public class DepositAndRefund extends AbstractAuditableCustom<AppUser, Long>{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2876090423570296480L;

	@Column(name="client_id", nullable=false, length=20)
	private Long clientId;
	
	@Column(name="transaction_date",nullable=false,length=100)
	private Date transactionDate;
	
	@Column(name="transaction_type", nullable=false, length=50)
	private String transactionType;
	
	@Column(name="item_id", nullable=true, length=20)
	private Long itemId;
	
	@Column(name="ref_id", nullable=true, length=20)
	private Long refId;
	
	@Column(name="payment_id", nullable=true, length=20)
	private Long paymentId;
	
	@Column(name="description", nullable=true, length=50)
	private String description;
	
	@Column(name="credit_amount", nullable=true, length=20)
	private BigDecimal creditAmount;
	
	@Column(name="debit_amount", nullable=true, length=20)
	private BigDecimal debitAmount;
	
	@Column(name = "is_refund")
	private char isRefund = 'N';
	

	public DepositAndRefund(){}


	public DepositAndRefund(Long clientId, Long itemId, BigDecimal amount,
			Date transactionDate, String transactionType) {
		
		this.clientId = clientId;
		this.transactionDate = transactionDate;
		this.transactionType = transactionType;
		this.itemId = itemId;
		this.debitAmount = amount;
		this.description = transactionType;
		
	}
	
	public DepositAndRefund(Long clientId, Long itemId, BigDecimal amount,
			Date transactionDate, String transactionType, String description, String transType) {
		
		this.clientId = clientId;
		this.transactionDate = transactionDate;
		this.transactionType = transactionType;
		this.itemId = itemId;
		if(transType.equalsIgnoreCase("Debit")){
			this.debitAmount = amount;
		}else if(transType.equalsIgnoreCase("Credit")){
			this.creditAmount = amount;
		}
		if(transactionType.equalsIgnoreCase("Refund")){
			this.isRefund = 'Y';
		}
		this.description = description;
	}

	public static DepositAndRefund fromJson(Long clientId,
			JsonCommand command) {
		
		final Long itemId = command.longValueOfParameterNamed("itemId");
		final BigDecimal amount = command.bigDecimalValueOfParameterNamed("amount");
		final Date transactionDate = DateUtils.getLocalDateOfTenant().toDate();
		final String transactionType = "Deposit";
		
		return new DepositAndRefund(clientId, itemId, amount, transactionDate, transactionType);
	}
	
	public static DepositAndRefund fromJson(Long clientId,Long itemId, 
			BigDecimal amount, String description, String transType) {
		
		final Date transactionDate = DateUtils.getLocalDateOfTenant().toDate();
		final String transactionType = "Refund";
		
		return new DepositAndRefund(clientId, itemId, amount, transactionDate, transactionType, description, transType);
	}


	public Long getRefId() {
		return refId;
	}


	public void setRefId(Long refId) {
		this.refId = refId;
	}


	public Long getClientId() {
		return clientId;
	}


	public String getTransactionType() {
		return transactionType;
	}


	public BigDecimal getDebitAmount() {
		return debitAmount;
	}


	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}


	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}


	public void setDebitAmount(BigDecimal debitAmount) {
		this.debitAmount = debitAmount;
	}


	public Long getItemId() {
		return itemId;
	}


	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}


	public Long getPaymentId() {
		return paymentId;
	}


	public void setPaymentId(Long paymentId) {
		this.paymentId = paymentId;
	}


	public char getIsRefund() {
		return isRefund;
	}


	public void setIsRefund(char isRefund) {
		this.isRefund = isRefund;
	}
	
	
}
