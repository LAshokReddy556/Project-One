package org.mifosplatform.billing.servicetransfer.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.feemaster.data.FeeMasterData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ServiceTransferReadPlatformServiceImpl implements ServiceTransferReadPlatformService {

	
	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	
	@Autowired
	public ServiceTransferReadPlatformServiceImpl(final PlatformSecurityContext context,final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	
	@Override
	public List<FeeMasterData> retrieveSingleFeeDetails(Long clientId, String transationType) {

		try {
			context.authenticatedUser();
			FeeMasterDataMapper mapper = new FeeMasterDataMapper();
			String sql = "select " + mapper.schemaWithClientId(clientId,transationType)+" where fm.transaction_type = ? and fm.is_deleted='N'  group by fm.id"; 
		
			return this.jdbcTemplate.query(sql, mapper, new Object[] {transationType});

		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	private static final class FeeMasterDataMapper implements RowMapper<FeeMasterData> {
		
		public String schemaWithClientId(final Long clientId, final String transationType) {
			
			/*return "fm.id AS id,fm.fee_code AS feeCode,fm.fee_description AS feeDescription, fm.transaction_type AS transactionType,"
				+"fm.charge_code AS chargeCode,ifnull(round(fd.amount , 2),fm.default_fee_amount ) as amount FROM b_fee_master fm "
				+"left join b_client_address ca on ca.client_id = "+clientId+" "
                +"left join b_state s on s.state_name = ca.state " 
                +"left join b_priceregion_detail pd on (pd.state_id = s.id or (pd.state_id = 0 and pd.country_id = s.parent_code ) ) " 
                +"left join b_priceregion_master prm ON prm.id = pd.priceregion_id "
                +"left join b_fee_detail fd on (fd.fee_id = fm.id and fd.region_id = prm.id and fd.is_deleted='N' ) ";*/
			
			/*return "  fm.id AS id,fm.fee_code AS feeCode,fm.fee_description AS feeDescription,fm.transaction_type AS transactionType," +
					" fm.charge_code AS chargeCode,ifnull(round(fd.amount, 2), fm.default_fee_amount) AS amount" +
					" FROM b_fee_master fm LEFT JOIN b_client_address ca ON ca.client_id = "+clientId+" " +
					" LEFT JOIN b_state s ON s.state_name = ca.state LEFT JOIN b_priceregion_detail pd " +
					" ON (pd.state_id =ifnull((SELECT DISTINCT c.id FROM b_fee_detail a, b_priceregion_detail b, b_state c, b_client_address d, b_fee_master m" +
					" WHERE b.priceregion_id = a.region_id AND b.state_id = c.id AND a.region_id = b.priceregion_id AND d.state = c.state_name" +
					" AND d.address_key = 'PRIMARY' AND d.client_id = "+clientId+" and m.transaction_type =? and m.id = a.fee_id AND a.is_deleted = 'N' AND m.is_deleted = 'N'),0) " +
					" AND pd.country_id =ifnull((SELECT DISTINCT c.id FROM b_fee_detail a, b_priceregion_detail b, b_country c, b_state s, b_client_address d ," +
					" b_fee_master m" +
					" WHERE b.priceregion_id = a.region_id AND b.country_id = c.id AND c.country_name = d.country AND d.address_key = 'PRIMARY'" +
					" AND d.client_id = "+clientId+" and m.transaction_type =? and m.id = a.fee_id and  a.is_deleted = 'N' AND m.is_deleted = 'N'),0)) " +
					" LEFT JOIN b_priceregion_master prm ON prm.id = pd.priceregion_id LEFT JOIN b_fee_detail fd ON (fd.fee_id = fm.id AND fd.region_id = prm.id" +
<<<<<<< HEAD
					" AND fd.is_deleted = 'N')  ";*/
					
			return "  fm.id AS id,fm.fee_code AS feeCode,fm.fee_description AS feeDescription,fm.transaction_type AS transactionType," +
		     " fm.charge_code AS chargeCode,ifnull(round(fd.amount, 2), fm.default_fee_amount) AS amount" +
		     " FROM b_fee_master fm LEFT JOIN b_client_address ca ON ca.client_id = "+clientId+" " +
		     " LEFT JOIN b_state s ON s.state_name = ca.state LEFT JOIN b_priceregion_detail pd " +
		     " ON (pd.state_id =ifnull((SELECT DISTINCT c.id FROM b_fee_detail a, b_priceregion_detail b, b_state c, b_client_address d, b_fee_master m" +
		     " WHERE b.priceregion_id = a.region_id AND b.state_id = c.id AND a.region_id = b.priceregion_id AND d.state = c.state_name" +
		     " AND d.address_key = 'PRIMARY' AND d.client_id = "+clientId+" and m.transaction_type = '"+transationType+"' and m.id = a.fee_id AND a.is_deleted = 'N' AND m.is_deleted = 'N'),0) " +
		     " AND pd.country_id =ifnull((SELECT DISTINCT c.id FROM b_fee_detail a, b_priceregion_detail b, b_country c, b_state s, b_client_address d ," +
		     " b_fee_master m" +
		     " WHERE b.priceregion_id = a.region_id AND b.country_id = c.id AND c.country_name = d.country AND d.address_key = 'PRIMARY'" +
		     " AND d.client_id = "+clientId+" and m.transaction_type = '"+transationType+"' and m.id = a.fee_id and  a.is_deleted = 'N' AND m.is_deleted = 'N'),0)) " +
		     " LEFT JOIN b_priceregion_master prm ON prm.id = pd.priceregion_id LEFT JOIN b_fee_detail fd ON (fd.fee_id = fm.id AND fd.region_id = prm.id" +
		     " AND fd.is_deleted = 'N')  ";


		}

		@Override
		public FeeMasterData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
		
			final Long id = rs.getLong("id");
			final String feeCode = rs.getString("feeCode");
			final String feeDescription = rs.getString("feeDescription");
			final String transactionType = rs.getString("transactionType");
			final String chargeCode = rs.getString("chargeCode");
			final BigDecimal amount = rs.getBigDecimal("amount");
			return new FeeMasterData(id,feeCode,feeDescription,transactionType,chargeCode,amount,null,null);
		
		
		}
}

}
