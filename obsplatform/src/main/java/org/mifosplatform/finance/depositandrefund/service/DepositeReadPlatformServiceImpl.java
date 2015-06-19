package org.mifosplatform.finance.depositandrefund.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.organisation.feemaster.data.FeeMasterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * @author hugo
 * 
 */
@Service
public class DepositeReadPlatformServiceImpl implements DepositeReadPlatformService {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public DepositeReadPlatformServiceImpl(final TenantAwareRoutingDataSource dataSource) {
		
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/*
	 * (non-Javadoc)
	 * 
	 */
	public FeeMasterData retrieveDepositDetails(Long feeId, Long clientId) {

		final String transaction="Deposit";
		
		final DepositeMapper mapper = new DepositeMapper();

		final String sql = "Select " + mapper.schema();

		return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] {clientId,clientId,transaction,feeId,clientId,transaction,feeId,transaction,feeId});
	}

	private static final class DepositeMapper implements RowMapper<FeeMasterData> {

		public String schema() {
			return "fm.id AS id,fm.fee_code AS feeCode,fm.item_id AS itemId,fm.is_refundable as isRefundable,fm.charge_code AS chargeCode,"+
				      " ifnull(round(fd.amount, 2), fm.default_fee_amount) AS amount FROM b_fee_master fm LEFT JOIN b_client_address ca ON ca.client_id = ? "+
				       " LEFT JOIN b_state s ON s.state_name = ca.state LEFT JOIN b_priceregion_detail pd ON ( pd.state_id = ifnull((SELECT DISTINCT c.id FROM b_fee_detail a,"+
				       " b_priceregion_detail b,b_state c,b_client_address d,b_fee_master m  WHERE b.priceregion_id = a.region_id AND b.state_id = c.id "+
				       " AND a.region_id = b.priceregion_id AND d.state = c.state_name AND d.address_key = 'PRIMARY' AND d.client_id = ? AND m.transaction_type = ? "+
				       " AND m.id = a.fee_id AND a.is_deleted = 'N' and m.id= ? AND m.is_deleted = 'N'),0) AND pd.country_id = ifnull((SELECT DISTINCT c.id FROM b_fee_detail a,"+
				       " b_priceregion_detail b,b_country c,b_state s,b_client_address d, b_fee_master m WHERE  b.priceregion_id = a.region_id AND b.country_id = c.id AND c.country_name = d.country "+
				       " AND d.address_key = 'PRIMARY' AND d.client_id = ? AND m.transaction_type = ? AND m.id = a.fee_id and m.id= ? AND a.is_deleted = 'N' AND m.is_deleted = 'N'),0)) "+
				       " LEFT JOIN b_priceregion_master prm ON prm.id = pd.priceregion_id LEFT JOIN b_fee_detail fd ON (fd.fee_id = fm.id AND fd.region_id = prm.id AND fd.is_deleted = 'N') "+
				       " WHERE fm.transaction_type =? AND fm.is_deleted = 'N' AND fm.id = ? GROUP BY fm.id";
			}

		@Override
		public FeeMasterData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			final Long id = rs.getLong("id");
			final String feeCode = rs.getString("feeCode");
			//final String feeDescription = rs.getString("feeDescription");
			//final String transactionType = rs.getString("transactionType");
			final String chargeCode = rs.getString("chargeCode");
			final BigDecimal defaultFeeAmount = rs.getBigDecimal("amount");
			final Long itemId = rs.getLong("itemId");
			final String isRefundable = rs.getString("isRefundable");
			return new FeeMasterData(id, feeCode, " ", "Depsoit", chargeCode, defaultFeeAmount, itemId, isRefundable);

		}
	}

}
