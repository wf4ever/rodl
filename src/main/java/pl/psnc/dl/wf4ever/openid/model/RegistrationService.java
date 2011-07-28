package pl.psnc.dl.wf4ever.openid.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParameters.NamedPair;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageException;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;

/**
 * Consolidates business logic from the UI code for Registration activities.
 * 
 * Most of this code modeled after ConsumerServlet, part of the openid4java 
 * sample code available at 
 * http://code.google.com/p/openid4java/wiki/SampleConsumer.
 * Some of this code was outright copied :->.
 * 
 * @author J Steven Perry
 * @author http://makotoconsulting.com
 *
 */
public class RegistrationService
{

	private static final Logger log = Logger
			.getLogger(RegistrationService.class);

	public static final String DISCOVERY_INFORMATION = "openid-disc";

	public static final Map<String, String> axFields = new HashMap<String, String>();
	static {
		axFields.put("FirstName", "http://axschema.org/namePerson/first");
		axFields.put("LastName", "http://axschema.org/namePerson/last");
		axFields.put("Country", "http://axschema.org/contact/country/home");
		axFields.put("Language", "http://axschema.org/pref/language");
		axFields.put("Email", "http://axschema.org/contact/email");
	}

	public static final Map<String, String> myopenidAxFields = new HashMap<String, String>();
	static {
		myopenidAxFields.put("FullName", "http://schema.openid.net/namePerson");
		myopenidAxFields
				.put("Country", "http://schema.openid.net/country/home");
		myopenidAxFields.put("Language",
			"http://schema.openid.net/pref/language");
		myopenidAxFields.put("Email", "http://schema.openid.net/contact/email");
	}

	public static final Set<String> sRegFields = new HashSet<String>();
	static {
		sRegFields.add("email");
		sRegFields.add("fullname");
		sRegFields.add("country");
		sRegFields.add("language");
	}


	/**
	 * Perform discovery on the User-Supplied identifier and return the
	 * DiscoveryInformation object that results from Association with the
	 * OP. This will probably be needed by the caller (stored in Session
	 * perhaps?).
	 * 
	 * I'm not thrilled about ConsumerManager being static, but it is
	 * very important to openid4java that the ConsumerManager object be the
	 * same instance all through a conversation (discovery, auth request, 
	 * auth response) with the OP. I didn't dig terribly deeply, but suspect
	 * that part of the key exchange or the nonce uses the ConsumerManager's
	 * hash, or some other instance-specific construct to do its thing.
	 * 
	 * @param userSuppliedIdentifier The User-Supplied identifier. It may already
	 *  be normalized.
	 *
	 *  @return DiscoveryInformation - The resulting DisoveryInformation object
	 *  returned by openid4java following successful association with the OP.
	 */
	public static DiscoveryInformation performDiscoveryOnUserSuppliedIdentifier(
			String userSuppliedIdentifier)
	{
		DiscoveryInformation ret = null;
		//
		ConsumerManager consumerManager = getConsumerManager();
		try {
			// Perform discover on the User-Supplied Identifier
			@SuppressWarnings("unchecked")
			List<DiscoveryInformation> discoveries = consumerManager
					.discover(userSuppliedIdentifier);
			// Pass the discoveries to the associate() method...
			ret = consumerManager.associate(discoveries);

		}
		catch (DiscoveryException e) {
			String message = "Error occurred during discovery!";
			log.error(message, e);
			throw new RuntimeException(message, e);
		}
		return ret;
	}


	/**
	 * Create an OpenID Auth Request, using the DiscoveryInformation object
	 * return by the openid4java library.
	 * 
	 * This method also uses the Exchange Attribute Extension to grant
	 * the Relying Party (RP).
	 * 
	 * @param discoveryInformation The DiscoveryInformation that should have
	 *  been previously obtained from a call to 
	 *  performDiscoveryOnUserSuppliedIdentifier().
	 *  
	 *  @param returnToUrl The URL to which the OP will redirect once the
	 *   authentication call is complete.
	 *  
	 * @return AuthRequest - A "good-to-go" AuthRequest object packed with all
	 *  kinds of great OpenID goodies for the OpenID Provider (OP). The caller
	 *  must take this object and forward it on to the OP. Or call
	 *  processAuthRequest() - part of this Service Class.
	 */
	public static AuthRequest createOpenIdAuthRequest(
			DiscoveryInformation discoveryInformation, String returnToUrl)
	{
		AuthRequest ret = null;
		//
		try {
			// Create the AuthRequest object
			ret = getConsumerManager().authenticate(discoveryInformation,
				returnToUrl);
			ret.addExtension(createSRegRequest());
			boolean myOpenID = discoveryInformation.getOPEndpoint().getHost()
					.equals("www.myopenid.com");
			FetchRequest axRequest = myOpenID ? createAttributeExchangeRequestMyOpenID()
					: createAttributeExchangeRequest();
			ret.addExtension(axRequest);

		}
		catch (Exception e) {
			String message = "Exception occurred while building AuthRequest object!";
			log.error(message, e);
			throw new RuntimeException(message, e);
		}
		return ret;
	}


