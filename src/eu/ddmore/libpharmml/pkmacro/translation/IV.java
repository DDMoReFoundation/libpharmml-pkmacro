package eu.ddmore.libpharmml.pkmacro.translation;

import eu.ddmore.libpharmml.dom.commontypes.Scalar;
import eu.ddmore.libpharmml.dom.modeldefn.pkmacro.IVMacro;
import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

class IV extends AbstractMacro implements InputSource {
	
	protected final AbstractCompartment target;
	protected final Scalar adm;
	
	protected IV(AbstractCompartment target, Scalar adm) {
		super();
		this.target = target;
		this.adm = adm;
	}
	
	static IV fromMacro(CompartmentFactory cf, VariableFactory vf, IVMacro macro) throws InvalidMacroException{
		ParamResolver pr = new ParamResolver(macro);
		
		Scalar adm;
		if(pr.contains("type")){
			adm = pr.getValue("type", Scalar.class);
		} else {
			adm = pr.getValue("adm", Scalar.class);
		}
		
		AbstractCompartment target = cf.getCompartment(pr.getValue("cmt").getContent().toString());
		
		return new IV(target, adm);
	}

	@Override
	public void generateInputs(InputList inputList) {		
		inputList.createInput(InputType.IV, adm, target.getAmount());
	}

}
