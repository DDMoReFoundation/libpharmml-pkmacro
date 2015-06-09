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
import eu.ddmore.libpharmml.dom.commontypes.IntValue;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.maths.Binop;
import eu.ddmore.libpharmml.dom.maths.Binoperator;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.EliminationMacro;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

class Elimination extends AbstractMacro implements CompartmentTargeter {
	
	final protected Operand k;
	final protected Operand V;
	final protected Operand CL;
	final protected Operand Km;
	final protected Operand Vm;
	final Type type;
	final AbstractCompartment target;
		
	protected Elimination(Operand k, Operand v, Operand cl, Operand km, Operand vm, 
			Type type, AbstractCompartment target) {
		this.k = k;
		V = v;
		CL = cl;
		Km = km;
		Vm = vm;
		this.type = type;
		this.target = target;
	}
	
	static Elimination createLinear(AbstractCompartment target, Operand k){
		return new Elimination(k, null, null, null, null, Type.LINEAR, target);
	}
	
	static Elimination createLinearWithCL(AbstractCompartment target, Operand v,Operand cl){
		return new Elimination(null, v, cl, null, null, Type.LINEAR_CL, target);
	}
	
	static Elimination createSaturable(AbstractCompartment target, Operand km, Operand vm){
		return new Elimination(null, null, null, km, vm, Type.SATURABLE, target);
	}
	
	static Elimination fromMacro(CompartmentFactory cf, VariableFactory vf, EliminationMacro macro) throws InvalidMacroException{
		ParamResolver resolver = new ParamResolver(macro);
		
		AbstractCompartment target = cf.getCompartment(resolver.getValue("cmt",IntValue.class).getValue().intValue());
		
		if(resolver.contains("k")){
			return createLinear(target,resolver.getValue("k",Operand.class));
		} else if (resolver.contains("V") && resolver.contains("CL")){
			return createLinearWithCL(target,resolver.getValue("V",Operand.class), resolver.getValue("CL",Operand.class));
		} else if (resolver.contains("Km") && resolver.contains("Vm")){
			return createSaturable(target,resolver.getValue("Km",Operand.class), resolver.getValue("Vm",Operand.class));
		} else {
			throw new InvalidMacroException("Invalid Elimination macro. "
					+ "Required arguments are either [k], [V,CL] or [Km,Vm]]].");
		}
	}
	
	enum Type {
		LINEAR,
		LINEAR_CL,
		SATURABLE;
	}

	public Operand getK() {
		return k;
	}

	public Operand getV() {
		return V;
	}

	public Operand getCL() {
		return CL;
	}

	public Operand getKm() {
		return Km;
	}

	public Operand getVm() {
		return Vm;
	}

	public Type getType() {
		return type;
	}

	@Override
	public void modifyTargetODE() {
		DerivativeVariable targetVar = target.getAmount();
		Operand math_el;
		switch(type){
			case LINEAR:
				math_el = new Binop(Binoperator.TIMES, k, new SymbolRef(targetVar.getSymbId()));
				break;
			case LINEAR_CL:
				math_el = new Binop(Binoperator.TIMES, 
						new Binop(Binoperator.DIVIDE, CL, V), 
						new SymbolRef(targetVar.getSymbId()));
				break;
			default: // Saturable
				math_el = new Binop(
							Binoperator.DIVIDE,
							new Binop(
									Binoperator.TIMES,
									Vm,
									new SymbolRef(targetVar.getSymbId())
									),
							new Binop(
									Binoperator.PLUS,
									Km,
									new SymbolRef(targetVar.getSymbId())
									)
						);
				break;
		}
		Utils.addOperand(targetVar, Binoperator.MINUS, math_el);
	}
	
}
