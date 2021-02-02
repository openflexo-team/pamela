package org.openflexo.pamela.perf;

/**
 * An operation that needs to be run. Different operations can provide performance measure to compare PAMELA against traditional classes.
 * 
 * @author Guillaume
 * 
 */
public interface TestRunnable {
	/**
	 * The operation that will be executed.
	 * 
	 * @return the root object of the model (in order to keep a reference to the model and avoid the GC to garbage collect the memory before
	 *         the memory footprint has been computed).
	 */
	public Object run();
}
