package org.mifosplatform.finance.billingorder.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.billing.discountmaster.data.DiscountMasterData;
import org.mifosplatform.billing.taxmaster.data.TaxMappingRateData;
import org.mifosplatform.finance.billingorder.data.BillingOrderData;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.organisation.partneragreement.data.AgreementData;
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
public class BillingOrderReadPlatformServiceImplementation implements BillingOrderReadPlatformService {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public BillingOrderReadPlatformServiceImplementation(final TenantAwareRoutingDataSource dataSource) {

		this.jdbcTemplate = new JdbcTemplate(dataSource);

	}
	
	/* (non-Javadoc)
	 * @see #retrieveOrderIds(java.lang.Long, org.joda.time.LocalDate)
	 * orders list
	 */
	@Override
	public List<BillingOrderData> retrieveOrderIds(final Long clientId,final LocalDate processDate) {
		
		final OrderIdMapper orderIdMapper = new OrderIdMapper();
		final String sql = "select" + orderIdMapper.orderIdSchema();
		return this.jdbcTemplate.query(sql, orderIdMapper,new Object[] { clientId,processDate.minusDays(1).toDate(),processDate.toDate() });
		
	}
	
	
	private static final class OrderIdMapper implements RowMapper<BillingOrderData> {

		@Override
		public BillingOrderData  mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			
			final Long orderId = resultSet.getLong("orderId");
			final String durationType = resultSet.getString("durationType");
			final Date billStartDate = resultSet.getDate("billStartDate");
			final Date nextBillableDate = resultSet.getDate("nextBillableDate");
			final Date invoiceTillDate = resultSet.getDate("invoiceTillDate");
			final String billingAlign =  resultSet.getString("billingAlign");
			
			return new BillingOrderData(orderId, durationType, billStartDate,nextBillableDate,invoiceTillDate,billingAlign);
		}

