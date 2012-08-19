/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2010-2011 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.search.BooleanQuery;
import org.xml.sax.SAXException;

import com.jaeksoft.searchlib.util.FilesUtils;
import com.jaeksoft.searchlib.util.properties.PropertyItem;
import com.jaeksoft.searchlib.util.properties.PropertyItemListener;
import com.jaeksoft.searchlib.util.properties.PropertyManager;
import com.jaeksoft.searchlib.web.StartStopListener;

public class ClientFactory implements PropertyItemListener {

	public static ClientFactory INSTANCE = null;

	public final InstanceProperties properties;

	private PropertyItem<Integer> booleanQueryMaxClauseCount;

	private PropertyManager advancedProperties;

	public ClientFactory() throws SearchLibException {
		try {
			properties = new InstanceProperties(new File(
					StartStopListener.OPENSEARCHSERVER_DATA_FILE,
					"properties.xml"));
			File advPropFile = new File(
					StartStopListener.OPENSEARCHSERVER_DATA_FILE,
					"advanced.xml");
			advancedProperties = new PropertyManager(advPropFile);
			booleanQueryMaxClauseCount = advancedProperties.newIntegerProperty(
					"booleanQueryMaxClauseCount", 1024, null, null);
			BooleanQuery.setMaxClauseCount(booleanQueryMaxClauseCount
					.getValue());
			booleanQueryMaxClauseCount.addListener(this);
		} catch (XPathExpressionException e) {
			throw new SearchLibException(e);
		} catch (ParserConfigurationException e) {
			throw new SearchLibException(e);
		} catch (SAXException e) {
			throw new SearchLibException(e);
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
	}

	protected Client newClient(File initFileOrDir,
			boolean createIndexIfNotExists, boolean disableCrawler)
			throws SearchLibException {
		return new Client(initFileOrDir, createIndexIfNotExists, disableCrawler);
	}

	final public Client getNewClient(File initFileOrDir,
			boolean createIndexIfNotExists, boolean disableCrawler)
			throws SearchLibException {
		try {
			if (!FilesUtils
					.isSubDirectory(
							StartStopListener.OPENSEARCHSERVER_DATA_FILE,
							initFileOrDir))
				throw new SearchLibException("Security alert: " + initFileOrDir
						+ " is outside OPENSEARCHSERVER_DATA ("
						+ StartStopListener.OPENSEARCHSERVER_DATA_FILE + ")");
		} catch (IOException e) {
			throw new SearchLibException(e);
		}
		return newClient(initFileOrDir, createIndexIfNotExists, disableCrawler);
	}

	public static void setInstance(ClientFactory cf) {
		INSTANCE = cf;
	}

	public PropertyItem<Integer> getBooleanQueryMaxClauseCount() {
		return booleanQueryMaxClauseCount;
	}

	@Override
	public void hasBeenSet(PropertyItem<?> prop) throws SearchLibException {
		if (prop == booleanQueryMaxClauseCount) {
			BooleanQuery.setMaxClauseCount(booleanQueryMaxClauseCount
					.getValue());
			try {
				advancedProperties.save();
			} catch (IOException e) {
				throw new SearchLibException(e);
			}
		}
	}

}