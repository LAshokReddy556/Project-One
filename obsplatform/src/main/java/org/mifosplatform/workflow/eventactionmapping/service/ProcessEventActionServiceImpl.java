package org.mifosplatform.workflow.eventactionmapping.service;


import java.util.List;

import org.mifosplatform.cms.eventmaster.domain.EventMaster;
import org.mifosplatform.cms.eventmaster.domain.EventMasterRepository;
import org.mifosplatform.finance.billingmaster.api.BillingMasterApiResourse;
import org.mifosplatform.finance.billingorder.service.InvoiceClient;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.service.DateUtils;
import org.mifosplatform.portfolio.association.data.HardwareAssociationData;
import org.mifosplatform.portfolio.association.service.HardwareAssociationReadplatformService;
import org.mifosplatform.portfolio.order.service.OrderWritePlatformService;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequest;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestDetails;
import org.mifosplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.mifosplatform.provisioning.provisioning.api.ProvisioningApiConstants;
import org.mifosplatform.scheduledjobs.scheduledjobs.data.EventActionData;
import org.mifosplatform.workflow.eventaction.domain.EventAction;
import org.mifosplatform.workflow.eventaction.domain.EventActionRepository;
import org.mifosplatform.workflow.eventaction.service.EventActionConstants;
import org.mifosplatform.workflow.eventaction.service.ProcessEventActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;


@Service
public class ProcessEventActionServiceImpl implements ProcessEventActionService {

	
	
	private final InvoiceClient invoiceClient;
	private final FromJsonHelper fromApiJsonHelper;
	private final EventActionRepository eventActionRepository;
	private final ProcessRequestRepository processRequestRepository;
    private final OrderWritePlatformService orderWritePlatformService;
    private final EventMasterRepository eventMasterRepository;
    private final HardwareAssociationReadplatformService hardwareAssociationReadplatformService;
    private final BillingMasterApiResourse billingMasterApiResourse;
    
 

	@Autowired
	public ProcessEventActionServiceImpl(final EventActionRepository eventActionRepository,final FromJsonHelper fromJsonHelper,
			final OrderWritePlatformService orderWritePlatformService,final InvoiceClient invoiceClient,
			final ProcessRequestRepository processRequestRepository,final HardwareAssociationReadplatformService hardwareAssociationReadplatformService,
			final BillingMasterApiResourse billingMasterApiResourse,final EventMasterRepository eventMasterRepository)
	{
		this.invoiceClient=invoiceClient;
        this.fromApiJsonHelper=fromJsonHelper;
        this.eventActionRepository=eventActionRepository;
        this.eventMasterRepository=eventMasterRepository;
        this.processRequestRepository=processRequestRepository;
        this.orderWritePlatformService=orderWritePlatformService;
        this.hardwareAssociationReadplatformService=hardwareAssociationReadplatformService;
        this.billingMasterApiResourse = billingMasterApiResourse;
        
	}
	
