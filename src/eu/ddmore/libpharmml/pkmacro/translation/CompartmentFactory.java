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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.ddmore.libpharmml.pkmacro.exceptions.InvalidMacroException;

class CompartmentFactory {
	
	private final Map<Integer, AbstractCompartment> map_compartments;
	
	CompartmentFactory() {
		map_compartments = new HashMap<Integer, AbstractCompartment>();
	}
	
	/**
	 * Gets the compartment corresponding to the given compartment number. If the compartment doesn't
	 * exist, an {@link InvalidMacroException} is thrown.
	 * @param cmt Compartment number, usually defined by the "cmt" macro parameter.
	 * @return The compartment defined by the given index.
	 * @throws InvalidMacroException If the compartment doesn't exist.
	 */
	AbstractCompartment getCompartment(Integer cmt) throws InvalidMacroException{
		if(map_compartments.containsKey(cmt)){
			return map_compartments.get(cmt);
		} else {
			throw new InvalidMacroException("Compartment \""+cmt+"\" does not exist");
		}
	}
	
	/**
	 * Adds the provided compartment to the factory so it can be indexed. If a compartment with the same
	 * cmt number already exists within this factory, an {@link InvalidMacroException} is thrown. One can use
	 * the method {@link #compartmentExists(Integer)} to check if a compartment with the same index already
	 * exists.
	 * @param comp The compartment to be indexed.
	 * @throws InvalidMacroException If a compartment with the same index exists.
	 */
	void addCompartment(AbstractCompartment comp) throws InvalidMacroException{
		AbstractCompartment previous = map_compartments.put(comp.getCmt(), comp);
		if(previous != null){
			throw new InvalidMacroException("Compartment \""+comp.getCmt()+"\" is duplicated");
		}
	}
	
	/**
	 * Gets the number of compartments in this factory.
	 * @return The number of compartments in this factory.
	 */
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
	
	Integer lowestAvailableId(){
		Set<Integer> keys = map_compartments.keySet();
		for(Integer i = 1;i<10000;i++){ // a safe limit
			if(!keys.contains(i)){
				return i;
			}
		}
		return null;
	}
	
	/**
	 * Checks if a compartment with the given cmt index has already been indexed.
	 * @param cmt The cmt parameter value of the compartment.
	 * @return true if the compartment already exists, else false.
	 */
	boolean compartmentExists(Integer cmt){
		return map_compartments.containsKey(cmt);
	}

}
