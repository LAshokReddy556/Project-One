package org.mifosplatform.portfolio.plan.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.infrastructure.codes.exception.CodeNotFoundException;
import org.mifosplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.plan.domain.Plan;
import org.mifosplatform.portfolio.plan.domain.PlanDetails;
import org.mifosplatform.portfolio.plan.domain.PlanQualifier;
import org.mifosplatform.portfolio.plan.domain.PlanRepository;
import org.mifosplatform.portfolio.plan.domain.VolumeDetails;
import org.mifosplatform.portfolio.plan.domain.VolumeDetailsRepository;
import org.mifosplatform.portfolio.plan.serialization.PlanCommandFromApiJsonDeserializer;
import org.mifosplatform.portfolio.service.domain.ServiceMaster;
import org.mifosplatform.portfolio.service.domain.ServiceMasterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import com.google.gson.JsonArray;


/**
 * @author hugo
 *
 */
@Service
public class PlanWritePlatformServiceImpl implements PlanWritePlatformService {
	 private final static Logger LOGGER = LoggerFactory.getLogger(PlanWritePlatformServiceImpl.class);
	private final PlatformSecurityContext context;
	private final PlanRepository planRepository;
	private final ServiceMasterRepository serviceMasterRepository;
	private final PlanCommandFromApiJsonDeserializer fromApiJsonDeserializer;
	private final VolumeDetailsRepository volumeDetailsRepository;
	
	
	@Autowired
	public PlanWritePlatformServiceImpl(final PlatformSecurityContext context,final PlanRepository planRepository,
			final ServiceMasterRepository serviceMasterRepository,final VolumeDetailsRepository volumeDetailsRepository,
			final PlanCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
		
		this.context = context;
		this.planRepository = planRepository;
		this.serviceMasterRepository =serviceMasterRepository;
		this.fromApiJsonDeserializer=fromApiJsonDeserializer;
		this.volumeDetailsRepository=volumeDetailsRepository;

	}
  
