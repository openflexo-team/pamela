package {$plainPackageName};

import org.openflexo.pamela.perf.PerformanceTests;
import org.openflexo.pamela.perf.TestRunnable;

/**
  * Automatically generated
  */
public class Main {

	public static int entitiesCreated = 0;

	public static void main(String[] args) {
		PerformanceTests.runRunnable(new MainRunnable());
	}

	public static class MainRunnable implements TestRunnable {

		@Override
		public Object run() {
			{$entities.get(0).name} returned = {$entities.get(0).name}();
			System.out.println("Created " + entitiesCreated + " entities");
			return returned;
		}

	}


{$plainInternalCode}

}

