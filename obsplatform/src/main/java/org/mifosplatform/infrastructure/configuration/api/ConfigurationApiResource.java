/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mifosplatform.infrastructure.configuration.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.ws.rs.Consumes;
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
import org.mifosplatform.infrastructure.configuration.data.ConfigurationData;
import org.mifosplatform.infrastructure.configuration.data.ConfigurationPropertyData;
import org.mifosplatform.infrastructure.configuration.service.ConfigurationReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/configurations")
@Component
@Scope("singleton")
public class ConfigurationApiResource {

    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("globalConfiguration"));

    private static final String RESOURCENAMEFORPERMISSIONS = "CONFIGURATION";
    private static final String CONFIGURATION_PATH_LOCATION = System.getProperty("user.home") + File.separator + ".obs" + File.separator + ".clientconfigurations";
    private static final String CONFIGURATION_FILE_LOCATION = CONFIGURATION_PATH_LOCATION + File.separator + "ClientConfiguration.txt";


    private final PlatformSecurityContext context;
    private final ConfigurationReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<ConfigurationData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<ConfigurationPropertyData> propertyDataJsonSerializer;

    @Autowired
    public ConfigurationApiResource(final PlatformSecurityContext context,
            final ConfigurationReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<ConfigurationData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final DefaultToApiJsonSerializer<ConfigurationPropertyData> propertyDataJsonSerializer) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.propertyDataJsonSerializer=propertyDataJsonSerializer;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllConfigurations(@Context final UriInfo uriInfo) throws JSONException {


        final ConfigurationData configurationData = this.readPlatformService.retrieveGlobalConfiguration();
        
       // String defaultConfiguration = "";
        
        /**	{\"payment\":\"false\",\"IPTV\":\"false\"," +
			"\"IsClientIndividual\":\"false\",\"deviceAgrementType\":\"SALE\"," +
			"\"SubscriptionPayment\":\"false\",\"nationalId\":\"false\"," +
			"\"clientListing\":{\"userName\":\"false\", \"group/parentId\":\"false\"," +
			"\"password\":\"false\",\"externalId\":\"false\",\"customerCategory\":\"false\"}}
         */        
        JSONObject defaultOne = new JSONObject();
		JSONObject defaultOneForClientList = new JSONObject();
		JSONObject registrationList = new JSONObject();
		JSONObject orderActionsList = new JSONObject();
		
		/*********  Preparing 'defaultOne' JSONObject ******/
        defaultOne.put("payment", "false");
		defaultOne.put("IPTV", "false");
		defaultOne.put("IsClientIndividual", "false");
		defaultOne.put("deviceAgrementType", "SALE");
		defaultOne.put("SubscriptionPayment", "false");
		defaultOne.put("nationalId", "false");
		defaultOne.put("date_format", "dd MMMM yyyy");
		defaultOne.put("codeDefinitionLength", "10");
		defaultOne.put("clientonlinecheck", "false");
		defaultOne.put("isAutoRenew", "false");
		
		
		/*********  Preparing 'defaultOneForClientList' JSONObject ******/
		defaultOneForClientList.put("userName", "false");
		defaultOneForClientList.put("group/parentId", "false");
		defaultOneForClientList.put("password", "false");
		defaultOneForClientList.put("externalId", "false");
		defaultOneForClientList.put("customerCategory", "false");
		
		/*********  Preparing 'orderActionsList' JSONObject ******/
		orderActionsList.put("disconnect", "true");
		orderActionsList.put("topup/renewal", "true");
		orderActionsList.put("addons", "true");
		orderActionsList.put("deviceswap", "true");
		orderActionsList.put("applypromo", "true");
		orderActionsList.put("changeplan", "true");
		orderActionsList.put("suspend", "true");
		orderActionsList.put("pairing", "true");
		orderActionsList.put("commandcenter", "true");
		orderActionsList.put("ipchange", "true");
		orderActionsList.put("terminate", "true");
		orderActionsList.put("reconnect", "true");
		
		/*********  Adding 'defaultOneForClientList' to 'defaultOne' JSONObject ******/
		defaultOne.put("clientListing", defaultOneForClientList);
		
		/*********  Preparing 'registrationClientList' JSONObject ******/
		registrationList.put("passport", "false");
		
		/*********  Adding 'registrationClientList' to 'defaultOne' JSONObject ******/
		defaultOne.put("registrationListing", registrationList);
		
		/*********  Adding 'orderActionsClientList' to 'defaultOne' JSONObject ******/
		defaultOne.put("orderActions", orderActionsList);
		
        String readDatas;  /****** Reding data from file ******/
        File fileForPath = new File(CONFIGURATION_PATH_LOCATION);
        if(!fileForPath.isDirectory()){
        	fileForPath.mkdir();
        }
        File fileForLocation = new File(CONFIGURATION_FILE_LOCATION);
        if (!fileForLocation.isFile()) {
        	writeFileData(CONFIGURATION_FILE_LOCATION, defaultOne.toString());
        }else if(defaultOne.toString().split(",").length != readFileData(fileForLocation).split(",").length){
        	
        	/** 	readOne && defaultOne  we can set readOne(preparing that element)
        			!readOne && defaultOne we can set defaultOne(preparing that element)
        			readOne && !defaultOne  here we no need to prepare(we dont set if defaultOne has not consists element)
        	*/
        	
        	readDatas = readFileData(fileForLocation);
        	
    		JSONObject readOne = new JSONObject(readDatas);
    		
    		JSONObject prepareOne = new JSONObject();
    		JSONObject prepareOneForClientList = new JSONObject();
    		JSONObject prepareOneForRegisterList = new JSONObject();
    		JSONObject prepareOneForOrderActions = new JSONObject();
    		
        	for(int i=0;i<defaultOne.length();i++){
        		LinkedList<String> listForDefaultOne = iteratorOperation(defaultOne);
    			if((listForDefaultOne.get(i).equalsIgnoreCase("clientListing")||(listForDefaultOne.get(i).equalsIgnoreCase("orderActions"))||
    					listForDefaultOne.get(i).equalsIgnoreCase("registrationListing")) && readOne.has(listForDefaultOne.get(i))){
    				
    				JSONObject insidedefaultOneObj = new JSONObject(defaultOne.getString(listForDefaultOne.get(i)));
    				JSONObject insidereadobj = new JSONObject(readOne.getString(listForDefaultOne.get(i)));
    				
    				LinkedList<String> listForDefaultOneClientList = iteratorOperation(insidedefaultOneObj);
    				System.out.println("In listing: "+insidedefaultOneObj.length());
    				
    				for(int j=0; j<insidedefaultOneObj.length(); j++){
    					if(insidereadobj.has(listForDefaultOneClientList.get(j))){
    						if(listForDefaultOne.get(i).equalsIgnoreCase("clientListing")){
    						prepareOneForClientList.put(listForDefaultOneClientList.get(j), insidereadobj.getString(listForDefaultOneClientList.get(j)));
    						prepareOne.put(listForDefaultOne.get(i), prepareOneForClientList);
    						}else if(listForDefaultOne.get(i).equalsIgnoreCase("registrationListing")){
    							prepareOneForRegisterList.put(listForDefaultOneClientList.get(j), insidereadobj.getString(listForDefaultOneClientList.get(j)));
        						prepareOne.put(listForDefaultOne.get(i), prepareOneForRegisterList);
    						}else if(listForDefaultOne.get(i).equalsIgnoreCase("orderActions")){
    							prepareOneForOrderActions.put(listForDefaultOneClientList.get(j), insidereadobj.getString(listForDefaultOneClientList.get(j)));
        						prepareOne.put(listForDefaultOne.get(i), prepareOneForOrderActions);

    						}
    					}else{
    						if(listForDefaultOne.get(i).equalsIgnoreCase("clientListing")){
    							prepareOneForClientList.put(listForDefaultOneClientList.get(j), insidedefaultOneObj.getString(listForDefaultOneClientList.get(j)));
    							prepareOne.put(listForDefaultOne.get(i), prepareOneForClientList);
    						}else if(listForDefaultOne.get(i).equalsIgnoreCase("registrationListing")){
    							prepareOneForRegisterList.put(listForDefaultOneClientList.get(j), insidedefaultOneObj.getString(listForDefaultOneClientList.get(j)));
        						prepareOne.put(listForDefaultOne.get(i), prepareOneForRegisterList);
    						}else if(listForDefaultOne.get(i).equalsIgnoreCase("orderActions")){
    							prepareOneForOrderActions.put(listForDefaultOneClientList.get(j), insidedefaultOneObj.getString(listForDefaultOneClientList.get(j)));
    							prepareOne.put(listForDefaultOne.get(i), prepareOneForOrderActions);
        					}
    					}
    				}
    				
    			}else if((listForDefaultOne.get(i).equalsIgnoreCase("clientListing")||(listForDefaultOne.get(i).equalsIgnoreCase("orderActions"))||
    					listForDefaultOne.get(i).equalsIgnoreCase("registrationListing")) && !readOne.has(listForDefaultOne.get(i))){
    				
    				JSONObject insidedefaultOneObj1 = new JSONObject(defaultOne.getString(listForDefaultOne.get(i)));
    				LinkedList<String> listForDefaultOneClientList1 = iteratorOperation(insidedefaultOneObj1);
    				for(int k=0; k<insidedefaultOneObj1.length(); k++){
    					if(listForDefaultOne.get(i).equalsIgnoreCase("clientListing")){
    						prepareOneForClientList.put(listForDefaultOneClientList1.get(k), insidedefaultOneObj1.getString(listForDefaultOneClientList1.get(k)));
    						prepareOne.put(listForDefaultOne.get(i), prepareOneForClientList);
    					}else if(listForDefaultOne.get(i).equalsIgnoreCase("registrationListing")){
    						prepareOneForRegisterList.put(listForDefaultOneClientList1.get(k), insidedefaultOneObj1.getString(listForDefaultOneClientList1.get(k)));
        					prepareOne.put(listForDefaultOne.get(i), prepareOneForRegisterList);
    					}else if(listForDefaultOne.get(i).equalsIgnoreCase("orderActions")){
    						prepareOneForOrderActions.put(listForDefaultOneClientList1.get(k), insidedefaultOneObj1.getString(listForDefaultOneClientList1.get(k)));
    						prepareOne.put(listForDefaultOne.get(i), prepareOneForOrderActions);
    					}
    				}
    			}else if(readOne.has(listForDefaultOne.get(i)) && defaultOne.has(listForDefaultOne.get(i))){
    				prepareOne.put(listForDefaultOne.get(i), readOne.getString(listForDefaultOne.get(i)));
    			}else if(!readOne.has(listForDefaultOne.get(i)) && defaultOne.has(listForDefaultOne.get(i))){
    				prepareOne.put(listForDefaultOne.get(i), defaultOne.getString(listForDefaultOne.get(i)));
    			}
    		}
        	/*for(int i = readDatas.split(",").length; i < defaultConfiguration.split(",").length; i++ ){
        		readDatas = readDatas.split("}")[0] + "," + defaultConfiguration.split(",")[i];
        	}
        	writeFileData(CONFIGURATION_FILE_LOCATION, readDatas);*/
        	writeFileData(CONFIGURATION_FILE_LOCATION, prepareOne.toString());
        }
        String returnData = readFileData(fileForLocation);
        configurationData.setClientConfiguration(returnData);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, configurationData, RESPONSE_DATA_PARAMETERS);
    }
    
    @GET
    @Path("{configId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveSingleConfiguration(@PathParam("configId") final Long configId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);

        final ConfigurationPropertyData configurationData = this.readPlatformService.retrieveGlobalConfiguration(configId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.propertyDataJsonSerializer.serialize(settings, configurationData, this.RESPONSE_DATA_PARAMETERS);
    }
    
    @PUT
    @Path("{configId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateConfiguration(@PathParam("configId") final Long configId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateConfiguration(configId) //
                .withJson(apiRequestBodyAsJson) //
                .build();
        
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        
        return this.toApiJsonSerializer.serialize(result);
    }
    
    @POST
    @Path("smtp")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String createSmtp(final String jsonRequestBody){
    	
    	final CommandWrapper commandRequest = new CommandWrapperBuilder().createSmtpConfiguration().withJson(jsonRequestBody).build();
    	final CommandProcessingResult result= this.commandsSourceWritePlatformService.logCommandSource(commandRequest); 
    	return this.toApiJsonSerializer.serialize(result);
    	
    }
    
    @PUT
    @Path("config")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateConfig(final String apiRequestBodyAsJson) throws JSONException {
    	String newtext = null;
    	JSONObject json = new JSONObject(apiRequestBodyAsJson);
    	String name = json.getString("name");
    	String oldValue = json.getString("oldValue");
    	String newValue = json.getString("newValue");
    	
    	File file = new File(CONFIGURATION_FILE_LOCATION);
    	String readData = readFileData(file);
    	newtext = readData.replaceAll("\""+name+"\":\""+oldValue+"\"", "\""+name+"\":\""+newValue+"\"");
    	writeFileData(CONFIGURATION_FILE_LOCATION, newtext);
		return newtext;
    }
    
    private String readFileData(File file){
    	String line = "", oldtext = "";
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			while((line = reader.readLine()) != null)
			{
				oldtext += line;
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return oldtext;
    }
    
    private void writeFileData(String fileLocation, String writeValue){
    		
		try {
			FileWriter writer = new FileWriter(fileLocation);
			writer.write(writeValue);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    private LinkedList<String> iteratorOperation(JSONObject object){
    	
    	Iterator iterator = object.keys();
		
		LinkedList<String> list = new LinkedList<String>();
		while(iterator.hasNext()){
			list.push(iterator.next().toString());
		}
		return list;
    }
}