package org.rascalmpl.value.exceptions;

import org.rascalmpl.value.type.Type;

public class UnexpectedMapKeyTypeException extends UnexpectedTypeException {
	private static final long serialVersionUID = -914783577719833513L;

	public UnexpectedMapKeyTypeException(Type expected, Type got) {
		super(expected, got);
	}

}
