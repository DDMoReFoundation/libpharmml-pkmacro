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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.ddmore.libpharmml.dom.IndependentVariable;
import eu.ddmore.libpharmml.dom.commontypes.CommonVariableDefinition;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.PharmMLElement;
import eu.ddmore.libpharmml.dom.commontypes.SymbolRef;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.modeldefn.CommonParameter;
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
import eu.ddmore.libpharmml.impl.PharmMLVersion;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

/**
 * Main class for handling PK macro translation.
 * 
 * <p>This class performs all the translation of a given valid {@link StructuralModel} instance. The basic
 * usage is at follows:
 * 
 * <pre>
 * {@code
 * Translator translator = new Translator();
 * MacroOutput output = translator.translate(structuralModel, PharmMLVersion.DEFAULT);
 * StructuralModel translated_sm = output.getStructuralModel();
 * }</pre>
 * 
 * <p>The {@link MacroOutput} object contains the translated {@link StructuralModel} (ie: with equations
 * and without macros) and the {@link Input} data. If the input model is not valid, an {@link InvalidMacroException}
 * is likely to be thrown. It is possible to translate a {@link StructuralModel} containing a mix of {@link PKMacro} and
 * equations. The equations that are already defined in the input model are copied in the generated {@link StructuralModel}.
 * 
 * <p>The translation can be paramaterised using the method {@link #setParameter(String, Boolean)}. The possible parameter names
 * used as first parameter of this method are:
 * 
 * <p><ul>
 * <li>{@link #KEEP_ORDER} (default=true): for keeping the order of the input model variables.</li>
 * <li>{@link #KEEP_BLOCK_ID} (default=true): for setting the same blkId value to the output structural model as the input one.</li>
 * </ul>
 * 
 * <p>The variable {@link #TRANSLATED_BLK_ID} contains the value of the translated block id, if the parameter {@link #KEEP_BLOCK_ID}
 * is set to false. For instance:
 * 
 * <pre>
 * {@code
 * translator.setParameter(Translator.KEEP_BLOCK_ID, false);
 * translator.TRANSLATED_BLK_ID = "my_blkId";
 * }
 * </pre>
 * 
 * <p>One should set carefully the blkId of the generated {@link StructuralModel} as the references to the symbols 
 * located in the input {@link StructuralModel} may be broken after the translation if the new {@link StructuralModel} 
 * blkId is different.
 * 
 * <p>The details of the translation for each type of macro can be found in the documentation of the following classes:<br>
 * <ul>
 * <li>{@link Absorption}</li>
 * <li>{@link Compartment}</li>
 * <li>{@link Depot}</li>
 * <li>{@link Effect}</li>
 * <li>{@link Elimination}</li>
 * <li>{@link IV}</li>
 * <li>{@link Peripheral}</li>
 * <li>{@link Transfer}</li>
 * </ul>
 * 
 * @author Florent Yvon
 * @version 0.3.2
 */
public class Translator {
		
	/**
	 * The blkID value used for the output {@link StructuralModel} if the option {@link #KEEP_BLOCK_ID} is set to false.
	 */
	public String TRANSLATED_BLK_ID = "translated_sm";
	
	private final Map<String, Boolean> parameters;
		
	/**
	 * Parameter for keeping the order of the input PK macros. The output ODEs that symbolise 
	 * compartments appear in the same order as the macros one. Default value: true.
	 */
	public final static String KEEP_ORDER = "translator.keeporder";
	
	/**
	 * Parameter for keeping the blkId during the translation process. If true, the output translated
	 * structural model will have the same blkId as the input structural model. If false, the translated
	 * structural model will have the blkId value of {@link #TRANSLATED_BLK_ID}, which can be changed.
	 * Default value: true.
	 */
	public final static String KEEP_BLOCK_ID = "translator.keepblockid";
	
	/**
	 * Empty constructor. The same instance can be used to translate different structural models.
	 */
	public Translator(){
		parameters = new HashMap<String, Boolean>();
		parameters.put(KEEP_ORDER, true);
		parameters.put(KEEP_BLOCK_ID, true);
	}
	
