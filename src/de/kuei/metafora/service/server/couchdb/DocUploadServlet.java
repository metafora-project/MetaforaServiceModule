package de.kuei.metafora.service.server.couchdb;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Document;
import com.fourspaces.couchdb.Session;
import com.fourspaces.couchdb.ViewResults;

import de.kuei.metafora.service.server.StartupServlet;
import de.kuei.metafora.service.server.xml.Classification;
import de.kuei.metafora.service.server.xml.CommonFormatCreator;
import de.kuei.metafora.service.server.xml.XMLException;

public class DocUploadServlet extends HttpServlet {
	public static String server = "metaforaserver.ku.de";
	public static String user = "admin";
	public static String password = "didPfCDB";

	private static final int port = 6984;
	
	private static boolean secureConnection = true;
	
	private static final String databaseName = "gwtfilebase";

	private byte[] docdata = null;
	private String contenttype = null;

	private static DocUploadServlet instance = null;

	public static DocUploadServlet getInstance() {
		if (instance == null) {
			instance = new DocUploadServlet();
		}
		return instance;
	}

	public DocUploadServlet() {
		DocUploadServlet.instance = this;
	}

	public Vector<String[]> getToolDocuments() {
		Session session = new Session(server, port, user, password, true, secureConnection);

		Database db = session.getDatabase(databaseName);

		ViewResults results = db.view("toolupload/toolUpload");

		List<Document> docs = results.getResults();

		Vector<String[]> documents = new Vector<String[]>();
		String[] document;

		for (int i = 0; i < docs.size(); i++) {
			Document d = docs.get(i);

			document = new String[2];

			document[0] = d.getId();
			document[1] = d.getString("value");
			documents.add(document);
		}
		return documents;

	}

