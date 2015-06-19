package org.mifosplatform.finance.paymentsgateway.data;

public class RecurringPaymentTransactionTypeConstants {

	// Recurring Payments Transaction Types
	
	public static final String WEB_ACCEPT	= "web_accept";
	public static final String RECURRING_PAYMENT_PROFILE_CREATED	= "recurring_payment_profile_created";
	public static final String RECURRING_PAYMENT					= "recurring_payment";
	public static final String RECURRING_PAYMENT_EXPIRED 			= "recurring_payment_expired";
	public static final String RECURRING_PAYMENT_FAILED 			= "recurring_payment_failed";
	public static final String RECURRING_PAYMENT_SKIPPED	 		= "recurring_payment_skipped";
	public static final String RECURRING_PAYMENT_SUSPENDED		= "recurring_payment_suspended";
	public static final String RECURRING_PAYMENT_SUSPENDED_DUE_TO_MAX_FAILED_PAYMENT = "recurring_payment_suspended_due_to_max_failed_payment";

	public static final String SUBSCR_SIGNUP						= "subscr_signup";
	public static final String SUBSCR_PAYMENT						= "subscr_payment";
	public static final String SUBSCR_FAILED						= "subscr_failed";
	public static final String SUBSCR_CANCELLED					= "subscr_cancelled";
	public static final String SUBSCR_EOT	 						= "subscr_eot";
	public static final String SUBSCR_MODIFY						= "subscr_modify";
	
	public static final String RECURRING_PAYMENT_FAILURE			= "FAILURE";
	public static final String RECURRING_PAYMENT_SUCCESS			= "SUCCESS";
	public static final String RECURRING_PAYMENT_UNKNOWN			= "UNKNOWN";
	
	//constants
	public static final String RECURRING_TXNTYPE		= "txn_type";
	public static final String RECURRING_VERIFIED		= "VERIFIED";
	public static final String SERVER_MODE			= "mode";
	public static final String API_USERNAME			= "acct1.UserName";
	public static final String API_PASSWORD			= "acct1.Password";
	public static final String API_SIGNATURE			= "acct1.Signature";
	
	public static final String CUSTOM 				= "custom";
	public static final String CLIENTID 				= "clientId";
	public static final String SUBSCRID				= "subscr_id";
	public static final String PLANID 				= "planId";
	public static final String PAYTERMCODE 			= "paytermCode";
	public static final String CONTRACTPERIOD 		= "contractPeriod";
	public static final String PLANCODE 				= "planCode";
	public static final String ORDERID 				= "orderId";
	public static final String RENEWALPERIOD			= "renewalPeriod";
	public static final String PRICEID 				= "priceId";
	public static final String RP_INVOICE_ID 			= "rp_invoice_id";
	public static final String MC_CURRENCY 			= "mc_currency";
	
	//Paypal Constant Variable Names
	public static final String PAYMENTSTATUS 			= "payment_status";
	public static final String TRANSACTIONID 			= "txn_id";
	public static final String MCGROSS 				= "mc_gross";
	public static final String MCCURRENCY 			= "mc_currency";
	public static final String PAYMENTDATE 			= "payment_date";
	public static final String DATEFORMAT				= "HH:MM:SS Mmm DD, YYYY PDT";
	public static final String PENDINGREASON			= "pending_reason";
	
	//Payment Status
	public static final String COMPLETED				= "Completed";
	public static final String PENDING				= "Pending";
	public static final String SUCCESS 				= "Success";
	public static final String PAYPAL 				= "paypal";
	
	// Paypal Credit/Debit Card Data Storage
	public static final String CARD_NUMBER			= "cardNumber";
	public static final String CARD_TYPE		  		= "cardType";
	public static final String CARD_EXPIRY_DATE		= "expiryDate";
	public static final String CARD_CVV				= "cardCVV";
	public static final String NAME_ON_CARD			= "nameOnCard";
			
	//paypal IPN Verification Use Parameters
	public static final String NOTIFY_VALIDATE		= "cmd=_notify-validate";
	public static final String CONTENT_TYPE_VALUE		= "application/x-www-form-urlencoded";
	public static final String CONTENT_TYPE			= "Content-Type";
	public static final String HOST					= "Host";
	public static final String POST					= "POST";
	
	
	

}
