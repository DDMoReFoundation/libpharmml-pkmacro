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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.ddmore.libpharmml.dom.MasterObjectFactory;
import eu.ddmore.libpharmml.dom.commontypes.CommonVariableDefinition;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.modeldefn.StructuralModel;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.AbsorptionOralMacro;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.CompartmentMacro;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.DepotMacro;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.EffectMacro;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.EliminationMacro;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.IVMacro;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.MacroValue;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.PKMacro;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.PKMacroList;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.PeripheralMacro;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.TransferMacro;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

/**
 * Main class for handling PK macro translation.
 * @author F. Yvon
 *
 */
public class Translator {
		
//	private final List<AbstractMacro> model;
	
//	private final VariableFactory variableFactory;
	
	private final Map<String, Boolean> parameters;
	
	/**
	 * Parameter for keeping the order of the input PK macros. The output ODEs that symbolise 
	 * compartments appear in the same order as the macros one. Default value: false.
	 */
	public final static String KEEP_ORDER = "translator.keeporder";
	
	public Translator(){
		parameters = new HashMap<String, Boolean>();
		parameters.put(KEEP_ORDER, false);
	}
	
	/**
	 * Change the settings of the translation. The only parameter available at the moment
	 * is {@link #KEEP_ORDER}, with default value "false".
	 * @param parameter The name of the parameter, that must be in the static fields of {@link Translator}.
	 * @param value The new value the parameter.
	 */
	public void setParameter(String parameter, Boolean value){
		parameters.put(parameter, value);
	}
	
	/**
	 * Parsing of the XML-binded macro objects to translatable macro objects.
	 * Equations are added by each fromMacro() method execution.
	 * @throws InvalidMacroException
	 */
	private List<AbstractMacro> parseMacros(PKMacroList list, CompartmentFactory cf, VariableFactory vf) throws InvalidMacroException{
		
		List<AbstractMacro> model = new ArrayList<AbstractMacro>();
		
		// Core macros
		for(int i = 0;i<list.getListOfMacro().size();i++){
			PKMacro xmlMacro = list.getListOfMacro().get(i);
			if(xmlMacro instanceof CompartmentMacro){
				Compartment macro = Compartment.fromMacro(cf, vf, (CompartmentMacro) xmlMacro);
				macro.setIndex(i);
				model.add(macro);
			}
			else if(xmlMacro instanceof PeripheralMacro){
				Peripheral macro = Peripheral.fromMacro(cf, vf, (PeripheralMacro) xmlMacro);
				macro.setIndex(i);
				model.add(macro);
			}
		}
		
		// Targetted macros
		for(int i = 0;i<list.getListOfMacro().size();i++){
			PKMacro xmlMacro = list.getListOfMacro().get(i);
			if(xmlMacro instanceof AbsorptionOralMacro){
				Absorption macro = Absorption.fromMacro(cf, vf, (AbsorptionOralMacro) xmlMacro);
				macro.setIndex(i);
				model.add(macro);
			}
			else if(xmlMacro instanceof IVMacro){
				IV macro = IV.fromMacro(cf, vf, (IVMacro) xmlMacro);
				macro.setIndex(i);
				model.add(macro);
			}
			else if(xmlMacro instanceof TransferMacro){
				Transfer macro = Transfer.fromMacro(cf, vf, (TransferMacro) xmlMacro);
				macro.setIndex(i);
				model.add(macro);
			}
			else if(xmlMacro instanceof EliminationMacro){
				Elimination macro = Elimination.fromMacro(cf, vf, (EliminationMacro) xmlMacro);
				macro.setIndex(i);
				model.add(macro);
			}
			else if(xmlMacro instanceof EffectMacro){
				Effect macro = Effect.fromMacro(cf, vf, (EffectMacro) xmlMacro);
				macro.setIndex(i);
				model.add(macro);
			}
			else if(xmlMacro instanceof DepotMacro){
				Depot macro = Depot.fromMacro(cf, vf, (DepotMacro) xmlMacro);
				macro.setIndex(i);
				model.add(macro);
			}
		}
		
		return model;
		
	}
	
//	private Compartment findTarget(PKMacro macro,Map<String,Compartment> map_compartments) throws InvalidMacroException{
//		ParamResolver pr = new ParamResolver(macro);
//		String cmt;
//		if(!pr.contains("cmt") && macro_compartments.size() == 1){
//			cmt = String.valueOf(1);
//		} else {
//			cmt = (String) pr.getValue("cmt").getContent();
//		}
//		return map_compartments.get(cmt);
//	}
	
