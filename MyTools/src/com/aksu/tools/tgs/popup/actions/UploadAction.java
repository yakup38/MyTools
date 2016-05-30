package com.aksu.tools.tgs.popup.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

import com.aksu.tools.tgs.TGSPlugin;

public class UploadAction implements IObjectActionDelegate {

	// private Shell shell;
	private ISelection selection;
	// private IAction action;
	private TGSPlugin plugin;
	private IWorkbenchPartSite site;

	private static final String JAZZ_AUTHORITY = "jazz";
	private static final String SEPARATOR = System.getProperty("file.separator");
	private static final String JAZZ_DEFAULT_PATH = "/default/";

	private static final String GET_DOCUMENT_ID = "select * from DOCUMENT d where D.ID = (select DOCUMENT_ID from DOCUMENT_METADATA md where MD.MDNAME = ':tgsID' and MD.MDVALUE_STR = ?)";
	private static final CharSequence EMPTY_STRING = "";

	/**
	 * Constructor for Action1.
	 */
	public UploadAction() {
		super();
		plugin = TGSPlugin.getDefault();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		site = targetPart.getSite();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
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

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
					IFile ifile = null;
					for (Object elem : ss.toList()) {
						ifile = (IFile) Platform.getAdapterManager().getAdapter(elem, IFile.class);
						if (ifile == null) {
							if (elem instanceof IAdaptable) {
								ifile = (IFile) ((IAdaptable) elem).getAdapter(IFile.class);
							}
						}
							try {
								uploadToDev(ifile);
							} catch (FileNotFoundException e) {
								plugin.log("The file " + ifile.getName()  + " couldn't be found in Tgs Tools, continuing processing eventual other templates in the list... ", Status.WARNING, e);
								continue;
							} catch (IOException e) {
								plugin.log("An I/O error occured in Tgs Tools, stop processing eventual other templates in the list... ", Status.ERROR, e);
								break;	
							} catch (SQLException e) {
								plugin.log("An  SQL error occured in Tgs Tools, continue processing eventual other templates in the list... ", Status.ERROR, e);
								continue;
							}
					}

			}

			private void uploadToDev(IFile ifile) throws FileNotFoundException, IOException, SQLException {

				File file = ifile.getRawLocation().toFile();

				String tgsId = retrieveTgsId(file);
				if (tgsId == null || tgsId.isEmpty()) {
					plugin.log("Couldn't find the TGS ID for file " +  file.getName() +  ", exiting ... ", Status.WARNING, null);
					return;
				}

				Long documentId = retrieveDocumentId(tgsId);
				if (documentId == null) {
					plugin.log("Couldn't find the Document ID for tgsId : " +  tgsId +  ", exiting ... ", Status.WARNING, null);
					return;
				}

				storeContentItem(documentId, file);
				StringBuilder msg = new StringBuilder("Document ").append(file.getName()).append(" with tgsID = ").append(tgsId)
						.append(" has been uploaded to DEV");
				plugin.log(msg.toString(), Status.INFO, null);
				setMessage(msg.toString());
			}

		});

	}

	private void setMessage(String msg) {
		((IViewSite) site).getActionBars().getStatusLineManager().setMessage(msg);
	}

	private String retrieveTgsId(File file) throws FileNotFoundException, IOException {
		BufferedReader br = null;
		String tgsIdCandidate = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String line;
			int count = 0;
			Pattern pattern = Pattern.compile(".*tgsID.*=(.*)-->.*");
			while ((line = br.readLine()) != null && count < 20) {
				// process the line.
				Matcher matcher = pattern.matcher(line);
				boolean result = matcher.matches();
				if (result) {
					tgsIdCandidate = matcher.group(1);
					if (tgsIdCandidate != null) {
						tgsIdCandidate = tgsIdCandidate.trim();
					}
				}
			}
		} catch (FileNotFoundException e) {
			plugin.log("Couldn't find TGS template file ", Status.ERROR, e);
			throw e;
		} catch (IOException e) {
			plugin.log("An I/O error occured while attempting to retrieve tgsId in Tgs Tools ", Status.ERROR, e);
			throw e;
		}finally {
			try {
				if(br != null) {
					br.close();
				}
			} catch (IOException e) {
				plugin.log("An I/O error occured while attempting to close the BufferedReader in Tgs Tools ", Status.ERROR, e);
			}
		}
		return tgsIdCandidate;
	}

	// private String getElePath(IFile ifile) {
	// String elePath = null;
	//
	// MessageDialog.openInformation(Display.getDefault().getActiveShell(),
	// "111",
	// "11111111" );
	//
	// URI locationURI = ifile.getLocationURI();
	//
	// MessageDialog.openInformation(Display.getDefault().getActiveShell(),
	// "222",
	// "2222222" );
	//
	// MessageDialog.openInformation(Display.getDefault().getActiveShell(),
	// "URI location",
	// "locationURI = " + locationURI.toString() );
	//
	// if (JAZZ_AUTHORITY.equals(locationURI.getAuthority())) {
	// if (locationURI.getQuery() != null) {
	// elePath = new File(locationURI.getQuery()).toString() + SEPARATOR;
	// } else {
	//
	// elePath = new File(ifile.getLocationURI()).toString() + SEPARATOR;
	//
	// }
	// }
	//
	// MessageDialog.openInformation(Display.getDefault().getActiveShell(),
	// "Element path",
	// "Before substitution - elePath = " + elePath);
	//
	// MessageDialog.openInformation(Display.getDefault().getActiveShell(),
	// "Element path",
	// "After substitution - elePath = " + elePath.replace("file:" + SEPARATOR, EMPTY_STRING).replace("file:/", EMPTY_STRING));
	//
	//
	// return elePath.replace("file:" + SEPARATOR, EMPTY_STRING).replace("file:/", EMPTY_STRING);
	// return locationURI.toString();
	// }

	private Long retrieveDocumentId(String tgsId) throws SQLException {
		Long documentId = null;
		Connection conn = plugin.getConnection();
		if (conn != null) {
			try {
				java.sql.PreparedStatement stmt = conn.prepareStatement(GET_DOCUMENT_ID);
				stmt.setString(1, tgsId);
				java.sql.ResultSet results1 = stmt.executeQuery();
				if (results1 != null) {
					documentId = printAndReturnDocId(results1);

				}
			} catch (java.sql.SQLException sqle) {
				plugin.log("An error occured while attempting to retrieve DocumentID in Tgs Tools ", Status.ERROR, sqle);
				throw sqle;
			}
		}
		return documentId;
	}

