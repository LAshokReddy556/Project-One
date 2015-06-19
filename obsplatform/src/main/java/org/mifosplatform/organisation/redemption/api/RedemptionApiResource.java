package org.mifosplatform.organisation.redemption.api;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONObject;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.codes.data.CodeData;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * Class to Create {@link Redemption}
 * 
 * @author Raghu Chiluka
 *
 */
@Path("/redemption")
@Component
@Scope("singleton")
public class RedemptionApiResource {

	/**
	 * The set of parameters that are supported in response for {@link CodeData}
	 */

	private final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService;
	
	@Autowired
	public RedemptionApiResource(final PortfolioCommandSourceWritePlatformService portfolioCommandSourceWritePlatformService){
		this.portfolioCommandSourceWritePlatformService = portfolioCommandSourceWritePlatformService;
	}
	
	/**
	 * Defining Post Method for Creating Redemption
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createRedemption(final String apiReqBodyAsJson) {
		
		try{
			final CommandWrapper commandRequest = new CommandWrapperBuilder().createRedemption().withJson(apiReqBodyAsJson).build();
			CommandProcessingResult result = this.portfolioCommandSourceWritePlatformService.logCommandSource(commandRequest);
			
			JSONObject withChanges = new JSONObject(apiReqBodyAsJson);
			String clientId = withChanges.getString("clientId");
			String txnId = withChanges.getString("pinNumber");
			
			if(result != null && result.getClientId() !=null){
				String resourceId = result.getClientId().toString();
		
				if(clientId.equalsIgnoreCase(resourceId)){
					
					withChanges.put("Result", "SUCCESS");
					withChanges.put("Description", "Transaction Successfully Completed");
					withChanges.put("TransactionId", txnId);
				
				}else{
					withChanges.put("Result", "FAILURE");
					withChanges.put("Description", "Transaction Failed");
					withChanges.put("TransactionId", txnId);
				}
				
			}else{
				withChanges.put("Result", "FAILURE");
				withChanges.put("Description", "Payment Failure");
				withChanges.put("TransactionId", txnId);
			}
			return withChanges.toString();
			
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
		//return this.toApiJsonSerializer.serialize(result);
		
	}
}
