package org.mifosplatform.finance.payments.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;

public class PaymentData {
	
    private Collection<McodeData> data;
	private LocalDate paymentDate;
	private String clientName;
	private BigDecimal amountPaid;
	private String payMode;
	private Boolean isDeleted;
	private Long billNumber;
	private String receiptNo;
	private Long id;
	private BigDecimal availAmount;
	private Date transactionDate;
	private BigDecimal debitAmount;
	private List<PaymentData> depositDatas;
	
	public PaymentData(final Collection<McodeData> data,final List<PaymentData> depositDatas){
		this.data= data;
		this.depositDatas = depositDatas;
	}
	
	
	public PaymentData(final String clientName, final String payMode,final LocalDate paymentDate, final BigDecimal amountPaid, final Boolean isDeleted, final Long billNumber, final String receiptNumber) {
		  this.clientName = clientName;
		  this.payMode = payMode;
		  this.paymentDate = paymentDate;
		  this.amountPaid = amountPaid;
		  this.isDeleted = isDeleted;
		  this.billNumber = billNumber;
		  this.receiptNo = receiptNumber;
		 }


	public PaymentData(final Long id, final LocalDate paymentdate, final BigDecimal amount,final String recieptNo, final BigDecimal availAmount) {
	
		this.id=id;
		this.paymentDate=paymentdate;
		this.amountPaid=amount;
		this.receiptNo=recieptNo;
		this.availAmount=availAmount;
	}


	public PaymentData(Long id, Date transactionDate, BigDecimal debitAmount) {
		
		this.id = id;
		this.transactionDate = transactionDate;
		this.debitAmount = debitAmount;
	}


	public PaymentData() {
		
	}


	public Collection<McodeData> getData() {
		return data;
	}


	public LocalDate getPaymentDate() {
		return paymentDate;
	}


	public String getClientName() {
		return clientName;
	}


	public BigDecimal getAmountPaid() {
		return amountPaid;
	}


	public String getPayMode() {
		return payMode;
	}


	public Boolean getIsDeleted() {
		return isDeleted;
	}


	public Long getBillNumber() {
		return billNumber;
	}


	public String getReceiptNo() {
		return receiptNo;
	}


	public Long getId() {
		return id;
	}


	public BigDecimal getAvailAmount() {
		return availAmount;
	}


	public void setAvailAmount(BigDecimal availAmount) {
		this.availAmount = availAmount;
	}
	
	
	
}
