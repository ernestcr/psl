/*
 * This file is part of the PSL software.
 * Copyright 2011 University of Maryland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.umd.cs.psl.reasoner.admm;

import java.util.Iterator;

import edu.umd.cs.psl.model.kernel.GroundConstraintKernel;
import edu.umd.cs.psl.reasoner.function.AtomFunctionVariable;
import edu.umd.cs.psl.reasoner.function.ConstraintTerm;
import edu.umd.cs.psl.reasoner.function.FunctionComparator;
import edu.umd.cs.psl.reasoner.function.FunctionSingleton;
import edu.umd.cs.psl.reasoner.function.FunctionSum;
import edu.umd.cs.psl.reasoner.function.FunctionSummand;
import edu.umd.cs.psl.reasoner.function.FunctionTerm;

class LinearConstraintWrapper extends HyperplaneWrapper {
	
	private final FunctionComparator comparator;
	
	LinearConstraintWrapper(ADMMReasoner reasoner, FunctionSum hyperplane, FunctionComparator comparator, double value) {
		super(reasoner, hyperplane);
		this.comparator = comparator;
	}
	
	@Override
	protected void minimize() {
		/* Initializes scratch data */
		double a[] = new double[x.size()];
		
		/* First minimizes without regard for any constraints, then projects on to a box */
		for (int i = 0; i < a.length; i++) {
			a[i] = reasoner.z.get(zIndices.get(i)) - y.get(i) / reasoner.stepSize;
			
			if (a[i] < 0)
				a[i] = 0;
			else if (a[i] > 1)
				a[i] = 1;
		}
		
		/* If it doesn't satisfy the constraint, projects it */
		
		/* If still outside the simplex, minimizes along the boundary */
		if (a.length > 1) {
			if (a[0] + a[1] > 1) {
				reasoner.linearConstraintSolvedFace++;
				a[0] = y.get(0) - reasoner.stepSize * reasoner.z.get(zIndices.get(0));
				a[0] -= y.get(1) - reasoner.stepSize * reasoner.z.get(zIndices.get(1));
				a[0] -= reasoner.stepSize;
				a[0] /= -2 * reasoner.stepSize;
			
				/* Checks boundaries and sets a[1] */
				if (a[0] > 1) {
					a[0] = 1;
					a[1] = 0;
				}
				else if (a[0] < 0) {
					a[0] = 0;
					a[1] = 1;
				}
				else {
					a[1] = 1 - a[0];
				}
			}
			else
				reasoner.linearConstraintSolvedInterior++;
		}
		
		/* Updates the local primal variables */
		for (int i = 0; i < a.length; i++)
			x.set(i, a[i]);
	}
}
