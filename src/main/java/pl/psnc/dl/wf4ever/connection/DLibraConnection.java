package pl.psnc.dl.wf4ever.connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Properties;

import org.apache.log4j.Logger;

import pl.psnc.dlibra.mgmt.UserServiceResolver;
import pl.psnc.dlibra.service.AccessDeniedException;
import pl.psnc.dlibra.service.AuthorizationToken;
import pl.psnc.dlibra.service.DLibraException;
import pl.psnc.dlibra.service.ServiceUrl;
import pl.psnc.dlibra.system.UserInterface;

/**
 * 
 * @author nowakm
 *
 */
public class DLibraConnection
{

	private final static Logger logger = Logger
			.getLogger(DLibraConnection.class);

	private int port;

	private String host;

	private long workspacesDirectory;


	public DLibraConnection(String configFileName)
	{
		logger.info("Loading connection properties file " + configFileName);
		InputStream inputStream = DLibraConnection.class.getClassLoader()
				.getResourceAsStream(configFileName);
		if (inputStream == null) {
			logger.error("Connection properties file not found! ");
			throw new RuntimeException("Connection properties file not found! ");
		}
		Properties properties = new Properties();
		try {
			properties.load(inputStream);
			inputStream.close();
		}
		catch (IOException e) {
			logger.error("Unable to read connection properties", e);
			throw new RuntimeException("Unable to read connection properties",
					e);
		}
		this.host = properties.getProperty("host");
		this.port = Integer.parseInt(properties.getProperty("port"));
		logger.debug("Connection parameters: " + this.host + ":" + this.port);

		this.workspacesDirectory = Long.parseLong(properties
				.getProperty("workspacesDir"));
		logger.debug("Workspaces directory: " + this.workspacesDirectory);
	}


	public DLibraDataSource getDLibraDataSource(String user, String password)
		throws MalformedURLException, RemoteException, AccessDeniedException,
		UnknownHostException, DLibraException
	{
		AuthorizationToken authorizationToken = new AuthorizationToken(user,
				password);

		UserServiceResolver userServiceResolver = new UserServiceResolver(
				new ServiceUrl(InetAddress.getByName(host),
						UserInterface.SERVICE_TYPE, port), authorizationToken);

		return new DLibraDataSource(userServiceResolver, user, workspacesDirectory);
	}
}
