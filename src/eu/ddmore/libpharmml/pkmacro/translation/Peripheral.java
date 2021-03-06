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
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.maths.Binop;
import eu.ddmore.libpharmml.dom.maths.Binoperator;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.MacroValue;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.PeripheralMacro;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.TransferRate;
import eu.ddmore.libpharmml.impl.LoggerWrapper;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

/**
 * <p>Class for the translation of {@link PeripheralMacro} objects.
 * 
 * <p>The following peripheral macro:<br>
 * <center><code>peripheral(k12,k21,amount=Ap)</code></center><br>
 * 
 * creates an empty ‘j’ ODE ‘dcmtAmount[j]/dt = ’, i.e. ‘dAp/dt = ’.<br>
 * <p>‘kij’ or ‘k_i_j’arguments are processed with the label ‘i’ being the one of the central compartment defined before, 
 * the label ‘j’ being for the current peripheral compartment.<br>
 * <p>The following mathematical expressions are then added:<br>
 * ‘- kij * cmtAmount[j]’ to the ‘i’ ODE<br>
 * ‘+ kij * cmtAmount[i]’ to the ‘current’ ODE<br>
 * And for the ‘kji’ or ‘k_j_i’ arguments:<br>
 * ‘- kji * cmtAmount[i]’ to the ‘j’ ODE<br>
 * ‘+ kji * cmtAmount[j]’ to the ‘current’ ODE<br>
 * @author Florent Yvon
 *
 */
class Peripheral extends AbstractCompartment implements CompartmentTargeter {
	
	/**
	 * Minimal constructor.
	 * @param cmt The identifier of the peripheral compartment.
	 * @param amount Amount variable of the peripheral compartment (Ap).
	 * @param volume Volume of the peripheral compartment, can be null.
	 * @param concentration Concentration of the peripheral compartment, can be null.
	 * @param inRate Transfer rate from central to peripheral.
	 * @param outRate Transfer rate from peripheral to central.
	 * @param target The target central compartment.
	 */
	private Peripheral(Integer cmt, DerivativeVariable amount, Operand volume, Operand concentration, Operand inRate, Operand outRate, AbstractCompartment target) {
		super(cmt, amount, volume, concentration);
		this.inRate = inRate;
		this.outRate = outRate;
		this.target = target;
		
		initOde();
	}

	private final Operand inRate;
	private final Operand outRate;
	private final AbstractCompartment target;
	
	static Peripheral fromMacro(CompartmentFactory cf, VariableFactory vf, PeripheralMacro macro) throws InvalidMacroException{
		ParamMapper resolver = new ParamMapper(macro);
		
		// Looking for transfer rates
		Integer periphCmt = null;
		Integer centralCmt = null;
		Operand inRate = null;
		Operand outRate = null;
		Integer[] firstIndexes = new Integer[2];
		for(MacroValue macroValue : macro.getListOfValue()){
			if(TransferRate.isValid(Translator.getArgumentName(macroValue))){
				TransferRate tr = new TransferRate(Translator.getArgumentName(macroValue));
				if(firstIndexes[0] == null){ // first tranfer rate met
					firstIndexes[0] = tr.getFrom();
					firstIndexes[1] = tr.getTo();
					if(cf.compartmentExists(tr.getFrom())){
						periphCmt = tr.getTo();
						centralCmt = tr.getFrom();
						inRate = resolver.getValue(Translator.getArgumentName(macroValue), Operand.class);
					} else if (cf.compartmentExists(tr.getTo())){
						periphCmt = tr.getFrom();
						centralCmt = tr.getTo();
						outRate = resolver.getValue(Translator.getArgumentName(macroValue), Operand.class);
					} else {
						throw new InvalidMacroException("Peripheral macro is not connected to "
								+ "any central compartment via transfer rates.");
					}
				} else { // second one
					if(!firstIndexes[0].equals(tr.getTo()) || !firstIndexes[1].equals(tr.getFrom())){
						throw new InvalidMacroException("Argument names for the transfer rates must be"
								+ "symmetric");
					}
					if(inRate == null){
						inRate = resolver.getValue(Translator.getArgumentName(macroValue), Operand.class);
					} else if(outRate == null){
						outRate = resolver.getValue(Translator.getArgumentName(macroValue), Operand.class);
					}
				}
			}
		}
		
		// amount, not required since 0.1.1
		DerivativeVariable amount;
		if(resolver.contains(PeripheralMacro.Arg.AMOUNT)){
			SymbolRef amountRef = resolver.getValue("amount", SymbolRef.class);
			amount = resolveDerivativeVariable(vf, amountRef, macro);
		} else {
			amount = vf.createDerivativeVariable(VariableFactory.PERIPH_CMT_PREFIX, periphCmt, macro);
		}
		LoggerWrapper.getLogger().info(amount+" order set to "+periphCmt);
		amount.setOrder(periphCmt);
		
		AbstractCompartment central = cf.getCompartment(centralCmt);
		Peripheral periph = new Peripheral(periphCmt, amount, null, null, inRate, outRate, central);
		cf.addCompartment(periph);
		return periph;
	}
	
	public Operand getInRate() {
		return inRate;
	}

	public Operand getOutRate() {
		return outRate;
	}
	
	/**
	 * Adds the mathematical expressions to the new peripheral amount:<br>
	 * dperiphAmt/dt = inRate x centralAmt - outRate * periphAmt
	 */
	protected void initOde() {
		Binop exp1 = new Binop(Binoperator.TIMES, inRate, new SymbolRef(target.getAmount().getSymbId()));
		Utils.addOperand(amount, Binoperator.PLUS, exp1);
		
		Binop exp2 = new Binop(Binoperator.TIMES, outRate, new SymbolRef(getAmount().getSymbId()));
		Utils.addOperand(amount, Binoperator.MINUS, exp2);
	}

	@Override
	public void modifyTargetODE() {		
		// inRate
		Binop exp1 = new Binop(Binoperator.TIMES, inRate, new SymbolRef(target.getAmount().getSymbId()));
		Utils.addOperand(target.getAmount(), Binoperator.MINUS, exp1);
		
		// outRate
		Binop exp2 = new Binop(Binoperator.TIMES, outRate, new SymbolRef(getAmount().getSymbId()));
		Utils.addOperand(target.getAmount(), Binoperator.PLUS, exp2);
	}

}
