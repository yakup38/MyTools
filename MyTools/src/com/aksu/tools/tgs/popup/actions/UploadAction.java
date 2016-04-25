package com.aksu.tools.tgs.popup.actions;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.datatools.connectivity.ConnectionProfileException;
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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UploadAction implements IObjectActionDelegate {

	private Shell shell;
	private ISelection selection;
	private IAction action;
	
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

		  // Ignore non structured selections
	    if (!(this.selection instanceof IStructuredSelection)) {
            System.err.printf("Unhandled DFS Action: " + selection.toString());
	      return;
	    }
	    

	    // operate on the DFS asynchronously to prevent blocking the main UI
	    final IStructuredSelection ss = (IStructuredSelection) selection;
	    final String actionDefinitionId = action.getActionDefinitionId();
	    final String actionId = action.getId();
	    final String actionText = action.getText();
	    
	    System.out.println("actionDefinitionId" + actionDefinitionId);
	    System.out.println("actionId" + actionId);
	    System.out.println("actionText" + actionText);
	    Display.getDefault().asyncExec(new Runnable() {
	      public void run() {
	        try {
	          switch (actionId) {
	            case "com.aksu.tools.tgs.uploadToDEVAction": {
	              uploadToDev();
	              break;
	            }
	            case "com.aksu.tools.tgs.uploadToTESTAction": {
	            	uploadToTest();
	            	break;
	            }
	            default: {
	              System.out.printf("Unhandled DFS Action: " + actionId);
	              break;
	            }
	          }

	        } catch (Exception e) {
	          e.printStackTrace();
	          MessageDialog.openError(Display.getDefault().getActiveShell(),
	              "DFS Action error",
	              "An error occurred while performing DFS operation: "
	                  + e.getMessage());
	        }
	      }

		private void uploadToDev() {
			System.out.println("Upload to DEV invoked ");
		}

		private void uploadToTest() {
			System.out.println("Upload to TEST invoked ");
		}

	    });
	 		
		

		
		Object obj = ss.getFirstElement();
        IFile file = (IFile) Platform.getAdapterManager().getAdapter(obj, IFile.class);
        if (file == null) {
            if (obj instanceof IAdaptable) {
                file = (IFile) ((IAdaptable) obj).getAdapter(IFile.class);
            }
        }
		
        try {
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(new File(file.getLocationURI()));
				System.out.println();
				traverse(document.getDocumentElement());
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	    }

	
	    public static void traverse(Node node) {
	        NodeList list = node.getChildNodes();
	        for (int i = 0; i < list.getLength(); i++) {
	            Node currentNode = list.item(i);
//	            System.out.println("This -> " + currentNode.getTextContent());
	            traverse(currentNode);

	        }

	        if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
	            System.out.println("This -> " + node.getTextContent());
	        }

	    }
	    
	    
	    
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
		this.action = action;
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
	   
	
	
	
//	public static String getXPathValue(IFile file,String xPath) throws Exception {
//		  DocumentBuilderFactory docFactory=DocumentBuilderFactory.newInstance();
//		  docFactory.setNamespaceAware(false);
//		  docFactory.setValidating(false);
//		  DocumentBuilder builder=docFactory.newDocumentBuilder();
//		  XPathFactory factory=XPathFactory.newInstance();
//		  XPath xpath=factory.newXPath();
//		  final XPathExpression exp=xpath.compile(xPath);
//		  Document doc=builder.parse(new InputSource(file.getContents()));
//		  final NodeList nodeList=(NodeList)exp.evaluate(doc,XPathConstants.NODESET);
//		  return "XMLUtils.getNodeValue(nodeList)";
//		}
	
}
