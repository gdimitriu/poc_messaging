/**
Copyright (c) 2017 Gabriel Dimitriu All rights reserved.
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

This file is part of poc_messaging project.
Chappy is the original project for this file.

poc_messaging is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

poc_messaging is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with poc_messaging.  If not, see <http://www.gnu.org/licenses/>.
*/
package stervers.jms.processing;

import java.io.IOException;

import servers.jms.MultiDataQueryHolder;
import utils.streams.wrappers.ByteArrayInputStreamWrapper;
import utils.streams.wrappers.ByteArrayOutputStreamWrapper;
import utils.streams.wrappers.StreamHolder;


/**
 * This is part of transformationsEngine project.
 * This is the Processing step class, this in the test case is the second transformation done by digester.
 * @author Gabriel Dimitriu
 *
 */
public class ProcessingStep {

	/** mapping or processing */
	private String mappingName = null;
	/** factory for the engine */
	private String factoryEngine = null;
	
	public ProcessingStep() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * get the processing mapping
	 * @return mapping
	 */
	public String getMappingName() {
		return mappingName;
	}

	/**
	 * set the processing mapping
	 * @param mapping
	 */
	public void setMappingName(final String mapping) {
		this.mappingName = mapping;
	}

	/**
	 * get the factory for the mapping.
	 * @return factory
	 */
	public String getFactoryEngine() {
		return factoryEngine;
	}

	/**
	 * set the factory for the mapping.
	 * @param tfFactoryEngine
	 */
	public void setFactoryEngine(final String tfFactoryEngine) {
		this.factoryEngine = tfFactoryEngine;
	}

	
	/**
	 * @param holder
	 * @param multipart
	 * @throws IOException
	 */
	public void execute(final StreamHolder holder, final MultiDataQueryHolder multipart)
			throws IOException {
		ByteArrayInputStreamWrapper input = holder.getInputStream();
		ByteArrayOutputStreamWrapper output = new ByteArrayOutputStreamWrapper();
		byte[] buffer = new byte[1024];
		int len;
		len = input.read(buffer);
		while (len != -1) {
			output.write(buffer, 0, len);
		    len = input.read(buffer);
		}
		String addedMode = "=>processing:" + mappingName + " factory =" + factoryEngine;
		output.write(addedMode.getBytes());
		holder.setOutputStream(output);
	}

}
