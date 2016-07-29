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

import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.IntValue;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.CompartmentMacro;
import eu.ddmore.libpharmml.impl.LoggerWrapper;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

/**
 * <p>Macro class for the translation of {@link CompartmentMacro} objects.
 * <p>Each maco compartment(cmt=1, amount=Ac) creates a new empty ODE "dAc/dt=".
 * @author Florent Yvon
 */
class Compartment extends AbstractCompartment {
	
	/**
	 * Minimal constructor.
	 * @param cmt The identifier of this compartment, defined by cmt=i in the input macro. Must be unique.
	 * @param amount The {@link DerivativeVariable} corresponding to the amount of this compartment (Ac).
	 * @param volume Volume, can be null.
	 * @param concentration Concentration, can be null.
	 */
	Compartment(Integer cmt, DerivativeVariable amount, Operand volume, Operand concentration) {
		super(cmt, amount, volume, concentration);
	}

	/**
	 * Creates a new {@link Compartment} from a PharmML {@link CompartmentMacro} object.
	 * @param cf The {@link CompartmentFactory} used in this translation process.
	 * @param vf The {@link VariableFactory} used in this translation process.
	 * @param macro The {@link CompartmentMacro} instance to be parsed.
	 * @return A new {@link Compartment} instance.
	 * @throws InvalidMacroException If the given PharmML macro is incorrect or cannot be translated.
	 */
	static Compartment fromMacro(CompartmentFactory cf, VariableFactory vf, CompartmentMacro macro) throws InvalidMacroException{
		ParamMapper resolver = new ParamMapper(macro);
		
		// Required parameters
		Integer cmt = resolver.getValue("cmt",IntValue.class).getValue().intValue();
		SymbolRef s = resolver.getValue("amount", SymbolRef.class);
		DerivativeVariable dv = resolveDerivativeVariable(vf, s, macro);
		LoggerWrapper.getLogger().info(dv+" order set to "+cmt);
		dv.setOrder(cmt); // Highest-priority order assignment
		
		// Optionals
		Operand volume;
		if(resolver.contains("volume")){
			volume = resolver.getValue("volume",Operand.class);
		} else {
			volume = null;
		}
		Operand concentration;
		if(resolver.contains("concentration")){
			concentration = resolver.getValue("concentration",Operand.class);
		} else {
			concentration = null;
		}
		Compartment comp = new Compartment(cmt, dv, volume, concentration);
		cf.addCompartment(comp);
		return comp;
	}
	
}
