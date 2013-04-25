/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	 Paul Klint (Paul.Klint@cwi.nl) - added new ListRelation datatype
 *   Michael Steindorfer (Michael.Steindorfer@cwi.nl)
 * based on code by
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation

 *******************************************************************************/

package org.eclipse.imp.pdb.facts.impl.reference;

import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IListRelation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.impl.func.ListFunctions;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.eclipse.imp.pdb.facts.visitors.VisitorException;

public class ListRelation extends List implements IListRelation {

    /*package*/ ListRelation(Type elementType, java.util.List<IValue> content) {
        super(elementType, content);
    }

    @Override
	public int arity() {
        return getType().getArity();
    }

    @Override
	public IListRelation closure() {
        return (IListRelation) ListFunctions.closure(getValueFactory(), this);
    }

    @Override
	public IListRelation closureStar() {
        return (IListRelation) ListFunctions.closureStar(getValueFactory(), this);
    }

    @Override
	public IList carrier() {
        return ListFunctions.carrier(getValueFactory(), this);
    }

    @Override
	public IList domain() {
        return ListFunctions.domain(getValueFactory(), this);
    }

    @Override
	public IList range() {
        return ListFunctions.range(getValueFactory(), this);
    }

    @Override
	public <T> T accept(IValueVisitor<T> v) throws VisitorException {
        return v.visitListRelation(this);
    }

    @Override
	public Type getFieldTypes() {
        return getType().getFieldTypes();
    }

    @Override
	public IList select(int... fields) {
        return ListFunctions.project(getValueFactory(), this, fields);
    }

    @Override
	public IList selectByFieldNames(String... fields) {
        return ListFunctions.projectByFieldNames(getValueFactory(), this, fields);
    }

    @Override
	public IListRelation compose(IListRelation that) {
        return (IListRelation) ListFunctions.compose(getValueFactory(), this, that);
    }

}