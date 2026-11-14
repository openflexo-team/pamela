package org.openflexo.pamela.exceptions;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

/**
 * A {@link ModelExecutionException} keeps the cause it wraps (PAMELA-D-6).
 */
public class TestModelExecutionException {

	@Test
	public void keepsTheCause() {
		NullPointerException npe = new NullPointerException("inner");
		ModelExecutionException e = new ModelExecutionException(npe);
		assertSame(npe, e.getCause());
		assertTrue(e.getMessage(), e.getMessage().contains("inner"));
		assertSame(npe, new ModelExecutionException("m", npe).getCause());
	}

	@Test
	public void keepsTheTargetOfAnInvocationTargetException() {
		IllegalStateException target = new IllegalStateException("target");
		assertSame(target, new ModelExecutionException(new InvocationTargetException(target)).getCause());
	}
}