	/**
	 * Change the settings of the translation. The only parameters available at the moment
	 * are {@link #KEEP_ORDER} and {@link #KEEP_BLOCK_ID}.
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
	private List<AbstractMacro> parseMacros(PKMacroList PKMacroList, CompartmentFactory cf, VariableFactory vf, Integer compartmentIndex) throws InvalidMacroException{
		
		List<AbstractMacro> model = new ArrayList<AbstractMacro>();
		List<PKMacro> list = PKMacroList.getListOfMacro();
		
		// Core macros
		for(int i = 0;i<list.size();i++){
			PKMacro xmlMacro = list.get(i);
			if(xmlMacro instanceof CompartmentMacro){
				Compartment macro = Compartment.fromMacro(cf, vf, (CompartmentMacro) xmlMacro);
				macro.setIndex(i + compartmentIndex);
				macro.setOrigin(xmlMacro);
				model.add(macro);
			}
			else if(xmlMacro instanceof PeripheralMacro){
				Peripheral macro = Peripheral.fromMacro(cf, vf, (PeripheralMacro) xmlMacro);
				macro.setIndex(i + compartmentIndex);
				macro.setOrigin(xmlMacro);
				model.add(macro);
			}
		}
		
		// Targetted macros
		for(int i = 0;i<list.size();i++){
			PKMacro xmlMacro = list.get(i);
			if(xmlMacro instanceof AbsorptionOralMacro){
				Absorption macro = Absorption.fromMacro(cf, vf, (AbsorptionOralMacro) xmlMacro);
				macro.setIndex(i + compartmentIndex);
				macro.setOrigin(xmlMacro);
				model.add(macro);
			}
			else if(xmlMacro instanceof IVMacro){
				IV macro = IV.fromMacro(cf, vf, (IVMacro) xmlMacro);
				macro.setIndex(i + compartmentIndex);
				macro.setOrigin(xmlMacro);
				model.add(macro);
			}
			else if(xmlMacro instanceof TransferMacro){
				Transfer macro = Transfer.fromMacro(cf, vf, (TransferMacro) xmlMacro);
				macro.setIndex(i + compartmentIndex);
				macro.setOrigin(xmlMacro);
				model.add(macro);
			}
			else if(xmlMacro instanceof EliminationMacro){
				Elimination macro = Elimination.fromMacro(cf, vf, (EliminationMacro) xmlMacro);
				macro.setIndex(i + compartmentIndex);
				macro.setOrigin(xmlMacro);
				model.add(macro);
			}
			else if(xmlMacro instanceof EffectMacro){
				Effect macro = Effect.fromMacro(cf, vf, (EffectMacro) xmlMacro);
				macro.setIndex(i + compartmentIndex);
				macro.setOrigin(xmlMacro);
				model.add(macro);
			}
			else if(xmlMacro instanceof DepotMacro){
				Depot macro = Depot.fromMacro(cf, vf, (DepotMacro) xmlMacro);
				macro.setIndex(i + compartmentIndex);
				macro.setOrigin(xmlMacro);
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
	
	/**
	 * Resolve the argument of the given {@link MacroValue} instance. If the attribute returned by {@link MacroValue#getArgument()}
	 * is not null, this attribute is returned. This attribute can be null if the {@link MacroValue} contains a {@link SymbolRef}
	 * object. In this case, the argument name is defined by {@link SymbolRef#getSymbIdRef()}.
	 * @param value The {@link MacroValue} object.
	 * @return The argument name as a {@link String}. If the argument name is not defined and if the {@link MacroValue} doesn't
	 * contain a {@link SymbolRef}, this method returns null.
	 */
	static String getArgumentName(MacroValue value){
		if(value.getArgument() != null){
			return value.getArgument();
		} else if (value.getSymbRef() != null){
			return value.getSymbRef().getSymbIdRef();
		} else {
			return null;
		}
	}
	
