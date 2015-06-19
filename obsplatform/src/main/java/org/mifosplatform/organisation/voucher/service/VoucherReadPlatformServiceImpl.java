package org.mifosplatform.organisation.voucher.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.StreamingOutput;

import org.mifosplatform.crm.clientprospect.service.SearchSqlQuery;
import org.mifosplatform.crm.ticketmaster.data.ClientTicketData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.Page;
import org.mifosplatform.infrastructure.core.service.PaginationHelper;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetColumnHeaderData;
import org.mifosplatform.infrastructure.dataqueries.data.ResultsetRowData;
import org.mifosplatform.infrastructure.dataqueries.service.GenericDataService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.voucher.data.VoucherData;
import org.mifosplatform.organisation.voucher.domain.VoucherPinCategory;
import org.mifosplatform.organisation.voucher.domain.VoucherPinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/**
 * 
 * @author ashokreddy
 *
 */
@Service
public class VoucherReadPlatformServiceImpl implements
		VoucherReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final GenericDataService genericDataService;
	private final PaginationHelper<VoucherData> paginationHelper = new PaginationHelper<VoucherData>();

	@Autowired
	public VoucherReadPlatformServiceImpl(
			final PlatformSecurityContext context,
			final TenantAwareRoutingDataSource dataSource,
			final GenericDataService genericDataService) {
		this.context = context;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.genericDataService = genericDataService;
	}

	@Override
	public String retrieveIndividualPin(String pinNo) {
		try {

			context.authenticatedUser();
			String sql;
			retrieveMapper mapper = new retrieveMapper();
			sql = "SELECT  " + mapper.schema();

			return this.jdbcTemplate.queryForObject(sql, mapper,
					new Object[] { pinNo });
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class retrieveMapper implements RowMapper<String> {

		private String schema() {
			return " d.pin_no as pinNo from b_pin_details d where d.pin_no =?";

		}

		@Override
		public String mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {
			return rs.getString("pinNo");

		}
	}

	@Override
	public List<EnumOptionData> pinCategory() {

		EnumOptionData numeric = VoucherEnumeration.enumOptionData(VoucherPinCategory.NUMERIC);
		EnumOptionData alpha = VoucherEnumeration.enumOptionData(VoucherPinCategory.ALPHA);
		EnumOptionData alphaNumeric = VoucherEnumeration.enumOptionData(VoucherPinCategory.ALPHANUMERIC);
		List<EnumOptionData> categotyType = Arrays.asList(numeric, alpha, alphaNumeric);
		return categotyType;
	}

	@Override
	public List<EnumOptionData> pinType() {

		EnumOptionData value = VoucherEnumerationType.enumOptionData(VoucherPinType.VALUE);
		EnumOptionData duration = VoucherEnumerationType.enumOptionData(VoucherPinType.DURATION);
		List<EnumOptionData> categotyType = Arrays.asList(value, duration);
		return categotyType;
	}

	@Override
	public Page<VoucherData> getAllVoucherById(SearchSqlQuery searchVoucher, String statusType, Long id) {
		try {

			context.authenticatedUser();
			retrieveRandomMapper mapper = new retrieveRandomMapper();
			StringBuilder sqlBuilder = new StringBuilder();
			
			sqlBuilder.append("SELECT ");
			sqlBuilder.append(mapper.schema());
			sqlBuilder.append(" where pd.is_deleted='N' and pd.pin_id=?");
			String sqlSearch = searchVoucher.getSqlSearch();
	        String extraCriteria = "";
	        if(statusType != null){
	        	sqlBuilder.append(" and (pd.status ='"+statusType+"') ");
		    }
		    if (sqlSearch != null) {
		    	sqlSearch = sqlSearch.trim();
		    	extraCriteria = " and (pd.pin_no like '%"+sqlSearch+"%' OR" 
		    			+ " pd.client_id like '%"+sqlSearch+"%' OR"
		    			+ " pd.serial_no like '%"+sqlSearch+"%' )";
		    }
		   
		    sqlBuilder.append(extraCriteria);
		    
			if (searchVoucher.isLimited()) {
				sqlBuilder.append(" limit ").append(searchVoucher.getLimit());
		    }

		    if (searchVoucher.isOffset()) {
		        sqlBuilder.append(" offset ").append(searchVoucher.getOffset());
		    }
		    
			return this.paginationHelper.fetchPage(this.jdbcTemplate, "SELECT FOUND_ROWS()", sqlBuilder.toString(),
		            new Object[] {id}, mapper);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	private static final class retrieveRandomMapper implements
			RowMapper<VoucherData> {

		public String schema() {
			/*return "m.id as id, m.batch_name as batchName, m.office_id as officeId, m.length as length,"
					+ "m.begin_with as beginWith,m.pin_category as pinCategory,m.quantity as quantity,"
					+ "m.serial_no as serialNo,m.pin_type as pinType,m.pin_value as pinValue,m.expiry_date as expiryDate, "
					+ "case m.pin_type when 'VALUE' then p.plan_code=null when 'PRODUCT' then p.plan_code end as planCode, "
					+ "m.is_processed as isProcessed from b_pin_master m  "
					+ "left join b_plan_master p on m.pin_value=p.id";*/
				return " pd.id, pm.batch_name AS batchName, pm.pin_type AS pinType, pm.office_id as officeId,"+
						"pm.pin_value AS pinValue, pm.pin_category as pinCategory, pd.serial_no as serialNo," +
						"pd.pin_no as pinNo, pd.status as status, pd.client_id as clientId, pm.expiry_date as expiryDate "+
						"FROM b_pin_master pm left join b_pin_details pd on pd.pin_id = pm.id ";

		}

		@Override
		public VoucherData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String batchName = rs.getString("batchName");
			Long officeId = rs.getLong("officeId");
			//Long length = rs.getLong("length");
			String pinCategory = rs.getString("pinCategory");
			String pinType = rs.getString("pinType");
			//Long quantity = rs.getLong("quantity");
			String serial = rs.getString("serialNo");
			Date expiryDate = rs.getDate("expiryDate");
			//String beginWith = rs.getString("beginWith");
			String pinValue = rs.getString("pinValue");
			//String planCode = rs.getString("planCode");
			//String isProcessed = rs.getString("isProcessed");
			String pinNo = rs.getString("pinNo");
			String status = rs.getString("status");
			Long clientId = rs.getLong("clientId");

			return new VoucherData(batchName, officeId, null,
					pinCategory, pinType, null, serial, expiryDate,
					null, pinValue, id, null, null, pinNo, status, clientId);

		}
	}

	@Override
	public Long retrieveMaxNo(Long minNo, Long maxNo) {
		try {
			context.authenticatedUser();
			String sql;
			Mapper mapper = new Mapper();
			sql = "SELECT  " + mapper.schema();

			return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] {
					minNo, maxNo });
		} catch (EmptyResultDataAccessException e) {
			return Long.valueOf(0);
		}
	}

	private static final class Mapper implements RowMapper<Long> {

		public String schema() {
			return "max(m.serial_no) as serialNo from b_pin_details m where serial_no BETWEEN ? AND ?";

		}

		@Override
		public Long mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {
			Long serialNo = rs.getLong("serialNo");

			return serialNo;
		}
	}

	@Override
	public StreamingOutput retrieveVocherDetailsCsv(final Long batchId) {
		this.context.authenticatedUser();
		return new StreamingOutput() {

			@Override
			public void write(final OutputStream out) {
				try {

					final String sql = "SELECT pm.id AS batchId, pd.serial_no AS serialNum, pd.pin_no AS hiddenNum, pd.client_id as clientId, pd.status FROM b_pin_master pm, b_pin_details pd"
							+ " WHERE pd.pin_id = pm.id AND pm.id ="
							+ batchId
							+ " order by serialNum desc ";
					GenericResultsetData result = genericDataService
							.fillGenericResultSet(sql);
					StringBuffer sb = generateCsvFileBuffer(result);
					InputStream in = new ByteArrayInputStream(sb.toString()
							.getBytes("UTF-8"));
					byte[] outputByte = new byte[4096];
					Integer readLen = in.read(outputByte, 0, 4096);
					while (readLen != -1) {
						out.write(outputByte, 0, readLen);
						readLen = in.read(outputByte, 0, 4096);
					}
				} catch (Exception e) {

					throw new PlatformDataIntegrityException(
							"error.msg.exception.error", e.getMessage());
				}
			}
		};
	}

	private StringBuffer generateCsvFileBuffer(final GenericResultsetData result) {
		StringBuffer writer = new StringBuffer();

		List<ResultsetColumnHeaderData> columnHeaders = result
				.getColumnHeaders();
		// logger.info("NO. of Columns: " + columnHeaders.size());
		Integer chSize = columnHeaders.size();
		for (int i = 0; i < chSize; i++) {
			writer.append('"' + columnHeaders.get(i).getColumnName() + '"');
			if (i < (chSize - 1))
				writer.append(",");
		}
		writer.append('\n');

		List<ResultsetRowData> data = result.getData();
		List<String> row;
		Integer rSize;
		// String currCol;
		String currColType;
		String currVal;
		String doubleQuote = "\"";
		String twoDoubleQuotes = doubleQuote + doubleQuote;
		// logger.info("NO. of Rows: " + data.size());
		for (int i = 0; i < data.size(); i++) {
			row = data.get(i).getRow();
			rSize = row.size();
			for (int j = 0; j < rSize; j++) {
				// currCol = columnHeaders.get(j).getColumnName();
				currColType = columnHeaders.get(j).getColumnType();
				currVal = row.get(j);
				if (currVal != null) {
					if (currColType.equals("DECIMAL")
							|| currColType.equals("DOUBLE")
							|| currColType.equals("BIGINT")
							|| currColType.equals("SMALLINT")
							|| currColType.equals("INT"))
						writer.append(currVal);
					else
						writer.append('"' + genericDataService.replace(currVal,
								doubleQuote, twoDoubleQuotes) + '"');

				}
				if (j < (rSize - 1))
					writer.append(",");
			}
			writer.append('\n');
		}

		return writer;
	}

	@Override
	public List<VoucherData> retrivePinDetails(String pinNumber) {

		try {
			context.authenticatedUser();
			String sql;
			PinMapper mapper = new PinMapper();
			sql = "SELECT  " + mapper.schema();

			return this.jdbcTemplate.query(sql, mapper, new Object[] {pinNumber});
			
		} catch (EmptyResultDataAccessException e) {
			return null;
		}

	}
	
	private static final class PinMapper implements RowMapper<VoucherData> {

		public String schema() {
			return " pm.pin_type as pinType, pm.pin_value as pinValue, pm.expiry_date as expiryDate " +
					" from b_pin_master pm, b_pin_details pd where pd.pin_id = pm.id and pd.status='NEW' and pd.is_deleted='N' and pd.pin_no=?";

		}

		@Override
		public VoucherData mapRow(final ResultSet rs, final int rowNum)
				throws SQLException {
			String pinValue = rs.getString("pinValue");
			Date expiryDate = rs.getDate("expiryDate");
			String pinType = rs.getString("pinType");

			return new VoucherData(pinType,pinValue,expiryDate);
		}
	}
	
	@Override
	public List<VoucherData> getAllData() {
		try {

			context.authenticatedUser();
			String sql;
			RetrieveAllRandomMapper mapper = new RetrieveAllRandomMapper();
			sql = "SELECT  " + mapper.schema();

			return this.jdbcTemplate.query(sql, mapper, new Object[] {});
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	private static final class RetrieveAllRandomMapper implements
	RowMapper<VoucherData> {

		public String schema() {
			
				return "m.id as id, m.batch_name as batchName, m.office_id as officeId, m.length as length,"
						+ "m.begin_with as beginWith,m.pin_category as pinCategory,m.quantity as quantity,"
						+ "m.serial_no as serialNo,m.pin_type as pinType,m.pin_value as pinValue,m.expiry_date as expiryDate, "
						+ "case m.pin_type when 'VALUE' then p.plan_code=null when 'PRODUCT' then p.plan_code end as planCode, "
						+ "m.is_processed as isProcessed from b_pin_master m  "
						+ "left join b_plan_master p on m.pin_value=p.id";

			}

			@Override
			public VoucherData mapRow(final ResultSet rs, final int rowNum)
					throws SQLException {

				Long id = rs.getLong("id");
				String batchName = rs.getString("batchName");
				Long officeId = rs.getLong("officeId");
				Long length = rs.getLong("length");
				String pinCategory = rs.getString("pinCategory");
				String pinType = rs.getString("pinType");
				Long quantity = rs.getLong("quantity");
				String serial = rs.getString("serialNo");
				Date expiryDate = rs.getDate("expiryDate");
				String beginWith = rs.getString("beginWith");
				String pinValue = rs.getString("pinValue");
				String planCode = rs.getString("planCode");
				String isProcessed = rs.getString("isProcessed");

				return new VoucherData(batchName, officeId, length,
						pinCategory, pinType, quantity, serial, expiryDate,
						beginWith, pinValue, id, planCode, isProcessed, null, null, null);

			}
	}
	

}