//	public static void traverse(Node node) {
//		NodeList list = node.getChildNodes();
//		for (int i = 0; i < list.getLength(); i++) {
//			Node currentNode = list.item(i);
//			// System.out.println("This -> " + currentNode.getTextContent());
//			traverse(currentNode);
//
//		}
//
//		if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
//			System.out.println("This -> " + node.getTextContent());
//		}
//
//	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
		// this.action = action;
	}

	private Long printAndReturnDocId(ResultSet resultSet) throws SQLException {
		Long documentId = null;
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		while (resultSet.next()) {
			for (int i = 1; i <= columnsNumber; i++) {
				if (i > 1) System.out.print(",  ");
				if (rsmd.getColumnName(i).equalsIgnoreCase("ID")) {
					documentId = resultSet.getLong(1);
				} else if (!rsmd.getColumnName(i).equalsIgnoreCase("BYTES")) {

					String columnValue = resultSet.getString(i);
					System.out.print(columnValue + " " + rsmd.getColumnName(i));
				} else {

					String columnValue = "BLOB_BINARY_VALUE";
					System.out.print(columnValue + " " + rsmd.getColumnName(i));
				}
			}
			System.out.println("");
		}
		return documentId;
	}

	public void storeContentItem(Long contentId, File file) throws SQLException, IOException {
		// Assume the table row containing other content metadata already exists
		// and we just do an update
		PreparedStatement updateAttachment = null;
		PreparedStatement updateDocument = null;
		ResultSet rs = null;
		Connection con = plugin.getConnection();
		String updateAttachmentSQL = "UPDATE EDOMEC_ATTACHMENT  SET BYTES = ?, LENGTH = ?, MODIFIED_ON = ? WHERE  DOCUMENT_ID  = ?";
		String updateDocumentSQL = "UPDATE DOCUMENT  SET MODIFIED_ON = ? WHERE  ID  = ?";
		try {
			// Make sure the content bytes column will have a BLOB value
			con.setAutoCommit(false);
			updateAttachment = con.prepareStatement(updateAttachmentSQL);
			updateDocument = con.prepareStatement(updateDocumentSQL);
			int length = (int) file.length();
			updateAttachment.setBinaryStream(1, new FileInputStream(file), length);
			updateAttachment.setLong(2, length);
			updateAttachment.setTimestamp(3, new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
			updateAttachment.setLong(4, contentId);
			int res = updateAttachment.executeUpdate();
			updateDocument.setTimestamp(1, new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
			updateDocument.setLong(2, contentId);
			res = updateDocument.executeUpdate();
			con.commit();
		} catch (SQLException e) {
			plugin.log("A SQL error occured while attempting to store the file " + file.getName()  + " in Tgs Tools ", Status.ERROR, e);
			if (con != null) {
				try {
					plugin.log("The update template transaction will be rolled back in Tgs Tools ", Status.INFO, e);
					con.rollback();
					plugin.log("Transaction is rolled back in Tgs Tools ", Status.INFO, e);
				} catch (SQLException excep) {
					plugin.log("An error occured while attempting to roll back the transaction in Tgs Tools ", Status.ERROR, e);
					e.setNextException(excep);
				} 
			}
			throw e;
		} catch (IOException e) {
			plugin.log("A I/O error occured while attempting to store the file " + file.getName()  + " in Tgs Tools ", Status.ERROR, e);
			throw e;
		} finally {
			closeResources(updateAttachment, rs);
		}
	}

	private void closeResources(PreparedStatement ps, ResultSet rs) {
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// public static String getXPathValue(IFile file,String xPath) throws
	// Exception {
	// DocumentBuilderFactory docFactory=DocumentBuilderFactory.newInstance();
	// docFactory.setNamespaceAware(false);
	// docFactory.setValidating(false);
	// DocumentBuilder builder=docFactory.newDocumentBuilder();
	// XPathFactory factory=XPathFactory.newInstance();
	// XPath xpath=factory.newXPath();
	// final XPathExpression exp=xpath.compile(xPath);
	// Document doc=builder.parse(new InputSource(file.getContents()));
	// final NodeList
	// nodeList=(NodeList)exp.evaluate(doc,XPathConstants.NODESET);
	// return "XMLUtils.getNodeValue(nodeList)";
	// }

}
