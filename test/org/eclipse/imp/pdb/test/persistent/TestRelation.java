/*******************************************************************************
* Copyright (c) 2009 Centrum Wiskunde en Informatica (CWI)
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Arnold Lankamp - interfaces and implementation
*******************************************************************************/
package org.eclipse.imp.pdb.test.persistent;

import org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory1;
import org.eclipse.imp.pdb.test.BaseTestRelation;

/**
 * @author Arnold Lankamp
 */
public class TestRelation extends BaseTestRelation{
	
	protected void setUp() throws Exception{
		super.setUp(ValueFactory1.getInstance());
	}
}