	@Override
	public void processEventActions(EventActionData eventActionData) {
		
		EventAction eventAction=this.eventActionRepository.findOne(eventActionData.getId());
		String jsonObject=eventActionData.getJsonData();
		 JsonCommand command=null;
		 JsonElement parsedCommand =null;
		try{
			switch (eventAction.getActionName()) {
			
			case EventActionConstants.ACTION_RENEWAL:
				
				 parsedCommand = this.fromApiJsonHelper.parse(jsonObject);
	            command = JsonCommand.from(jsonObject,parsedCommand,this.fromApiJsonHelper,"RenewalOrder",
						eventActionData.getClientId(), null,null,eventActionData.getClientId(), null, null, null,null, null, null,null);
			    	this.orderWritePlatformService.renewalClientOrder(command,eventActionData.getOrderId());
			    break;
			
			case EventActionConstants.ACTION_ACTIVE :
				this.orderWritePlatformService.reconnectOrder(eventActionData.getOrderId());
				break;
				
			case EventActionConstants.ACTION_DISCONNECT :
					
					 parsedCommand = this.fromApiJsonHelper.parse(jsonObject);
					 command = JsonCommand.from(jsonObject,parsedCommand,this.fromApiJsonHelper,"DissconnectOrder",eventActionData.getClientId(), null,
						null,eventActionData.getClientId(), null, null, null,null, null, null,null);
					 this.orderWritePlatformService.disconnectOrder(command,eventActionData.getOrderId());
				 break;
				
			case EventActionConstants.ACTION_NEW :

               parsedCommand = this.fromApiJsonHelper.parse(jsonObject);
				command = JsonCommand.from(jsonObject,parsedCommand,this.fromApiJsonHelper,"CreateOrder",eventActionData.getClientId(), null,
						null,eventActionData.getClientId(), null, null, null,null, null, null,null);
				this.orderWritePlatformService.createOrder(eventActionData.getClientId(), command);
				break;	
				
			case EventActionConstants.ACTION_INVOICE :
				try{
					CommandProcessingResult result = null;
					parsedCommand = this.fromApiJsonHelper.parse(jsonObject);
					command = JsonCommand.from(jsonObject,parsedCommand,this.fromApiJsonHelper,"CreateInvoice",eventActionData.getClientId(), null,
						null,eventActionData.getClientId(), null, null, null,null, null, null,null);
					result=this.invoiceClient.createInvoiceBill(command);
					if(result!=null){
						this.billingMasterApiResourse.printInvoice(result.resourceId(),eventActionData.getClientId());
					}
					
				}catch(Exception exception){
					
				}
				break;
				
			case EventActionConstants.ACTION_SEND_PROVISION :
				try{
					final List<HardwareAssociationData> associationDatas= this.hardwareAssociationReadplatformService.retrieveClientAllocatedHardwareDetails(eventActionData.getClientId());
					if(!associationDatas.isEmpty()){
						Long none=Long.valueOf(0);
						final ProcessRequest processRequest=new ProcessRequest(none,eventActionData.getClientId(),none,ProvisioningApiConstants.PROV_BEENIUS,
													ProvisioningApiConstants.REQUEST_TERMINATE,'N','N');
						final ProcessRequestDetails processRequestDetails=new ProcessRequestDetails(none,none,null,"success",associationDatas.get(0).getProvSerialNum(), 
																	DateUtils.getDateOfTenant(), null, DateUtils.getDateOfTenant(),null,'N', ProvisioningApiConstants.REQUEST_TERMINATE,null);
						processRequest.add(processRequestDetails);
						this.processRequestRepository.save(processRequest);
					}
				}catch(Exception exception){
					
				}
				break;
				
			case EventActionConstants.ACTION_ACTIVE_LIVE_EVENT : 
				
				EventMaster eventMaster=this.eventMasterRepository.findOne(eventActionData.getResourceId());
				eventMaster.setStatus(Integer.valueOf(1));
				this.eventMasterRepository.saveAndFlush(eventMaster);
				
				break;
				
			case EventActionConstants.ACTION_INACTIVE_LIVE_EVENT : 
				
				eventMaster=this.eventMasterRepository.findOne(eventActionData.getResourceId());
				eventMaster.setStatus(Integer.valueOf(2));
				this.eventMasterRepository.saveAndFlush(eventMaster);
				
				break;
				
			case EventActionConstants.ACTION_SEND_PAYMENT :
				
				this.billingMasterApiResourse.printPayment(eventAction.getResourceId(), eventAction.getClientId());
				
				break;
			
			default:
				break;
			}
	    	eventAction.updateStatus('Y');
	    	this.eventActionRepository.save(eventAction);
		}catch(DataIntegrityViolationException exception){
			eventAction.updateStatus('F');
	    	this.eventActionRepository.save(eventAction);
			exception.printStackTrace();
		}catch (Exception exception) {
	    	eventAction.updateStatus('F');
	    	this.eventActionRepository.save(eventAction);
		}
	}
}