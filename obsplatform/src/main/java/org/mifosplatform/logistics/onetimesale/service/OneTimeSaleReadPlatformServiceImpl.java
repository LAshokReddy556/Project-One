package org.mifosplatform.logistics.onetimesale.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.item.data.ItemData;
import org.mifosplatform.logistics.onetimesale.data.AllocationDetailsData;
import org.mifosplatform.logistics.onetimesale.data.OneTimeSaleData;
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
public class OneTimeSaleReadPlatformServiceImpl implements	OneTimeSaleReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;

	@Autowired
	public OneTimeSaleReadPlatformServiceImpl(final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);

	}

	/* (non-Javadoc)
	 * @see #retrieveItemData()
	 */
	@Override
	public List<ItemData> retrieveItemData() {
		
		 this.context.authenticatedUser();
		 final ItemMapper mapper = new ItemMapper();
		 final String sql = "select " + mapper.schema();

		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	private static final class ItemMapper implements RowMapper<ItemData> {

		public String schema() {
			
			return "i.id AS id,i.item_description AS itemCode,i.units AS units,i.unit_price AS unitPrice" +
					" FROM b_item_master i  where i.is_deleted='N'";
		}

		@Override
		public ItemData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String itemCode = rs.getString("itemCode");
			final String units = rs.getString("units");
			final BigDecimal unitPrice = rs.getBigDecimal("unitPrice");

			return new ItemData(id, itemCode, units, units, units, null, 0,unitPrice,null,null,null,null);
		}
	}

	/* (non-Javadoc)
	 * @see #retrieveClientOneTimeSalesData(java.lang.Long)
	 */
	@Override
	public List<OneTimeSaleData> retrieveClientOneTimeSalesData(final Long clientId) {
		
		this.context.authenticatedUser();
		final SalesDataMapper mapper = new SalesDataMapper();
		final String sql = "select " + mapper.schema()
				+ " where o.item_id=i.id  and o.client_id=? and o.is_deleted = 'N' group by o.id order by o.id";

		return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId });
	}

	private static final class SalesDataMapper implements RowMapper<OneTimeSaleData> {

		public String schema() {
			
			return "o.id AS id,i.item_code AS itemCode, i.item_class as itemClass, a.serial_no as serialNo,o.sale_date as saleDate,o.charge_code AS chargeCode,"
					+ "o.quantity as quantity,o.total_price as totalPrice,o.hardware_allocated as hardwareAllocated,o.units as units,o.device_mode as saleType,id.warranty_date as warrantyDate "
					+ "  FROM b_item_master i,b_onetime_sale o" +
					" left join b_allocation a on a.order_id=o.id and a.is_deleted = 'N' "+
					" left join   b_item_detail id ON id.serial_no = a.serial_no AND id.is_deleted = 'N'  ";

		}

		@Override
		public OneTimeSaleData mapRow(ResultSet rs, int rowNum)throws SQLException {

			final Long id = rs.getLong("id");
			final LocalDate saleDate = JdbcSupport.getLocalDate(rs, "saleDate");
			final String itemCode = rs.getString("itemCode");
			final String chargeCode = rs.getString("chargeCode");
			final String quantity = rs.getString("quantity");
			final BigDecimal totalPrice = rs.getBigDecimal("totalPrice");
			final String haardwareAllocated = rs.getString("hardwareAllocated");
			final String itemClass = rs.getString("itemClass");
			final String serialNo = rs.getString("serialNo");
			final String units = rs.getString("units");
			final String saleType = rs.getString("saleType");
			final LocalDate warrantyDate = JdbcSupport.getLocalDate(rs, "warrantyDate");
			return new OneTimeSaleData(id, saleDate, itemCode, chargeCode,quantity, totalPrice,haardwareAllocated,itemClass,serialNo,units,saleType,warrantyDate);

		}

	}
	
	
	/* (non-Javadoc)
	 * @see #retrieveOnetimeSalesForInvoice(java.lang.Long)
	 */
	@Override
	public List<OneTimeSaleData> retrieveOnetimeSalesForInvoice(final Long clientId) {
		
		this.context.authenticatedUser();
		final OneTimeSalesDataMapper mapper = new OneTimeSalesDataMapper();
	    final String sql = "select " + mapper.schema() + " and ots.is_invoiced='N' and ots.client_id = ? ";

		return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId });
	}

	private static final class OneTimeSalesDataMapper implements RowMapper<OneTimeSaleData> {

		public String schema() {
			return " ots.id as oneTimeSaleId, ots.client_id AS clientId,ots.units AS units,ots.charge_code AS chargeCode,ots.unit_price AS unitPrice,"+
				   " ots.quantity AS quantity,ots.total_price AS totalPrice,ots.is_invoiced as isInvoiced,ots.item_id as itemId,ots.discount_id as discountId,"+
				   "  cc.tax_inclusive as taxInclusive,cc.charge_type as chargeType FROM b_onetime_sale ots,b_charge_codes cc where ots.charge_code=cc.charge_code ";

		}

		@Override
		public OneTimeSaleData mapRow(ResultSet rs, int rowNum)throws SQLException {

			final Long oneTimeSaleId = rs.getLong("oneTimeSaleId");
			final Long clientId = rs.getLong("clientId");
			final String units = rs.getString("units");
			final String chargeCode = rs.getString("chargeCode");
			final BigDecimal unitPrice = rs.getBigDecimal("unitPrice");
			final String quantity = rs.getString("quantity");
			final BigDecimal totalPrice = rs.getBigDecimal("totalPrice");
			final String isInvoiced = rs.getString("isInvoiced");
			final Long itemId = rs.getLong("itemId");
			final Long discountId=rs.getLong("discountId");
			final Integer taxInclusive = rs.getInt("taxInclusive");
			final String chargeType = rs.getString("chargeType");
			return new OneTimeSaleData(oneTimeSaleId,clientId, units, chargeCode,chargeType, unitPrice,quantity, totalPrice,isInvoiced,
					                    itemId,discountId,taxInclusive);

		}

	}

	/* (non-Javadoc)
	 * @see #retrieveSingleOneTimeSaleDetails(java.lang.Long)
	 */
	@Override
	public OneTimeSaleData retrieveSingleOneTimeSaleDetails(final Long saleId) {
		
		this.context.authenticatedUser();
		final OneTimeSalesDataMapper mapper = new OneTimeSalesDataMapper();
		final String sql = "select " + mapper.schema() + " where ots.id = ? ";

		return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { saleId });
	}

	/* (non-Javadoc)
	 * @see #retrieveAllocationDetails(java.lang.Long)
	 */
	@Override
	public List<AllocationDetailsData> retrieveAllocationDetails(final Long orderId) {
		
		this.context.authenticatedUser();
		final AllocationDataMapper mapper = new AllocationDataMapper();
		final String sql = "select " + mapper.schema()+ " and a.order_id=? ";

		return this.jdbcTemplate.query(sql, mapper, new Object[] { orderId });
	}

	private static final class AllocationDataMapper implements	RowMapper<AllocationDetailsData> {

		public String schema() {
			return "  a.id AS id,id.id AS itemDetailId,id.quality as quality,a.status as hardwareStatus,i.item_description AS itemDescription,a.serial_no AS serialNo,a.allocation_date AS allocationDate" +
					" FROM b_allocation a, b_item_master i, b_item_detail id WHERE  a.item_master_id = i.id   AND a.is_deleted = 'N' and id.client_id = a.client_id " +
					"  and id.serial_no = a.serial_no ";

		}
		
		public String schemaForUnallocated(Long clientId) {
			return "  a.id AS id,id.id AS itemDetailId,id.quality as quality,a.status as hardwareStatus,"+
					" i.item_description AS itemDescription,a.serial_no AS serialNo,a.allocation_date AS allocationDate "+
					" FROM b_allocation a, b_item_master i, b_item_detail id "+
					" WHERE  a.item_master_id = i.id "+
					" and a.client_id = "+clientId+" and id.serial_no = a.serial_no ";

		}

		@Override
		public AllocationDetailsData mapRow(ResultSet rs, int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final Long itemDetailId = rs.getLong("itemDetailId");
			final String itemDescription = rs.getString("itemDescription");
            final String serialNo = rs.getString("serialNo");
		    final LocalDate allocationDate=JdbcSupport.getLocalDate(rs,"allocationDate");
		    final String quality = rs.getString("quality");
		    final String hardwareStatus = rs.getString("hardwareStatus");
		    
			return new AllocationDetailsData(id,itemDescription,serialNo,allocationDate,itemDetailId,null,quality,hardwareStatus);

		}
	}

	/* (non-Javadoc)
	 * @see #retrieveAllocationDetailsBySerialNo(java.lang.String)
	 */
	@Override
	public AllocationDetailsData retrieveAllocationDetailsBySerialNo(final String serialNo) {
 
		try{
			
		final AllocationDataMapper mapper = new AllocationDataMapper();
		final String sql = "select " + mapper.schema()+ " and a.serial_no=?";

		return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { serialNo });
		
		}catch(EmptyResultDataAccessException accessException){
			return null;
		}
	
	}
	
	@Override
	public List<AllocationDetailsData> retrieveUnAllocationDetails(final Long orderId, final Long clientId) {
		
		this.context.authenticatedUser();
		final AllocationDataMapper mapper = new AllocationDataMapper();
		final String sql = "select " + mapper.schemaForUnallocated(clientId)+ " and a.order_id=? ";

		return this.jdbcTemplate.query(sql, mapper, new Object[] { orderId });
	}
	
	}

