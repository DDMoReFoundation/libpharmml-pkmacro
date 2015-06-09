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

import static eu.ddmore.libpharmml.impl.LoggerWrapper.getLogger;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.Scalar;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.DepotMacro;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

class Depot extends AbstractMacro implements InputSource {
	
	/**
	 * The target is this time a {@link DerivativeVariable}, because this macro works with explicit
	 * ODEs. This means that there is no compartment macro defined for the target.
	 */
	protected final DerivativeVariable target;
	protected final Scalar adm;
	protected final Operand ka;
	
	private Depot(Scalar adm, DerivativeVariable target, Operand ka){
		this.adm = adm;
		this.target = target;
		this.ka = ka;
	}
	
	static Depot fromMacro(CompartmentFactory cf, VariableFactory vf, DepotMacro macro) throws InvalidMacroException{
		ParamResolver pr = new ParamResolver(macro);
		
		SymbolRef targetRef = pr.getValue(DepotMacro.Arg.TARGET, SymbolRef.class);
		DerivativeVariable target = AbstractCompartment.resolveDerivativeVariable(vf, targetRef);
		
		Scalar adm = pr.getValue(DepotMacro.Arg.ADM, Scalar.class);
		
		Operand ka;
		if(pr.contains(DepotMacro.Arg.KA)){
			ka = pr.getValue(DepotMacro.Arg.KA, Operand.class);
			// Depot with ka equals to compartment and oral macros
			getLogger().info("Depot macro translated to 1 Compartment and 1 Oral");
			Compartment comp = new Compartment(String.valueOf(cf.highestCompartmentId()+1), target, null, null);
			cf.addCompartment(comp);
			cf.addCompartment(new Absorption(
					adm, null, null, ka, null, null, null, comp, Absorption.Type.FIRST_ORDER, 
					String.valueOf(cf.highestCompartmentId()+1), target, vf));
		} else {
			ka = null;
		}
		
		return new Depot(adm, target, ka);
	}

	@Override
	public void generateInputs(InputList inputList) throws InvalidMacroException {
		if(ka != null){
//			inputList.createInput(InputType.ORAL, adm, target);
			// nothing, input will be generated by the Absorption created by fromMacro().
		} else {
			inputList.createInput(InputType.IV, adm, target);
		}
	}
	
	
}