		public String orderIdSchema() {
			
			return " distinct os.id as orderId,op.duration_type as durationType, Date_format(IFNULL(op.invoice_tilldate,op.bill_start_date), '%Y-%m-%d') as billStartDate," +
				    "ifnull(op.next_billable_day,op.bill_start_date) as  nextBillableDate,os.billing_align as billingAlign,op.invoice_tilldate as invoiceTillDate FROM b_orders os "+
					" left outer join b_order_price op on os.id = op.order_id"+
					" WHERE os.client_id = ? and os.order_status=1 AND Date_format(IFNULL(op.next_billable_day, ?), '%Y-%m-%d') <= ? and os.is_deleted = 'N' "+
					" and Date_format(IFNULL(op.next_billable_day,Date_format(IFNULL(op.bill_start_date, '3099-12-12'),'%Y-%m-%d')), '%Y-%m-%d')" +
					" <=Date_format(IFNULL(op.bill_end_date, '3099-12-12'),'%Y-%m-%d') group by os.id; ";
		}
		
	}

	/* (non-Javadoc)
	 * @see #retrieveBillingOrderData(java.lang.Long, org.joda.time.LocalDate, java.lang.Long)
	 * single orderData
	 */
	@Override
	public List<BillingOrderData> retrieveBillingOrderData(final Long clientId,final LocalDate date,final Long orderId) {

		final BillingOrderMapper billingOrderMapper = new BillingOrderMapper();
		final String sql = "select " + billingOrderMapper.billingOrderSchema();
		return this.jdbcTemplate.query(sql, billingOrderMapper,new Object[] { clientId,orderId,date.toString(),date.toString() });
	}

	private static final class BillingOrderMapper implements RowMapper<BillingOrderData> {

		@Override
		public BillingOrderData mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			
			final Long clientOderId = resultSet.getLong("clientOrderId");
			final Long orderPriceId = resultSet.getLong("orderPriceId");
			final Long planId = resultSet.getLong("planId");
			final Long clientId = resultSet.getLong("clientId");
			final Date startDate = resultSet.getDate("startDate");
			final Date nextBillableDate = resultSet.getDate("nextBillableDate");
			final Date endDate = resultSet.getDate("endDate");
			final String billingFrequency = resultSet.getString("billingFrequency");
			final String chargeCode = resultSet.getString("chargeCode");
			final String chargeType = resultSet.getString("chargeType");
			final Integer chargeDuration = resultSet.getInt("chargeDuration");
			final String durationType = resultSet.getString("durationType");
			final Date invoiceTillDate = resultSet.getDate("invoiceTillDate");
			final BigDecimal price = resultSet.getBigDecimal("price");
			final String billingAlign = resultSet.getString("billingAlign");
			final Date billStartDate = resultSet.getDate("billStartDate");
			final Date billEndDate = resultSet.getDate("billEndDate");
			final Long orderStatus = resultSet.getLong("orderStatus");
			final Integer taxInclusive = resultSet.getInt("taxInclusive");
			
			return new BillingOrderData(clientOderId,orderPriceId,planId, clientId, startDate,
					nextBillableDate, endDate, billingFrequency, chargeCode,
					chargeType, chargeDuration, durationType, invoiceTillDate,
					price, billingAlign,billStartDate,billEndDate,orderStatus,taxInclusive);
		}

		public String billingOrderSchema() {

			return " co.id as clientOrderId,op.id AS orderPriceId,co.plan_id as planId,co.client_id AS clientId,co.start_date AS startDate,IFNULL(op.next_billable_day, co.start_date) AS nextBillableDate,"
					+ "co.end_date AS endDate,co.billing_frequency AS billingFrequency,op.charge_code AS chargeCode,op.charge_type AS chargeType,"
					+ "op.charge_duration AS chargeDuration,op.duration_type AS durationType,op.invoice_tilldate AS invoiceTillDate,op.price AS price,co.order_status as orderStatus,op.tax_inclusive as taxInclusive, "
					+ "co.billing_align AS billingAlign,op.bill_start_date as billStartDate,Date_format(IFNULL(op.bill_end_date,'3099-12-31'), '%Y-%m-%d') AS billEndDate "
					+ "FROM b_orders co left JOIN b_order_price op ON co.id = op.order_id"
					+ " WHERE co.client_id = ? AND co.id = ? AND Date_format(IFNULL(op.invoice_tilldate,? ),'%Y-%m-%d') <= ? "
					+ " AND Date_format(IFNULL(op.next_billable_day, co.start_date ), '%Y-%m-%d')  <= Date_format(IFNULL(op.bill_end_date,'3099-12-31'), '%Y-%m-%d')";
		}

	}

	/* (non-Javadoc)
	 * @see #retrieveTaxMappingData(java.lang.Long, java.lang.String)
	 * regional base taxes
	 */
	@Override
	public List<TaxMappingRateData> retrieveTaxMappingData(final Long clientId,final String chargeCode) {
		
		try{
			
		final TaxMappingMapper taxMappingMapper = new TaxMappingMapper();
		final String sql = "select" + taxMappingMapper.taxMappingSchema()
				          + " AND CA.client_id = ?  AND tm.charge_code =? AND pd.state_id =S.id";
		return this.jdbcTemplate.query(sql, taxMappingMapper,new Object[] { clientId,chargeCode });
	   }catch(EmptyResultDataAccessException accessException){
	       return null;	
	   }
	}
	private static final class TaxMappingMapper implements RowMapper<TaxMappingRateData> {

		@Override
		public TaxMappingRateData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String chargeCode = rs.getString("chargeCode");
			final String taxCode = rs.getString("taxCode");
			final Date startDate = rs.getDate("startDate");
			final BigDecimal rate = rs.getBigDecimal("rate");
			final String taxType= rs.getString("type");

			return new TaxMappingRateData(id, chargeCode, taxCode, startDate,rate,taxType);
		}

		public String taxMappingSchema() {

			return " tm.id AS id,tm.charge_code AS chargeCode,tm.tax_code AS taxCode,tm.start_date AS startDate,tm.type AS type,tm.rate AS rate" +
					" FROM b_state S,b_tax_mapping_rate tm,b_priceregion_detail pd,b_priceregion_master prm,b_client_address CA WHERE  pd.priceregion_id = tm.tax_region_id" +
					" AND prm.id = pd.priceregion_id AND CA.state = S.state_name";
		}

	}
	
	
	/* (non-Javadoc)
	 * #retrieveDefaultTaxMappingData(java.lang.Long, java.lang.String)
	 * default tax
	 */
	@Override
	public List<TaxMappingRateData> retrieveDefaultTaxMappingData(final Long clientId, final String chargeCode) {

		try{
		
		final TaxMappingMapper taxMappingMapper = new TaxMappingMapper();
		final String sql = "select" + taxMappingMapper.taxMappingSchema()
				          + " AND CA.client_id = ?  AND tm.charge_code =? AND pd.state_id =0";
		return this.jdbcTemplate.query(sql, taxMappingMapper,new Object[] { clientId,chargeCode });
		
	}catch(EmptyResultDataAccessException accessException){
	  return null;	
	}
		
}
	/* (non-Javadoc)
	 * @see #retrieveDiscountOrders(java.lang.Long, java.lang.Long)
	 * order discount data
	 */
	@Override
	public List<DiscountMasterData> retrieveDiscountOrders(final Long orderId,final Long orderPriceId) {
		
		final DiscountOrderMapper discountOrderMapper = new DiscountOrderMapper();
		final String sql = "select " + discountOrderMapper.discountOrderSchema();
		return this.jdbcTemplate.query(sql, discountOrderMapper,new Object[] {orderId,orderPriceId});

	}
	
	private static final class DiscountOrderMapper implements RowMapper<DiscountMasterData> {

		@Override
		public DiscountMasterData mapRow(ResultSet rs, int rowNum) throws SQLException {
			
			final Long discountId = rs.getLong("discountId");
			final Long orderPriceId = rs.getLong("orderPriceId");
			final Long orderDiscountId = rs.getLong("orderDiscountId");
			final LocalDate discountStartDate = JdbcSupport.getLocalDate(rs,"discountStartDate");
			final LocalDate discountEndDate = JdbcSupport.getLocalDate(rs,"discountEndDate");
			final String discountType = rs.getString("discountType");
			final BigDecimal discountRate = rs.getBigDecimal("discountRate");
			final String isDeleted = rs.getString("isDeleted");
			
			return new DiscountMasterData(discountId, orderPriceId,orderDiscountId, 
					      discountStartDate, discountEndDate,discountType, discountRate, isDeleted);
		}

		public String discountOrderSchema() {
			
			return    " od.id AS orderDiscountId,od.orderprice_id AS orderPriceId,od.discount_id AS discountId,od.discount_startdate AS discountStartDate,"
					+ " od.discount_enddate AS discountEndDate,od.discount_type AS discountType,od.discount_rate AS discountRate,od.is_deleted AS isDeleted "
					+ " FROM b_orders os inner join b_order_price op on op.order_id = os.id inner join b_order_discount od on od.order_id = os.id and od.orderprice_id=op.id "
					+ " WHERE od.is_deleted='N' and os.id= ? and op.id= ? ";

		}
	}
	
	/* (non-Javadoc)
	 * @see #getReverseBillingOrderData(java.lang.Long, org.joda.time.LocalDate, java.lang.Long)
	 * for disconnect
	 */
	@Override
	public List<BillingOrderData> getReverseBillingOrderData(final Long clientId,final LocalDate disconnectionDate, final Long orderId) {

		final ReverseBillingOrderMapper billingOrderMapper = new ReverseBillingOrderMapper();
		final String sql = "select " + billingOrderMapper.billingOrderSchema();
		return this.jdbcTemplate.query(sql, billingOrderMapper,new Object[] { clientId,orderId });
	}

	private static final class ReverseBillingOrderMapper implements	RowMapper<BillingOrderData> {

		@Override
		public BillingOrderData mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			
			final Long clientOderId = resultSet.getLong("clientOrderId");
			final Long orderPriceId = resultSet.getLong("orderPriceId");
			final Long planId = resultSet.getLong("planId");
			final Long clientId = resultSet.getLong("clientId");
			final Date startDate = resultSet.getDate("startDate");
			final Date nextBillableDate = resultSet.getDate("nextBillableDate");
			final Date endDate = resultSet.getDate("endDate");
			final String billingFrequency = resultSet.getString("billingFrequency");
			final String chargeCode = resultSet.getString("chargeCode");
			final String chargeType = resultSet.getString("chargeType");
			final Integer chargeDuration = resultSet.getInt("chargeDuration");
			final String durationType = resultSet.getString("durationType");
			final Date invoiceTillDate = resultSet.getDate("invoiceTillDate");
			final BigDecimal price = resultSet.getBigDecimal("price");
			final String billingAlign = resultSet.getString("billingAlign");
			final Date billStartDate = resultSet.getDate("billStartDate");
			final Date billEndDate = resultSet.getDate("billEndDate");
			final Long orderStatus = resultSet.getLong("orderStatus");
			final Integer taxInclusive = resultSet.getInt("taxInclusive");
			//Long invoiceId = resultSet.getLong("invoiceId");
			return new BillingOrderData(clientOderId,orderPriceId,planId, clientId, startDate,nextBillableDate, endDate, billingFrequency,
					chargeCode,chargeType, chargeDuration, durationType, invoiceTillDate,price, billingAlign,billStartDate,billEndDate,orderStatus,
					taxInclusive,null);
		}

		public String billingOrderSchema() {

			return " co.id as clientOrderId,op.id AS orderPriceId,co.plan_id as planId,co.client_id AS clientId,co.start_date AS startDate,IFNULL(op.next_billable_day, co.start_date) AS nextBillableDate,"
					+ "co.end_date AS endDate,co.billing_frequency AS billingFrequency,op.charge_code AS chargeCode,op.charge_type AS chargeType,"
					+ "op.charge_duration AS chargeDuration,op.duration_type AS durationType,op.invoice_tilldate AS invoiceTillDate,op.price AS price,co.order_status as orderStatus,op.tax_inclusive as taxInclusive,"
					+ "co.billing_align AS billingAlign,op.bill_start_date as billStartDate,Date_format(IFNULL(op.bill_end_date,'3099-12-31'), '%Y-%m-%d') AS billEndDate "
					+ "FROM b_orders co left JOIN b_order_price op ON co.id = op.order_id"
					+ " WHERE co.client_id = ? AND co.id = ?"/* AND Date_format(IFNULL(op.invoice_tilldate,now() ),'%Y-%m-%d') >= ? "*/;
		}
	}
	 
	 /* (non-Javadoc)
	 * @see #retriveExemptionTaxDetails(java.lang.Long)
	 */
	@Override
		public TaxMappingRateData retriveExemptionTaxDetails(final Long clientId) {
		 
		 try{
			 
			final taxMapper mapper=new taxMapper();
			final String sql= "select exempt_tax as taxExemption from m_client where id=?";
			return this.jdbcTemplate.queryForObject(sql, mapper,new Object[]{clientId});
		   }catch(EmptyResultDataAccessException e){
			 return null;
		   }
		 }
	 
		private final static class taxMapper implements RowMapper<TaxMappingRateData>
		  {
			@Override
			public TaxMappingRateData mapRow(ResultSet rs, int rowNum)throws SQLException {
				final String taxExemption=rs.getString("taxExemption");
				return new TaxMappingRateData(taxExemption);
			}
		}
		
	@Override
	public AgreementData retriveClientOfficeDetails(final Long clientId) {

		try {

			final OfficeMapper mapper = new OfficeMapper();
			final String sql = "select " + mapper.schema() + " where c.id = ?";
			return this.jdbcTemplate.queryForObject(sql, mapper,new Object[] { clientId });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private final static class OfficeMapper implements RowMapper<AgreementData> {

		public String schema() {

			return " o.id as officeId, co.code_value as officeType,a.id as agreementId from m_office o inner join m_client c ON o.id = c.office_id "
					+ " left join m_office_agreement a ON o.id = a.office_id and a.agreement_status='signed' left join m_code_value co ON co.id = o.office_type ";
		}

		@Override
		public AgreementData mapRow(ResultSet rs, int rowNum) throws SQLException {
			final Long officeId = rs.getLong("officeId");
			final Long agreementId = rs.getLong("agreementId");
			final String officeType = rs.getString("officeType");
			return new AgreementData(officeId, officeType, agreementId);
		}
	}

	@Override
	public AgreementData retrieveOfficeChargesCommission(Long chareId) {

		try {
			final CommisionMapper mapper = new CommisionMapper();
			final String sql = "Select * from  v_office_commission  v where v.charge_id=? and v.amt <> 0";
			return this.jdbcTemplate.queryForObject(sql, mapper,new Object[] {chareId});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private final static class CommisionMapper implements RowMapper<AgreementData> {

		@Override
		public AgreementData mapRow(ResultSet rs, int rowNum)throws SQLException {
			final Long chargeId = rs.getLong("charge_id");
			final Long officeId = rs.getLong("office_id");
			final LocalDate invoiceDate = JdbcSupport.getLocalDate(rs,"invoice_date");
			final Long source = rs.getLong("source");
			final BigDecimal shareAmount = rs.getBigDecimal("share_amount");
			final String shareType = rs.getString("share_type");
			final String commisionSource = rs.getString("comm_source");
			final BigDecimal commisionAmount = rs.getBigDecimal("amt");
			return new AgreementData(chargeId, officeId, invoiceDate, source,
					shareAmount, shareType, commisionSource, commisionAmount);
		}
	}

	@Override
	public List<Long> listOfInvoices(Long clientId, Long orderId) {
		
		final ListInvoiceMapper invoicemapper = new ListInvoiceMapper();
		final String sql = "select" + invoicemapper.schema();
		return this.jdbcTemplate.query(sql, invoicemapper,new Object[] { clientId, orderId});
		
	}
	
	private static final class ListInvoiceMapper implements RowMapper<Long> {

		@Override
		public Long  mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			
			final Long invoiceId = resultSet.getLong("invoiceId");
			
			return invoiceId;
		}

		public String schema() {
			
			return " i.id as invoiceId FROM b_charge c, b_invoice i "+
					" WHERE c.client_id = ? and c.invoice_id = i.id and c.order_id = ? and c.charge_end_date > now() " +
					" and c.charge_type = 'RC' order by i.id desc; ";
		}
		
	}

}



/*@Override
		public List<OrderPriceData> retrieveInvoiceTillDate(final Long orderId) {

			final OrderPriceMapper orderPriceMapper = new OrderPriceMapper();
			final String sql = "select " + orderPriceMapper.orderPriceSchema();
			return this.jdbcTemplate.query(sql, orderPriceMapper,new Object[] { orderId });

		}

		private static final class OrderPriceMapper implements RowMapper<OrderPriceData> {

			@Override
			public OrderPriceData mapRow(ResultSet rs, int rowNum)throws SQLException {
				
				final Long id = rs.getLong("orderPriceId");
				final Long orderId = rs.getLong("orderId");
				final Long serviceId = rs.getLong("serviceId");
				final String chargeCode = rs.getString("chargeCode");
				final String chargeType = rs.getString("chargeType");
				final String chargeDuration = rs.getString("chargeDuration");
				final String durationType = rs.getString("durationType");
				final Date invoiceTillDate = rs.getDate("invoiceTillDate");
				final BigDecimal price = rs.getBigDecimal("price");
				final Long createdbyId = rs.getLong("createdId");
				final Date createdDate = rs.getDate("createdDate");
				final Date lastModefiedDate = rs.getDate("lastModefiedDate");
				final Long lastModefiedId = rs.getLong("lastModefiedId");
				return new OrderPriceData(id, orderId, serviceId, chargeCode,
						chargeType, chargeDuration, durationType, invoiceTillDate,
						price, createdbyId, createdDate, lastModefiedDate,
						lastModefiedId);
			}

			public String orderPriceSchema() {
				return " op.id as orderPriceId,op.order_id as orderId,op.service_id as serviceId,"
						+ " op.charge_code as chargeCode,op.charge_type as chargeType,op.charge_duration as chargeDuration,"
						+ " op.duration_type as durationType,op.invoice_tilldate as invoiceTillDate,"
						+ " op.price as price,op.createdby_id as createdId,op.created_date as createdDate,"
						+ " op.lastmodified_date lastModefiedDate,op.lastmodifiedby_id as lastModefiedId "
						+ "FROM b_order_price op WHERE order_id = ? ";

			}
		}
		
		
		@Override
		public List<GenerateInvoiceData> retrieveClientsWithOrders(LocalDate processDate) {
			
			AppUser user = this.context.authenticatedUser();
			final OrderMapper mapper = new OrderMapper();
			final String schema = "Select " + mapper.schema() + " and o.createdby_id = ? ";
			return this.jdbcTemplate.query(schema, mapper, new Object[]{processDate.toDate(),processDate.toDate(),user.getId()});
		}
		
		 private static final class OrderMapper implements RowMapper<GenerateInvoiceData> {
				
				public String schema() {
					return " o.client_id as clientId, o.next_billable_day as nextBillableDay from b_orders o join m_client c on c.id=o.client_id  and Date_format(IFNULL(o.next_billable_day, ?), '%Y-%m-%d') <= ? ";
				}

				@Override
				public GenerateInvoiceData mapRow(ResultSet rs, int rowNum) throws SQLException {
					
					final Long clientId = rs.getLong("clientId");
					final Date nextBillableDay = rs.getDate("nextBillableDay");
					return new GenerateInvoiceData(clientId, nextBillableDay,null); 
				}
				
			}*/	

   
