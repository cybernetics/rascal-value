package org.eclipse.imp.pdb.test.persistent;

import org.eclipse.imp.pdb.facts.impl.persistent.ValueFactory1;
import org.eclipse.imp.pdb.test.BaseTestBasicValues;

public class TestBasicValues extends BaseTestBasicValues {
	
	@Override
	protected void setUp() throws Exception {
		super.setUp(ValueFactory1.getInstance());
	}
}
