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

import eu.ddmore.libpharmml.dom.modeldefn.IndividualParameter;
import eu.ddmore.libpharmml.dom.modeldefn.PopulationParameter;

/**
 * Enumeration of the possible parameter types. The type determines if the {@link TransientParameter} objects are
 * exported as {@link IndividualParameter} or {@link PopulationParameter} at the end of the translation process.
 * 
 * @author Florent Yvon
 */
enum ParameterType {
	
	INDIVIDUAL,
	POPULATION;

}
