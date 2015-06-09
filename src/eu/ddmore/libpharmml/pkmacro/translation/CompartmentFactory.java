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

import java.util.HashMap;
import java.util.Map;

import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

class CompartmentFactory {
	
	private final Map<Integer, AbstractCompartment> map_compartments;
	
	CompartmentFactory() {
		map_compartments = new HashMap<Integer, AbstractCompartment>();
	}
	
	AbstractCompartment getCompartment(Integer cmt) throws InvalidMacroException{
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
	
	Integer highestCompartmentId(){
		Integer highest = 0;
		for(Integer intId : map_compartments.keySet()){
			if(intId > highest){
				highest = intId;
			}
		}
		return highest;
	}
	
	boolean compartmentExists(Integer cmt){
		return map_compartments.containsKey(cmt);
	}

}
