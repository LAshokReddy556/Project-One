package org.mifosplatform.billing.selfcare.service;

import org.apache.commons.lang.RandomStringUtils;
import org.mifosplatform.billing.loginhistory.domain.LoginHistory;
import org.mifosplatform.billing.loginhistory.domain.LoginHistoryRepository;
import org.mifosplatform.billing.selfcare.domain.SelfCare;
import org.mifosplatform.billing.selfcare.domain.SelfCareTemporary;
import org.mifosplatform.billing.selfcare.domain.SelfCareTemporaryRepository;
import org.mifosplatform.billing.selfcare.exception.SelfCareAlreadyVerifiedException;
import org.mifosplatform.billing.selfcare.exception.SelfCareEmailIdDuplicateException;
import org.mifosplatform.billing.selfcare.exception.SelfCareTemporaryGeneratedKeyNotFoundException;
import org.mifosplatform.billing.selfcare.exception.SelfcareEmailIdNotFoundException;
import org.mifosplatform.infrastructure.configuration.domain.Configuration;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.security.service.RandomPasswordGenerator;
import org.mifosplatform.organisation.message.domain.BillingMessage;
import org.mifosplatform.organisation.message.domain.BillingMessageRepository;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplate;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateConstants;
import org.mifosplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.mifosplatform.organisation.message.exception.BillingMessageTemplateNotFoundException;
import org.mifosplatform.organisation.message.service.MessagePlatformEmailService;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.client.exception.ClientStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;




@Service
public class SelfCareWritePlatformServiceImp implements SelfCareWritePlatformService{
	
	private PlatformSecurityContext context;
	private ClientRepository clientRepository;
	private SelfCareRepository selfCareRepository;
	private final LoginHistoryRepository loginHistoryRepository;
	private MessagePlatformEmailService messagePlatformEmailService;
	private SelfCareReadPlatformService selfCareReadPlatformService;
	private SelfCareTemporaryRepository selfCareTemporaryRepository;
	private final BillingMessageTemplateRepository billingMessageTemplateRepository;
	private SelfCareCommandFromApiJsonDeserializer selfCareCommandFromApiJsonDeserializer;
	private final BillingMessageRepository messageDataRepository;
	private final ConfigurationRepository configurationRepository;
	private final static Logger logger = (Logger) LoggerFactory.getLogger(SelfCareWritePlatformServiceImp.class);
	private BillingMessageTemplate createSelfcareMessageDetails = null;
	private BillingMessageTemplate registerSelfcareMessageDetails = null;
	private BillingMessageTemplate newSelfcarePasswordMessageDetails = null;
	private BillingMessageTemplate createSelfcareMessageDetailsForSMS = null;
	private BillingMessageTemplate newSelfcarePasswordMessageDetailsForSMS = null;
	
	

	@Autowired
	public SelfCareWritePlatformServiceImp(final PlatformSecurityContext context, 
			final SelfCareRepository selfCareRepository, 
		    final SelfCareCommandFromApiJsonDeserializer selfCareCommandFromApiJsonDeserializer,
		    final SelfCareReadPlatformService selfCareReadPlatformService, 
			final SelfCareTemporaryRepository selfCareTemporaryRepository,
			final BillingMessageTemplateRepository billingMessageTemplateRepository,
			final MessagePlatformEmailService messagePlatformEmailService,
			final ClientRepository clientRepository,
			final LoginHistoryRepository loginHistoryRepository,
			final BillingMessageRepository messageDataRepository,
			final ConfigurationRepository configurationRepository) {
		
		this.context = context;
		this.selfCareRepository = selfCareRepository;
		this.selfCareCommandFromApiJsonDeserializer = selfCareCommandFromApiJsonDeserializer;
		this.selfCareReadPlatformService = selfCareReadPlatformService;
		this.selfCareTemporaryRepository = selfCareTemporaryRepository;
		this.messagePlatformEmailService = messagePlatformEmailService;
		this.billingMessageTemplateRepository = billingMessageTemplateRepository;
		this.messagePlatformEmailService= messagePlatformEmailService;
		this.clientRepository=clientRepository;
		this.loginHistoryRepository=loginHistoryRepository;
		this.messageDataRepository = messageDataRepository;
		this.configurationRepository = configurationRepository;
				
	}
	
