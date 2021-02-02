package org.openflexo.pamela.perf;

import java.lang.management.ManagementFactory;

/**
 * Class of tests to measure the performance of pamela vs regular written classes.
 * 
 * Tests are made of:
 * <ol>
 * <li>A model interface and a default implementation</li>
 * <li>Test runnables: one kind of operation performed once with a pamela implementation and once with regular classes</li>
 * <li>Tests: which simply executes the different runnables in a given order</li>
 * </ol>
 * The execution time and the used memory are automatically computed and printed to the error console. Theses measures are to be taken with
 * great care since execution time can always be influenced by the workload of the computer on which it is executed, while the memory
 * measure can always be influenced by the GC running in a separate thread.
 * 
 * @author Guillaume
 * 
 */
public class PerformanceTests {

	/**
	 * The result of a TestRunnable.
	 * 
	 * @author Guillaume
	 * 
	 */
	public static class TestRunnableResult {
		public Object returned;
		public long usedMemory;
		public long usedTime;
	}

	/**
	 * Runs a TestRunnable <code>runnable</code> and return result of computation
	 * 
	 * @param runnable
	 *            the TestRunnable to run
	 * @return the TestResult, ie, time execution, memory footprint and the root object of the model
	 */
	public static TestRunnableResult runRunnable(TestRunnable runnable) {
		TestRunnableResult result = new TestRunnableResult();
		long startMem, endMem, start, end;
		startMem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		start = System.currentTimeMillis();
		result.returned = runnable.run();
		end = System.currentTimeMillis();
		result.usedTime = end - start;
		endMem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
		result.usedMemory = endMem - startMem;

		System.err.println("Runnable took: " + result.usedTime + " ms");
		System.err.println("Runnable eats: " + result.usedMemory + " bytes");

		return result;
	}

	/**
	 * 
	 * @param runnable
	 *            the test runnable to execute.
	 */
	public static void compareRunnable(TestRunnable runnable1, TestRunnable runnable2) {
		long r2Time = 0, r2Mem = 0, r1Time = 0, r1Mem = 0;
		for (int i = 0; i < 10; i++) {
			TestRunnableResult result = runRunnable(runnable1);
			if (i > 0) {
				r1Time += result.usedTime;
				r1Mem += result.usedMemory;
			}
			System.gc();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}

			result = runRunnable(runnable2);
			if (i > 0) {
				r2Time += result.usedTime;
				r2Mem += result.usedMemory;
			}
			result = null;
			System.gc();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}

		}
		System.err.println("Test " + runnable1.getClass().getSimpleName() + " against " + runnable2.getClass().getSimpleName());
		System.err.println("\tRunnable1 took: " + r1Time + "ms");
		System.err.println("\tRunnable2 took: " + r2Time + "ms");
		System.err.println("\tRunnable2 is " + (double) r2Time / r1Time + " slower than Runnable1");
		System.err.println("\tRunnable1 took: " + r1Mem + " bytes");
		System.err.println("\tRunnable2 took: " + r2Mem + " bytes");
		System.err.println("\tRunnable2 eats " + (double) r2Mem / r1Mem + " more memory than Runnable1");
	}

}
