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

import java.util.Collections;
import java.util.List;

import eu.ddmore.libpharmml.dom.commontypes.CommonVariableDefinition;
import eu.ddmore.libpharmml.dom.commontypes.IntValue;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.maths.Binop;
import eu.ddmore.libpharmml.dom.maths.Binoperator;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.TransferMacro;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

class Transfer extends AbstractMacro implements CompartmentTargeter{
	
	private final AbstractCompartment from;
	private final AbstractCompartment to;
	private final Operand rate;
	
	private Transfer(AbstractCompartment from, AbstractCompartment to, Operand rate) {
		this.from = from;
		this.to = to;
		this.rate = rate;
	}

	Operand getTransferRate(){
		return rate;
	}
	
	AbstractCompartment getFrom() {
		return from;
	}

	AbstractCompartment getTo() {
		return to;
	}

	@Override
	public void modifyTargetODE() {
		// to
		Binop exp1 = new Binop(Binoperator.TIMES, rate, new SymbolRef(getFrom().getAmount().getSymbId()));
//		to.getOde().addOperand(Binoperator.PLUS, exp1);
		Utils.addOperand(to.getAmount(), Binoperator.PLUS, exp1);
		// from
		Binop exp2 = new Binop(Binoperator.TIMES, rate, new SymbolRef(getFrom().getAmount().getSymbId()));
//		from.getOde().addOperand(Binoperator.MINUS, exp2);
		Utils.addOperand(from.getAmount(), Binoperator.MINUS, exp2);
	}

	static Transfer fromMacro(CompartmentFactory cf, VariableFactory vf, TransferMacro macro) throws InvalidMacroException {
		ParamResolver pr = new ParamResolver(macro);
		
		AbstractCompartment from = cf.getCompartment(pr.getValue("from",IntValue.class).getValue().intValue());
		AbstractCompartment to = cf.getCompartment(pr.getValue("to",IntValue.class).getValue().intValue());
		Operand rate = pr.getValue(TransferMacro.Arg.KT, Operand.class);
		
		return new Transfer(from, to, rate);
	}

	@Override
	List<CommonVariableDefinition> getVariables() {
		return Collections.emptyList();
	}

}
