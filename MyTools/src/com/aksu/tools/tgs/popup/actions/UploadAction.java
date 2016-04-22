package com.aksu.tools.tgs.popup.actions;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.datatools.connectivity.ConnectionProfileException;
import org.eclipse.datatools.connectivity.IConnection;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.IManagedConnection;
import org.eclipse.datatools.connectivity.ProfileManager;
import org.eclipse.datatools.connectivity.drivers.DriverInstance;
import org.eclipse.datatools.connectivity.drivers.DriverManager;
import org.eclipse.datatools.connectivity.drivers.IDriverMgmtConstants;
import org.eclipse.datatools.connectivity.drivers.IPropertySet;
import org.eclipse.datatools.connectivity.drivers.PropertySetImpl;
import org.eclipse.datatools.connectivity.drivers.jdbc.IJDBCConnectionProfileConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class UploadAction implements IObjectActionDelegate {

	private Shell shell;

	private static String providerID = "org.eclipse.datatools.connectivity.db.derby.embedded.connectionProfile"; //$NON-NLS-1$
	private static String vendor = "Oracle"; //$NON-NLS-1$
	private static String version = "11"; //$NON-NLS-1$

//	private static String jarList = "C:\\Derby10.1.3.1\\db-derby-10.1.3.1-bin\\lib\\derby.jar"; //$NON-NLS-1$
	private static String jarList = "C:\\Pgm\\R3-DEV\\maven3_repository\\com\\oracle\\weblogic\\ojdbc6\\12.1.2-0-0\\ojdbc6-12.1.2-0-0.jar"; //$NON-NLS-1$
//	private static String dbPath = "c:\\DerbyDatabases\\MyDB"; //$NON-NLS-1$
	private static String userName = "tgs_rw"; //$NON-NLS-1$
	private static String password = "uWXET_7E5"; //$NON-NLS-1$

	private static String driverClass = "oracle.jdbc.OracleDriver"; //$NON-NLS-1$
	private static String driverURL = "jdbc:oracle:thin:@ldap://oid.cc.cec.eu.int:389/fp6rtdy,cn=OracleContext,dc=cc,dc=cec,dc=eu,dc=int"; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Constructor for Action1.
	 */
	public UploadAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		MessageDialog.openInformation(shell, "MyTools", "MyTools was executed.");

		
//		listProfiles();
//		listDriverDefs();

		try {
//			createTransientDerbyProfile();
			registerConnectionProfile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void registerConnectionProfile() {
		IPropertySet ips = new PropertySetImpl("Our Driver Name", "Our Driver ID");
		Properties baseProperties = generateTransientDerbyProperties();
		ips.setBaseProperties(baseProperties);
		
		DriverInstance di = new DriverInstance( ips );
		DriverManager.getInstance().addDriverInstance(di);
		
		ProfileManager pm = ProfileManager.getInstance();
		/* Now that we have the driver definition above, create a profile that references it. */
		IConnectionProfile icp = pm.getProfileByName("TGS_DEV");
		if(icp != null){
			try {
				pm.deleteProfile(icp);
			} catch (ConnectionProfileException e) {
				e.printStackTrace();
			}
		}
		
		baseProperties.setProperty("org.eclipse.datatools.connectivity.driverDefinitionID", "Our Driver ID");
//		String providerID = "org.eclipse.datatools.connectivity.db.derby.embedded.connectionProfile";
		try {
			pm.createProfile("Our Connection Profile", "Our Profile Description", providerID, baseProperties);
		} catch (ConnectionProfileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	private void listProfiles() {
		IConnectionProfile[] plist = ProfileManager.getInstance().getProfiles();
		for (int i = 0; i < plist.length; i++) {
			System.out.println("Profile: " + plist[i].getName());
			System.out.println("Profileider ID: " + plist[i].getProviderId());
			System.out.println("Profileider Name: " + plist[i].getProviderName());
			plist[i].getBaseProperties().list(System.out);
		}
	}

	private void listDriverDefs() {
		DriverInstance[] list = DriverManager.getInstance().getAllDriverInstances();
		for (int i = 0; i < list.length; i++) {
			System.out.println("=============");
			System.out.println("Driver" + list[i].getId());
			System.out.println("DriverList: " + list[i].getJarList());
			System.out.println("Driver: " + list[i].getName());
			list[i].getPropertySet().getBaseProperties().list(System.out);
		}
	}

	
	
	  public static Properties generateTransientDerbyProperties() {
	       Properties baseProperties = new Properties();
	       baseProperties.setProperty( IDriverMgmtConstants.PROP_DEFN_JARLIST, jarList );
	       baseProperties.setProperty(IJDBCConnectionProfileConstants.DRIVER_CLASS_PROP_ID, driverClass);
	       baseProperties.setProperty(IJDBCConnectionProfileConstants.URL_PROP_ID, driverURL);
	       baseProperties.setProperty(IJDBCConnectionProfileConstants.USERNAME_PROP_ID, userName);
	       baseProperties.setProperty(IJDBCConnectionProfileConstants.PASSWORD_PROP_ID, password);
	       baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_VENDOR_PROP_ID, vendor);
	       baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_VERSION_PROP_ID, version);
	       baseProperties.setProperty(IJDBCConnectionProfileConstants.SAVE_PASSWORD_PROP_ID, String.valueOf( true ) );
	       return baseProperties;
	   }

	   public void createTransientDerbyProfile() throws Exception {
	       ProfileManager pm = ProfileManager.getInstance();
	      
	       IConnectionProfile transientDerby = pm.createTransientProfile(null, generateTransientDerbyProperties());
	       // do something with the profile
//			IConnectionProfile dev = ProfileManager.getInstance().getProfileByName("DEV");

			IConnection c = transientDerby.createConnection("java.sql.Connection");
			IStatus status = transientDerby.connect();
//	       IStatus status1 = dev.connect();
//	       if (status1.isOK()) {
//	           // success
//	    	   java.sql.Connection conn1 = (java.sql.Connection) (dev.getManagedConnection("java.sql.Connection").getConnection().getRawConnection());
//	           if (conn1 != null) {
//	               try {
//	                   java.sql.Statement stmt1 = conn1.createStatement();
//	                   java.sql.ResultSet results1 = stmt1.executeQuery("select * from TGS.DOCUMENT");
//	                   if(results1 != null) {
//	                	   print(results1);
//	                   }
//	               } catch (java.sql.SQLException sqle) {
//	                   sqle.printStackTrace();
//	               }
//
//	           }
//	          
//	       } else {
//	           // failure :(
//	           if (status.getException() != null) {
//	               status.getException().printStackTrace();
//	           }
//	       }
	       
	       if (status.isOK()) {
	    	   // success
	    	   java.sql.Connection conn = getJavaConnectionForProfile(transientDerby);
	    	   if (conn != null) {
	    		   try {
	    			   java.sql.Statement stmt = conn.createStatement();
	    			   java.sql.ResultSet results = stmt.executeQuery("select * from TGS.DOCUMENT");
	    			   if(results != null) {
	    				   print(results);
	    			   }
	    		   } catch (java.sql.SQLException sqle) {
	    			   sqle.printStackTrace();
	    		   }
	    		   
	    	   }
	    	   
	       } else {
	    	   // failure :(
	    	   if (status.getException() != null) {
	    		   status.getException().printStackTrace();
	    	   }
	       }
	       
	       
	   }	
	   
	   private void print(ResultSet resultSet) throws SQLException {
		   ResultSetMetaData rsmd = resultSet.getMetaData();
		   int columnsNumber = rsmd.getColumnCount();
		   while (resultSet.next()) {
		       for (int i = 1; i <= columnsNumber; i++) {
		           if (i > 1) System.out.print(",  ");
		           if(!rsmd.getColumnName(i).equalsIgnoreCase("BYTES")) {
		        	   
		           String columnValue = resultSet.getString(i);
		           System.out.print(columnValue + " " + rsmd.getColumnName(i));
		           } else {
		        	   
		        	   String columnValue = "BLOB_BINARY_VALUE";
		        	   System.out.print(columnValue + " " + rsmd.getColumnName(i));
		           }
		       }
		       System.out.println("");
		   }		
	}

	public java.sql.Connection getJavaConnectionForProfile (IConnectionProfile profile) {
		     IManagedConnection managedConnection = ((IConnectionProfile)profile).getManagedConnection("java.sql.Connection");
		     if (managedConnection != null) {
		        return (java.sql.Connection) managedConnection.getConnection().getRawConnection();
		     }
		     return null;
		  }
	   
	
	
}
