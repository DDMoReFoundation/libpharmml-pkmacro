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
	
	private List<CompartmentMacro> macro_compartments;
	private List<PeripheralMacro> macro_peripherals;
	private List<AbsorptionOralMacro> macro_absorals;
	private List<IVMacro> macro_ivs;
	private List<TransferMacro> macro_transfers;
	private List<EliminationMacro> macro_eliminations;
	private List<EffectMacro> macro_effects;
	private List<DepotMacro> macro_depots;
	
	private final Map<String, AbstractCompartment> map_compartments;
	
	private final List<AbstractMacro> model;
	
	private final VariableFactory variableFactory;
	
	public Translator(StructuralModel sm){
		PKMacroList macros = sm.getPKmacros();
		
		// Sorting macros first
		macro_compartments = new ArrayList<CompartmentMacro>();
		macro_peripherals = new ArrayList<PeripheralMacro>();
		macro_absorals = new ArrayList<AbsorptionOralMacro>();
		macro_ivs = new ArrayList<IVMacro>();
		macro_transfers = new ArrayList<TransferMacro>();
		macro_eliminations = new ArrayList<EliminationMacro>();
		macro_effects = new ArrayList<EffectMacro>();
		macro_depots = new ArrayList<DepotMacro>();
		map_compartments = new HashMap<String, AbstractCompartment>();
		for(PKMacro macro : macros.getListOfMacro()){
			if(macro instanceof CompartmentMacro){
				macro_compartments.add((CompartmentMacro) macro);
			} else if (macro instanceof PeripheralMacro){
				macro_peripherals.add((PeripheralMacro) macro);
			} else if (macro instanceof AbsorptionOralMacro){
				macro_absorals.add((AbsorptionOralMacro) macro);
			} else if (macro instanceof IVMacro){
				macro_ivs.add((IVMacro) macro);
			} else if (macro instanceof TransferMacro){
				macro_transfers.add((TransferMacro) macro);
			} else if (macro instanceof EliminationMacro){
				macro_eliminations.add((EliminationMacro) macro);
			} else if (macro instanceof EffectMacro){
				macro_effects.add((EffectMacro) macro);
			} else if (macro instanceof DepotMacro){
				macro_depots.add((DepotMacro) macro);
			}
		}
		
		variableFactory = new VariableFactory(sm);
		
		model = new ArrayList<AbstractMacro>();
	}
	
	/**
	 * Parsing of the XML-binded macro objects to translatable macro objects.
	 * Equations are added by each fromMacro() method execution.
	 * @throws InvalidMacroException
	 */
	private void parseMacros() throws InvalidMacroException{

		// Core
		for(CompartmentMacro macro : macro_compartments){
			model.add(Compartment.fromMacro(this, macro));
		}
		for(PeripheralMacro macro : macro_peripherals){
			model.add(Peripheral.fromMacro(this, macro));
		}
		
		// Targetted/Input macros
		for(AbsorptionOralMacro macro : macro_absorals){
			model.add(Absorption.fromMacro(this, macro));
		}
		for(IVMacro macro : macro_ivs){
			model.add(IV.fromMacro(this, macro));
		}
		for(TransferMacro macro : macro_transfers){
			model.add(Transfer.fromMacro(this, macro));
		}
		for(EliminationMacro macro : macro_eliminations){
			model.add(Elimination.fromMacro(this,macro));
		}
		for(EffectMacro macro : macro_effects){
			model.add(Effect.fromMacro(this, macro));
		}
		for(DepotMacro macro : macro_depots){
			model.add(Depot.fromMacro(this, macro));
		}
		
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
	
	AbstractCompartment getCompartment(String cmt) throws InvalidMacroException{
		if(map_compartments.containsKey(cmt)){
			return map_compartments.get(cmt);
		} else {
			throw new InvalidMacroException("Compartment \""+cmt+"\" does not exist");
		}
	}
	
	void addCompartment(AbstractCompartment comp) throws InvalidMacroException{
		AbstractCompartment previous = map_compartments.put(comp.getCmt(), comp);
		if(previous != null){
			throw new InvalidMacroException("Compartment \""+comp.getCmt()+"\" is duplicated");
		}
	}
	
	void addAbsOral(Absorption abs){
		model.add(abs);
	}
	
	Integer compartmentsSize(){
		return map_compartments.size();
	}
	
	boolean compartmentExists(String cmt){
		return map_compartments.containsKey(cmt);
	}
	
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
	 * Translates the given structural model into equations and input data.
	 * @return A {@link MacroOutput} object that contains a new structural model and the input data.
	 * @throws InvalidMacroException If any macro contained in the initial structural model is not valid.
	 */
	public MacroOutput translate() throws InvalidMacroException{
		
		parseMacros();
		
		final StructuralModel sm = new StructuralModel();
		sm.setBlkId("translated_sm");
		
		final InputList inputList = new InputList();
		
		for(AbstractMacro item : model){
			if(item instanceof CompartmentTargeter){
				((CompartmentTargeter) item).modifyTargetODE();
			}
			if(item instanceof InputSource){
				((InputSource) item).generateInputs(inputList);
			}
		}
		
		for(CommonVariableDefinition var : getVariableFactory().getDefinedVariables()){
			if(var instanceof DerivativeVariable){
				sm.getCommonVariable().add(MasterObjectFactory.COMMONTYPES_OF.createDerivativeVariable(
						(DerivativeVariable) var));
			} else if (var instanceof VariableDefinition){
				sm.getCommonVariable().add(MasterObjectFactory.COMMONTYPES_OF.createVariable(
						(VariableDefinition) var));
			}
		}
		sm.getSimpleParameter().addAll(getVariableFactory().getDefinedParameters());
		
		return new MacroOutput() {
			@Override
			public StructuralModel getStructuralModel() {
				return sm;
			}
			@Override
			public List<Input> getListOfInput() {
				return inputList;
			}
		};
	}
	
	/**
	 * Location where the variables are stored and generated. All variables created in macros must
	 * be refered in this factory. The content will be pasted to the variable list of the generated
	 * structural model.
	 * @return A {@link VariableFactory} object.
	 */
	VariableFactory getVariableFactory(){
		return variableFactory;
	}

}
