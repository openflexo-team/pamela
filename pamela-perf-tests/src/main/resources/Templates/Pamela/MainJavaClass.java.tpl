package {$pamelaPackageName};

import org.openflexo.pamela.exceptions.ModelDefinitionException;
import org.openflexo.pamela.factory.ModelFactory;
import org.openflexo.pamela.perf.PerformanceTests;
import org.openflexo.pamela.perf.TestRunnable;

/**
  * Automatically generated
  */
public class PamelaCode {

	public static int entitiesCreated = 0;

	public static ModelFactory modelFactory;

	static {
		try {
			modelFactory = new ModelFactory({$entities.get(0).name}.class) {
				@Override
				public <I> I newInstance(Class<I> implementedInterface) {
					entitiesCreated++;
					return super.newInstance(implementedInterface);
				}
			};
		} catch (ModelDefinitionException e) {
			e.printStackTrace();
		}
	}

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


{$pamelaInternalCode}

}