	@Override
	public CommandProcessingResult createSelfCare(JsonCommand command) {
		
		try{
			this.context.authenticatedUser();
			this.selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			SelfCare selfCare = SelfCare.fromJson(command);
			String password = command.stringValueOfParameterNamed("password");
			boolean mailnotification = command.booleanPrimitiveValueOfParameterNamed("mailNotification");
			
			if(null == selfCare.getClientId() || selfCare.getClientId()<1L){
				throw new PlatformDataIntegrityException("client does not exist", "client not registered","clientId", "client is null ");
			}
			
			if(password == null || password.isEmpty()){
				selfCare.setPassword(new RandomPasswordGenerator(8).generate().toString());
			}else{
				selfCare.setPassword(password);
			}
			
			this.selfCareRepository.save(selfCare);
			
			Client client = this.clientRepository.findOne(selfCare.getClientId());
			
			Configuration configuration = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_SMS);
			
			if(null != configuration && configuration.isEnabled()) {
				
				if(null == createSelfcareMessageDetailsForSMS){
					createSelfcareMessageDetailsForSMS = this.billingMessageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_CREATE_SELFCARE);
				}
				
				if (createSelfcareMessageDetailsForSMS != null) {
					
					String subject = createSelfcareMessageDetailsForSMS.getSubject();
					String body = createSelfcareMessageDetailsForSMS.getBody();
					body = body.replace("<PARAM1>", selfCare.getUserName().trim());
					body = body.replace("<PARAM2>", selfCare.getPassword().trim());
					
					BillingMessage billingMessage = new BillingMessage(null, body, null, 
							BillingMessageTemplateConstants.MESSAGE_TEMPLATE_EMAIL_FROM, client.getPhone(), subject,
							BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, createSelfcareMessageDetailsForSMS, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_TYPE, null);

					this.messageDataRepository.save(billingMessage);

				} else throw new BillingMessageTemplateNotFoundException(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_CREATE_SELFCARE);
			}
			
			if (mailnotification) {

				if(null == createSelfcareMessageDetails){
					createSelfcareMessageDetails = this.billingMessageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_CREATE_SELFCARE);
				}
				
