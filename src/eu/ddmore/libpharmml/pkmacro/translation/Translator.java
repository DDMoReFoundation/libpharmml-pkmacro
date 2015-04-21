package eu.ddmore.libpharmml.pkmacro.translation;

import java.util.ArrayList;
import java.util.List;

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
	
	
	public Translator(){

	}
	
	/**
	 * Parsing of the XML-binded macro objects to translatable macro objects.
	 * Equations are added by each fromMacro() method execution.
	 * @throws InvalidMacroException
	 */
	private List<AbstractMacro> parseMacros(PKMacroList list, CompartmentFactory cf, VariableFactory vf) throws InvalidMacroException{
		
		List<AbstractMacro> model = new ArrayList<AbstractMacro>();
		
		// Core macros
		for(PKMacro xmlMacro : list.getListOfMacro()){
			if(xmlMacro instanceof CompartmentMacro){
				model.add(Compartment.fromMacro(cf, vf, (CompartmentMacro) xmlMacro));
			}
			else if(xmlMacro instanceof PeripheralMacro){
				model.add(Peripheral.fromMacro(cf, vf, (PeripheralMacro) xmlMacro));
			}
		}
		
		// Targetted macros
		for(PKMacro xmlMacro : list.getListOfMacro()){
			if(xmlMacro instanceof AbsorptionOralMacro){
				model.add(Absorption.fromMacro(cf, vf, (AbsorptionOralMacro) xmlMacro));
			}
			else if(xmlMacro instanceof IVMacro){
				model.add(IV.fromMacro(cf, vf, (IVMacro) xmlMacro));
			}
			else if(xmlMacro instanceof TransferMacro){
				model.add(Transfer.fromMacro(cf, vf, (TransferMacro) xmlMacro));
			}
			else if(xmlMacro instanceof EliminationMacro){
				model.add(Elimination.fromMacro(cf, vf, (EliminationMacro) xmlMacro));
			}
			else if(xmlMacro instanceof EffectMacro){
				model.add(Effect.fromMacro(cf, vf, (EffectMacro) xmlMacro));
			}
			else if(xmlMacro instanceof DepotMacro){
				model.add(Depot.fromMacro(cf, vf, (DepotMacro) xmlMacro));
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
		
		for(CommonVariableDefinition var : vf.getDefinedVariables()){
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
