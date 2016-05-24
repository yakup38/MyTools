package com.aksu.tools.tgs.popup.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.aksu.tools.tgs.Activator;

import oracle.jdbc.internal.OracleResultSet;
import oracle.sql.BLOB;

public class UploadAction implements IObjectActionDelegate {

	private Shell shell;
	private ISelection selection;
	private IAction action;
	private Activator activator;

	private static final String GET_DOCUMENT_ID = "select * from DOCUMENT d where D.ID = (select DOCUMENT_ID from DOCUMENT_METADATA md where MD.MDNAME = ':tgsID' and MD.MDVALUE_STR = ?)";

	/**
	 * Constructor for Action1.
	 */
	public UploadAction() {
		super();
		activator = Activator.getDefault();
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
					MessageDialog.openError(Display.getDefault().getActiveShell(), "DFS Action error",
							"An error occurred while performing DFS operation: " + e.getMessage());
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
		IFile ifile = (IFile) Platform.getAdapterManager().getAdapter(obj, IFile.class);
		if (ifile == null) {
			if (obj instanceof IAdaptable) {
				ifile = (IFile) ((IAdaptable) obj).getAdapter(IFile.class);
			}
		}

		File file = new File(ifile.getLocationURI());
		System.out.println("=================== file path : " + file.getAbsolutePath() + " ========================");
		String tgsId = retrieveTgsId(file);
		if (tgsId == null || tgsId.isEmpty()) {
			System.out.println("========== Couldn't find the TGS ID, exiting ... =========");
			return;
		}
		System.out.println("========== tgsId : " + tgsId + " =========");
		Long documentId = retrieveDocumentId(tgsId);
		if (documentId == null) {
			System.out.println("========== Couldn't find the Document ID, exiting ... =========");
			return;
		}
		System.out.println("========== documentId : " + documentId + " =========");

		try {
			storeContentItem(documentId, new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// try {
		// DocumentBuilderFactory factory =
		// DocumentBuilderFactory.newInstance();
		// DocumentBuilder builder = factory.newDocumentBuilder();
		// Document document = builder.parse(file);
		// System.out.println();
		// traverse(document.getDocumentElement());
		// System.out.println(document.getNodeValue());
		// } catch (ParserConfigurationException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (SAXException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	private String retrieveTgsId(File file) {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			int count = 0;
			Pattern pattern = Pattern.compile(".*tgsID.*=(.*)-->.*");
			String tgsIdCandidate = null;
			while ((line = br.readLine()) != null && count < 20) {
				// process the line.
				Matcher matcher = pattern.matcher(line);
				boolean result = matcher.matches();
				if (result) {
					tgsIdCandidate = matcher.group(1);
					if (tgsIdCandidate != null) {
						tgsIdCandidate = tgsIdCandidate.trim();
						System.out.println("============================ tgsIdCandidate : " + tgsIdCandidate
								+ " ====================");
						return tgsIdCandidate;
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("============================ tgsId not found  ====================");
		return null;
	}

	private Long retrieveDocumentId(String tgsId) {
		Long documentId = null;
		Connection conn = activator.getConnection();
		if (conn != null) {
			try {
				java.sql.PreparedStatement stmt = conn.prepareStatement(GET_DOCUMENT_ID);
				stmt.setString(1, tgsId);
				java.sql.ResultSet results1 = stmt.executeQuery();
				if (results1 != null) {
					documentId = printAndReturnDocId(results1);

				}
			} catch (java.sql.SQLException sqle) {
				sqle.printStackTrace();
			}
		}
		return documentId;
	}

	public static void traverse(Node node) {
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node currentNode = list.item(i);
			// System.out.println("This -> " + currentNode.getTextContent());
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

	private Long printAndReturnDocId(ResultSet resultSet) throws SQLException {
		Long documentId = null;
		ResultSetMetaData rsmd = resultSet.getMetaData();
		int columnsNumber = rsmd.getColumnCount();
		while (resultSet.next()) {
			for (int i = 1; i <= columnsNumber; i++) {
				if (i > 1)
					System.out.print(",  ");
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

	public void storeContentItem(Long contentId, InputStream contentStream) {
		// Assume the table row containing other content metadata already exists
		// and we just do an update
		PreparedStatement ps = null;
		OracleResultSet rs = null;
		Connection con = activator.getConnection();
		try {
			// Make sure the content bytes column will have a BLOB value
			String update = "UPDATE DOCUMENT  SET BYTES = EMPTY_BLOB()  WHERE  ID  = ?";
			ps = con.prepareStatement(update);
			ps.setLong(1, contentId);
			ps.execute();

			try {
				ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			String sql = "SELECT BYTES  FROM DOCUMENT  WHERE ID = ? FOR UPDATE";

			ps = con.prepareStatement(sql);
			ps.setLong(1, contentId);
			rs = (OracleResultSet) ps.executeQuery();
			if (rs.next()) {
				BLOB blob = (BLOB) rs.getBLOB(1);
				blob.truncate(0);
				OutputStream outputStream = blob.setBinaryStream(0L);
				byte[] buffer = new byte[blob.getBufferSize()];
				int byteread = 0;
				while ((byteread = contentStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, byteread);
				}
				outputStream.close();
				contentStream.close();
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeResources(ps, rs);
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
