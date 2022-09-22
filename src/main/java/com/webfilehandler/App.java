package com.webfilehandler;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.json.JSONObject;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import com.filehandler.FileHandler;
import com.filehandler.FileStats.FileInfo;

/**
 * Listens to localhost, and serves the following directories:
 * 1. /browse/ : List the contents of directories.
 * 2. /info/ : Get information of file or directory.
 * 3. /edit/ : Edit the file if exists at given path, or create new file (if possible).
 * 4. /delete/ : Moves file or folder to recycle bin set for the environment.
 *
 */
public class App {
	private static FileHandler fh;

	/**
	 * Runs the FileHandlingAPI.
	 * @param args Ignored.
	 */
	public static void main(String[] args) {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
			fh = new FileHandler("D:\\Siddharth\\JavaOOPProject\\test-files");
			App app = new App();
			HttpContext[] auth_contexts = {
					server.createContext("/browse/", app.new FileBrowseContext()),
					server.createContext("/info/", app.new FileInfoContext()),
					server.createContext("/edit/", app.new FileEditContext()),
					server.createContext("/delete/", app.new FileDeleteContext())
			};
			for (HttpContext context : auth_contexts) {
				context.setAuthenticator(new BasicAuthenticator("checkAuthorities") {

					@Override
					public boolean checkCredentials(String username, String password) {
						return username.equals("username") && password.equals("password");
					}

				});
			}
			server.start();
			return;
		} catch (IOException e) {
			System.out.println("Unable to initiate the HTTP server.");
			e.printStackTrace();
		}
		return;
	}

	class FileBrowseContext implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			fh.log(exchange.getPrincipal().getUsername() + ": '" + exchange.getRequestMethod() + "' - "
					+ exchange.getRequestURI());
			String filePath = exchange.getRequestURI().getPath().substring(8);
			if (fh.exists(filePath) && fh.getInfo(filePath).type == "File") {
				byte[] fileData = fh.readFile(filePath);
				exchange.sendResponseHeaders(200, fileData.length);
				exchange.getResponseBody().write(fileData);
				exchange.close();
				return;
			}
			FileInfo[] fileInfos = fh.getList(filePath);
			String res = "<!doctype html><html><head><title>Edit file</title><style>table{width: 100%;border-collapse: collapse;}td,th{border: 0.1em solid #ccc;}</style></head><body>"
					+ "<table><thead><tr><th>Name</th><th>Size (bytes)</th><th>Modified</th></tr></thead><tbody>";
			if (fileInfos.length == 0) {
				res += "<tr><td colspan=\"3\">Empty directory</td></tr>";
			} else {
				for (int i = 0; i < fileInfos.length; i++) {
					res += "<tr><td><a href=\"/browse/" + fileInfos[i].path + "\">"
							+ fileInfos[i].name + "</a></td><td>" + (fileInfos[i].size != 0 ? fileInfos[i].size : "")
							+ "</td><td>"
							+ fileInfos[i].lastModified.substring(0, 16) + "</td></tr>";
				}
			}
			res += "</tbody></table>"
					+ "</body></html>";
			exchange.sendResponseHeaders(200, res.length());
			exchange.getResponseBody().write(res.getBytes());
			exchange.close();
			return;
		}
	}

	class FileInfoContext implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			fh.log(exchange.getPrincipal().getUsername() + ": '" + exchange.getRequestMethod() + "' - "
					+ exchange.getRequestURI());
			FileInfo fi = fh.getInfo(exchange.getRequestURI().getPath().substring(6));
			String res;
			JSONObject info = new JSONObject();
			if (fi != null) {
				info.put("auth", exchange.getPrincipal().getUsername());
				info.put("location", fi.path);
				info.put("type", fi.type);
				info.put("size", fi.size);
				info.put("lastModified", fi.lastModified);
			} else {
				info.put("error", "File not found.");
			}
			res = info.toString();
			exchange.sendResponseHeaders(200, res.length());
			exchange.getResponseBody().write(res.getBytes());
			exchange.close();
			return;
		}
	}

	class FileEditContext implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			fh.log(exchange.getPrincipal().getUsername() + ": '" + exchange.getRequestMethod() + "' - "
					+ exchange.getRequestURI());
			String filePath = exchange.getRequestURI().getPath().substring(6);
			byte[] fr = fh.readFile(filePath);
			String res = "";
			if (!fh.exists(filePath) || fh.getInfo(filePath).type == "File") {
				JSONObject info = new JSONObject();
				if (exchange.getRequestMethod().equals("PUT")) {
					if (fh.write(filePath, exchange.getRequestBody().readAllBytes(), false)) {
						info.put("status", "File edited.");
					} else {
						info.put("error", "Error editing file.");
					}
					res = info.toString();
				} else if (exchange.getRequestMethod().equals("GET")) {
					res = "<!doctype html><html><head><title>Edit file</title></head><body>";
					res += "<p id=\"editStatus\">"
							+ (fr == null ? "File not found. New file can be written" : "Editing file at '" + filePath + "'.<br>")
							+ "</p><pre id=\"textEdited\" style=\"border: 1px solid;padding: 1rem;\" contenteditable=\"true\">"
							+ (fr == null ? "" : new String(fr))
							+ "</pre><button onclick='fetch(document.location.pathname, {method: \"PUT\",body: document.getElementById(\"textEdited\").innerText}).then(e => e.text()).then(e => {document.getElementById(\"editStatus\").innerHTML=e;})'>Submit</button>";
					res += "</body></html>";
				}
			} else {
				res += "Found directory at path '" + filePath + "'.";
			}
			exchange.sendResponseHeaders(200, res.length());
			exchange.getResponseBody().write(res.getBytes());
			exchange.close();
			return;
		}
	}

	class FileDeleteContext implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			fh.log(exchange.getPrincipal().getUsername() + ": '" + exchange.getRequestMethod() + "' - "
					+ exchange.getRequestURI());
			String filePath = exchange.getRequestURI().getPath().substring(8);
			String res = "";
			JSONObject info = new JSONObject();
			if (exchange.getRequestMethod().equals("DELETE")) {
				if (fh.delete(filePath)) {
					info.put("status", "File deleted.");
				} else {
					info.put("error", "Error deleting file.");
				}
				res = info.toString();
			} else if (exchange.getRequestMethod().equals("GET")) {
				res = "<!doctype html><html><head><title>Edit file</title></head><body>";
				res += "<p id=\"editStatus\">"
						+ (!fh.exists(filePath) ? "File/Directory not found at this path."
								: "File/Directory found at '" + filePath + "'.")
						+ "</p>"
						+ (fh.exists(filePath)
								? "<button onclick='window.confirm(\"Are you sure, you want to delete this file?\") && fetch(document.location.pathname, {method: \"DELETE\"}).then(e => e.text()).then(e => {document.getElementById(\"editStatus\").innerHTML=e;})'>Delete</button>"
								: "");
				res += "</body></html>";
			}
			exchange.sendResponseHeaders(200, res.length());
			exchange.getResponseBody().write(res.getBytes());
			exchange.close();
			return;
		}
	}
}