				if (createSelfcareMessageDetails != null) {
					
					String subject = createSelfcareMessageDetails.getSubject();
					String body = createSelfcareMessageDetails.getBody();
					String footer = createSelfcareMessageDetails.getFooter();
					String header = createSelfcareMessageDetails.getHeader().replace("<PARAM1>", client.getDisplayName()==null || client.getDisplayName().isEmpty()?client.getFirstname(): client.getDisplayName() + ",");
					body = body.replace("<PARAM2>", selfCare.getUserName().trim());
					body = body.replace("<PARAM3>", selfCare.getPassword().trim());

					BillingMessage billingMessage = new BillingMessage(header, body, footer, 
							BillingMessageTemplateConstants.MESSAGE_TEMPLATE_EMAIL_FROM, client.getEmail(), subject,
							BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, createSelfcareMessageDetails, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);

					this.messageDataRepository.save(billingMessage);

				} else throw new BillingMessageTemplateNotFoundException(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_CREATE_SELFCARE);
				
			}	
			
			
		
			
			return new CommandProcessingResultBuilder().withEntityId(selfCare.getId()).withClientId(selfCare.getClientId()).build();
			
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.username", "duplicate.username","duplicate.username", "duplicate.username");
		}catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
		
	}
	
	@Override
	public CommandProcessingResult createSelfCareUDPassword(JsonCommand command) {
		SelfCare selfCare = null;
		Long clientId = null;
		String ipAddress=command.stringValueOfParameterNamed("ipAddress");
		String session=command.stringValueOfParameterNamed("");
		Long loginHistoryId=null;
		
		try{
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreateUDPassword(command);
			selfCare = SelfCare.fromJsonODP(command);
			try{
			clientId = selfCareReadPlatformService.getClientId(selfCare.getUniqueReference());
			if(clientId == null || clientId <= 0 ){
				throw new PlatformDataIntegrityException("client does not exist", "this user is not registered","clientId", "client is null ");
			}
			selfCare.setClientId(clientId);

			selfCareRepository.save(selfCare);
			String username=selfCare.getUserName();
			LoginHistory loginHistory=new LoginHistory(ipAddress,null,session,DateUtils.getDateOfTenant(),null,username,"ACTIVE");
    		this.loginHistoryRepository.save(loginHistory);
    		loginHistoryId=loginHistory.getId();
			}
			catch(EmptyResultDataAccessException dve){
				throw new PlatformDataIntegrityException("invalid.account.details","invalid.account.details","this user is not registered");
			}
			
			
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.email", "duplicate.email","duplicate.email", "duplicate.email");
		}catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
		return new CommandProcessingResultBuilder().withEntityId(loginHistoryId).withClientId(clientId).build();
	}
		
	@Override
	public CommandProcessingResult updateSelfCareUDPassword(JsonCommand command) {
		   SelfCare selfCare=null;
		   context.authenticatedUser();
		   selfCareCommandFromApiJsonDeserializer.validateForUpdateUDPassword(command);
		   String email=command.stringValueOfParameterNamed("uniqueReference");
		   String password=command.stringValueOfParameterNamed("password");
		   selfCare=this.selfCareRepository.findOneByEmail(email);
		   if(selfCare==null){
			   throw new ClientNotFoundException(email);
		   }
		   selfCare.setPassword(password);
		   this.selfCareRepository.save(selfCare);
		   return new CommandProcessingResultBuilder().withEntityId(selfCare.getClientId()).build();
	}	
	
	@Override
	public CommandProcessingResult forgotSelfCareUDPassword(JsonCommand command) {
		SelfCare selfCare=null;
		context.authenticatedUser();
		selfCareCommandFromApiJsonDeserializer.validateForForgotUDPassword(command);
		String email=command.stringValueOfParameterNamed("uniqueReference");
		selfCare=this.selfCareRepository.findOneByEmail(email);
		if(selfCare == null){
			throw new ClientNotFoundException(email);
		}
		String password=selfCare.getPassword();
		Client client= this.clientRepository.findOne(selfCare.getClientId());
		String body="Dear "+client.getDisplayName()+","+"\n"+"Your login information is mentioned below."+"\n"+"Email Id : "+email+"\n"+"Password :"+password+"\n"+"Thanks";
		String subject="Login Information";
		messagePlatformEmailService.sendGeneralMessage(email, body, subject);
		return new CommandProcessingResult(selfCare.getClientId());
	}

	private void handleDataIntegrityIssues(JsonCommand command,DataIntegrityViolationException dve) {
		 Throwable realCause = dve.getMostSpecificCause();
		 logger.error(dve.getMessage(), dve);
	        if (realCause.getMessage().contains("username")){	
	        	throw new PlatformDataIntegrityException("validation.error.msg.selfcare.duplicate.userName", "User Name: " + command.stringValueOfParameterNamed("userName")+ " already exists", "userName", command.stringValueOfParameterNamed("userName"));
	        }else if (realCause.getMessage().contains("unique_reference")){
	        	throw new PlatformDataIntegrityException("validation.error.msg.selfcare.duplicate.email", "email: " + command.stringValueOfParameterNamed("uniqueReference")+ " already exists", "email", command.stringValueOfParameterNamed("uniqueReference"));
	        }

	}
	
	@Override
	public CommandProcessingResult updateClientStatus(JsonCommand command,Long entityId) {
            try{
            	
            	this.context.authenticatedUser();
            	String status=command.stringValueOfParameterNamed("status");
            	SelfCare client=this.selfCareRepository.findOneByClientId(entityId);
            	if(client == null){
            		throw new ClientNotFoundException(entityId);
            	}
            	if(status.equalsIgnoreCase("ACTIVE")){
            	
            		if(status.equals(client.getStatus())){
            			throw new ClientStatusException(entityId);
            		}
            	}
            	client.setStatus(status);
            	this.selfCareRepository.save(client);
            	return new CommandProcessingResult(Long.valueOf(entityId));
            	
            }catch(DataIntegrityViolationException dve){
            	handleDataIntegrityIssues(command, dve);
            	return new CommandProcessingResult(Long.valueOf(-1));
            }

	}

	@Override
	public CommandProcessingResult registerSelfCare(JsonCommand command) {
		
		//SelfCareTemporary selfCareTemporary = null;
		Long clientId = 0L;
		try {
			context.authenticatedUser();
			selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			String uniqueReference = command.stringValueOfParameterNamed("userName");
			String returnUrl = command.stringValueOfParameterNamed("returnUrl");
			SelfCare repository = selfCareRepository.findOneByEmail(uniqueReference);
			
			if (null == repository) {

				SelfCareTemporary selfCareTemporary = SelfCareTemporary.fromJson(command);
				String unencodedPassword = RandomStringUtils.randomAlphanumeric(27);
				selfCareTemporary.setGeneratedKey(unencodedPassword);

				selfCareTemporaryRepository.save(selfCareTemporary);
				String generatedKey = selfCareTemporary.getGeneratedKey() + BillingMessageTemplateConstants.SELFCARE_REGISTRATION_CONSTANT;

				if(null == registerSelfcareMessageDetails){
					registerSelfcareMessageDetails = this.billingMessageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SELFCARE_REGISTER);
				}
				 
				if (registerSelfcareMessageDetails != null) {
					String subject = registerSelfcareMessageDetails.getSubject();
					String body = registerSelfcareMessageDetails.getBody();
					String header = registerSelfcareMessageDetails.getHeader() + ",";
					String footer = registerSelfcareMessageDetails.getFooter();

					body = body.replace("<PARAM1>", returnUrl + generatedKey);
					
					BillingMessage billingMessage = new BillingMessage(header, body, footer, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_EMAIL_FROM,
							selfCareTemporary.getUserName(), subject, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS,
							registerSelfcareMessageDetails, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);

					this.messageDataRepository.save(billingMessage);

					return new CommandProcessingResultBuilder().withEntityId(selfCareTemporary.getId()).withClientId(clientId).build();

				} else throw new BillingMessageTemplateNotFoundException(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SELFCARE_REGISTER);

			} else throw new SelfCareEmailIdDuplicateException(uniqueReference);

		} catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.username", "duplicate.username","duplicate.username", "duplicate.username");
		} catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
	}

	@Override
	public CommandProcessingResult selfCareEmailVerification(JsonCommand command) {
	
		try{
		
			this.context.authenticatedUser();
			this.selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			
			String verificationKey = command.stringValueOfParameterNamed("verificationKey");
			String uniqueReference = command.stringValueOfParameterNamed("uniqueReference");
			
			SelfCareTemporary selfCareTemporary = this.selfCareTemporaryRepository.findOneByGeneratedKey(verificationKey,uniqueReference);

			if(null == selfCareTemporary){				
				throw new SelfCareTemporaryGeneratedKeyNotFoundException(verificationKey,uniqueReference);				
			} else {
				if (selfCareTemporary.getStatus().equalsIgnoreCase("INACTIVE")
						|| selfCareTemporary.getStatus().equalsIgnoreCase("PENDING"))
					selfCareTemporary.setStatus("PENDING");
				else throw new SelfCareAlreadyVerifiedException(verificationKey);
			}
			
			return new CommandProcessingResultBuilder().withEntityId(selfCareTemporary.getId()).withClientId(0L).build();
			
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.username", "duplicate.username","duplicate.username", "duplicate.username");
		}catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
	}

	@Override
	public CommandProcessingResult generateNewSelfcarePassword(JsonCommand command) {
		
		try{
			this.context.authenticatedUser();
			this.selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			String uniqueReference = command.stringValueOfParameterNamed("uniqueReference");

			SelfCare selfCare =selfCareRepository.findOneByEmail(uniqueReference);
			
			if(selfCare == null){				
				throw new SelfcareEmailIdNotFoundException(uniqueReference);			
			}else{		
				String generatedKey = RandomStringUtils.randomAlphabetic(10);	
				selfCare.setPassword(generatedKey);
				
				Client client = this.clientRepository.findOne(selfCare.getClientId());
				Configuration configuration = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_SMS);
				
				if(null != configuration && configuration.isEnabled()) {
					
					if(null == newSelfcarePasswordMessageDetailsForSMS){
						newSelfcarePasswordMessageDetailsForSMS = this.billingMessageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NEW_SELFCARE_PASSWORD);
					}
					
					if (newSelfcarePasswordMessageDetailsForSMS != null) {
						
						String subject = newSelfcarePasswordMessageDetailsForSMS.getSubject();
						String body = newSelfcarePasswordMessageDetailsForSMS.getBody();
						body = body.replace("<PARAM1>", selfCare.getUserName().trim());
						body = body.replace("<PARAM2>", selfCare.getPassword().trim());

						BillingMessage billingMessage = new BillingMessage(null, body, null, 
								BillingMessageTemplateConstants.MESSAGE_TEMPLATE_EMAIL_FROM, client.getPhone(), subject,
								BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, newSelfcarePasswordMessageDetailsForSMS, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_TYPE, null);

						this.messageDataRepository.save(billingMessage);

					} else throw new BillingMessageTemplateNotFoundException(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SMS_NEW_SELFCARE_PASSWORD);
				}
				
				if(null == newSelfcarePasswordMessageDetails){
					newSelfcarePasswordMessageDetails =this.billingMessageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NEW_SELFCARE_PASSWORD);
				}
				
				if(newSelfcarePasswordMessageDetails != null){
				String subject = newSelfcarePasswordMessageDetails.getSubject();
				String body = newSelfcarePasswordMessageDetails.getBody();
				String footer = newSelfcarePasswordMessageDetails.getFooter();
				String header = newSelfcarePasswordMessageDetails.getHeader().replace("<PARAM1>", client.getDisplayName()==null || client.getDisplayName().isEmpty()?client.getFirstname(): client.getDisplayName() + ",");
				body = body.replace("<PARAM2>", selfCare.getUserName().trim());
				body = body.replace("<PARAM3>", generatedKey);
				
				BillingMessage billingMessage = new BillingMessage(header, body, footer, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_EMAIL_FROM, client.getEmail(),
						subject, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATUS, newSelfcarePasswordMessageDetails, BillingMessageTemplateConstants.MESSAGE_TEMPLATE_MESSAGE_TYPE, null);
				
				this.messageDataRepository.save(billingMessage);
				
				} else throw new BillingMessageTemplateNotFoundException(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_NEW_SELFCARE_PASSWORD);
				
			}
			
			return new CommandProcessingResultBuilder().withEntityId(selfCare.getId()).withClientId(selfCare.getClientId()).build();
			
		}catch(DataIntegrityViolationException dve){
			handleDataIntegrityIssues(command, dve);
			throw new PlatformDataIntegrityException("duplicate.username", "duplicate.username","duplicate.username", "duplicate.username");
		}catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
		
	}

	@Override
	public CommandProcessingResult selfcareChangePassword(JsonCommand command) {
		
		try{
			
			this.context.authenticatedUser();
			this.selfCareCommandFromApiJsonDeserializer.validateForCreate(command);
			String uniqueReference = command.stringValueOfParameterNamed("uniqueReference");
			String password = command.stringValueOfParameterNamed("password");
			SelfCare selfCare =selfCareRepository.findOneByEmail(uniqueReference);
			
			if(selfCare == null){				
				throw new SelfcareEmailIdNotFoundException(uniqueReference);			
			}else{		
				if(command.parameterExists("userName")){
					String userName = command.stringValueOfParameterNamed("userName");
					selfCare.setUserName(userName);
				}
				selfCare.setPassword(password);
				this.selfCareRepository.save(selfCare);
			}
			
			return new CommandProcessingResultBuilder().withEntityId(selfCare.getId()).withClientId(selfCare.getClientId()).build();
			
		} catch(EmptyResultDataAccessException emp){
			throw new PlatformDataIntegrityException("empty.result.set", "empty.result.set");
		}
		
	}
}