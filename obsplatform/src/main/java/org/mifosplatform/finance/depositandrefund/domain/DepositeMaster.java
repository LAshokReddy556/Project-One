package org.mifosplatform.finance.depositandrefund.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * @author hugo
 * 
 */
@Entity
@Table(name = "b_deposit_refund")
public class DepositeMaster extends AbstractPersistable<Long> {

	private static final long serialVersionUID = 1L;
	
	@Column(name="client_id")
	private Long clientId;
	
	@Column(name="description")
	private String description;

	public DepositeMaster() {
	}

	public DepositeMaster(final Long clientId, final String description) {

		this.clientId = clientId;
		this.description = description;
		
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static DepositeMaster fromJson(final JsonCommand command) {
		final Long clientId = command
				.longValueOfParameterNamed("clientId");
		final String description = command
				.stringValueOfParameterNamed("description");
		
		return new DepositeMaster(clientId,description);
	}
	
}
