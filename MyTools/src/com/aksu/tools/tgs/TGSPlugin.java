package com.aksu.tools.tgs;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.datatools.connectivity.ConnectionProfileException;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.IManagedConnection;
import org.eclipse.datatools.connectivity.ProfileManager;
import org.eclipse.datatools.connectivity.drivers.IDriverMgmtConstants;
import org.eclipse.datatools.connectivity.drivers.jdbc.IJDBCConnectionProfileConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.Preferences;

import com.aksu.tools.tgs.preferences.PreferenceConstants;

/**
 * The TGSPlugin class controls the plug-in life cycle
 */
public class TGSPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.aksu.tools.tgs"; //$NON-NLS-1$
	public static final String PROFILE_NAME = "TGS Dev";
	public static final String DEV_HOSTNAME = "yakup-PC";
	private String hostname = "LOCALHOST";

	// DEV Host Properties
	// private static String DEV_QUERY = "select * from hr.employees";

	// Default Properties
	// private static String DEFAULT_QUERY = "select * from TGS.DOCUMENT";
	private static String providerID = "org.eclipse.datatools.enablement.oracle.connectionProfile"; //$NON-NLS-1$
	private static String vendor = "Oracle"; //$NON-NLS-1$
	private static String version = "11"; //$NON-NLS-1$

	// private static String jarList =
	// "C:\\Oracle\\Oracle_Home\\oracle_common\\rda\\da\\lib\\ojdbc14.jar";
	// //$NON-NLS-1$
	private static String jarList = "ojdbc6-12.1.2-0-0.jar"; //$NON-NLS-1$
	private static String dbName = "service_name"; //$NON-NLS-1$
	private static String userName = "encrypted_username"; //$NON-NLS-1$
	private static String password = "encrypted_passwordt"; //$NON-NLS-1$

	private static String driverClass = "oracle.jdbc.OracleDriver"; //$NON-NLS-1$
	private static String driverURL = "jdbc:oracle:thin:@ldap://oid.cc.cec.eu.int:389/fp6rtdy,cn=OracleContext,dc=cc,dc=cec,dc=eu,dc=int"; //$NON-NLS-1$ //$NON-NLS-2$

	// The shared instance
	private static TGSPlugin plugin;
	private IConnectionProfile profile;

	/**
	 * The constructor
	 */
	public TGSPlugin() {
		System.out.println("**************** TGSPlugin() CONSTRUCTOR ********************");
		try {
			hostname = java.net.InetAddress.getLocalHost().getHostName();
			System.out.println("*************** hostname : " + hostname + " **************************");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		System.out.println("**************** TGSPlugin start() ********************");
		System.out.println("**************** createDTPConnection() ********************");
		profile = createDTPProfile();
//		createPrefs();
		createConnection(profile);
	}

	private IConnectionProfile createDTPProfile() {
		System.out.println("**************** getting profile ********************");
		IConnectionProfile profile = ProfileManager.getInstance().getProfileByName(PROFILE_NAME);
		if (profile == null) {
			System.out.println("**************** profile is null .... ********************");
			System.out.println("**************** Creating profile .... ********************");
			profile = createProfile();
		}
		return profile;
	}

//	private void createPrefs() {
//
//		Preferences preferences = ConfigurationScope.INSTANCE.getNode("com.aksu.tools.tgs");
//		
//		Preferences username = preferences.node("username");
//		Preferences password = preferences.node("password");
//		username.put("username", "aksuaya");
//		password.put("password", "igVdf_KU4");
//
//		try {
//			// forces the application to save the preferences
//			preferences.flush();
//		} catch (BackingStoreException e2) {
//			e2.printStackTrace();
//		}
//	}

	public Connection getConnection() {
		IManagedConnection connection = profile.getManagedConnection("java.sql.Connection");
		if (connection != null) {
			return (java.sql.Connection) connection.getConnection().getRawConnection();
		}
		return null;
	}

	private void createConnection(IConnectionProfile profile) {

		// IConnection conn = profile.createConnection("java.sql.Connection");
		IStatus status = profile.connect();
		if (status.isOK()) {
			System.out.println("**************** DTP Profile status is OK ********************");

			// success
			// java.sql.Connection conn1 = (java.sql.Connection)
			// (conn.getRawConnection());
			// if (conn1 != null) {
			// try {
			// java.sql.Statement stmt1 = conn1.createStatement();
			// java.sql.ResultSet results1 = stmt1.executeQuery(getQuery());
			// if (results1 != null) {
			// print(results1);
			// }
			// } catch (java.sql.SQLException sqle) {
			// sqle.printStackTrace();
			// }
			//
			// }

		} else {
			// failure :(
			if (status.getException() != null) {
				status.getException().printStackTrace();
			}
		}

	}

	// private String getQuery() {
	// String res = DEFAULT_QUERY;
	// if (isDevHost()) {
	// res = DEV_QUERY;
	// }
	// return res;
	// }

	private boolean isDevHost() {
		return DEV_HOSTNAME.equalsIgnoreCase(hostname);
	}

	private IConnectionProfile createProfile() {
		IConnectionProfile res = null;
		Properties properties = null;
		if (isDevHost()) {
			properties = getDevMachineProperties();
		} else {
			properties = getDefaultProperties();
		}
		ProfileManager pm = ProfileManager.getInstance();
		try {
			res = pm.createProfile("TGS Dev", "TGS DTP Connection profile", providerID, properties);
		} catch (ConnectionProfileException e) {
			e.printStackTrace();
		}

		return res;
	}

	private Properties getDevMachineProperties() {
		Properties baseProperties = new Properties();
		baseProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_JARLIST, "C:\\oraclexe\\app\\oracle\\product\\11.2.0\\server\\jdbc\\lib\\ojdbc6.jar");
		baseProperties.setProperty(IJDBCConnectionProfileConstants.DRIVER_CLASS_PROP_ID, "oracle.jdbc.OracleDriver");
		baseProperties.setProperty(IJDBCConnectionProfileConstants.URL_PROP_ID, "jdbc:oracle:thin:@yakup-PC:1521:XE");
		baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_NAME_PROP_ID, "XE");
		baseProperties.setProperty(IJDBCConnectionProfileConstants.USERNAME_PROP_ID, "SYSTEM");
		baseProperties.setProperty(IJDBCConnectionProfileConstants.PASSWORD_PROP_ID, "admin");
		baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_VENDOR_PROP_ID, "Oracle");
		baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_VERSION_PROP_ID, "11");
		baseProperties.setProperty(IJDBCConnectionProfileConstants.SAVE_PASSWORD_PROP_ID, String.valueOf(true));
		baseProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_TYPE, "org.eclipse.datatools.enablement.oracle.11.driverTemplate");
		baseProperties.setProperty("org.eclipse.datatools.connectivity.driverDefinitionID",
				"DriverDefn.org.eclipse.datatools.enablement.oracle.11.driverTemplate.Oracle Thin Driver");

		return baseProperties;
	}

	private Properties getDefaultProperties() {
		Properties baseProperties = new Properties();
		baseProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_JARLIST, jarList);
		baseProperties.setProperty(IJDBCConnectionProfileConstants.DRIVER_CLASS_PROP_ID, driverClass);
		baseProperties.setProperty(IJDBCConnectionProfileConstants.URL_PROP_ID, driverURL);
		baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_NAME_PROP_ID, dbName);
		

		IPreferenceStore prefsStore =  TGSPlugin.getDefault().getPreferenceStore();
		
		System.out.println("************************* Prefs *****************");
		System.out.println("username : " + prefsStore.getString(PreferenceConstants.USERNAME_STRING));
		System.out.println("password : " + prefsStore.getString(PreferenceConstants.PASSWORD_STRING));
		
		baseProperties.setProperty(IJDBCConnectionProfileConstants.USERNAME_PROP_ID, prefsStore.getString(PreferenceConstants.USERNAME_STRING));
		baseProperties.setProperty(IJDBCConnectionProfileConstants.PASSWORD_PROP_ID, prefsStore.getString(PreferenceConstants.PASSWORD_STRING));
		baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_VENDOR_PROP_ID, vendor);
		baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_VERSION_PROP_ID, version);
		baseProperties.setProperty(IJDBCConnectionProfileConstants.SAVE_PASSWORD_PROP_ID, String.valueOf(true));
		baseProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_TYPE, "org.eclipse.datatools.enablement.oracle.11.driverTemplate");
		baseProperties.setProperty("org.eclipse.datatools.connectivity.driverDefinitionID",
				"DriverDefn.org.eclipse.datatools.enablement.oracle.11.driverTemplate.Oracle Thin Driver");
		
		return baseProperties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		System.out.println("**************** TGSPlugin stop() ********************");

	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static TGSPlugin getDefault() {
		System.out.println("**************** TGSPlugin getDefault() ********************");
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		System.out.println("**************** TGSPlugin getImageDecriptor() ********************");
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	// private void print(ResultSet resultSet) throws SQLException {
	// ResultSetMetaData rsmd = resultSet.getMetaData();
	// int columnsNumber = rsmd.getColumnCount();
	// while (resultSet.next()) {
	// for (int i = 1; i <= columnsNumber; i++) {
	// if (i > 1)
	// System.out.print(", ");
	// if (!rsmd.getColumnName(i).equalsIgnoreCase("BYTES")) {
	//
	// String columnValue = resultSet.getString(i);
	// System.out.print(columnValue + " " + rsmd.getColumnName(i));
	//
	// } else {
	//
	// String columnValue = "BLOB_BINARY_VALUE";
	// System.out.print(columnValue + " " + rsmd.getColumnName(i));
	// }
	// }
	// System.out.println("");
	// }
	// }

	public void log(String msg, Exception e) {
		log(msg, Status.INFO, e);
	}

	public void log(String msg, int status, Exception e) {
		getLog().log(new Status(status, PLUGIN_ID, Status.OK, msg, e));
	}

}
