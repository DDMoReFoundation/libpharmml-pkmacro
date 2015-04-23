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

import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.PKMacro;

/**
 * Exception thrown if a required argument is missing in the given macro.
 * @author F. Yvon
 *
 */
public class UndefinedMacroArgumentException extends InvalidMacroException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2237245450228024793L;
	
	private final PKMacro macro;
	private final String argument;
	
	public UndefinedMacroArgumentException(PKMacro macro, String argument){
		super("Missing argument \"" + argument + "\" in macro \"" + macro.getName() + "\".");
		this.macro = macro;
		this.argument = argument;
	}
	
	/**
	 * Gets the incriminated macro at the origin if this exception.
	 * @return The invalid {@link PKMacro} object.
	 */
	public PKMacro getMacro(){
		return macro;
	}
	
	/**
	 * Gets the name of the missing argument.
	 * @return The argument name as {@link String}.
	 */
	public String getArgument(){
		return argument;
	}

}
