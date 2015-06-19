package org.mifosplatform.provisioning.provisioning.api;

/**......*/
public class ProvisioningApiConstants {
	
	//Provisioning Systems
	public static final String PROV_PACKETSPAN="Packetspan";
	public static final String PROV_COMVENIENT="Comvenient";
	public static final String PROV_STALKER="Stalker";
	public static final String PROV_BEENIUS="Beenius";
	public static final String PROV_ZEBRAOTT="ZebraOTT";
	public static final String PROV_CUBIWARE="CubiWare";
	
	
	//Events
	public static final String REQUEST_TERMINATE="TERMINATE";
	public static final String REQUEST_ACTIVATION_VOD="ACTIVATION_VOD";
	public static final String REQUEST_ACTIVATION="ACTIVATION";
	public static final String REQUEST_DISCONNECTION ="DISCONNECTION";
	public static final String REQUEST_RECONNECTION ="RECONNECTION";
	public static final String REQUEST_RENEWAL_AE ="RENEWAL_AE";
	public static final String REQUEST_RENEWAL_BE ="RENEWAL_BE";
	public static final String REQUEST_CHANGE_PLAN="CHANGE_PLAN";
	public static final String REQUEST_TERMINATION ="TERMINATION";
	public static final String REQUEST_REACTIVATION ="REACTIVATION";
	public static final String REQUEST_SUSPENTATION ="SUSPENTATION";
	public static final String REQUEST_ADDON_ACTIVATION="ADDON_ACTIVATION";
	public static final String REQUEST_CLIENT_ACTIVATION="CLIENT ACTIVATION";
	public static final String REQUEST_CREATE_AGENT="CREATE AGENT";
	public static final String REQUEST_RELEASE_DEVICE="RELEASE DEVICE";
	
	//Json Data
	public static final String PROV_DATA_CLIENTID="clientId";
	public static final String PROV_DATA_CLIENTNAME="clientName";
	public static final String PROV_DATA_ORDERID="orderId";
	public static final String PROV_DATA_MACID="macId";
	public static final String PROV_DATA_PLANNAME="planName";
	public static final String PROV_DATA_GROUPNAME="GROUP_NAME";
	public static final String PROV_DATA_IPTYPE="ip_type";
	public static final String PROV_DATA_NEW_IPTYPE="new_ip_type";
	public static final String PROV_DATA_OLD_IPTYPE="old_ip_type";
	public static final String PROV_DATA_IPADDRESS="IP_ADDRESS";
	public static final String PROV_DATA_SERVICE="SERVICE";
	public static final String PROV_DATA_OLD_IPADDRESS="OLD_IP_ADDRESS";
	public static final String PROV_DATA_NEW_IPADDRESS="NEW_IP_ADDRESS";
	public static final String PROV_DATA_SERVICETYPE="service_type";
	public static final String PROV_DATA_VLANID="VLAN_ID";
	public static final String PROV_DATA_OLD_VLANID="OLD_VLAN_ID";
	public static final String PROV_DATA_NEW_VLANID="NEW_VLAN_ID";
	public static final String PROV_DATA_OLD_GROUPNAME="OLD_GROUP_NAME";
	public static final String PROV_DATA_NEW_GROUPNAME="NEW_GROUP_NAME";
	public static final String PROV_DATA_SUBNET="subnet";
	public static final String PROV_DATA_OLD_SERIALNO="OLD_SERIALNO";
	public static final String PROV_DATA_NEW_SERIALNO="NEW_SERIALNO";
	public static final String PROV_DATA_OLD_ORDERID="ORDER_ID";
	
	//Provisioning Actions
	public static final String PROV_EVENT_CREATE_CLIENT="Create Client";
	public static final String PROV_EVENT_CREATE_AGENT="Create Agent";
	public static final String PROV_EVENT_CLOSE_CLIENT="Close Client";
	public static final String PROV_EVENT_RELEASE_DEVICE="Release Device";
	public static final String PROV_EVENT_CREATE_NAS="Create Nas";
	public static final String PROV_EVENT_CREATE_RADSERVICE="Create RadSevice";
	public static final String PROV_EVENT_Change_CREDENTIALS="Change Credentials";
	public static final String PROV_EVENT_UPDATE_RADSERVICE="Update RadService";
	public static final String PROV_EVENT_REMOVE_RADSERVICE = "Remove RadService";
	

	

}

