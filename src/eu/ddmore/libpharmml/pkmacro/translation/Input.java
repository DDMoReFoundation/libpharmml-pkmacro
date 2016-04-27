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

import eu.ddmore.libpharmml.dom.commontypes.CommonVariableDefinition;
import eu.ddmore.libpharmml.dom.commontypes.DerivativeVariable;
import eu.ddmore.libpharmml.dom.commontypes.Scalar;
import eu.ddmore.libpharmml.dom.commontypes.VariableDefinition;
import eu.ddmore.libpharmml.dom.maths.Operand;

/**
 * Class defining a PK input.
 */
public class Input {
	
	private final int number;
	private final InputType type;
	private final Scalar adm;
	private final CommonVariableDefinition target;
	
	private final Operand tlag;
	private final Operand p;

	Input(int number, InputType type, Scalar adm, CommonVariableDefinition target) {
		this(number,type,adm,target,null,null);
	}
	
	Input(int number, InputType type, Scalar adm, CommonVariableDefinition target, Operand tlag, Operand p) {
		this.number = number;
		this.type = type;
		this.adm = adm;
		this.target = target;
		this.tlag = tlag;
		this.p = p;
	}

	/**
	 * Gets the number of this input within the list it comes from.
	 * @return An integer starting from 1 to *.
	 */
	public int getNumber() {
		return number;
	}

	/**
	 * Gets the type of input, either IV or ORAL.
	 * @return An {@link InputType} item.
	 */
	public InputType getType() {
		return type;
	}

	/**
	 * Gets the type of administration.
	 * @return The type of administration as a {@link Scalar}.
	 */
	public Scalar getAdm() {
		return adm;
	}

	/**
	 * Gets the target variable of the input.
	 * @return The target as a {@link VariableDefinition} or a {@link DerivativeVariable}.
	 */
	public CommonVariableDefinition getTarget() {
		return target;
	}
	
	/**
	 * Lag time before the absorption. May be null.
	 * @return The Tlag variable as an {@link Operand}.
	 */
	public Operand getTlag(){
		return tlag;
	}
	
	/**
	 * Final absorption of the absorbed amount. May be null.
	 * @return The p variable as an {@link Operand}.
	 */
	public Operand getP(){
		return p;
	}
	
	/**
	 * Returns a string representation of this input with the following format:<br>
	 * Input[&lt;n>] &lt;Type>, adm=&lt;i>, target=&lt;variable>
	 */
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Input["+number+"] "+type+", adm="+adm+", target="+target.getSymbId());
		if(tlag != null){
			sb.append("; Tlag="+tlag);
		}
		if(p != null){
			sb.append("; p="+p);
		}
		return sb.toString();
	}

}