	/* 
     * @param JsonData
     * @return ResourceId
     */
    @Transactional
	@Override
	public CommandProcessingResult createPlan(final JsonCommand command) {

		try {
			  this.context.authenticatedUser();
		      this.fromApiJsonDeserializer.validateForCreate(command.json());
			  final Plan plan=Plan.fromJson(command);
			  final String[] services = command.arrayValueOfParameterNamed("services");
		      final Set<PlanDetails> selectedServices = assembleSetOfServices(services);
		      plan.addServieDetails(selectedServices);
             this.planRepository.save(plan);
             	
             if(plan.isPrepaid() == ConfigurationConstants.CONST_IS_Y){
            	 final VolumeDetails volumeDetails=VolumeDetails.fromJson(command,plan);
            	 this.volumeDetailsRepository.save(volumeDetails);
             }
             
             return new CommandProcessingResult(Long.valueOf(plan.getId()));

		} catch (DataIntegrityViolationException dve) {
			 handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
	}

	private void handleCodeDataIntegrityIssues(final JsonCommand command,
			final DataIntegrityViolationException dve) {
		
		 final Throwable realCause = dve.getMostSpecificCause();
	        if (realCause.getMessage().contains("uplan_code_key")) {
	            final String name = command.stringValueOfParameterNamed("planCode");
	            throw new PlatformDataIntegrityException("error.msg.code.duplicate.name", "A code with name '" + name + "' already exists");
	        }
	        if (realCause.getMessage().contains("plan_description")) {
	            final String name = command.stringValueOfParameterNamed("planDescription");
	            throw new PlatformDataIntegrityException("error.msg.description.duplicate.name", "A description with name '" + name + "' already exists");
	        }

	        LOGGER.error(dve.getMessage(), dve);
	        throw new PlatformDataIntegrityException("error.msg.cund.unknown.data.integrity.issue",
	                "Unknown data integrity issue with resource: " + realCause.getMessage());
		
	}

	/*@Param planid and jsondata
	 * @return planId
	 */
	@Override
	public CommandProcessingResult updatePlan(final Long planId,final JsonCommand command) {
		try
		{
			
				context.authenticatedUser();
	            this.fromApiJsonDeserializer.validateForCreate(command.json());
	            final Plan plan = retrievePlanBy(planId);
	            final Map<String, Object> changes = plan.update(command);
 		  
	            if (changes.containsKey("services")) {
	            	final String[] serviceIds = (String[]) changes.get("services");
	            	final Set<PlanDetails> selectedServices = assembleSetOfServices(serviceIds);
	            	plan.addServieDetails(selectedServices);
	            }
 		  
             this.planRepository.save(plan);

             if(plan.isPrepaid()!= ConfigurationConstants.CONST_IS_N){
            	//final  VolumeDetailsData detailsData=this.eventActionReadPlatformService.retrieveVolumeDetails(plan.getId());
            	VolumeDetails volumeDetails = this.volumeDetailsRepository.findoneByPlanId(plan.getId());
            	
            	 if(volumeDetails == null){
            		 volumeDetails=VolumeDetails.fromJson(command, plan);
            	 
            	 }else{
            		 volumeDetails.update(command,planId);	 
            	 }
            	 this.volumeDetailsRepository.save(volumeDetails);
             }

             return new CommandProcessingResultBuilder() //
         .withCommandId(command.commandId()) //
         .withEntityId(planId) //
         .with(changes) //
         .build();
	} catch (DataIntegrityViolationException dve) {
		 handleCodeDataIntegrityIssues(command, dve);
		return new CommandProcessingResult(Long.valueOf(-1));
	}
	}

	private Set<PlanDetails> assembleSetOfServices(final String[] serviceArray) {

        final Set<PlanDetails> allServices = new HashSet<>();
        if (!ObjectUtils.isEmpty(serviceArray)) {
            for (final String serviceId : serviceArray) {
                final ServiceMaster serviceMaster = this.serviceMasterRepository.findOne(Long.valueOf(serviceId));
                if (serviceMaster != null) { 
                	  PlanDetails detail=new PlanDetails(serviceMaster.getServiceCode());
                allServices.add(detail);
                }
            }
        }

        return allServices;
    }
	
	private Plan retrievePlanBy(final Long planId) {
		  final Plan plan = this.planRepository.findOne(planId);
	        if (plan == null) { throw new CodeNotFoundException(planId.toString()); }
	        return plan;
	}


	/* @param planid
	 * @return planId
	 */
	@Transactional
	@Override
	public CommandProcessingResult deleteplan(final Long planId) {
		final  Plan plan=this.planRepository.findOne(planId);
		 plan.delete();
		 this.planRepository.save(plan);
		 return new CommandProcessingResultBuilder().withEntityId(planId).build();
	}

	@Override
	public CommandProcessingResult updatePlanQualifierData(Long entityId,JsonCommand command) {
		try{
			
			 this.context.authenticatedUser();
			 Plan plan = this.planRepository.findOne(entityId);
			/* if(!plan.getPlanQualifiers().isEmpty()){
				 plan.getPlanQualifiers().clear();
			 }*/
			 final JsonArray array=command.arrayOfParameterNamed("partners").getAsJsonArray();
			 String[] partners =null;
			 partners=new String[array.size()];
			 for(int i=0; i<array.size();i++){
				 partners[i] =array.get(i).getAsString();
			 }
			 
			 for (String partnerId : partners) {
				 
				 final Long id = Long.valueOf(partnerId);
				 PlanQualifier planQualifier=new PlanQualifier(id);
				// plan.addPlanQualifierDetails(planQualifier);
			 }
		 
			 this.planRepository.save(plan);
			return new CommandProcessingResult(entityId);
		}catch(DataIntegrityViolationException dve){
			handleCodeDataIntegrityIssues(command, dve);
			return new CommandProcessingResult(Long.valueOf(-1));
		}
		
	}

}
