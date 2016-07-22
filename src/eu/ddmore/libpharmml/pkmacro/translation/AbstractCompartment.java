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
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.PKMacro;
import eu.ddmore.libpharmml.impl.LoggerWrapper;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;
import eu.ddmore.libpharmml.util.ChainedList;

/**
 * Every new macro extending this class means adding a new {@link DerivativeVariable} definition
 * in the variable factory.
 * 
 * @author Florent Yvon
 */
abstract class AbstractCompartment extends AbstractMacro {

	protected final Integer cmt; // transient for refering
	protected final DerivativeVariable amount;
	protected final Operand volume;
	protected final Operand concentration;
	
	AbstractCompartment(Integer cmt, DerivativeVariable amount, Operand volume, Operand concentration) {
		this.cmt = cmt;
		this.amount = amount;
		this.volume = volume;
		this.concentration = concentration;
	}

	public Integer getCmt() {
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

	/**
	 * Tries to find an already defined {@link DerivativeVariable} within the variable factory. If
	 * the variable exists as a {@link VariableDefinition}, this one is transformed to a {@link DerivativeVariable}.
	 * If the variable doesn't exist at all, a new {@link DerivativeVariable} is created.
	 * @param vf {@link VariableFactory} used during this translation process.
	 * @param sref {@link SymbolRef} that refers to the wanted derivative variable.
	 * @return The referred {@link DerivativeVariable} that was found or created.
	 * @throws InvalidMacroException If the provided {@link SymbolRef} doesn't have a valid symbIdRef.
	 */
	protected static DerivativeVariable resolveDerivativeVariable(VariableFactory vf, SymbolRef sref, PKMacro origin) throws InvalidMacroException{
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
					LoggerWrapper.getLogger().info("New derivative variable \""+symbId+"\".");
					dv = vf.generateDerivativeVariable(symbId, origin);
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
	
	@Override
	List<CommonVariableDefinition> getVariables() {
		return new ChainedList<CommonVariableDefinition>().addIfNotNull(amount);
	}
		
}
