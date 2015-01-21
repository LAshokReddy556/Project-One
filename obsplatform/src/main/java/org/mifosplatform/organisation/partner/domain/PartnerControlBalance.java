package org.mifosplatform.organisation.partner.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.useradministration.domain.AppUser;


@Entity
@Table(name = "m_controlaccount_balance")
public class PartnerControlBalance extends AbstractAuditableCustom<AppUser,Long> {

	/**
      */
	private static final long serialVersionUID = 1L;

	@Column(name = "account_type")
	private String accountType;

	@Column(name = "balance_amount", nullable = false, length = 20)
	private BigDecimal balanceAmount;

	@Column(name = "office_id", insertable = true, updatable = true, nullable = true)
	private Long officeId;

	public PartnerControlBalance() {

	}

	public PartnerControlBalance(final BigDecimal amount, final String accountType,
			final Long officeId) {

		this.accountType = accountType;
		this.balanceAmount = amount;
		this.officeId = officeId;
	}

	public static PartnerControlBalance create(final BigDecimal amount,
			final String accountType, final Long officeId) {

		return new PartnerControlBalance(amount, accountType, officeId);

	}

	public String getAccountType() {
		return accountType;
	}

	public BigDecimal getBalanceAmount() {
		return balanceAmount;
	}

	public Long getOffice() {
		return officeId;
	}

	public void update(final BigDecimal transatcionAmount, final Long officeId) {

		this.balanceAmount = this.balanceAmount.add(transatcionAmount);
		// this.officeId = officeId;

	}
	
}
