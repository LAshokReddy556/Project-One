package org.mifosplatform.workflow.eventaction.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.mifosplatform.crm.clientprospect.service.SearchSqlQuery;
import org.mifosplatform.infrastructure.core.domain.JdbcSupport;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.scheduledjobs.scheduledjobs.data.EventActionData;
import org.mifosplatform.workflow.eventaction.data.OrderNotificationData;
import org.mifosplatform.workflow.eventaction.data.VolumeDetailsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;


@Service
public class EventActionReadPlatformServiceImpl implements EventActionReadPlatformService{
	

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final PaginationHelper<EventActionData> paginationHelper = new PaginationHelper<EventActionData>();

	@Autowired
	public EventActionReadPlatformServiceImpl(final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}
	@Override
	public VolumeDetailsData retrieveVolumeDetails(Long planId) {
		
		try{
	//	context.authenticatedUser();
		PlanMapper mapper = new PlanMapper();

		String sql = "select " + mapper.schema();

		return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] { planId });
	}catch(EmptyResultDataAccessException exception){
		return null;
	}
	}
	private static final class PlanMapper implements RowMapper<VolumeDetailsData> {

		public String schema() {
			return "v.id as id,v.plan_id as planId, v.volume_type as volumeType,v.units as units,v.units_type as unitType" +
					" FROM b_volume_details v WHERE v.plan_id =?";

		}

		@Override
		public VolumeDetailsData mapRow(final ResultSet rs,
				@SuppressWarnings("unused") final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			Long planId = rs.getLong("planId");
			String volumeType = rs.getString("volumeType");
			Long units = rs.getLong("units");
			String unitType = rs.getString("unitType");
			return new VolumeDetailsData(id,planId,volumeType,units,unitType);

		}
	}
	@Override
	public Page<EventActionData> retriveAllEventActions(SearchSqlQuery searchEventAction, String statusType) {
		context.authenticatedUser();
		EventActionMapper mapper = new EventActionMapper();
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("select ");
        sqlBuilder.append(mapper.schema() + " where a.id is not null ");
        
        if(statusType != null){
        	sqlBuilder.append("AND (a.is_processed = '"+statusType+"') ");
	    }
        if(searchEventAction.getSqlSearch() != null){
        	
        	sqlBuilder.append("AND (concat(a.event_action,' ',a.entity_name) like '%"+searchEventAction.getSqlSearch()+
        					  "%' OR a.action_name like '%"+searchEventAction.getSqlSearch()+
        					  "%' OR a.entity_name like '%"+searchEventAction.getSqlSearch()+"%') ");
	    }
        if (searchEventAction.isLimited()) {
            sqlBuilder.append(" limit ").append(searchEventAction.getLimit());
        }

        if (searchEventAction.isOffset()) {
            sqlBuilder.append(" offset ").append(searchEventAction.getOffset());
        }
		return this.paginationHelper.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()", sqlBuilder.toString(),
	            new Object[] {}, mapper);
		
	}
	
	private static final class EventActionMapper implements RowMapper<EventActionData> {

		public String schema() {
			return " a.id as id,a.event_action AS eventaction,a.entity_name AS entityName,a.action_name AS actionName, a.command_as_json as json,a.resource_id as resourceId, " +
					" a.order_id as orderId,a.client_id as clientId,a.is_processed as status,a.trans_date as transactionDate FROM b_event_actions a ";

		}

		@Override
		public EventActionData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {
			Long id = rs.getLong("id");
			String eventaction = rs.getString("eventaction");
			String entityName = rs.getString("entityName");
			String actionName = rs.getString("actionName");
			String jsonData = rs.getString("json");
			Long resourceId = rs.getLong("resourceId");
			Long orderId = rs.getLong("orderId");
			Long clientId = rs.getLong("clientId");
			String status = rs.getString("status");
			DateTime transactionDate = JdbcSupport.getDateTime(rs,"transactionDate");
			return new EventActionData(id, eventaction, entityName, actionName, jsonData, resourceId, orderId, clientId, status, transactionDate);

		}
	}
	@Override
	public List<EventActionData> retrievePendingActionRequest(Long paymentGatewayId) {
		// TODO Auto-generated method stub
		EventActionMapper mapper = new EventActionMapper();
		String sql = "select " + mapper.schema() + " where a.is_processed='P' and a.resource_id = ?";

		return this.jdbcTemplate.query(sql, mapper, new Object[] {paymentGatewayId});
	}
	
	@Override
	public List<EventActionData> retrievePendingRecurringRequest(Long clientId) {
		
		EventActionMapper mapper = new EventActionMapper();
		
		String sql = "select " + mapper.schema() + " where a.is_processed='R' and a.client_id = ?";

		return this.jdbcTemplate.query(sql, mapper, new Object[] {clientId});
	}
	
	@Override
	public OrderNotificationData retrieveNotifyDetails(Long clientId, Long orderId) {
		
		OrderNotificationMapper mapper = new OrderNotificationMapper();
		
		String sql = "select " + mapper.schema();
		
		if(null == orderId){
			sql = sql + " group by c.id";
		} else{
			sql = sql + " and o.id = " + orderId;
		}

		return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] {clientId});
		
	}
	
	private static final class OrderNotificationMapper implements RowMapper<OrderNotificationData> {

		public String schema() {
			return " c.firstname as firstName, c.lastname as lastName, c.phone as clientPhone, p.plan_description as planName, c.email as emailId," +
					" mo.name as officeName, boa.email_id as officeEmail, boa.phone_number as officePhoneNo, " +
					" o.active_date as activationDate, o.start_date as startDate, o.end_date as endDate from m_client c " +
					" Join m_office mo ON mo.id = c.office_id left Join b_office_address boa ON boa.office_id = mo.id " +
					" Join b_orders o ON o.client_id = c.id Join b_plan_master p ON o.plan_id = p.id where c.id = ? ";
		}
		
		@Override
		public OrderNotificationData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {
			
			String firstName = rs.getString("firstName");
			String lastName = rs.getString("lastName");
			String planName = rs.getString("planName");
			String emailId = rs.getString("emailId");
			String clientPhone = rs.getString("clientPhone");
			
			String officeName = rs.getString("officeName");
			String officeEmail = rs.getString("officeEmail");
			String officePhoneNo = rs.getString("officePhoneNo");
			
			LocalDate activationDate = JdbcSupport.getLocalDate(rs, "activationDate");
			LocalDate startDate = JdbcSupport.getLocalDate(rs, "startDate");
			LocalDate endDate = JdbcSupport.getLocalDate(rs, "endDate");
			
			return new OrderNotificationData(firstName, lastName, planName, emailId, officeName, officeEmail, officePhoneNo, 
					activationDate, startDate, endDate, clientPhone);

		}
	}

}
