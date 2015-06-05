package org.mifosplatform.portfolio.property.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.billing.servicetransfer.data.ClientPropertyData;
import org.mifosplatform.crm.clientprospect.service.SearchSqlQuery;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.mifosplatform.portfolio.property.data.PropertyDefinationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class PropertyReadPlatformServiceImp implements PropertyReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final PaginationHelper<PropertyDefinationData> paginationHelper = new PaginationHelper<PropertyDefinationData>();

	@Autowired
	public PropertyReadPlatformServiceImp(final PlatformSecurityContext context,final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public Page<PropertyDefinationData> retrieveAllProperties(SearchSqlQuery searchPropertyDetails) {

		try {
			this.context.authenticatedUser();
			final PropertyMapper mapper = new PropertyMapper();	
			StringBuilder sqlBuilder = new StringBuilder(200);
			sqlBuilder.append("select ");
			sqlBuilder.append( mapper.schema() +" where pd.is_deleted='N' ");
			String sqlSearch = searchPropertyDetails.getSqlSearch();
			String extraCriteria = "";
			if(sqlSearch != null) {
			    	sqlSearch=sqlSearch.trim();
			    	extraCriteria = " and (pd.property_code like '%"+sqlSearch+"%' OR" 
			    			+ " pd.precinct like '%"+sqlSearch+"%' OR"
			    			+ " pd.status like '%"+sqlSearch+"%' )";
			    }
			
			sqlBuilder.append(extraCriteria);
			
		   if (searchPropertyDetails.isLimited()) {
		            sqlBuilder.append(" limit ").append(searchPropertyDetails.getLimit());
		        }

		   if (searchPropertyDetails.isOffset()) {
		            sqlBuilder.append(" offset ").append(searchPropertyDetails.getOffset());
		        }

	    	return this.paginationHelper.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()",sqlBuilder.toString(), new Object[] {}, mapper);
			    
		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}

	}

	private static final class PropertyMapper implements RowMapper<PropertyDefinationData> {

		public String schema() {
			return  " pd.id as Id,pd.property_type_id as propertyTypeId,c.code_value as propertyType,pd.property_code as propertyCode,unit_code as unitCode,pd.floor as floor," +
					 " pd.building_code as buildingCode, pd.parcel as parcel,pd.street as street,pd.precinct as precinct,pd.po_box as poBox, pd.state as state, "+
					 " pd.country as country, pd.status as status, ifnull(pd.client_id,'VACANT') AS clientId,pm.description  as floorDesc,pp.description  as parcelDesc" +
					 " from b_property_defination pd left join  b_property_master pm on (pd.floor = pm.code and pm.property_code_type='Level/Floor') "  +
					 " left join b_property_master pp on (pd.parcel = pp.code and pp.property_code_type='Parcel')"  +
					 " left join m_code_value c on c.id=pd.property_type_id";

		}

		@Override
		public PropertyDefinationData mapRow(ResultSet rs, int rowNum) throws SQLException {
			
			final Long Id =rs.getLong("Id");
			final Long propertyTypeId = rs.getLong("propertyTypeId");
			final String propertyType = rs.getString("propertyType");
			final String propertyCode = rs.getString("propertyCode");
			final String unitCode = rs.getString("unitCode");
			final String floor = rs.getString("floor");
			final String buildingCode = rs.getString("buildingCode");
			final String parcel = rs.getString("parcel");
			final String precinct = rs.getString("precinct");
			final String street = rs.getString("street");
			final String poBox = rs.getString("poBox");
			final String state = rs.getString("state");
			final String country = rs.getString("country");
			final String status = rs.getString("status");
			final String clientId = rs.getString("clientId");
			final String floorDesc = rs.getString("floorDesc");
			final String parcelDesc = rs.getString("parcelDesc");
			
			return new PropertyDefinationData(Id,propertyTypeId,propertyType,propertyCode,unitCode,floor,buildingCode,parcel,
					precinct,street,poBox,state,country,status,clientId,floorDesc,parcelDesc);
			
		}


	}

	@Override
	public List<PropertyDefinationData> retrieveAllPropertiesForSearch(final String propertyCode) {
		
            try{
			context.authenticatedUser();
			final PropertyMapper mapper = new PropertyMapper();
			final String sql = "SELECT " + mapper.schema() + " WHERE pd.client_id IS NULL AND pd.status='VACANT' AND pd.is_deleted='N' AND (pd.property_code LIKE '%"+propertyCode+"%') ORDER BY pd.id  LIMIT 15" ;
			return this.jdbcTemplate.query(sql, mapper, new Object[] {});
            }catch (EmptyResultDataAccessException accessException) {
    			return null;
    		}
            
	}

	@Override
	public PropertyDefinationData retrievePropertyDetails(final Long propertyId) {
		
			try {
				context.authenticatedUser();
				final PropertyMapper mapper = new PropertyMapper();
				final String sql = "select " + mapper.schema() + " where pd.id = ? and pd.is_deleted='N'";
				return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] {propertyId});
			} catch (EmptyResultDataAccessException accessException) {
				return null;
			}
		}

	@Override
	public Page<PropertyDefinationData> retrievePropertyHistory(SearchSqlQuery searchPropertyDetails) {
		
		try {
			context.authenticatedUser();
			final PropertyHistoryMapper mapper = new PropertyHistoryMapper();	
			StringBuilder sqlBuilder = new StringBuilder(200);
			sqlBuilder.append("select ");
			sqlBuilder.append( mapper.schema());
			String sqlSearch = searchPropertyDetails.getSqlSearch();
			String extraCriteria = "";
			if(sqlSearch != null) {
			    	sqlSearch=sqlSearch.trim();
			    	extraCriteria = " where (ph.property_code like '%"+sqlSearch+"%') order by ph.id desc  ";
			    }
			
			sqlBuilder.append(extraCriteria);
			
		   if (searchPropertyDetails.isLimited()) {
		            sqlBuilder.append(" limit ").append(searchPropertyDetails.getLimit());
		        }

		   if (searchPropertyDetails.isOffset()) {
		            sqlBuilder.append(" offset ").append(searchPropertyDetails.getOffset());
		        }

	    	return this.paginationHelper.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()",sqlBuilder.toString(), new Object[] {}, mapper);
			    
		} catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}
	
	
	private static final class PropertyHistoryMapper implements RowMapper<PropertyDefinationData> {

		public String schema() {
			return  " ph.id as Id,ph.ref_id as refId,ph.transaction_date as transactionDate,ph.property_code as propertyCode ,ph.ref_desc as description," +
                     " ph.client_id as clientId,if((ph.client_id is null),'VACANT','OCCUPIED') as status, mc.display_name as displayName "+
                     " from b_property_history ph join  b_property_defination pd on ph.ref_id = pd.id left join m_client mc on ph.client_id=mc.id  ";

		}

		@Override
		public PropertyDefinationData mapRow(ResultSet rs, int rowNum) throws SQLException {
			
			final Long Id =rs.getLong("Id");
			final Long refId = rs.getLong("refId");
			final String description = rs.getString("description");
			final String propertyCode = rs.getString("propertyCode");
			final String status = rs.getString("status");
			final String clientId = rs.getString("clientId");
			final LocalDate transactionDate=JdbcSupport.getLocalDate(rs,"transactionDate");
			final String clientName = rs.getString("displayName");
			
			return new PropertyDefinationData(Id,refId,description,propertyCode,status,clientId,transactionDate,clientName);
			
		}


	}

	@Override
	public ClientPropertyData retrieveClientPropertyDetails(final Long clientId) {
		
		 try{
				context.authenticatedUser();
				final ClientPropertyMapper mapper = new ClientPropertyMapper();
				final String sql = "SELECT " + mapper.schema() + " WHERE pd.client_id = ? group by pd.id" ;
				return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] {clientId});
	            }catch (EmptyResultDataAccessException accessException) {
	    			return null;
	    		}
	}
	
	private static final class ClientPropertyMapper implements RowMapper<ClientPropertyData> {

		public String schema() {
			return " pd.id as Id,m.account_no as clientId, pd.property_type_id as propertyTypeId,c.code_value as propertyType, pd.property_code as propertyCode, "+
				    " unit_code as unitCode, pd.floor as floor,pp.description as floorDesc, pd.building_code as buildingCode,pd.parcel as parcel,pm.description as parcelDesc, pd.street as street,pd.precinct as precinct, " +
				    " pd.po_box as zip, pd.state as state,pd.country as country,pd.status as status,m.firstname as firstName,m.lastname as lastName, "+
				    " m.display_name as displayName,m.email as email,ma.address_no as addressNo,address_key as addressKey,mc.code_value as categoryType "+
			 	    " from b_property_defination pd join m_client m on (pd.client_id = m.id and pd.is_deleted='N') join b_client_address ma " +
				    " on (ma.client_id = m.id and ma.address_no = pd.property_code and ma.is_deleted='n'  and  ma.address_key='PRIMARY') left join m_code_value c on (c.id = pd.property_type_id)" +
				    " left join b_property_master pm on (pd.parcel=pm.code and pm.is_deleted='N' and pm.property_code_type='Parcel')  " +
				    " left join b_property_master pp on (pd.floor=pp.code and pp.is_deleted='N' and pp.property_code_type='Level/Floor')  "+
				    " left  join  m_code_value mc on  mc.id =m.category_type  ";
		}

		@Override
		public ClientPropertyData mapRow(ResultSet rs, int rowNum) throws SQLException {
			
			final Long Id =rs.getLong("Id");
			final Long propertyTypeId = rs.getLong("propertyTypeId");
			final String propertyType = rs.getString("propertyType");
			final String propertyCode = rs.getString("propertyCode");
			final String unitCode = rs.getString("unitCode");
			final String floor = rs.getString("floor");
			final String floorDesc = rs.getString("floorDesc");
			final String buildingCode = rs.getString("buildingCode");
			final String parcel = rs.getString("parcel");
			final String parcelDesc = rs.getString("parcelDesc");
			final String precinct = rs.getString("precinct");
			final String street = rs.getString("street");
			final String zip = rs.getString("zip");
			final String state = rs.getString("state");
			final String country = rs.getString("country");
			final String status = rs.getString("status");
			final String clientId = rs.getString("clientId");
			final String firstName = rs.getString("firstName");
			final String lastName = rs.getString("lastName");
			final String displayName = rs.getString("displayName");
			final String email = rs.getString("email");
			final String addressNo = rs.getString("addressNo");
			final String addressKey = rs.getString("addressKey");
			final String categoryType = rs.getString("categoryType");
			
			return new ClientPropertyData(Id,propertyTypeId,propertyType,propertyCode,unitCode,floor,floorDesc,buildingCode,parcel,parcelDesc,
					precinct,street,zip,state,country,status,clientId,firstName,lastName,displayName,email,addressNo,addressKey,categoryType);
		}

	}

	@Override
	public List<PropertyDefinationData> retrieveAllProperties() {
	
		 try{
				context.authenticatedUser();
				final PropertyMapper mapper = new PropertyMapper();
				final String sql = "SELECT " + mapper.schema() + " WHERE pd.client_id IS NULL AND pd.status='VACANT' AND pd.is_deleted='N' ORDER BY pd.id " ;
				return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	            }catch (EmptyResultDataAccessException accessException) {
	    			return null;
	    		}
	}
	
	@Override
	public Page<PropertyDefinationData> retrieveAllPropertyMasterData(SearchSqlQuery searchPropertyDetails) {
		try {
			context.authenticatedUser();
			final PropertyMasterMapper mapper = new PropertyMasterMapper();	
			StringBuilder sqlBuilder = new StringBuilder(200);
			sqlBuilder.append("select ");
			sqlBuilder.append( mapper.schema());
			String sqlSearch = searchPropertyDetails.getSqlSearch();
			String extraCriteria = "";
			if(sqlSearch != null) {
			    	sqlSearch=sqlSearch.trim();
			    	extraCriteria = "  and (pm.property_code_type like '%"+sqlSearch+"%' OR" 
			    			+ " pm.code like '%"+sqlSearch+"%' )";
			    }
			
			sqlBuilder.append(extraCriteria);
			
		   if (searchPropertyDetails.isLimited()) {
		            sqlBuilder.append(" limit ").append(searchPropertyDetails.getLimit());
		        }

		   if (searchPropertyDetails.isOffset()) {
		            sqlBuilder.append(" offset ").append(searchPropertyDetails.getOffset());
		        }

	    	return this.paginationHelper.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()",sqlBuilder.toString(), new Object[] {}, mapper);
			    
		  } catch (EmptyResultDataAccessException accessException) {
			return null;
		}
	}
	

	private static final class PropertyMasterMapper implements RowMapper<PropertyDefinationData>{

		public String schema(){
			return " pm.id as id,pm.property_code_type as  propertyCodeType,pm.code as code,pm.description as description,pm.reference_value as referenceValue " +
					" from b_property_master pm  where  pm.is_deleted='N' ";

		}

		@Override
		public PropertyDefinationData mapRow(final ResultSet rs, final int rowNum) throws SQLException {

			final Long id = rs.getLong("id");
			final String propertyCodeType = rs.getString("propertyCodeType");
			final String code = rs.getString("code");
			final String description = rs.getString("description");
			final String referenceValue = rs.getString("referenceValue");

			return new PropertyDefinationData(id,propertyCodeType,code,description,referenceValue);
		}
	}


	@Override
	public List<PropertyDefinationData> retrievPropertyType(final String propertyType,final String code) {
		
		try {
			context.authenticatedUser();
			final PropertyMasterMapper mapper = new PropertyMasterMapper();
			final String sql = "select "+ mapper.schema()+ "  and pm.property_code_type like '%"+propertyType+"%' AND pm.code like '%"+code+"%'  order by pm.id LIMIT 20";
			return this.jdbcTemplate.query(sql, mapper, new Object[] {});
		    } catch (final EmptyResultDataAccessException e) {
			return null;
		}
	}

	@Override
	public PropertyDefinationData retrieveSinglePropertyMaster(final Long codeId) {
		
		try {
			context.authenticatedUser();
			final PropertyMasterMapper mapper = new PropertyMasterMapper();
			final String sql = "select "+ mapper.schema()+ "  and pm.id=?";
			return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] {codeId});
		  } catch (final EmptyResultDataAccessException e) {
			return null;
		}
     }

	@Override
	public Boolean retrievePropertyMasterCount(final String codeValue,final String propertyCodeType) {
		
		 this.context.authenticatedUser();
		 boolean result = false;
		 StringBuilder sql=new StringBuilder();
		 sql.append("select count(id) from b_property_defination pd where pd.is_deleted='N' and ");
		 if(propertyCodeType.equalsIgnoreCase(CodeNameConstants.CODE_PROPERTY_PARCEL)){
		      sql.append(" pd.parcel= ? ");
		 }else if(propertyCodeType.equalsIgnoreCase(CodeNameConstants.CODE_PROPERTY_FLOOR)){
			 sql.append(" pd.floor= ? ");
		 }else if(propertyCodeType.equalsIgnoreCase(CodeNameConstants.CODE_PROPERTY_UNIT)){
			 sql.append(" pd.unit_code= ? ");
		 }else{
			 sql.append(" pd.building_code= ? ");
		 }
		 final int count=	this.jdbcTemplate.queryForObject(sql.toString(), Integer.class,new Object[]{codeValue});
		 if(count > 0){
			 result = true;
		 }
		 return result;
	}
	
	
}