	public static FetchRequest createAttributeExchangeRequest()
	{
		FetchRequest fetch = FetchRequest.createFetchRequest();
		try {
			for (Map.Entry<String, String> e : axFields.entrySet()) {
				fetch.addAttribute(e.getKey(), e.getValue(), true);
			}
		}
		catch (MessageException e) {
			log.warn("Erorr when adding attributes", e);
		}
		return fetch;
	}


	public static FetchRequest createAttributeExchangeRequestMyOpenID()
	{
		FetchRequest fetch = FetchRequest.createFetchRequest();
		try {
			for (Map.Entry<String, String> e : myopenidAxFields.entrySet()) {
				fetch.addAttribute(e.getKey(), e.getValue(), false);
			}
		}
		catch (MessageException e) {
			log.warn("Erorr when adding attributes", e);
		}
		return fetch;
	}


	public static SRegRequest createSRegRequest()
	{
		SRegRequest fetch = SRegRequest.createFetchRequest();
		for (String e : sRegFields) {
			fetch.addAttribute(e, false);
		}
		return fetch;
	}


	/**
	 * Processes the returned information from an authentication request
	 * from the OP.
	 * 
	 * @param discoveryInformation DiscoveryInformation that was created earlier
	 *  in the conversation (by openid4java). This will need to be verified with
	 *  openid4java to make sure everything went smoothly and there are no
	 *  possible problems. This object was probably stored in session and retrieved
	 *  for use in calling this method.
	 *  
	 * @param pageParameters PageParameters passed to the page handling the
	 *  return verification.
	 *  
	 * @param returnToUrl The "return to" URL that was passed to the OP. It must
	 *  match exactly, or openid4java will issue a verification failed message
	 *  in the logs.
	 *  
	 * @return RegistrationModel - null if there was a problem, or a RegistrationModel
	 *  object, with parameters filled in as completely as possible from the
	 *  information available from the OP. If you are using MyOpenID, most of the
	 *  time what is returned is from your "Default" profile, so if you need more 
	 *  information returned, make sure your Default profile is completely filled
	 *  out.
	 */
	public static RegistrationModel processReturn(
			DiscoveryInformation discoveryInformation,
			PageParameters pageParameters, String returnToUrl)
	{
		RegistrationModel ret = null;
		try {
			// Verify the Information returned from the OP
			/// This is required according to the spec
			ParameterList response = new ParameterList(
					convertToMap(pageParameters));
			VerificationResult verificationResult = getConsumerManager()
					.verify(returnToUrl, response, discoveryInformation);
			Identifier verifiedIdentifier = verificationResult.getVerifiedId();
			if (verifiedIdentifier != null) {
				AuthSuccess authSuccess = (AuthSuccess) verificationResult
						.getAuthResponse();
				if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
					MessageExtension extension = authSuccess
							.getExtension(AxMessage.OPENID_NS_AX);
					if (extension instanceof FetchResponse) {
						if (ret == null) {
							ret = new RegistrationModel();
						}
						provisionRegistrationModel(verifiedIdentifier,
							(FetchResponse) extension, ret);
					}
				}
				else {
					log.warn("Authentication response does not have a axMessage extension");
				}
				if (authSuccess.hasExtension(SRegMessage.OPENID_NS_SREG)) {
					MessageExtension extension = authSuccess
							.getExtension(SRegMessage.OPENID_NS_SREG);
					if (extension instanceof SRegResponse) {
						if (ret == null) {
							ret = new RegistrationModel();
						}
						provisionRegistrationModel(verifiedIdentifier,
							(SRegResponse) extension, ret);
					}
				}
				else {
					log.warn("Authentication response does not have a SRegMessage extension");
				}
			}
			else {
				log.warn("Verified identifier is null");
			}
		}
		catch (Exception e) {
			String message = "Exception occurred while verifying response!";
			log.error(message, e);
			throw new RuntimeException(message, e);
		}
		return ret;
	}


	private static void provisionRegistrationModel(
			Identifier verifiedIdentifier, FetchResponse axResponse,
			RegistrationModel ret)
	{
		ret.setOpenId(verifiedIdentifier.getIdentifier());
		String value;
		value = axResponse.getAttributeValue("Email");
		if (value != null && ret.getEmailAddress() == null) {
			ret.setEmailAddress(value);
		}
		value = axResponse.getAttributeValue("FirstName");
		if (value != null && ret.getFirstName() == null) {
			ret.setFirstName(value);
		}
		value = axResponse.getAttributeValue("LastName");
		if (value != null && ret.getLastName() == null) {
			ret.setLastName(value);
		}
		value = axResponse.getAttributeValue("FullName");
		if (value != null && ret.getFullName() == null) {
			ret.setFullName(value);
		}
		value = axResponse.getAttributeValue("Language");
		if (value != null && ret.getLanguage() == null) {
			ret.setLanguage(value);
		}
		value = axResponse.getAttributeValue("Country");
		if (value != null && ret.getCountry() == null) {
			ret.setCountry(value);
		}
		if (ret.getFullName() == null) {
			if (ret.getFirstName() != null && ret.getLastName() != null) {
				ret.setFullName(ret.getFirstName() + " " + ret.getLastName());
			}
			else {
				if (ret.getFirstName() != null) {
					ret.setFullName(ret.getFirstName());
				}
				else {
					ret.setFullName(ret.getLastName());
				}
			}
		}
	}


	private static void provisionRegistrationModel(
			Identifier verifiedIdentifier, SRegResponse res,
			RegistrationModel ret)
	{
		ret.setOpenId(verifiedIdentifier.getIdentifier());
		String value;
		value = res.getAttributeValue("email");
		if (value != null && ret.getEmailAddress() == null) {
			ret.setEmailAddress(value);
		}
		value = res.getAttributeValue("fullname");
		if (value != null && ret.getFullName() == null) {
			ret.setFullName(value);
		}
		value = res.getAttributeValue("language");
		if (value != null && ret.getLanguage() == null) {
			ret.setLanguage(value);
		}
		value = res.getAttributeValue("country");
		if (value != null && ret.getCountry() == null) {
			ret.setCountry(value);
		}
	}


	private static Map<String, String> convertToMap(
			PageParameters pageParameters)
	{
		Map<String, String> res = new HashMap<String, String>();
		for (NamedPair np : pageParameters.getAllNamed()) {
			res.put(np.getKey(), np.getValue());
		}
		return res;
	}

	private static ConsumerManager consumerManager;


	/**
	 * Retrieves an instance of the ConsumerManager object. It is static
	 * (see note in Class-level JavaDoc comments above) because openid4java
	 * likes it that way.
	 * 
	 * Note: if you are planning to debug the code, set the lifespan parameter
	 * of the InMemoryNonceVerifier high enough to outlive your debug cycle, or
	 * you may notice Nonce Verification errors. Depending on where you are
	 * debugging, this might pose an artificial problem for you (of your own
	 * making) that has nothing to do with either your code or openid4java.
	 * 
	 * @return ConsumerManager - The ConsumerManager object that handles
	 *  communication with the openid4java API.
	 */
	private static ConsumerManager getConsumerManager()
	{
		if (consumerManager == null) {
			consumerManager = new ConsumerManager();
			consumerManager
					.setAssociations(new InMemoryConsumerAssociationStore());
			consumerManager.setNonceVerifier(new InMemoryNonceVerifier(10000));
		}
		return consumerManager;
	}


	/**
	 * Generates the returnToUrl parameter that is passed to the OP. The
	 * User Agent (i.e., the browser) will be directed to this page following
	 * authentication.
	 * 
	 * @param representedPage The RegistrationPage object whose cover is to be
	 *  cracked open to get at the raw HttpServlet goodies inside.
	 *  
	 * @return String - the returnToUrl to be used for the authentication request.
	 */
	public static String getReturnToUrl()
	{
		return "http://localhost:8081/rosrs/openid/save?is_return=true";
	}
}
