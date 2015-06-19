package org.mifosplatform.organisation.feemaster.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
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
public class FeeMasterReadplatformServiceImpl implements FeeMasterReadplatformService {
	
	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	
	@Autowired
	public FeeMasterReadplatformServiceImpl(final PlatformSecurityContext context,final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	

	@Override
	public FeeMasterData retrieveSingleFeeMasterDetails(Long id) {
		try {
			context.authenticatedUser();
			FeeMasterDataMapper mapper = new FeeMasterDataMapper();
			String sql;
				sql = "select " + mapper.schema()+" where fm.id=? and  fm.is_deleted='N'"; 
		
			return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { id });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	private static final class FeeMasterDataMapper implements RowMapper<FeeMasterData> {

			public String schema() {
				return " fm.id as id,fm.fee_code as feeCode,fm.fee_description as feeDescription,fm.transaction_type as transactionType,fm.charge_code as chargeCode," +
						" fm.default_fee_amount as defaultFeeAmount,fm.item_id as itemId, fm.is_refundable as isRefundable from b_fee_master fm";
				
			}
			@Override
			public FeeMasterData mapRow(ResultSet rs, int rowNum)
					throws SQLException {
			
				final Long id = rs.getLong("id");
				final String feeCode = rs.getString("feeCode");
				final String feeDescription = rs.getString("feeDescription");
				final String transactionType = rs.getString("transactionType");
				final String chargeCode = rs.getString("chargeCode");
				final BigDecimal defaultFeeAmount = rs.getBigDecimal("defaultFeeAmount");
				final Long itemId = rs.getLong("itemId");
				final String isRefundable = rs.getString("isRefundable");
				return new FeeMasterData(id,feeCode,feeDescription,transactionType,chargeCode,defaultFeeAmount,itemId,isRefundable);
			
			
			}
}

	@Override
	public List<FeeMasterData> retrieveRegionPrice(Long id) {
		
		try{
			final RetrieveFeedetailDataMapper mapper = new RetrieveFeedetailDataMapper();			
			final String sql = "select " + mapper.scheme() +"  where fee_id =? and is_deleted = 'N'";
	    	return this.jdbcTemplate.query(sql, mapper, new Object[] {id});
		
		}catch (final EmptyResultDataAccessException e) {
		    return null;
		}

	}
	
	private static final class RetrieveFeedetailDataMapper implements RowMapper<FeeMasterData> {

		public String scheme() {
			return " id as id,fee_id as feeId,region_id as regionId,amount as amount from b_fee_detail ";
		}
		
		@Override
		public FeeMasterData mapRow(final ResultSet resultSet, final int rowNum)
				throws SQLException {
			
			final Long id = resultSet.getLong("id");
			final Long feeId = resultSet.getLong("feeId");
			final Long regionId = resultSet.getLong("regionId");
			final BigDecimal amount = resultSet.getBigDecimal("amount");
			
			
			return new FeeMasterData(id, feeId, regionId, amount);
		}
	}

	@Override
	public Collection<FeeMasterData> retrieveAllData(final String transactionType) {
		
		try{
			final FeeMasterDataMapper mapper = new FeeMasterDataMapper();			
			 String sql = "select " + mapper.schema() +"  where is_deleted = 'N'";
			if(transactionType != null){
				sql = "select " + mapper.schema() +"  where is_deleted = 'N' and fm.transaction_type ="+transactionType;
			}
	    	return this.jdbcTemplate.query(sql, mapper, new Object[] {});
		
		}catch (final EmptyResultDataAccessException e) {
		    return null;
		}
	}

}
