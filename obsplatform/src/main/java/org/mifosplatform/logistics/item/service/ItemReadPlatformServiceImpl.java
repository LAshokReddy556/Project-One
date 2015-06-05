package org.mifosplatform.logistics.item.service;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.mifosplatform.billing.chargecode.data.ChargesData;
import org.mifosplatform.cms.mediadetails.data.MediaLocationData;
import org.mifosplatform.crm.clientprospect.service.SearchSqlQuery;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.logistics.item.data.ItemData;
import org.mifosplatform.logistics.item.data.UniteTypeData;
import org.mifosplatform.logistics.item.domain.ItemEnumType;
import org.mifosplatform.logistics.item.domain.ItemTypeData;
import org.mifosplatform.logistics.item.domain.UnitEnumType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class ItemReadPlatformServiceImpl implements ItemReadPlatformService{
	
	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final PaginationHelper<ItemData> paginationHelper = new PaginationHelper<ItemData>();

	@Autowired
	public ItemReadPlatformServiceImpl(final PlatformSecurityContext context,final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	


	@Override
	public List<EnumOptionData> retrieveItemClassType() {
		 final EnumOptionData hardware = ItemTypeData.ItemClassType(ItemEnumType.HARDWARE);
		 final EnumOptionData prepaidCard = ItemTypeData.ItemClassType(ItemEnumType.PREPAID_CARD);
	     final EnumOptionData softCharge = ItemTypeData.ItemClassType(ItemEnumType.SOFT_CHARGE);
	     final EnumOptionData event = ItemTypeData.ItemClassType(ItemEnumType.EVENT);
	     final List<EnumOptionData> categotyType = Arrays.asList(hardware, prepaidCard,softCharge,event);
	    return categotyType;
	 }

	@Override
	public List<EnumOptionData> retrieveUnitTypes() {
		final EnumOptionData meters = UniteTypeData.UnitClassType(UnitEnumType.METERS);
		final EnumOptionData pieces = UniteTypeData.UnitClassType(UnitEnumType.PIECES);
		final EnumOptionData hours = UniteTypeData.UnitClassType(UnitEnumType.HOURS);
		final EnumOptionData days = UniteTypeData.UnitClassType(UnitEnumType.DAYS);
		final List<EnumOptionData> categotyType = Arrays.asList(meters, pieces, hours, days);
		return categotyType;
	}



	@Override
	public List<ChargesData> retrieveChargeCode() {
		final String sql = "select s.id as id,s.charge_code as charge_code,s.charge_description as charge_description from b_charge_codes s  " +
		 		"where s.charge_type='NRC'";
		 
		 final RowMapper<ChargesData> rm = new ChargeMapper();
        return this.jdbcTemplate.query(sql, rm, new Object[] {});
     }


 private static final class ChargeMapper implements RowMapper<ChargesData> {

        @Override
        public ChargesData mapRow(final ResultSet rs,final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String chargeCode = rs.getString("charge_code");
            final String chargeDesc= rs.getString("charge_description");
            return new ChargesData(id,chargeCode,chargeDesc);
        }
}


@Override
public List<ItemData> retrieveAllItems() {
	
	context.authenticatedUser();
	SalesDataMapper mapper = new SalesDataMapper();
	String sql = "select " + mapper.schema()+" where  a.is_deleted='n'";
	return this.jdbcTemplate.query(sql, mapper, new Object[] {  });
}

private static final class SalesDataMapper implements
		RowMapper<ItemData> {

	public String schema() {
		return " a.id as id,a.item_code as itemCode,a.item_description as itemDescription,a.item_class as itemClass,a.units as units,a.charge_code as chargeCode,round(a.unit_price,2) price,a.warranty as warranty,a.reorder_level as reorderLevel,"+
				"b.Used as used,b.Available as available," +
				" b.Total_items as totalItems from b_item_master a "+
				"left join ( Select item_master_id,Sum(Case When Client_id IS NULL "+
                "        Then 1 "+
                "        Else 0 "+
                " End) Available,"+
                "Sum(Case When Client_id Is Not NULL "+
                "         Then 1 "+
                "        Else 0 "+
                " End) Used,"+
                "Count(1) Total_items "+
                "From b_item_detail group by item_master_id ) b on a.id=b.item_master_id ";
		
	}
	public String schemaWithClientId(final Long clientId, Long itemId) {
		
		/*return " a.id AS id,a.item_code AS itemCode,a.item_description AS itemDescription,a.item_class AS itemClass,a.units AS units, "+
				"a.charge_code AS chargeCode,ifnull(round(p.price , 2),a.unit_price ) as price,a.warranty AS warranty,b.Used AS used,b.Available AS available,a.reorder_level as reorderLevel, "+
				"b.Total_items AS totalItems FROM b_item_master a "+
				"LEFT JOIN(SELECT item_master_id,Sum(CASE WHEN Client_id IS NULL THEN 1 ELSE 0 END) Available, "+
                "Sum(CASE WHEN Client_id IS NOT NULL THEN 1 ELSE 0 END) Used, "+
                "Count(1) Total_items FROM b_item_detail GROUP BY item_master_id) b "+
                "ON a.id = b.item_master_id "+
                "left join b_client_address ca on ca.client_id = "+clientId+" "+
                "left join b_state s on s.state_name = ca.state "+
                "left join b_priceregion_detail pd on (pd.state_id = s.id or (pd.state_id = 0 and pd.country_id = s.parent_code ) ) "+
                "left join b_priceregion_master prm ON prm.id = pd.priceregion_id "+
                "left join b_item_price p on (p.item_id = a.id and p.region_id = prm.id and p.is_deleted='N' ) ";*/
		
		return " a.id AS id,a.item_code AS itemCode,a.item_description AS itemDescription,a.item_class AS itemClass,a.units AS units," +
				" a.charge_code AS chargeCode,round(p.price , 2) as price,(SELECT 1 FROM b_grn WHERE item_master_id="+itemId+"  LIMIT 1) as isActive, " +
				"a.warranty AS warranty,b.Used AS used,b.Available AS available,a.reorder_level as reorderLevel," +
				" b.Total_items AS totalItems FROM b_item_master a" +
				" LEFT JOIN(SELECT item_master_id,Sum(CASE WHEN Client_id IS NULL THEN 1 ELSE 0 END) Available, Sum(CASE WHEN Client_id IS NOT NULL THEN 1 ELSE 0 END) Used," +
				" Count(1) Total_items FROM b_item_detail GROUP BY item_master_id) b ON a.id = b.item_master_id" +
				" left join b_client_address ca on ca.client_id = "+clientId+" left join b_state s on s.state_name = ca.state" +
				" left join b_priceregion_detail pd on (pd.state_id = ifnull((SELECT DISTINCT c.id FROM b_item_price a, b_priceregion_detail b, b_state c," +
				" b_client_address d " +
				" WHERE b.priceregion_id = a.region_id AND b.state_id = c.id   AND a.region_id  = b.priceregion_id" +
				" AND d.state = c.state_name AND d.address_key = 'PRIMARY' AND d.client_id = "+clientId+" and a.item_id  ="+itemId+"),0) and pd.country_id = ifnull((SELECT DISTINCT c.id" +
				" FROM b_item_price a,b_priceregion_detail b,b_country c, b_state s,b_client_address d " +
				" WHERE b.priceregion_id = a.region_id AND b.country_id = c.id AND c.country_name = d.country AND d.address_key = 'PRIMARY'" +
				" AND d.client_id ="+clientId+" and a.item_id  = "+itemId+" and  d.state = s.state_name and (s.id =b.state_id or(b.state_id = 0 and b.country_id = c.id ))), 0)) " +
				" left join b_priceregion_master prm ON prm.id = pd.priceregion_id " +
				" left join b_item_price p on (p.item_id = a.id and p.region_id = prm.id and p.is_deleted='N') ";
	}
	
	public String schemaWithRegion(String region, Long itemId) {
		
		return " a.id AS id,a.item_code AS itemCode,a.item_description AS itemDescription,a.item_class AS itemClass,a.units AS units,a.charge_code AS chargeCode," +
			   " round(p.price, 2) AS price,(SELECT 1 FROM b_grn WHERE item_master_id = "+itemId+" LIMIT 1) AS isActive,a.warranty AS warranty,b.Used AS used," +
			   " b.Available AS available,a.reorder_level AS reorderLevel,b.Total_items AS totalItems " +
			   " FROM b_item_master a LEFT JOIN (SELECT item_master_id, Sum(CASE WHEN Client_id IS NULL THEN 1 ELSE 0 END) Available," +
			   " Sum(CASE WHEN Client_id IS NOT NULL THEN 1 ELSE 0 END) Used, Count(1) Total_items FROM b_item_detail GROUP BY item_master_id) b ON a.id = b.item_master_id" +
			   " LEFT JOIN b_state s ON s.state_name ='"+region+"' LEFT JOIN b_priceregion_detail pd ON ( pd.state_id = ifnull((SELECT DISTINCT c.id FROM b_item_price a," +
			   " b_priceregion_detail b,b_state c WHERE b.priceregion_id = a.region_id AND b.state_id = c.id AND a.region_id = b.priceregion_id AND  c.state_name ='"+region+"'" +
			   " AND a.item_id = "+itemId+"),0) AND pd.country_id = ifnull((SELECT DISTINCT c.id FROM b_item_price a, b_priceregion_detail b, b_country c, b_state s " +
			   " WHERE b.priceregion_id = a.region_id AND b.country_id = c.id AND c.id = s.parent_code AND a.item_id = "+itemId+" AND  s.state_name='"+region+"'" +
			   " AND (s.id = b.state_id OR (b.state_id = 0 AND b.country_id = c.id))),0)) LEFT JOIN b_priceregion_master prm ON prm.id = pd.priceregion_id " +
			   " LEFT JOIN b_item_price p ON (p.item_id = a.id AND p.region_id = prm.id AND p.is_deleted = 'N') ";
	}

	@Override
	public ItemData mapRow(ResultSet rs, int rowNum)
			throws SQLException {

		final Long id = rs.getLong("id");
		final String itemCode = rs.getString("itemCode");
		final String itemDescription = rs.getString("itemDescription");
		final String itemClass = rs.getString("itemClass");
		final String units = rs.getString("units");
		final String chargeCode = rs.getString("chargeCode");
		final BigDecimal unitPrice = rs.getBigDecimal("price");
		final int warranty = rs.getInt("warranty");
		final Long used = rs.getLong("used");
		final Long available = rs.getLong("available");
		final Long totalItems = rs.getLong("totalItems");
		final Long reorderLevel = rs.getLong("reorderLevel");
		return new ItemData(id,itemCode,itemDescription,itemClass,units,chargeCode,warranty,unitPrice,used,available,totalItems, reorderLevel);


	}
	
	}


@Override
public ItemData retrieveSingleItemDetails(final Long clientId, final Long itemId,final String region, boolean isWithClientId) {
	try {
		context.authenticatedUser();
		SalesDataMapper mapper = new SalesDataMapper();
		String sql;
		if(isWithClientId){
			sql = "select " + mapper.schemaWithClientId(clientId,itemId)+" where a.id = ? and  a.is_deleted='n'  group by a.id"; 
		}else if(region != null){
			sql = "select " + mapper.schemaWithRegion(region,itemId)+" where a.id=? and  a.is_deleted='n'";
		}else{
			sql = "select " + mapper.schema()+" where a.id=? and  a.is_deleted='n'";
		}
	
		return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { itemId });
	} catch (EmptyResultDataAccessException e) {
		return null;
	}
}

@Override
public Page<ItemData> retrieveAllItems(SearchSqlQuery searchItems) {
	
	context.authenticatedUser();
	SalesDataMapper mapper = new SalesDataMapper();
	final StringBuilder sqlBuilder = new StringBuilder(200);
    sqlBuilder.append("select ");
    sqlBuilder.append(mapper.schema());
    sqlBuilder.append(" where a.is_deleted='n' ");
    
    String sqlSearch = searchItems.getSqlSearch();
    String extraCriteria = "";
    if (sqlSearch != null) {
    	sqlSearch=sqlSearch.trim();
    	extraCriteria = " and (a.item_description like '%"+sqlSearch+"%' OR" 
    			+ " a.item_code like '%"+sqlSearch+"%' )";
    			
    			
    }
        sqlBuilder.append(extraCriteria);
    
   /* if (StringUtils.isNotBlank(extraCriteria)) {
        sqlBuilder.append(extraCriteria);
    }*/


    if (searchItems.isLimited()) {
        sqlBuilder.append(" limit ").append(searchItems.getLimit());
    }

    if (searchItems.isOffset()) {
        sqlBuilder.append(" offset ").append(searchItems.getOffset());
    }

	return this.paginationHelper.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()",sqlBuilder.toString(),
            new Object[] {}, mapper);
}



