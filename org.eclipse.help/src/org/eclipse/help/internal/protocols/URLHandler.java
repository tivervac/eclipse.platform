/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.protocols;
import java.io.IOException;
import java.net.*;
public class URLHandler extends URLStreamHandler {
	/**
	 * Constructor for URLHandler
	 */
	public URLHandler() {
		super();
	}
	/**
	 * @see URLStreamHandler#openConnection(URL)
	 */
	protected URLConnection openConnection(URL url) throws IOException {
		String protocol = url.getProtocol();
		if (protocol.equals("help"))
			return new HelpURLConnection(url);
		else if (protocol.equals("search"))
			return new SearchURLConnection(url);
		else if (protocol.equals("links"))
			return new LinksURLConnection(url);
		else if (protocol.equals("search"))
			return new LiveHelpURLConnection(url);
		else
			return null;
	}
}