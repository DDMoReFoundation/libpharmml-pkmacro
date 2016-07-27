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

import java.util.List;

import eu.ddmore.libpharmml.dom.commontypes.CommonVariableDefinition;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.PKMacro;

/**
 * Root class of any translated macro.
 * 
 * @author Florent Yvon
 */
public abstract class AbstractMacro {
	
	private Integer index = -1;
	
	private PKMacro origin = null;
	
	/**
	 * Position of the macro as it is read in the input PharmML model.
	 * @return The index starting from 0. Returns -1 if the order has not been saved.
	 */
	public Integer getIndex(){
		return index;
	}
	
	/**
	 * Sets the position of the input xml macro to keep track of it.
	 * @param index The current position of the untranslated macro.
	 */
	void setIndex(Integer index){
		this.index = index;
	}
	
	public PKMacro getOrigin(){
		return origin;
	}
	
	public void setOrigin(PKMacro macro){
		this.origin = macro;
	}
	
	/**
	 * Gets the {@link VariableDefinition} and {@link DerivativeVariable} objects created by this
	 * macro.
	 * @return A {@link List} of variables.
	 */
	abstract List<CommonVariableDefinition> getVariables();

}
