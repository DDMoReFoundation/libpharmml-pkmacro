package eu.ddmore.libpharmml.pkmacro.standalone;

import java.io.FileInputStream;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import eu.ddmore.libpharmml.IPharmMLResource;
import eu.ddmore.libpharmml.PharmMlFactory;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.PharmMLElement;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.modeldefn.IndividualParameter;
import eu.ddmore.libpharmml.dom.modeldefn.Parameter;
import eu.ddmore.libpharmml.dom.modeldefn.PopulationParameter;
import eu.ddmore.libpharmml.dom.modeldefn.StructuralModel;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.PKMacro;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.PKMacroList;
import eu.ddmore.libpharmml.impl.IdFactoryImpl;
import eu.ddmore.libpharmml.impl.MarshalListener;
import eu.ddmore.libpharmml.impl.PharmMLVersion;
import eu.ddmore.libpharmml.impl.XMLFilter;
import eu.ddmore.libpharmml.pkmacro.translation.MacroOutput;
import eu.ddmore.libpharmml.pkmacro.translation.Translator;
import eu.ddmore.libpharmml.pkmacro.translation.Utils;

public class TranslateToString {

	public static void main(String[] args) throws Exception {
				
		FileInputStream in = new FileInputStream(args[0]);
		IPharmMLResource resource = PharmMlFactory.getInstance().createLibPharmML().createDomFromResource(in);
		
		StructuralModel sm;
		
		sm = resource.getDom().getModelDefinition().getListOfStructuralModel().get(0);
		if(sm == null) throw new NullPointerException();
		
		println("Input:");
		printStructuralModel(sm);
		
		Translator tl = new Translator();
		MacroOutput output = tl.translate(sm, sm.getUnmarshalVersion());
		
		StructuralModel tl_sm = output.getStructuralModel();
		
		System.out.println("Variables:");
		printStructuralModel(tl_sm);
		
		JAXBContext context = JAXBContext.newInstance("eu.ddmore.libpharmml.dom:eu.ddmore.libpharmml.dom.uncertml");
		Marshaller m = context.createMarshaller();
		setMarshalVersion(tl_sm, PharmMLVersion.DEFAULT);
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		MarshalListener listener = new MarshalListener(PharmMLVersion.DEFAULT, new IdFactoryImpl());
		listener.autosetId(false);
		m.setListener(listener);
		JAXBElement<StructuralModel> jaxbEl = new JAXBElement<StructuralModel>(new QName(XMLFilter.NS_DEFAULT_MDEF, "StructuralModel"), StructuralModel.class, tl_sm);
		m.marshal(jaxbEl, System.out);
					
	}
	
	private static void printStructuralModel(StructuralModel sm) {
		for(PharmMLElement var : sm.getListOfStructuralModelElements()){
			if(var instanceof VariableDefinition){
				System.out.println(Utils.variableToString((VariableDefinition) var));
			} else if(var instanceof DerivativeVariable){
				System.out.println(Utils.variableToString((DerivativeVariable) var));
			} else if(var instanceof PopulationParameter){
				System.out.println(Utils.variableToString((PopulationParameter) var));
			} else if(var instanceof IndividualParameter){
				System.out.println(Utils.variableToString((IndividualParameter) var));
			} else if(var instanceof Parameter){
				System.out.println(Utils.variableToString((Parameter) var));
			} else if(var instanceof PKMacroList){
				for(PKMacro macro : ((PKMacroList) var).getListOfMacro()){
					System.out.println(macro.toString());
				}
			} else {
				System.out.println(String.valueOf(var));
			}
		}
		println("------------------------------------------");
	}

	private static void setMarshalVersion(PharmMLElement node, PharmMLVersion version){
		node.setMarshalVersion(version);
		Enumeration<TreeNode> children = node.children();
		while(children.hasMoreElements()){
			TreeNode child = children.nextElement();
			if(child instanceof PharmMLElement){
				setMarshalVersion((PharmMLElement) child, version);
			}
		}
	}
	
	private static void println(Object o){
		System.out.println(String.valueOf(o));
	}

}
