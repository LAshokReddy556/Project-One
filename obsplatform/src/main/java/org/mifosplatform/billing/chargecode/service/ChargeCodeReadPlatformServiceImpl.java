package org.mifosplatform.billing.chargecode.service;

import java.math.BigDecimal;
import java.sql.ResultSet;

import java.sql.SQLException;
import java.util.List;

import org.mifosplatform.billing.chargecode.data.BillFrequencyCodeData;
import org.mifosplatform.billing.chargecode.data.ChargeCodeData;
import org.mifosplatform.billing.chargecode.data.ChargeTypeData;
import org.mifosplatform.billing.chargecode.data.DurationTypeData;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * @author hugo
 * 
 */
@Service
public class ChargeCodeReadPlatformServiceImpl implements
		ChargeCodeReadPlatformService {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public ChargeCodeReadPlatformServiceImpl(
			final TenantAwareRoutingDataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #retrieveAllChargeCodes()
	 */
	public List<ChargeCodeData> retrieveAllChargeCodes() {

		final ChargeCodeMapper mapper = new ChargeCodeMapper();

		final String sql = "Select " + mapper.schema();

		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	private static final class ChargeCodeMapper implements
			RowMapper<ChargeCodeData> {

		public String schema() {
			return "id as id, charge_code as chargeCode, charge_description as chargeDescription, charge_type as chargeType,"
					+ "charge_duration as chargeDuration, duration_type as durationType, tax_inclusive as taxInclusive,"
					+ "billfrequency_code as billFrequencyCode from b_charge_codes";
		}

		@Override
		public ChargeCodeData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			final Long id = rs.getLong("id");
			final String chargeCode = rs.getString("chargeCode");
			final String chargeDescription = rs.getString("chargeDescription");
			final String chargeType = rs.getString("chargeType");
			final Integer chargeDuration = rs.getInt("chargeDuration");
			final String durationType = rs.getString("durationType");
			final Integer taxInclusive = rs.getInt("taxInclusive");
			final String billFrequencyCode = rs.getString("billFrequencyCode");

			return new ChargeCodeData(id, chargeCode, chargeDescription,
					chargeType, chargeDuration, durationType, taxInclusive,
					billFrequencyCode);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #getChargeType() like RC ,NRC
	 */
	public List<ChargeTypeData> getChargeType() {

		final ChargeTypeDataMapper typeMapper = new ChargeTypeDataMapper();

		final String sql = "select mcv.id as id,mcv.code_value as chargeType from m_code_value mcv,m_code mc "
				+ "where mcv.code_id=mc.id and mc.code_name='Charge Type' order by mcv.id";

		return jdbcTemplate.query(sql, typeMapper);
	}

	private static final class ChargeTypeDataMapper implements
			RowMapper<ChargeTypeData> {

		public ChargeTypeData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			final Long id = rs.getLong("id");
			final String chargeType = rs.getString("chargeType");

			return new ChargeTypeData(id, chargeType);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #getDurationType() like month(s),week(s),day(s)
	 */
	public List<DurationTypeData> getDurationType() {

		final DurationTypeDataMapper durationMapper = new DurationTypeDataMapper();

		final String sql = "select mcv.id as id,mcv.code_value as durationType from m_code_value mcv,m_code mc "
				+ "where mcv.code_id=mc.id and mc.code_name='Duration Type' order by mcv.id";

		return jdbcTemplate.query(sql, durationMapper);
	}

	private static final class DurationTypeDataMapper implements
			RowMapper<DurationTypeData> {

		public DurationTypeData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			final Long id = rs.getLong("id");
			final String durationTypeCode = rs.getString("durationType");
			return new DurationTypeData(id, durationTypeCode);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #getBillFrequency() like monthly,weekly,quaterly etc
	 */
	public List<BillFrequencyCodeData> getBillFrequency() {

		final BillFrequencyMapper frequencyMapper = new BillFrequencyMapper();

		final String sql = "select mcv.id as id,mcv.code_value as billFrequency from m_code_value mcv,m_code mc "
				+ "where mcv.code_id=mc.id and mc.code_name='Bill Frequency' order by mcv.id";

		return jdbcTemplate.query(sql, frequencyMapper);

	}

	private static final class BillFrequencyMapper implements
			RowMapper<BillFrequencyCodeData> {

		public BillFrequencyCodeData mapRow(final ResultSet rs, int rowNum)
				throws SQLException {

			final Long id = rs.getLong("id");
			final String billFrequencyCode = rs.getString("billFrequency");

			return new BillFrequencyCodeData(id, billFrequencyCode);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see #retrieveSingleChargeCodeDetails(java.lang.Long)
	 */
	public ChargeCodeData retrieveSingleChargeCodeDetails(
			final Long chargeCodeId) {

		try {

			final ChargeCodeMapper mapper = new ChargeCodeMapper();

			final String sql = "select " + mapper.schema() + " where id = ?";

			return jdbcTemplate.queryForObject(sql, mapper,
					new Object[] { chargeCodeId });
		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}

	private static final class ChargeCodeRecurringMapper implements RowMapper<ChargeCodeData> {

		public String schema() {

			return "  cc.id AS id,c.contract_duration AS contractDuration,c.contract_type AS contractType,cc.duration_type AS chargeType," +
					" cc.charge_duration AS chargeDuration,p.price AS price FROM b_charge_codes cc, b_plan_pricing p " +
					" left join b_contract_period c on c.contract_period = p.duration WHERE  p.id = ? AND p.charge_code = cc.charge_code";
  
		}

		@Override
		public ChargeCodeData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			final Long id = rs.getLong("id");
			final String contractType = rs.getString("contractType");
			final BigDecimal price = rs.getBigDecimal("price");
			final String chargeType = rs.getString("chargeType");
			final Integer chargeDuration = rs.getInt("chargeDuration");
			final Integer contractDuration = rs.getInt("contractDuration");
			ChargeCodeData chargeCodeData = new ChargeCodeData(id,contractType,contractDuration,chargeType,chargeDuration,price);
			chargeCodeData.setPrice(price);
			return chargeCodeData;	
		}
	}
	
	@Override
	public ChargeCodeData retrieveChargeCodeForRecurring(Long priceId) {
		try {

			final ChargeCodeRecurringMapper mapper = new ChargeCodeRecurringMapper();
			final String sql = "select " + mapper.schema();
			return jdbcTemplate.queryForObject(sql, mapper, new Object[] { priceId});
		
		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}

}
