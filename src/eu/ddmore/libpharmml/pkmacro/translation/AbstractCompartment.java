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
package eu.ddmore.libpharmml.pkmacro.translation;

import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.impl.LoggerWrapper;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

/**
 * Every new macro extending this class means adding a new {@link DerivativeVariable} definition
 * in the variable factory.
 */
abstract class AbstractCompartment extends AbstractMacro implements EquationSource {

	protected final String cmt; // transient for refering
	protected final DerivativeVariable amount;
	protected final Operand volume;
	protected final Operand concentration;
	
	AbstractCompartment(String cmt, DerivativeVariable amount, Operand volume, Operand concentration) {
		this.cmt = cmt;
		this.amount = amount;
		this.volume = volume;
		this.concentration = concentration;
	}

	public String getCmt() {
		return cmt;
	}

	public DerivativeVariable getAmount() {
		return amount;
	}

	public Operand getVolume() {
		return volume;
	}

	public Operand getConcentration() {
		return concentration;
	}

	protected static DerivativeVariable resolveDerivativeVariable(VariableFactory vf, SymbolRef sref) throws InvalidMacroException{
		String symbId = sref.getSymbIdRef();
		DerivativeVariable dv;
		if(symbId == null){
			throw new InvalidMacroException("Missing symbIdRef attribute in SymbRef.");
		} else {
			dv = vf.fetchDerivativeVariable(symbId);
			if (dv == null){
				// Check if the variable already exists as a normal variable
				VariableDefinition var = vf.fetchVariable(symbId);
				if(var != null){
					LoggerWrapper.getLogger().info("Variable "+symbId+" transformed to DerivativeVariable.");
					dv = vf.transformToDerivativeVariable(var);
				} else {
					dv = vf.generateDerivativeVariable(symbId);
				}				
			}
		}
		return dv;
	}
	
	protected static VariableDefinition resolveVariable(VariableFactory vf, SymbolRef sref) throws InvalidMacroException{
		String symbId = sref.getSymbIdRef();
		VariableDefinition v;
		if(symbId == null){
			throw new InvalidMacroException("Missing symbIdRef attribute in SymbRef.");
		} else {
			v = vf.fetchVariable(symbId);
			if (v == null){
//				throw new InvalidMacroException("Unresolved SymbRef.");
				v = vf.generateVariable(symbId);
			}
		}
		return v;
	}
		
}