	/**
	 * Creates a new list of macro objects, sorted by their index.
	 * @param unsorted The initial unsorted list.
	 * @return A copy of the given list, sorted by index.
	 */
	private static List<AbstractMacro> sortByIndex(List<AbstractMacro> unsorted){
		List<AbstractMacro> sorted = new ArrayList<AbstractMacro>();
		sorted.addAll(unsorted);
		Collections.sort(sorted, new MacroComparator());
		return sorted;
	}
	
	/**
	 * Comparator for macro objects using their index as criterion.
	 */
	private static class MacroComparator implements Comparator<AbstractMacro> {

		@Override
		public int compare(AbstractMacro o1, AbstractMacro o2) {
			return o1.getIndex().compareTo(o2.getIndex());
		}
		
	}
	
	/**
	 * Translates the given structural model to a set of equations and input data. The equations are
	 * listed within a transient translated structural model available through the generated {@link MacroOutput}
	 * implementation. The equations that are defined within the input {@link StructuralModel} alongside with the
	 * macros are copied in the output model.
	 * @param sm The structural model that contains PK macros to be translated.
	 * @param version The wanted PharmML version of the output.
	 * @return A {@link MacroOutput} implementation.
	 * @throws InvalidMacroException If the translation is not possible because of any invalid
	 * macro within the model.
	 * 
	 * @deprecated The independent "t" variable should be provided to the translator. Use {@link #translate(StructuralModel, PharmMLVersion, IndependentVariable)}.
	 */
	@Deprecated
	public MacroOutput translate(StructuralModel sm, PharmMLVersion version) throws InvalidMacroException{
		return translate(sm, version, null);
	}
	