	public void doGet(HttpServletRequest req, HttpServletResponse response) {

		if (req.getParameterMap().containsKey("id")) {
			String id = req.getParameter("id");

			Object[] doc = getDocFromDatabase(id);
			if (doc != null) {
				String datatype = (String) doc[0];
				byte[] docdata = (byte[]) doc[1];

				if (datatype != null && docdata != null) {
					response.setContentType(datatype);
					OutputStream out;
					try {
						out = response.getOutputStream();
						out.write(docdata);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					response.setContentType("text/plain");
					try {
						response.getWriter().write("invalid document");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} else {
				response.setContentType("text/plain");
				try {
					response.getWriter().write("file not found");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} else if (docdata != null && contenttype != null
				&& !req.getParameterMap().containsKey("list")) {
			response.setContentType(contenttype);
			try {
				PrintWriter writer = response.getWriter();
				for (int i = 0; i < docdata.length; i++)
					writer.write(docdata[i]);
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			response.setContentType("text/plain");
			try {
				Vector<String[]> documents = getToolDocuments();

				String resp = "";
				for (int i = 0; i < documents.size(); i++) {
					String[] document = documents.get(i);
					resp += document[0] + ":" + document[1] + "\n";
				}

				response.getWriter().write(resp);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) {
		response.setContentType("text/plain");

		String responseText = "";

		if (ServletFileUpload.isMultipartContent(request)) {

			boolean update = false;
			if (request.getParameterMap().containsKey("fileid"))
				update = true;

			String tool = "toolUpload";

			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);

			try {

				@SuppressWarnings("unchecked")
				List<FileItem> items = upload.parseRequest(request);

				for (FileItem item : items) {
					if (item.isFormField())
						continue;

					contenttype = item.getContentType();
					System.out.println(responseText += "Filename: "
							+ item.getName() + ", ");
					docdata = item.get();
					System.out.println(responseText += "Bytes: "
							+ item.getSize());

					byte[] encdata = Base64.encodeBase64(docdata, true);

					String docdataenc = null;
					try {
						docdataenc = new String(encdata, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					if (update) {
						updateDoc(item.getName(), item.getContentType(),
								item.getString(),
								request.getParameter("fileid"), true);
					} else {
						responseText += "\nFile-ID:\n"
								+ putDocToDatabase(item.getName(),
										item.getContentType(), docdataenc,
										tool, true);
					}

					break;
				}

			} catch (FileUploadException e1) {
				e1.printStackTrace();
			}
		} else {
			responseText = "there was no document!";
		}

		try {
			response.getWriter().write(responseText);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the filetype and data of the Document with the specified ID from the
	 * couchDB. Meant for downloading the files.
	 * 
	 * @param id
	 *            ID of the Document to be got
	 * @return An array of length 2 with the filetype in 0-coordinate and the
	 *         data or null, if there was a problem retrieving the Document from
	 *         the couchDB
	 */
	private Object[] getDocFromDatabase(String id) {
		Object[] doc = null;

		Session session = new Session(server, port, user, password, true, secureConnection);

		Database db = null;
		System.err
				.println("ServiceModul: DocUploadServlet: getDocFromDatabase(): DBname: "
						+ databaseName + ", Session: " + session);
		db = session.getDatabase(databaseName);
		try {
			Document dc = db.getDocument(id);
			if (dc != null) {
				@SuppressWarnings("rawtypes")
				Set keys = dc.keySet();
				if (keys.contains("filetype") && keys.contains("data")) {
					doc = new Object[2];
					doc[0] = dc.getString("filetype");

					String docdata = dc.getString("data");
					byte[] data = null;
					if (keys.contains("encoding")
							&& dc.getString("encoding").equals("base64")) {
						byte[] encData = docdata.getBytes();
						data = Base64.decodeBase64(encData);
					} else {
						if (keys.contains("encoding")) {
							System.err
									.println("Unknown document data encoding: "
											+ dc.getString("encoding"));
						}
						data = docdata.getBytes();
					}

					doc[1] = data;
				} else {
					System.err
							.println("DocUploadServlet: getDocFromDatabase: invalid document!");
				}
			} else {
				System.err
						.println("DocUploadServlet: getDocFromDatabase: document not found!");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return doc;
	}

	/**
	 * Creates and saves a new file with the specified name, type and data into
	 * the couchDB
	 * 
	 * @param docname
	 *            Name under which the file is to be saved
	 * @param doctype
	 *            Type of the file to be saved
	 * @param docdata
	 *            Conten of the file to be saved
	 */
	private String putDocToDatabase(String docname, String doctype,
			String docdata, String tool, boolean base) {
		String id = "unknown";

		Session session = new Session(server, port, user, password, true, secureConnection);

		Database db = null;
		db = session.getDatabase(databaseName);

		Document doc = new Document();
		doc.put("filename", docname);
		doc.put("filetype", doctype);
		doc.put("tool", tool);
		doc.put("data", docdata);
		if (base) {
			doc.put("encoding", "base64");
		}

		try {
			db.saveDocument(doc);

			id = doc.getId();

			try {
				CommonFormatCreator cfc = new CommonFormatCreator(
						System.currentTimeMillis(), Classification.other,
						"TOOL_DOCUMENT_UPLOADED", StartupServlet.logged, true);

				cfc.setCdataDescription("The file " + docname
						+ " was uploaded to the couchdb file store.");
				cfc.setObject(id, "FILE_UPLOAD_INFO");

				cfc.addProperty("ID", id);
				cfc.addProperty("FILE", docname);
				cfc.addProperty("FILE_TYPE", doctype);
				cfc.addProperty("TYPE", tool);
				if (base) {
					cfc.addProperty("encoding", "base64");
				}
				cfc.addContentProperty("SENDING_TOOL", "METAFORA");
				cfc.addContentProperty("RECEIVING_TOOL", tool);

				String xml = cfc.getDocument();

				if (StartupServlet.logger != null)
					StartupServlet.logger.sendMessage(xml);
			} catch (XMLException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return id;
	}

	/**
	 * Updates the Document in the couchDB with the specified ID by setting the
	 * values of filename, filetype and data
	 * 
	 * @param docname
	 *            Name under which the Document ist to be saved
	 * @param doctype
	 *            Type of the Document
	 * @param docdata
	 *            Content of the Document
	 * @param docid
	 *            ID of the Document
	 */
	private void updateDoc(String docname, String doctype, String docdata,
			String docid, boolean base) {
		Session session = new Session(server, port, user, password, true, secureConnection);

		Database db = null;
		db = session.getDatabase(databaseName);

		try {
			Document doc = db.getDocument(docid);
			if (doc != null) {
				doc.put("filename", docname);
				doc.put("filetype", doctype);
				doc.put("data", docdata);
				doc.put("time", System.currentTimeMillis());
				if (base) {
					doc.put("encoding", "base64");
				}

				try {
					db.saveDocument(doc);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