@Override
public List<ItemData> retrieveAuditDetails(final Long itemId) {
	
	String sql="select bia.id as id,bia.itemmaster_id as itemMasterId,bia.item_code as itemCode,bia.unit_price as unitPrice,bia.region_id as regionId,"+
				"bia.changed_date as changedDate from b_item_audit bia where itemmaster_id=?";
	
	final RowMapper<ItemData> rm = new AuditMapper();

    return this.jdbcTemplate.query(sql, rm, new Object[] {itemId});
	
}
private static final class AuditMapper implements RowMapper<ItemData> {

    @Override
    public ItemData mapRow(final ResultSet rs,final int rowNum) throws SQLException {
    	
    	final Long id = rs.getLong("id");
    	final Long itemMasterId = rs.getLong("itemMasterId");
    	final String itemCode = rs.getString("itemCode");
    	final BigDecimal unitPrice = rs.getBigDecimal("unitPrice");
    	final Date changedDate = rs.getDate("changedDate");
    	final Long regionId = rs.getLong("regionId");
        return new ItemData(id,itemMasterId,itemCode,unitPrice,changedDate, regionId);
    }
}


@Override
public List<ItemData> retrieveItemPrice(Long itemId) {
	try{
		final RetrieveItemPriceDataMapper mapper = new RetrieveItemPriceDataMapper();			
		final String sql = "select " + mapper.scheme() + itemId+" and is_deleted = 'N'";
    	return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	
	}catch (final EmptyResultDataAccessException e) {
	    return null;
	}
}

private static final class RetrieveItemPriceDataMapper implements RowMapper<ItemData> {

	public String scheme() {
		return " id as id,item_id as itemId,region_id as regionId,price as price from b_item_price where item_id = ";
	}
	
	@Override
	public ItemData mapRow(final ResultSet resultSet, final int rowNum)
			throws SQLException {
		
		final Long id = resultSet.getLong("id");
		final Long itemId = resultSet.getLong("itemId");
		final Long regionId = resultSet.getLong("regionId");
		final String price = resultSet.getString("price");
		
		
		return new ItemData(id, itemId, regionId, price);
	}
}

}