	/**
	 * Translates the given structural model to a set of equations and input data. The equations are
	 * listed within a transient translated structural model available through the generated {@link MacroOutput}
	 * implementation. The equations that are defined within the input {@link StructuralModel} alongside with the
	 * macros are copied in the output model.
	 * @param sm The structural model that contains PK macros to be translated.
	 * @param version The wanted PharmML version of the output.
	 * @param t The {@link IndependentVariable} of the model corresponding to time.
	 * @return A {@link MacroOutput} implementation.
	 * @throws InvalidMacroException If the translation is not possible because of any invalid
	 * macro within the model.
	 */
	public MacroOutput translate(StructuralModel sm, PharmMLVersion version, IndependentVariable t) throws InvalidMacroException{
		
		// Instanciating variable and compartment factories used for the translation
		VariableFactory vf = new VariableFactory(sm);
		vf.setTimeVariable(t);
		CompartmentFactory cf = new CompartmentFactory();
		
		List<AbstractMacro> model = new ArrayList<AbstractMacro>();
		
		Integer compartmentIndex = 0;
		for(PharmMLElement smEl : sm.getListOfStructuralModelElements()){
			if(smEl instanceof PKMacroList){
				List<AbstractMacro> macroSublist = parseMacros((PKMacroList) smEl, cf, vf, compartmentIndex);
				model.addAll(macroSublist);
				compartmentIndex += macroSublist.size();
			}
		}
		
		final StructuralModel translated_sm = new StructuralModel();
		if(parameters.get(KEEP_BLOCK_ID)){
			translated_sm.setBlkId(sm.getBlkId());
		} else {
			translated_sm.setBlkId(TRANSLATED_BLK_ID);
		}
		
		
		final InputList inputList = new InputList();
		
		// The modification of ODEs and generation of Inputs from the given macros are executed at the end to
		// make sure that all ODE left-hand sides have been created before.
		for(AbstractMacro item : model){
			if(item instanceof CompartmentTargeter){
				((CompartmentTargeter) item).modifyTargetODE();
			}
			if(item instanceof InputSource){
				((InputSource) item).generateInputs(inputList);
			}
		}
		
		// Now it's time to fetch all the variables created during the process
		List<CommonVariableDefinition> variables = new ArrayList<CommonVariableDefinition>();
		if(parameters.get(KEEP_ORDER)){
			List<AbstractMacro> sorted = sortByIndex(model);

			// Fetching variables from each ordered macro first
			for(AbstractMacro macro : sorted){
				for(CommonVariableDefinition var : macro.getVariables()){
					if(!variables.contains(var)){ // macro can share some variables
						variables.add(var);
					}
				}
			}
			
			// Fetching other variables that might have been created during the translation
			for(CommonVariableDefinition variable : vf.getDefinedVariables()){
				if(!variables.contains(variable)){
					variables.add(0,variable); // added at the beginning so all variables are declared before being used in other equations
				}
			}
			
			// checking
			if(vf.getDefinedVariables().size() != variables.size()){
				throw new RuntimeException("Missing variables after sorting.");
			}
			
		} else {
			for(CommonVariableDefinition var : vf.getDefinedVariables()){
				variables.add(var);
			}
		}
		
		// Piece of code added to make NONMEM happy about the order of the variables.
		// Some optimisation needs to be done here, as repeated loops are performed.
		// --- Start of ugly piece of code
		Set<Integer> bookedIndexes = new HashSet<Integer>();
		// Registering the already existing indexes. Typically the ones that are defined by PKmacro parameters (cmt)
		for(CommonVariableDefinition var : variables){
			if(var instanceof DerivativeVariable){
				if(((DerivativeVariable) var).getOrder() != null){
					bookedIndexes.add(((DerivativeVariable) var).getOrder());
				}
			}
		}
		// Browsing the elements from the input structural model to preserve the order.
		for(PharmMLElement el : sm.getListOfStructuralModelElements()){
			if(el instanceof DerivativeVariable){
				if(((DerivativeVariable) el).getOrder() == null){
					((DerivativeVariable) el).setOrder(getAndIncrementLowestAvailableIndex(bookedIndexes));
				}
			}
			if(el instanceof PKMacroList){
				for(PKMacro xmlmacro : ((PKMacroList) el).getListOfMacro()){
					AbstractMacro macro = getMacroByOrigin(model, xmlmacro);
					for(CommonVariableDefinition var : macro.getVariables()){
						if(var instanceof DerivativeVariable){
							if(((DerivativeVariable) var).getOrder() == null){
									((DerivativeVariable) var).setOrder(getAndIncrementLowestAvailableIndex(bookedIndexes));
							}
						}
					}
				}
			}
		}
		// --- End of ugly piece of code.
		
		// Now adding the variables to the new StructuralModel
		for(CommonVariableDefinition var : variables){
			if(var instanceof DerivativeVariable){
				translated_sm.getListOfStructuralModelElements().add((DerivativeVariable) var);
			} else if (var instanceof VariableDefinition){
				translated_sm.getListOfStructuralModelElements().add((VariableDefinition) var);
			}
		}
		
		// Adding parameters
		for(TransientParameter tp : vf.getDefinedParameters()){
			CommonParameter parameter;
			if(tp.containsReference()){ // the parameter was already defined, so the reference to the same object is added
				parameter = tp.getReference();
			} else {
				if(version.isEqualOrLaterThan(PharmMLVersion.V0_7_3)){
					if(tp.getType() != null && tp.getType().equals(ParameterType.INDIVIDUAL)){
						parameter = tp.toIndiviualParameter();
					} else {
						parameter = tp.toPopulationParameter();
					}
				} else {
					parameter = tp.toSimpleParameter();
				}
			}
			translated_sm.getListOfStructuralModelElements().add(parameter);
		}
		
		
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
	
	private static Integer getAndIncrementLowestAvailableIndex(Set<Integer> set){
		Integer currentIndex = 1;
		while(set.contains(currentIndex)){
			currentIndex++;
		}
		set.add(currentIndex);
		return currentIndex;
	}
	
	private static AbstractMacro getMacroByOrigin(List<AbstractMacro> listOfMacros,PKMacro origin){
		for(AbstractMacro macro : listOfMacros){
			if(macro.getOrigin().equals(origin)){
				return macro;
			}
		}
		return null;
	}
	
}
