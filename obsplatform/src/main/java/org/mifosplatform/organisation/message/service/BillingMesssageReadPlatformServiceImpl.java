package org.mifosplatform.organisation.message.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.mifosplatform.cms.media.domain.MediaTypeEnumaration;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.mifosplatform.infrastructure.core.data.MediaEnumoptionData;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.core.service.TenantAwareRoutingDataSource;
import org.mifosplatform.infrastructure.jobs.service.JobName;
import org.mifosplatform.organisation.message.data.BillingMessageDataForProcessing;
import org.mifosplatform.organisation.message.data.BillingMessageTemplateData;
import org.mifosplatform.organisation.message.data.EnumMessageType;
import org.mifosplatform.organisation.message.domain.BillingMessage;
import org.mifosplatform.organisation.message.domain.BillingMessageRepository;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplate;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.order.domain.UserActionStatusTypeEnum;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequest;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestDetails;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.mifosplatform.provisioning.provisioning.domain.ProvisioningCommand;
import org.mifosplatform.provisioning.provisioning.domain.ProvisioningCommandRepository;
import org.mifosplatform.template.domain.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * 
 * @author ashokreddy
 * 
 */
@Service
public class BillingMesssageReadPlatformServiceImpl implements
		BillingMesssageReadPlatformService {

	private final JdbcTemplate jdbcTemplate;
	private static List<BillingMessageTemplateData> messageparam;
	private static BillingMessageTemplateData templateData;
	private static BillingMessageRepository messageDataRepository;
	private static BillingMessageTemplateRepository messageTemplateRepository;
	private static ProcessRequestRepository processRequestRepository;
	private static Long messageId;
	private static String parameterValue;
	private static BillingMesssageReadPlatformService billingMesssageReadPlatformService;
	private static FileWriter fw;
	private static ConfigurationRepository globalConfigurationRepository;
	private static ProvisioningCommandRepository provisioningCommandRepository;

	

	@SuppressWarnings("static-access")
	@Autowired
	public BillingMesssageReadPlatformServiceImpl(
			final TenantAwareRoutingDataSource dataSource,
			final BillingMessageRepository messageDataRepository,
			final ProcessRequestRepository processRequestRepository,
			final BillingMessageTemplateRepository messageTemplateRepository,
			final ConfigurationRepository globalConfigurationRepository,
			final ProvisioningCommandRepository provisioningCommandRepository) {

		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.messageDataRepository = messageDataRepository;
		this.processRequestRepository = processRequestRepository;
		this.messageTemplateRepository = messageTemplateRepository;
		this.globalConfigurationRepository = globalConfigurationRepository;
		this.provisioningCommandRepository = provisioningCommandRepository;
	}

	// for message params
	@Override
	public List<BillingMessageTemplateData> retrieveMessageParams(Long messageId) {
		
		BillingMessageParamMapper mapper = new BillingMessageParamMapper();
		final String sql = "select " + mapper.schema() + messageId;
		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	private static final class BillingMessageParamMapper implements
			RowMapper<BillingMessageTemplateData> {

		public String schema() {

			return "mp.id as msgTemplateId,mp.parameter_name as parameterName,mp.sequence_no as sequenceNo "
					+ "from b_message_params mp where mp.msgtemplate_id=";
		}

		@Override
		public BillingMessageTemplateData mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			Long messageTemplateId = rs.getLong("msgTemplateId");
			String parameterName = rs.getString("parameterName");
			return new BillingMessageTemplateData(messageTemplateId,
					parameterName);
		}
	}

	// for mesage template

	private static final class BillingAllMessageTemplateParamMapper implements
			RowMapper<BillingMessageTemplateData> {

		public String schema() {

			return " mt.id as messageId, mt.template_description, mt.subject, mt.header, mt.body, mt.footer, mt.message_type as messageType,"
					+ " (select group_concat(mp.parameter_name separator ', ') from b_message_params mp "
					+ " where  mp.msgtemplate_id = mt.id ) as messageParameters from b_message_template mt where mt.is_deleted='N'";
		}

		@Override
		public BillingMessageTemplateData mapRow(ResultSet rs, int rowNum)
				throws SQLException {

			final Long messageId = rs.getLong("messageId");
			final String templateDescription = rs.getString("template_description");
			final String subject = rs.getString("subject");
			final String header = rs.getString("header");
			final String body = rs.getString("body");
			final String footer = rs.getString("footer");
			final String messageParameters = rs.getString("messageParameters");
			final String messageTypeinString = rs.getString("messageType");
			char messageType = 1;
			if (messageTypeinString != null) {
				messageType = messageTypeinString.charAt(0);
			}

			return new BillingMessageTemplateData(messageId, templateDescription,
					subject, header, body, footer, messageParameters, messageType);
		}
	}

	@Override
	public BillingMessageTemplateData retrieveMessageTemplate(Long messageId) {
		
		final BillingAllMessageTemplateParamMapper mapper = new BillingAllMessageTemplateParamMapper();
		final String sql = "select " + mapper.schema() + " and mt.id = " +messageId;
		return this.jdbcTemplate.queryForObject(sql, mapper, new Object[] {});
	}

	@Override
	public List<BillingMessageTemplateData> retrieveAllMessageTemplateParams() {

		final BillingAllMessageTemplateParamMapper mapper = new BillingAllMessageTemplateParamMapper();
		final String sql = "select " + mapper.schema();
		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	@Override
	public List<BillingMessageDataForProcessing> retrieveMessageDataForProcessing(Long id) {
		
		final BillingMessageDataForProcessingMapper mapper = new BillingMessageDataForProcessingMapper();
		String sql = "select " + mapper.schema();
		
		if (id != null) {
			sql = sql + " and md.id limit " + id;
		}

		return this.jdbcTemplate.query(sql, mapper, new Object[] {});
	}

	private static final class BillingMessageDataForProcessingMapper implements
			RowMapper<BillingMessageDataForProcessing> {

		public String schema() {

			return " md.id as id,md.message_to as messageto,md.message_from as messagefrom,md.subject as subject,md.header as header,"
					+ " md.body as body,md.footer as footer,md.message_type as messageType,md.attachment as attachment from"
					+ " b_message_data md where md.status='N' ";
		}

		@Override
		public BillingMessageDataForProcessing mapRow(ResultSet rs, int rowNum)
				throws SQLException {

			Long id = rs.getLong("id");
			String messageto = rs.getString("messageto");
			String messagefrom = rs.getString("messagefrom");
			String subject = rs.getString("subject");
			String header = rs.getString("header");
			String body = rs.getString("body");
			String footer = rs.getString("footer");
			String messageType = rs.getString("messageType");
			String attachment = rs.getString("attachment");
			char c = messageType.charAt(0);
			
			return new BillingMessageDataForProcessing(id, messageto,
					messagefrom, subject, header, body, footer, c, attachment);
		}
	}

	@Override
	public BillingMessageTemplateData retrieveTemplate() {

		MediaEnumoptionData email = MediaTypeEnumaration.enummessageData(EnumMessageType.EMAIL);
		MediaEnumoptionData message = MediaTypeEnumaration.enummessageData(EnumMessageType.Message);
		MediaEnumoptionData osdMessage = MediaTypeEnumaration.enummessageData(EnumMessageType.OSDMESSAGE);
		List<MediaEnumoptionData> categotyType = Arrays.asList(email, message, osdMessage);

		BillingMessageTemplateData messagedata = new BillingMessageTemplateData();
		messagedata.setMessageType(categotyType);

		return messagedata;

	}

	private static void handleCodeDataIntegrityIssues() {
		throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
				"Unknown data integrity issue with resource: message params count is lessthan or greaterthan to sending query parameters count ");
	}

	@Override
	public Long retrieveClientId(String hardwareId) throws IOException {
		try {
			
			final String sql = "select b.client_id as clientId from b_item_detail b where b.provisioning_serialno = '" + hardwareId + "' ";
			
			return jdbcTemplate.queryForLong(sql);
			
		} catch (EmptyResultDataAccessException e) {
			fw.append("provisioningSerialNo is= " + hardwareId + " Failed. Exception Reason is: " + e.getMessage() + " .\r\n");
			throw new ClientNotFoundException("client Not found with this hardwareId :"+hardwareId, hardwareId);
		} catch (Exception e) {
			fw.append("provisioningSerialNo is= " + hardwareId + " Failed. Exception Reason is: " + e.getMessage() + " .\r\n");
			throw new ClientNotFoundException(e.getMessage(), hardwareId);
		}

	}

	private static final class BillingMessageDataMapper implements
			RowMapper<BillingMessageTemplateData> {

		@Override
		public BillingMessageTemplateData mapRow(ResultSet rs, int rowNum)
				throws SQLException {

			try {
				locationInitialization();
				
				/** To Know the Record count and column count*/
				rs.last();
				int totalRows = rs.getRow();
				rs.beforeFirst();
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnCount = rsmd.getColumnCount();
				fw.append("Total Number Of Rows for Processing is= " + totalRows + " . \r\n");
				
				ArrayList<String> columndata = new ArrayList<String>();
				
				for (int currentRow = 1; currentRow <= totalRows; currentRow++) {
					rs.next();
					
					for (int currentColumn = 1; currentColumn <= columnCount; currentColumn++) {
						parameterValue = rs.getString(currentColumn);
						columndata.add(parameterValue);
					}

					processingMessages(columndata, currentRow);
					
					columndata.removeAll(columndata);

				}// for Rows
				fw.append("Billing Message Completed . \r\n");
				fw.flush();
				fw.close();
				return new BillingMessageTemplateData(messageId);
			} catch (IOException e) {
				return null;
			}

		}

		@SuppressWarnings("deprecation")
		private void locationInitialization() throws IOException {
			
				String fileUploadLocation = FileUtils.generateLogFileDirectory() + JobName.MESSAGE_MERGE.toString() + File.separator + "BillingMessage";
				
				File file = new File(fileUploadLocation);
				
				if (!file.isDirectory()) {
					file.mkdirs();
				}
				
				Date date = DateUtils.getDateOfTenant();
				String dateTime = date.getHours() + "" + date.getMinutes() + "" + date.getSeconds();
				String path = fileUploadLocation + File.separator + "billingMessage_" + new LocalDate().toString().replace("-", "") + "_" + dateTime + ".log";
				
				File fileHandler = new File(path.trim());
				fileHandler.createNewFile();
				fw = new FileWriter(fileHandler);
		}

		private void processingMessages(ArrayList<String> columndata, int currentRow) throws IOException {
			
			/**  processing each row and save the row as record in table */	
			
			String header = templateData.getHeader();
			String footer = templateData.getFooter();
			String body = templateData.getBody();
			String subject = templateData.getSubject();
			char messgeType = templateData.getMessageType();
			String status = "N";
			String messageFrom = "";

			if (messageparam.size() == columndata.size() - 1) {
				
				for (int messageParamCount = 0, columnDataCount = 1; messageParamCount < messageparam.size() & 
						columnDataCount < columndata.size(); messageParamCount++, columnDataCount++) {
					
					final String parameterName = messageparam.get(messageParamCount).getParameter();
					parameterValue = columndata.get(columnDataCount).toString();
					
					if (!org.apache.commons.lang.StringUtils.isBlank(body)) {
						body = body.replaceAll(parameterName, parameterValue);
					}
					if (!org.apache.commons.lang.StringUtils.isBlank(header)) {
						header = header.replaceAll(parameterName, parameterValue);
					}
					if (!org.apache.commons.lang.StringUtils.isBlank(footer)) {
						footer = footer.replaceAll(parameterName, parameterValue);
					}
				}
				
			} else {
				handleCodeDataIntegrityIssues();
			}
			
			if (messgeType == 'E' || messgeType == 'M') {
				String messageTo = columndata.get(0).toString();
				BillingMessageTemplate billingMessageTemplate = messageTemplateRepository.findOne(messageId);
				BillingMessage billingMessage = new BillingMessage(header, body, footer, messageFrom, messageTo,
						subject, status, billingMessageTemplate, messgeType, null);
				messageDataRepository.save(billingMessage);
			}

			if (messgeType == 'O') {
				String requstStatus = UserActionStatusTypeEnum.MESSAGE.toString();
				Long clientId = billingMesssageReadPlatformService.retrieveClientId(columndata.get(0).toString());
				
				ProvisioningCommand provisioningCommand = provisioningCommandRepository.findByCommandName(ConfigurationConstants.OSM_COMMAND);
				
				if (clientId != null && provisioningCommand != null) {
					Long id = Long.valueOf(0);
					ProcessRequest processRequest = new ProcessRequest(id, clientId, id, provisioningCommand.getProvisioningSystem(), requstStatus, 'N', 'N');
					processRequest.setNotify();
					
					ProcessRequestDetails processRequestDetails = new ProcessRequestDetails(
							id, id, body, "Recieved", columndata.get(0).toString(), DateUtils.getDateOfTenant(), null,
							null, null, 'N', requstStatus, null);
					processRequest.add(processRequestDetails);
					processRequestRepository.save(processRequest);
				} else {
					fw.append("rowNo:" + currentRow + " failed . \r\n");
					System.out.println("Provisioning System Not Configured with 'OSM' in Provision Command");
				}

			}
		}
	}

	// for messageData
	@SuppressWarnings("static-access")
	@Override
	public List<BillingMessageTemplateData> retrieveData(
			Long id,
			String query,
			BillingMessageTemplateData templateData,
			List<BillingMessageTemplateData> messageparam,
			BillingMesssageReadPlatformService billingMesssageReadPlatformService) {
		
		this.messageparam = messageparam;
		this.templateData = templateData;
		this.messageId = id;
		this.billingMesssageReadPlatformService = billingMesssageReadPlatformService;
		BillingMessageDataMapper mapper = new BillingMessageDataMapper();

		return this.jdbcTemplate.query(query, mapper, new Object[] {});

	}

	@Override
	public List<List<Map<String, Object>>> retrieveMessageQuery(final String query, final Template template) {

		final TemplateMappers mapper = new TemplateMappers(template);
		return this.jdbcTemplate.query(query, mapper, new Object[] {});
	}

	private static final class TemplateMappers implements RowMapper<List<Map<String, Object>>> {

		Template template;

		public TemplateMappers(final Template template) {

			this.template = template;
		}

		@Override
		public List<Map<String, Object>> mapRow(ResultSet rs, int rowNum) throws SQLException {

			try {
				final String messageType = this.template.getType().getName();
				rs.beforeFirst();
				final ResultSetMetaData metaData = rs.getMetaData();
				final List<Map<String, Object>> scopes = new ArrayList<>();
				final int row = metaData.getColumnCount();
				final MustacheFactory mf = new DefaultMustacheFactory();
			    Mustache mustache = mf.compile(new StringReader(this.template.getText()), this.template.getName());
				locationInitialization();
				if(rs!=null){
				while(rs.next()) {
					final Map<String, Object> scope = new HashMap<>();
					 for (int i = 1; i <= row; i++) {
						scope.put(metaData.getColumnName(i), rs.getObject(i));
					  }
					String emailTo=null;
					for(String key:scope.keySet()){
						if(key.contains("email")){
						  emailTo=scope.get(key).toString();
						}
					}
					if(emailTo == null) {
						fw.append("current client doesn't have email id: \r\n");
					}
					final StringWriter stringWriter = new StringWriter();
					mustache.execute(stringWriter, scope).flush();
					if (messageType.equalsIgnoreCase("E-Mail") || messageType.equalsIgnoreCase("SMS")&&emailTo!=null) {
						BillingMessage billingMessage = new BillingMessage("", stringWriter.toString(),"Thank you", 
								emailTo,emailTo,this.template.getName(), "N", this.template.getId(),	messageType.charAt(0), null);
						messageDataRepository.save(billingMessage);
					}
					scopes.add(scope);
				}
			}
	  return scopes;
	} catch (Exception e) {
	  e.printStackTrace();
	  return  null;
		}

}

		private void locationInitialization() throws IOException {
			
				String fileUploadLocation = FileUtils.generateLogFileDirectory() + JobName.MESSAGE_MERGE.toString() + File.separator + "BillingMessage";
				File file = new File(fileUploadLocation);
				if (!file.isDirectory()) {
					file.mkdirs();
				}
				LocalTime date=new LocalTime();
		        String dateTime=date.getHourOfDay()+"_"+date.getMinuteOfHour()+"_"+date.getSecondOfMinute();
				String path = fileUploadLocation + File.separator + "billingMessage_" + new LocalDate().toString().replace("-", "") + "_" + dateTime + ".log";
				File fileHandler = new File(path.trim());
				fileHandler.createNewFile();
				fw = new FileWriter(fileHandler);
		}
	}
}