	static String getArgumentName(MacroValue value){
		if(value.getArgument() != null){
			return value.getArgument();
		} else if (value.getSymbRef() != null){
			return value.getSymbRef().getSymbIdRef();
		} else {
			return null;
		}
	}
	
	private static List<AbstractMacro> sortByIndex(List<AbstractMacro> unsorted){
		List<AbstractMacro> sorted = new ArrayList<AbstractMacro>(unsorted.size());
		while(sorted.size() < unsorted.size()){
			sorted.add(null);
		}
		for(AbstractMacro macro : unsorted){
			sorted.set(macro.getIndex(), macro);
		}
		return sorted;
	}
	
	/**
	 * Translates the given structural model to a set of equations and input data. The equations are
	 * listed within a transient translated structural model available through the generated {@link MacroOutput}
	 * implementation.
	 * @param sm The structural model that contains PK macros to be translated.
	 * @return A {@link MacroOutput} implementation.
	 * @throws InvalidMacroException If the translation is not possible because of any invalid
	 * macro within the model.
	 */
	public MacroOutput translate(StructuralModel sm) throws InvalidMacroException{
		
		VariableFactory vf = new VariableFactory(sm);
		CompartmentFactory cf = new CompartmentFactory();
		
		List<AbstractMacro> model = parseMacros(sm.getPKmacros(),cf,vf);
		
		final StructuralModel translated_sm = new StructuralModel();
		translated_sm.setBlkId("translated_sm");
		
		final InputList inputList = new InputList();
		
		for(AbstractMacro item : model){
			if(item instanceof CompartmentTargeter){
				((CompartmentTargeter) item).modifyTargetODE();
			}
			if(item instanceof InputSource){
				((InputSource) item).generateInputs(inputList);
			}
		}
		
		List<CommonVariableDefinition> variables;
		if(parameters.get(KEEP_ORDER)){
			List<AbstractMacro> sorted = sortByIndex(model);
			variables = new ArrayList<CommonVariableDefinition>();

			for(AbstractMacro macro : sorted){
				variables.addAll(macro.getVariables());
			}
			
			for(CommonVariableDefinition variable : vf.getDefinedVariables()){
				if(!variables.contains(variable)){
					variables.add(0, variable);
				}
			}
			
			// checking
			if(vf.getDefinedVariables().size() != variables.size()){
				throw new RuntimeException("Missing variables after sorting.");
			}
			
		} else {
			variables = vf.getDefinedVariables();
		}
		
		for(CommonVariableDefinition var : variables){
			if(var instanceof DerivativeVariable){
				translated_sm.getCommonVariable().add(MasterObjectFactory.COMMONTYPES_OF.createDerivativeVariable(
						(DerivativeVariable) var));
			} else if (var instanceof VariableDefinition){
				translated_sm.getCommonVariable().add(MasterObjectFactory.COMMONTYPES_OF.createVariable(
						(VariableDefinition) var));
			}
		}
		translated_sm.getSimpleParameter().addAll(vf.getDefinedParameters());
		
		return new MacroOutput() {
			@Override
			public StructuralModel getStructuralModel() {
				return translated_sm;
			}
			@Override
			public List<Input> getListOfInput() {
				return inputList;
			}
		};
	}
	
}
