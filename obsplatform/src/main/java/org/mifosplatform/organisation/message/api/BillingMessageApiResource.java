package org.mifosplatform.organisation.message.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.codes.data.CodeData;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.message.data.BillingMessageDataForProcessing;
import org.mifosplatform.organisation.message.data.BillingMessageTemplateData;
import org.mifosplatform.organisation.message.domain.BillingMessage;
import org.mifosplatform.organisation.message.domain.BillingMessageRepository;
import org.mifosplatform.organisation.message.service.BillingMesssageReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * The class <code>BillingMessageApiResource</code> is 
 * developed for Creating Message Template, which is 
 * useful in Emails Preparation. 
 * 
 * For More info see in the OBS Documentation.
 * 
 * @author ashokreddy
 *
 */

@Path("/messages")
@Component
@Scope("singleton")
public class BillingMessageApiResource {
	
	/**
	 * The set of parameters that are supported in response for {@link CodeData}
	 */
	private final Set<String> RESPONSE_PARAMETERS = new HashSet<String>(Arrays.asList("id", "templateDescription", 
			"subject", "header", "body","footer","messageParams","deleteButtonId"));
	
	private final String resourceNameForPermissions = "MESSAGE";
	
	private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
	private final DefaultToApiJsonSerializer<BillingMessageTemplateData> toApiJsonSerializer;
	private final BillingMesssageReadPlatformService billingMesssageReadPlatformService;
	private final PlatformSecurityContext context;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final DefaultToApiJsonSerializer<BillingMessageDataForProcessing> messageApiJsonSerializer;
	private final BillingMessageRepository messageDataRepository;
	
	
	
	
	@Autowired
	public BillingMessageApiResource(
			final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
			final DefaultToApiJsonSerializer<BillingMessageTemplateData> toApiJsonSerializer,
			final BillingMesssageReadPlatformService billingMesssageReadPlatformService,
			final PlatformSecurityContext context,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final DefaultToApiJsonSerializer<BillingMessageDataForProcessing> messageApiJsonSerializer,
			final BillingMessageRepository messageDataRepository) {

		this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.context = context;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.billingMesssageReadPlatformService = billingMesssageReadPlatformService;
		this.messageApiJsonSerializer = messageApiJsonSerializer;
		this.messageDataRepository = messageDataRepository;

	}

	/**
	 * This method <code>createMessageTemplate</code> is 
	 * used For Creating Unique Message Templates.
	 * 
	 * For More Info see in the OBS Documentation.
	 * 
	 * @param requestData
	 * 			Containg input data in the Form of JsonObject.
	 * @return
	 */
	@POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createMessageTemplate(final String requestData) {
		
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createBillingMessage().withJson(requestData).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    } 
	
