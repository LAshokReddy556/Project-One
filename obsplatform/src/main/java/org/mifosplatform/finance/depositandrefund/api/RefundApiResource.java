package org.mifosplatform.finance.depositandrefund.api;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.billing.selfcare.domain.SelfCareTemporaryRepository;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.finance.clientbalance.domain.ClientBalance;
import org.mifosplatform.finance.clientbalance.domain.ClientBalanceRepository;
import org.mifosplatform.finance.depositandrefund.exception.ItemQualityAndStatusException;
import org.mifosplatform.finance.payments.data.PaymentData;
import org.mifosplatform.finance.payments.service.PaymentReadPlatformService;
import org.mifosplatform.infrastructure.codes.data.CodeData;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/refund")
@Component
@Scope("singleton")
public class RefundApiResource {

	/**
	 * The set of parameters that are supported in response for {@link CodeData}
	 */
	private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(
			Arrays.asList("id", "clientId", "refundAmount"));
	private final static String RESOURCENAMEFORPERMISSIONS = "REFUND";

	private final PlatformSecurityContext context;
	private final PaymentReadPlatformService readPlatformService;
	private final DefaultToApiJsonSerializer<PaymentData> toApiJsonSerializer;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final PortfolioCommandSourceWritePlatformService writePlatformService;
	private final SelfCareTemporaryRepository selfCareTemporaryRepository;
	private final ClientBalanceRepository clientBalanceRepository;

	@Autowired
	public RefundApiResource(final PlatformSecurityContext context,final PaymentReadPlatformService readPlatformService,
			final DefaultToApiJsonSerializer<PaymentData> toApiJsonSerializer,final ApiRequestParameterHelper apiRequestParameterHelper,
			final PortfolioCommandSourceWritePlatformService writePlatformService,
			final SelfCareTemporaryRepository selfCareTemporaryRepository,
			final ClientBalanceRepository clientBalanceRepository) {
		
		this.context = context;
		this.readPlatformService = readPlatformService;
		this.toApiJsonSerializer = toApiJsonSerializer;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.writePlatformService = writePlatformService;
		this.selfCareTemporaryRepository = selfCareTemporaryRepository;
		this.clientBalanceRepository = clientBalanceRepository;
	}

	/**
	 * This method is using for posting data to create payment
	 */
	@POST
	@Path("{depositId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String createRefundAmount(@PathParam("depositId") final Long depositId,	final String apiRequestBodyAsJson) {
		final CommandWrapper commandRequest = new CommandWrapperBuilder().createRefundAmount(depositId).withJson(apiRequestBodyAsJson).build();
		final CommandProcessingResult result = this.writePlatformService.logCommandSource(commandRequest);
		return this.toApiJsonSerializer.serialize(result);
	}
	
	@GET
	@Path("{clientId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveRefundAmount(@PathParam("clientId") final Long clientId,@Context final UriInfo uriInfo,
			@QueryParam("depositAmount") BigDecimal depositAmount) {
		context.authenticatedUser().validateHasReadPermission(RESOURCENAMEFORPERMISSIONS);
		
		ClientBalance clientBalance = clientBalanceRepository.findByClientId(clientId);
		BigDecimal returnAmount = null;
		if(clientBalance.getBalanceAmount().intValue() == 0){
			returnAmount = depositAmount;
		}else if(clientBalance.getBalanceAmount().intValue() <= depositAmount.intValue()){
			
			if(clientBalance.getBalanceAmount().intValue() > 0){
				//MathContext mc = new MathContext(4);
				returnAmount = depositAmount.subtract(clientBalance.getBalanceAmount());
			}else{
				returnAmount = depositAmount;
			}
			
		}else{
			throw new ItemQualityAndStatusException(clientBalance.getBalanceAmount());	
		}
		PaymentData paymentData = new PaymentData();
		paymentData.setAvailAmount(returnAmount);
		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
		return this.toApiJsonSerializer.serialize(settings, paymentData,RESPONSE_DATA_PARAMETERS);

	}
	
	
    	 
}
