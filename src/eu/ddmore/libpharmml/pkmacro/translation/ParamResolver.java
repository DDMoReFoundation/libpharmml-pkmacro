/*******************************************************************************
 * Copyright (c) 2015-2016 European Molecular Biology Laboratory,
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
package eu.ddmore.libpharmml.pkmacro.translation;

import java.util.Hashtable;
import java.util.Map;

import eu.ddmore.libpharmml.dom.commontypes.Rhs;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.MacroValue;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.PKMacro;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

/**
 * Class for helping to fetch paramaters in macros
 *
 * @author Florent Yvon
 */
class ParamResolver {
	
	private final Map<String,Rhs> data;
	private final String macroName;

	/**
	 * Minimal constructor. The resolver is built around an existing macro that contains defined parameters.
	 * @param macro A XML macro object.
	 * @throws InvalidMacroException If one of the parameters is invalid.
	 */
	ParamResolver(PKMacro macro) throws InvalidMacroException {
		data = new Hashtable<String,Rhs>();
		for(MacroValue value : macro.getListOfValue()){
			if(value.getArgument() != null){
				if(value.getAssign() == null){
					throw new InvalidMacroException(value.getArgument());
				}
				data.put(value.getArgument(), value.getAssign());
			} else {
				if(value.getSymbRef() == null){
					throw new InvalidMacroException(
							"A macro parameter should either have an argument name "
							+ "or be defined by a symbol reference.");
				}
				data.put(value.getSymbRef().getSymbIdRef(), new Rhs(value.getSymbRef()));
			}
		}
		macroName = macro.getName();
	}
	
	/**
	 * Returns the value of the given parameter.
	 * @param argument Name of the argument
	 * @return The value of the wanted parameter wrapped in a {@link Rhs} element.
	 * @throws InvalidMacroException If the given argument is not defined for the provided macro.
	 */
	Rhs getValue(String argument) throws InvalidMacroException{
		if(!data.containsKey(argument) || data.get(argument) == null){
			throw new InvalidMacroException("Argument \""+argument+"\" is undefined in \""+macroName+"\".");
		}
		return data.get(argument);
	}
	
	/**
	 * This method gets the value of a parameter with assumption of its type. If the parameter is
	 * assigned to an equation, the assumption will be on the content of the equation, and that content
	 * returned.
	 * @param argument Name of the parameter.
	 * @param _class The expect type of the parameter.
	 * @return The value of the parameter casted to the expected type.
	 * @throws InvalidMacroException If the parameter does not exist or if the type is not the expected one.
	 */
	@SuppressWarnings("unchecked")
	<T> T getValue(String argument, Class<T> _class) throws InvalidMacroException{
		Rhs rhs_value = getValue(argument);
		Object rhs_content = rhs_value.getContent();
		// Equation not used anymore
//		if(rhs_content instanceof Equation){
//			Equation eq = (Equation) rhs_value.getContent();
//			Object content = getEquationContent(eq);
//			if(clazz.isInstance(content)){
//				return (T) content;
//			} else {
//				throw new InvalidMacroException(argument + " must be defined as a "+ clazz +" object. Actual is "+content.getClass());
//			}
		if(_class.isInstance(rhs_content)){
			return (T) rhs_value.getContent();
		} else {
			throw new InvalidMacroException(argument + " must be defined as a"+ _class +" object. Actual is "+rhs_content);
		}
	}
	
	Rhs getValue(Object argument) throws InvalidMacroException{
		return getValue(argument.toString());
	}
	
	<T> T getValue(Object argument, Class<T> _class) throws InvalidMacroException{
		return getValue(argument.toString(), _class);
	}
	
	/**
	 * Checks if the given argument is defined for the provided macro.
	 * @param argument Argument name
	 * @return true if found, else false.
	 */
	boolean contains(String argument){
		return data.containsKey(argument);
	}
	
	boolean contains(Object argument){
		return data.containsKey(argument.toString());
	}
	
}