	/**
	 * This method <code>createMessage</code> used for
	 * Creating Email or OSD(on-screen data/message) Messages.
	 * with the Message Template and requestData.
	 *
	 * For More Info see in the OBS Documentation.
	 * 
	 * @param messageId
	 * 			id of the MessageTemplate
	 * @param requestData
	 * 			Containg input data in the Form of JsonObject.
	 * @return
	 */
	@POST
	@Path("{messageId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createMessage(@PathParam("messageId") final Long messageId, final String requestData) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().createMessageData(messageId).withJson(requestData).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}
	
	/**
	 * This method <code>retrieveMessageTemplates</code> 
	 * used for Retrieving the All Message Templates Data 
	 * with it's Parameters.
	 * 
	 * @param uriInfo
	 * 			Containing Url information
	 * @return
	 */	
	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	 public String retrieveMessageTemplates(@Context final UriInfo uriInfo) {
		
        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
        
        final Collection<BillingMessageTemplateData> templateData = this.billingMesssageReadPlatformService.retrieveAllMessageTemplateParams(); 
        
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        
        return this.toApiJsonSerializer.serialize(settings, templateData, RESPONSE_PARAMETERS);
    }
	
	/**
	 * This method <code>retrieveTemplate</code> 
	 * used for Retrieving the all mandatory/necessary data
	 * For creating a Message Template. like MessageType etc..
	 * 
	 * @param uriInfo
	 * 			Containing Url information
	 * @return
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	 public String retrieveTemplate(@Context final UriInfo uriInfo) {
		
        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
        
        final BillingMessageTemplateData messageType=this.billingMesssageReadPlatformService.retrieveTemplate();
        
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        
        return this.toApiJsonSerializer.serialize(settings, messageType, RESPONSE_PARAMETERS);
    }
	
	/**
	 * This method <code>retrieveMessageTemplate</code> 
	 * used for Retrieving the Particular Message Template 
	 * with it's Parameters.
	 * 
	 * @param messageId
	 * 			id of the MessageTemplate
	 * @param uriInfo
	 * 			Containing Url information
	 * @return
	 */
	@GET
	@Path("{messageId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveMessageTemplate(@PathParam("messageId") final Long messageId, @Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

		final List<BillingMessageTemplateData> messageParams = this.billingMesssageReadPlatformService.retrieveMessageParams(messageId);
		
		final BillingMessageTemplateData messageType = this.billingMesssageReadPlatformService.retrieveTemplate();
		
		final BillingMessageTemplateData templateData = this.billingMesssageReadPlatformService.retrieveMessageTemplate(messageId);
		templateData.setMessageParams(messageParams);
		templateData.setMessageType(messageType.getMessageTypes());
		
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		
		return this.toApiJsonSerializer.serialize(settings, templateData,RESPONSE_PARAMETERS);
	}
	
	/**
	 * This method <code>updateMessageTemplate</code> used for
	 * Updating/edit the Existing Message Template.
	 *
	 * For More Info see in the OBS Documentation.
	 * 
	 * @param messageId
	 * 			id of the MessageTemplate
	 * @param requestData
	 * 			Containg input data in the Form of JsonObject.
	 * @return
	 */
	@PUT
	@Path("{messageId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateMessageTemplate(@PathParam("messageId") final Long messageId, final String requestData) {

		final CommandWrapper commandRequest = new CommandWrapperBuilder().updateBillingMessage(messageId).withJson(requestData).build();

		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

		return this.toApiJsonSerializer.serialize(result);
	}
	
	/**
	 * This method <code>deleteMessageTemplate</code> used for
	 * delete the Existing Message Template.
	 * 
	 * Note: Delete Message Template means changing the
	 * status of the Message Template 'N' to 'Y' Only.
	 *  
	 * @param messageId
	 * 			id of the MessageTemplate
	 * @return
	 */
	@DELETE
	@Path("{messageId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteMessageTemplate(@PathParam("messageId") final Long messageId) {
		
		final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteBillingMessage(messageId).build();
		
		final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
		
		return this.toApiJsonSerializer.serialize(result);
	}
	
	@GET
	@Path("messageData/{id}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	 public String retrieveMessageData(@PathParam("id") final Long id, @Context final UriInfo uriInfo) {
		
        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
        
        final List<BillingMessageDataForProcessing> templateData = this.billingMesssageReadPlatformService.retrieveMessageDataForProcessing(id); 
        
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        
        return this.messageApiJsonSerializer.serialize(settings, templateData, RESPONSE_PARAMETERS);
    }
	
	@PUT
	@Path("messageData/{messageId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String updateMessageData(@PathParam("messageId") final Long messageId, final String requestData) {
		
		JSONObject obj = null;
		try {
			
			obj = new JSONObject(requestData);
			context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
			BillingMessage billingMessage = this.messageDataRepository.findOne(messageId);
			
			billingMessage.setStatus(obj.getString("status"));
			billingMessage.setDescription(obj.getString("description"));
			
			this.messageDataRepository.save(billingMessage);
			
			obj = new JSONObject();
			obj.put("result", "Success");
			obj.put("error", "");
			return obj.toString();
			
		} catch (JSONException e) {
			String error = "JsonException Throwing and Reason="+ stackTrace(e);
			obj = new JSONObject();
			try {
				obj.put("result", "Failure");
				obj.put("error", error);
			} catch (JSONException e1) {
				e.printStackTrace();
			}
			return obj.toString();
		}	
	}
	
	private String stackTrace(Exception ex){
		StringWriter errors = new StringWriter();
		ex.printStackTrace(new PrintWriter(errors));
		return errors.toString();
	}
	
}
