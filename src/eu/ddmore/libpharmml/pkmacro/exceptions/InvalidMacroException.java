/*******************************************************************************
 * Copyright (c) 2015 European Molecular Biology Laboratory,
 * Heidelberg, Germany.
 *
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of
 * the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on 
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY 
 * KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations 
 * under the License.
 *******************************************************************************/
package eu.ddmore.libpharmml.pkmacro.exceptions;

/**
 * Exception thrown during the translation process if a macro misses the required
 * elements.
 * @author F. Yvon
 *
 */
public class InvalidMacroException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1263071353887741794L;
	
	private final String message;
	
	public InvalidMacroException(String message){
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}

}
