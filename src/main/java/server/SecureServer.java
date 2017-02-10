/*
 * Server.java
 * Oct 7, 2012
 *
 * Simple Web Server (SWS) for CSSE 477
 *
 * Copyright (C) 2012 Chandan Raj Rupakheti
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 *
 */

package server;

import dynamic.PluginRouter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;

/**
 * This represents a welcoming server for the incoming TCP request from a HTTP
 * client such as a web browser.
 *
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class SecureServer extends Server {
	Logger logger = LogManager.getLogger(this.getClass());

	public SecureServer(String rootDirectory, int port, PluginRouter router, boolean isCacheEnabled, long cacheTimeLimit) {
	    super(rootDirectory,port,router,isCacheEnabled,cacheTimeLimit);
	}

    @Override
    void setupServerSocket() throws IOException {
        setServerSocket(SSLServerSocketFactory.getDefault().createServerSocket(getPort()));
    }
}
