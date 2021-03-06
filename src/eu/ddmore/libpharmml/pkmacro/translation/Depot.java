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

import static eu.ddmore.libpharmml.impl.LoggerWrapper.getLogger;

import java.util.ArrayList;
import java.util.List;

import eu.ddmore.libpharmml.dom.commontypes.CommonVariableDefinition;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.Scalar;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.maths.Operand;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.DepotMacro;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

/**
 * <p>Macro class for the translation of {@link DepotMacro} objects.
 * 
 * <p>In the MLXTRAN literature, a depot occurs only in connection with explicitly defined ODEs. Example:<br>
 * <pre>
 * PK: depot(adm=a, target=Ac) 
 * EQUATION: ddt_Ac = -k*Ac
 * and means bolus IV administration
 * or
 * PK: depot(adm=a, target=Ac, ka) 
 * EQUATION: ddt_Ac = -k*Ac 
 * and means ORAL administration
 * </pre>
 * 
 * <p><h3>case 1: without "ka" argument</h3><br>
 * <center><code>depot(adm=a, target=Ac)</code></center><br>
 * Creates a new Input[inputNumber] IV administration, adm=a, target=[Ac compartment]
 *
 * <p><h3>case 2: with "ka" argument</h3><br>
 * <center><code>depot(adm=i, target=Ac, ka)</code></center><br>
 * corresponds to these 2 macros:
 * <center><code>compartment(cmt=1, amount=Ac)</code></center>
 * <center><code>oral(cmt=1, ka)</code></center><br>
 * i.e. creates new depot compartment and according ODE "dcmtAmount[new depot name]/dt = ", i.e. "dAd/dt = - ka*Ad"
 * A new depot compartment is created with the following ODE: "dAd/dt = - ka*Ad".<br>
 * An expression " + ka*Ad" is added to the target compartment Ac.<br>
 * A new Input[inputNumber] ORAL administration, adm=a, target=[Ad compartment]<br>
 * 
 * @author Florent Yvon
 */
class Depot extends AbstractMacro implements InputSource, CompartmentTargeter {
	
	/**
	 * The target is this time a {@link DerivativeVariable}, because this macro works with explicit
	 * ODEs. This means that there is no compartment macro defined for the target.
	 */
	protected final DerivativeVariable target;
	protected final Scalar adm;
	protected final Operand ka;
	protected final Operand tlag;
	protected final Operand p;
	// When using ka, an oral macro is created. This reference is used to generated the inputs
	protected final Absorption absorption;
	
	private Depot(Scalar adm, DerivativeVariable target, Operand ka, Operand tlag, Operand p, Absorption absorption){
		this.adm = adm;
		this.target = target;
		this.ka = ka;
		this.absorption = absorption;
		this.tlag = tlag;
		this.p = p;
	}
	
	static Depot fromMacro(CompartmentFactory cf, VariableFactory vf, DepotMacro macro) throws InvalidMacroException{
		ParamMapper pr = new ParamMapper(macro);
		
		SymbolRef targetRef = pr.getValue(DepotMacro.Arg.TARGET, SymbolRef.class);
		DerivativeVariable target = AbstractCompartment.resolveDerivativeVariable(vf, targetRef, macro);
		
		Scalar adm = pr.getValue(DepotMacro.Arg.ADM, Scalar.class);
		
		Operand ka;
		Operand tlag;
		Operand p;
		Absorption absorption;
		
		if(pr.contains(DepotMacro.Arg.TLAG)){
			tlag = pr.getValue(DepotMacro.Arg.TLAG, Operand.class);
		} else {
			tlag = null;
		}
		
		if(pr.contains(DepotMacro.Arg.P)){
			p = pr.getValue(DepotMacro.Arg.P, Operand.class);
		} else {
			p = null;
		}
		
		if(pr.contains(DepotMacro.Arg.KA)){
			ka = pr.getValue(DepotMacro.Arg.KA, Operand.class);
			// Depot with ka equals to compartment and oral macros
			// 
			getLogger().info("Depot macro translated to 1 Compartment and 1 Oral");
			Compartment comp = new Compartment(cf.lowestAvailableId(), target, null, null);
			cf.addCompartment(comp);
			
			DerivativeVariable depot_variable = vf.generateDerivativeVariable(VariableFactory.DEPOT_PREFIX, macro);
			absorption = new Absorption(
					adm, tlag, null, ka, null, null, p, comp, Absorption.Type.FIRST_ORDER, 
					cf.lowestAvailableId(), depot_variable, vf);
			cf.addCompartment(absorption);
		} else {
			ka = null;
			absorption = null;
		}
		
		return new Depot(adm, target, ka, tlag, p, absorption);
	}

	@Override
	public void generateInputs(InputList inputList) throws InvalidMacroException {
		if(absorption != null){
			absorption.generateInputs(inputList);
		} else {
			inputList.createInput(InputType.IV, adm, target, tlag, p);
		}
	}

	@Override
	List<CommonVariableDefinition> getVariables() {
		List<CommonVariableDefinition> variables = new ArrayList<CommonVariableDefinition>();
		if(target != null){
			variables.add(target);
		}
		if(absorption != null){
			variables.addAll(absorption.getVariables());
		}
		return variables;
	}

	@Override
	public void modifyTargetODE() {
		// Executed if the argument "ka" is used
		if(absorption != null){
			absorption.modifyTargetODE();
		}
	}
	
	
}
