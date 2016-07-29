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
import eu.ddmore.libpharmml.dom.commontypes.IntValue;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.maths.Binop;
import eu.ddmore.libpharmml.dom.maths.Binoperator;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.EffectMacro;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.PKMacro;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;
import eu.ddmore.libpharmml.util.ChainedList;

/**
 * <p>Macro class for the translation of {@link EffectMacro} objects.
 * 
 * <p>The given effet macro:<br>
 * <center><code>effect(cmt=i, ke0, concentration=Ce)</code></center><br>
 * creates a new algebraic equation:<br>
 * <br>
 * <code>cmtConcentration[i] = cmtAmount[i] / cmtVolume[i]</code><br>
 * <br>
 * and a new ODE:<br>
 * <br>
 * <code>dCe/dt = ke0*(cmtConcentration[i] - Ce)</code><br>
 * 
 * <p>The target compartment "i" must have a predefined concentration, or a {@link InvalidMacroException} will be thrown.
 * 
 * <p>Despite this class extending {@link AbstractCompartment}, no amount is defined for this new compartment. The derivation
 * is made on the concentration of this compartment (Ce). Its amount is null.
 * 
 * @author Florent Yvon
 */
class Effect extends AbstractCompartment {
	
	/**
	 * Parameter of the input macro.
	 */
	final protected Operand ke0;
	/**
	 * Target of the effect, defined by cmt=i in the input macro.
	 */
	final protected AbstractCompartment target;
	/**
	 * The derivative variable to be created (dCe).
	 */
	final protected DerivativeVariable d_concentration;
		
	/**
	 * Minimal constructor. Called via static {@link #fromMacro(CompartmentFactory, VariableFactory, EffectMacro)}.
	 * @param cmt Identifier if this new compartment. Must be unique. It's <b>not</b> the target id as described in the doc of this class.
	 * @param target The target compartment for the effect. Corresponding to the "cmt=i" defined in the {@link PKMacro} object.
	 * @param ke0 Parameter to be added to new ODE dCe.
	 * @param d_concentration The variable of the new ODE, dCe.
	 * @param targetConcentration The concentration of the targetted compartment.
	 */
	private Effect(Integer cmt, AbstractCompartment target, Operand ke0, DerivativeVariable d_concentration, 
			VariableDefinition targetConcentration){
		super(cmt, null, null, new SymbolRef(d_concentration.getSymbId()));
		this.ke0 = ke0;
		this.target = target;
		this.d_concentration = d_concentration;
		
		// C = Ac/V
		targetConcentration.assign(new Binop(Binoperator.DIVIDE, 
				new SymbolRef(target.getAmount().getSymbId()), 
				target.getVolume()));
		
		// dCe/dt = ke0*(C - Ce)
		d_concentration.assign(new Binop(Binoperator.TIMES,
				ke0,
				new Binop(Binoperator.MINUS, new SymbolRef(targetConcentration.getSymbId()), new SymbolRef(d_concentration.getSymbId()))
				));
	}
	
	static Effect fromMacro(CompartmentFactory cf, VariableFactory vf, EffectMacro macro) throws InvalidMacroException{
		ParamMapper pr = new ParamMapper(macro);
		
		SymbolRef concentrationRef = pr.getValue(EffectMacro.Arg.CONCENTRATION, SymbolRef.class);
//		DerivativeVariable concentration = tl.getVariableFactory().generateDerivativeVariable(
//				concentrationRef.getSymbIdRef());
		DerivativeVariable concentration = resolveDerivativeVariable(vf, concentrationRef, macro);
		
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

	/**
	 * Gets dCe/dt.
	 * @return dCe/dt.
	 */
	public DerivativeVariable getDConcentration() {
		return d_concentration;
	}

	@Override
	List<CommonVariableDefinition> getVariables() {
		ChainedList<CommonVariableDefinition> variables = new ChainedList<CommonVariableDefinition>();
		variables.addIfNotNull(amount);
		variables.addIfNotNull(d_concentration);
		return variables;
	}
	
//	@Override
//	public List<AbstractEquation> getEquations() {
//		List<AbstractEquation> list = new ArrayList<AbstractEquation>();
//		list.add(ode);
//		list.add(concentration_equation);
//		return list;
//	}

}
