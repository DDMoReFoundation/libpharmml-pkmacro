package eu.ddmore.libpharmml.pkmacro.translation;

import java.util.HashMap;
import java.util.Map;

import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

class CompartmentFactory {
	
	private final Map<String, AbstractCompartment> map_compartments;
	
	CompartmentFactory() {
		map_compartments = new HashMap<String, AbstractCompartment>();
	}
	
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
	
	Integer compartmentsSize(){
		return map_compartments.size();
	}
	
	boolean compartmentExists(String cmt){
		return map_compartments.containsKey(cmt);
	}

}
