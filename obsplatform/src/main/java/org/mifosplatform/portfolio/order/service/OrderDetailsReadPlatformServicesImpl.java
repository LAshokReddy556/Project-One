package org.mifosplatform.portfolio.order.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.mifosplatform.billing.planprice.data.PriceData;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.portfolio.plan.data.ServiceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailsReadPlatformServicesImpl implements OrderDetailsReadPlatformServices{

	  private final JdbcTemplate jdbcTemplate;
	
 @Autowired

 public OrderDetailsReadPlatformServicesImpl(final TenantAwareRoutingDataSource dataSource) {
	        this.jdbcTemplate = new JdbcTemplate(dataSource);
	    }

	@Override
	public List<ServiceData> retrieveAllServices(Long plan_code) {


		PlanMapper mapper = new PlanMapper();

		String sql = "select " + mapper.schema()+" and da.plan_id = '"+plan_code+"'" ;
		return this.jdbcTemplate.query(sql, mapper, new Object[] {});

	}

	private static final class PlanMapper implements RowMapper<ServiceData> {

		public String schema() {
			return "da.id as id,se.id as serviceId, da.service_code as service_code, da.plan_id as plan_code,se.service_type as serviceType "
					+" from b_plan_detail da,b_service se where da.service_code = se.service_code";

		}

		@Override
		public ServiceData mapRow(final ResultSet rs,
				@SuppressWarnings("unused") final int rowNum)
				throws SQLException {
			Long id = rs.getLong("id");
			String serviceCode = rs.getString("service_code");
			String serviceType = rs.getString("serviceType");
			Long serviceid = rs.getLong("serviceId");

			return new ServiceData(id,serviceid,serviceCode,null, null,null,null, null,serviceType,null);
	}
	}
	@Override
		public List<PriceData> retrieveAllPrices(Long planId,String billingFreq, Long clientId ) {


			PriceMapper mapper = new PriceMapper();

			/*String sql = "select " + mapper.schema()+" and da.plan_id = '"+plan_code+"' and (c.billfrequency_code='"+billingFreq+"'  or c.billfrequency_code='Once')" +
					" AND ca.client_id = ?  AND da.price_region_id =pd.priceregion_id AND s.state_name = ca.state And s.parent_code=pd.country_id" +
					" AND pd.state_id = s.id group by da.id";*/
			
			String sql ="SELECT da.id AS id,if(da.service_code = 'None', 0, se.id) AS serviceId,da.service_code AS service_code,da.charge_code AS charge_code," +

				     " da.charging_variant AS charging_variant,c.charge_type AS charge_type,c.charge_duration AS charge_duration,c.duration_type AS duration_type," +
				     " da.discount_id AS discountId,c.tax_inclusive AS taxInclusive,da.price AS price,da.price_region_id,s.id AS stateId,s.parent_code AS countryId," +
				     " pd.state_id AS regionState,con.country_name,pd.country_id AS regionCountryId" +
				     " FROM b_plan_pricing da LEFT JOIN b_charge_codes c ON c.charge_code = da.charge_code " +
				     " LEFT JOIN b_service se ON (da.service_code = se.service_code OR da.service_code = 'None')" +
				     " LEFT JOIN b_priceregion_detail pd ON pd.priceregion_id = da.price_region_id" +
				     " JOIN b_client_address ca LEFT JOIN b_state s ON ca.state = s.state_name LEFT JOIN b_country con ON ca.country = con.country_name" +
				     " WHERE da.is_deleted = 'n' AND ca.address_key = 'PRIMARY' AND da.plan_id = ? AND c.billfrequency_code =? " +
				     " AND ca.client_id = ? AND (pd.state_id =ifnull((SELECT DISTINCT c.id FROM b_plan_pricing a, b_priceregion_detail b, b_state c," +
				     " b_charge_codes cc,b_client_address d" +
				     " WHERE b.priceregion_id = a.price_region_id  AND cc.charge_code = a.charge_code AND  b.state_id = c.id   AND a.price_region_id = b.priceregion_id " +
				     " AND d.state = c.state_name AND d.address_key = 'PRIMARY' AND d.client_id = ? and a.plan_id=? AND cc.billfrequency_code = ? ),0)" +
				     " AND pd.country_id = ifnull( (SELECT DISTINCT c.id FROM b_plan_pricing a, b_priceregion_detail b, b_country c, b_charge_codes cc,b_client_address d " +
				     " WHERE b.priceregion_id = a.price_region_id AND b.country_id = c.id AND cc.charge_code = a.charge_code AND a.price_region_id = b.priceregion_id " +
				     " AND c.country_name = d.country AND d.address_key = 'PRIMARY' AND d.client_id = ? AND a.plan_id = ? AND cc.billfrequency_code =  ?),0))" +
				     " GROUP BY da.id";
			return this.jdbcTemplate.query(sql, mapper, new Object[] { planId,billingFreq,clientId,clientId,planId,billingFreq,clientId,planId,billingFreq});

		} 

		private static final class PriceMapper implements RowMapper<PriceData> {

			public String schema() {
				return " da.id AS id,if(da.service_code ='None',0, se.id) AS serviceId, da.service_code AS service_code,da.charge_code AS charge_code,da.charging_variant AS charging_variant," +
						"c.charge_type AS charge_type,c.charge_duration AS charge_duration,c.duration_type AS duration_type,da.discount_id AS discountId," +
						"c.tax_inclusive AS taxInclusive,da.price AS price,da.price_region_id,s.id AS stateId,s.parent_code AS countryId,pd.state_id AS regionState," +
						"pd.country_id AS regionCountryId FROM b_plan_pricing da,b_charge_codes c,b_service se,b_client_address ca,b_state s,b_country con,b_priceregion_detail pd,b_priceregion_master prd" +
						" WHERE  da.charge_code = c.charge_code  AND ( da.service_code = se.service_code or da.service_code ='None') AND da.is_deleted = 'n' AND ca.address_key='PRIMARY'" ;
					   

			}

			@Override
			public PriceData mapRow(final ResultSet rs,
					@SuppressWarnings("unused") final int rowNum)
					throws SQLException {

				Long id = rs.getLong("id");
				String serviceCode = rs.getString("service_code");
				String chargeCode = rs.getString("charge_code");
				String chargingVariant = rs.getString("charging_variant");
				BigDecimal price=rs.getBigDecimal("price");
				String chargeType = rs.getString("charge_type");
				String chargeDuration = rs.getString("charge_duration");
				String durationType = rs.getString("duration_type");
				Long serviceid = rs.getLong("serviceId");
				Long discountId=rs.getLong("discountId");
				Long stateId = rs.getLong("stateId");
				Long countryId=rs.getLong("countryId");
				Long regionState = rs.getLong("regionState");
				Long regionCountryId=rs.getLong("regionCountryId");
				boolean taxinclusive=rs.getBoolean("taxInclusive");
				
				return new PriceData(id, serviceCode, chargeCode,chargingVariant,price,chargeType,chargeDuration,durationType,
			              serviceid,discountId,taxinclusive,stateId,countryId,regionState,regionCountryId);

			}
	}
		
		@Override
		public List<PriceData> retrieveDefaultPrices(Long planId,String billingFrequency, Long clientId) {
			
			PriceMapper mapper1 = new PriceMapper();
			/*String sql = "select " + mapper1.schema()+" and da.plan_id = '"+planId+"' and (c.billfrequency_code='"+billingFrequency+"'  or c.billfrequency_code='Once')" +
					" AND ca.client_id = ?  AND da.price_region_id =pd.priceregion_id AND s.state_name = ca.state" +
					" And (pd.country_id =s.parent_code or (pd.country_id =0 and prd.priceregion_code ='Default'))" +
					" AND pd.state_id =0 group by da.id";*/
			String sql="select " + mapper1.schema()+" and da.plan_id ='"+planId+"' AND c.billfrequency_code = '"+billingFrequency+"' AND ca.client_id =?" +
					" AND da.price_region_id = pd.priceregion_id AND s.state_name = ca.state AND con.country_name=ca.country" +
					" AND pd.country_id=con.id and prd.id =pd.priceregion_id AND (pd.country_id = s.parent_code or (pd.country_id =0 and prd.priceregion_code ='default'))" +
					" AND pd.state_id = 0 GROUP BY da.id; ";
			return this.jdbcTemplate.query(sql, mapper1, new Object[] { clientId });
		}



		

	}


