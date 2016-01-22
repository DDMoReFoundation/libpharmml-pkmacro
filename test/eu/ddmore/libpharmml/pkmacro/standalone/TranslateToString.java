package eu.ddmore.libpharmml.pkmacro.standalone;

import java.io.FileInputStream;

import eu.ddmore.libpharmml.IPharmMLResource;
import eu.ddmore.libpharmml.PharmMlFactory;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.PharmMLElement;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.modeldefn.StructuralModel;
import eu.ddmore.libpharmml.pkmacro.translation.MacroOutput;
import eu.ddmore.libpharmml.pkmacro.translation.Translator;
import eu.ddmore.libpharmml.pkmacro.translation.Utils;

public class TranslateToString {

	public static void main(String[] args) throws Exception {
		
		FileInputStream in = new FileInputStream("/automount/vnas-homes_vol-vol_homes-homes/florent/git/libPharmML-PKMacro/examples/UseCase7.xml");
		IPharmMLResource resource = PharmMlFactory.getInstance().createLibPharmML().createDomFromResource(in);
		
		StructuralModel sm;
		
			sm = resource.getDom().getModelDefinition().getListOfStructuralModel().get(0);
			if(sm == null) throw new NullPointerException();
			
			Translator tl = new Translator();
			MacroOutput output = tl.translate(sm, sm.getUnmarshalVersion());
			
			StructuralModel tl_sm = output.getStructuralModel();
			
			System.out.println("Variable:");
			for(PharmMLElement var : tl_sm.getListOfStructuralModelElements()){
				if(var instanceof VariableDefinition){
					System.out.println(Utils.variableToString((VariableDefinition) var));
				} else if(var instanceof DerivativeVariable){
					System.out.println(Utils.variableToString((DerivativeVariable) var));
				} else {
					System.out.println(String.valueOf(var));
				}
			}
					
	}

}
