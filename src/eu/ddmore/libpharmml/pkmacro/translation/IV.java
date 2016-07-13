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

import java.util.Collections;
import java.util.List;

import eu.ddmore.libpharmml.dom.commontypes.CommonVariableDefinition;
import eu.ddmore.libpharmml.dom.commontypes.IntValue;
import eu.ddmore.libpharmml.dom.commontypes.Scalar;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.IVMacro;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

/**
 * <p>Class for the translation of {@link IVMacro} objects.
 * 
 * <p>This macro doesn't generate an ODE but only an input element. The {@link Input} element type is {@link InputType#IV},
 * the administration number is given by either "type" or "adm", and the target determined by the "cmt" number.
 * 
 * 
 * @author Florent Yvon
 *
 */
class IV extends AbstractMacro implements InputSource {
	
	protected final AbstractCompartment target;
	protected final Scalar adm;
	
	protected IV(AbstractCompartment target, Scalar adm) {
		super();
		this.target = target;
		this.adm = adm;
	}
	
	static IV fromMacro(CompartmentFactory cf, VariableFactory vf, IVMacro macro) throws InvalidMacroException{
		ParamMapper pr = new ParamMapper(macro);
		
		Scalar adm;
		if(pr.contains("type")){
			adm = pr.getValue("type", Scalar.class);
		} else {
			adm = pr.getValue("adm", Scalar.class);
		}
		
		AbstractCompartment target = cf.getCompartment(pr.getValue("cmt",IntValue.class).getValue().intValue());
		
		return new IV(target, adm);
	}

	@Override
	public void generateInputs(InputList inputList) {		
		inputList.createInput(InputType.IV, adm, target.getAmount());
	}

	@Override
	List<CommonVariableDefinition> getVariables() {
		return Collections.emptyList();
	}

}
