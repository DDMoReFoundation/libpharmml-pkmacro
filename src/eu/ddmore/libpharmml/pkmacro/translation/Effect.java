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

import java.util.List;

import eu.ddmore.libpharmml.dom.commontypes.CommonVariableDefinition;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.IntValue;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.maths.Binop;
import eu.ddmore.libpharmml.dom.maths.Binoperator;
import eu.ddmore.libpharmml.dom.maths.Equation;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.EffectMacro;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;
import eu.ddmore.libpharmml.util.ChainedList;

class Effect extends AbstractCompartment {
	
	final protected Operand ke0;
	final protected AbstractCompartment target;
	
//	protected VariableDefinition targetConcentration;
	
	private Effect(Integer cmt, AbstractCompartment target, Operand ke0, DerivativeVariable concentration, 
			VariableDefinition targetConcentration){
		super(cmt, null, null, new SymbolRef(concentration.getSymbId()));
		this.ke0 = ke0;
		this.target = target;
		
		// C = Ac/V
//		this.targetConcentration = targetConcentration;
		Equation eq = new Equation();
		eq.setBinop(new Binop(Binoperator.DIVIDE, 
				new SymbolRef(target.getAmount().getSymbId()), 
				target.getVolume()));
		targetConcentration.assign(eq);
		
		// dCe/dt = ke0*(C - Ce)
		Equation eq2 = new Equation();
		eq2.setBinop(new Binop(Binoperator.TIMES,
				ke0,
				new Binop(Binoperator.MINUS, new SymbolRef(targetConcentration.getSymbId()), new SymbolRef(concentration.getSymbId()))
				));
		concentration.assign(eq2);
	}
	
	static Effect fromMacro(CompartmentFactory cf, VariableFactory vf, EffectMacro macro) throws InvalidMacroException{
		ParamResolver pr = new ParamResolver(macro);
		
		SymbolRef concentrationRef = pr.getValue(EffectMacro.Arg.CONCENTRATION, SymbolRef.class);
//		DerivativeVariable concentration = tl.getVariableFactory().generateDerivativeVariable(
//				concentrationRef.getSymbIdRef());
		DerivativeVariable concentration = resolveDerivativeVariable(vf, concentrationRef);
		
		Integer targetCmt = pr.getValue("cmt",IntValue.class).getValue().intValue();
		AbstractCompartment target = cf.getCompartment(targetCmt);
		
		Operand ke0 = pr.getValue(EffectMacro.Arg.KE0.toString(), Operand.class);
		
		// fetch target concentration
		Operand targetConcentrationRef = target.getConcentration();
		if(targetConcentrationRef == null){
			throw new InvalidMacroException("Target compartment (cmt="+target.getCmt()+
					") of Effect macro must have a defined concentration parameter.");
		}
		if(!(targetConcentrationRef instanceof SymbolRef)){
			throw new InvalidMacroException("Concentration in compartment "+target.getCmt()+" must be a symbol reference.");
		}
		VariableDefinition targetConcentration = vf.fetchVariable(((SymbolRef)targetConcentrationRef).getSymbIdRef());
		if(targetConcentration == null){
			throw new InvalidMacroException("Symbol reference for concentration in compartment "+target.getCmt()+"is not resolved.");
		}
		
		Effect effect = new Effect(cf.lowestAvailableId(),target,ke0,concentration,targetConcentration);
		cf.addCompartment(effect);
		
		return effect;
	}

	public Operand getKe0() {
		return ke0;
	}

	public Operand getConcentration() {
		return concentration;
	}

	@Override
	List<CommonVariableDefinition> getVariables() {
		return new ChainedList<CommonVariableDefinition>().addIfNotNull(amount);
	}
	
//	@Override
//	public List<AbstractEquation> getEquations() {
//		List<AbstractEquation> list = new ArrayList<AbstractEquation>();
//		list.add(ode);
//		list.add(concentration_equation);
//		return list;
//	}

}
