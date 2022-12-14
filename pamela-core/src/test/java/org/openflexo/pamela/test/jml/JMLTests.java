package org.openflexo.pamela.test.jml;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.pamela.PamelaMetaModel;
import org.openflexo.pamela.factory.PamelaModelFactory;
import org.openflexo.pamela.jml.SpecificationsViolationException;
import org.openflexo.pamela.test.AbstractPAMELATest;

/**
 * Test JML annotations
 * 
 * @author sylvain
 *
 */
public class JMLTests extends AbstractPAMELATest {

	private PamelaModelFactory factory;
	private PamelaMetaModel pamelaMetaModel;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Override
	@Before
	public void setUp() throws Exception {
		pamelaMetaModel = new PamelaMetaModel(BankAccount.class);
		factory = new PamelaModelFactory(pamelaMetaModel);
	}

	@Override
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNormalBehaviour() throws Exception {
		BankAccount bankAccount = factory.newInstance(BankAccount.class);
		bankAccount.enableAssertionChecking();
		// bankAccount.toto();
		System.out.println("bankAccount=" + bankAccount);
		System.out.println("balance=" + bankAccount.getBalance());
		bankAccount.credit(10);
		bankAccount.debit(5);
		assertEquals(5, bankAccount.getBalance());
	}

	@Test
	public void testOverCredit() throws Exception {
		BankAccount bankAccount = factory.newInstance(BankAccount.class);
		bankAccount.enableAssertionChecking();
		// bankAccount.toto();
		System.out.println("bankAccount=" + bankAccount);
		System.out.println("balance=" + bankAccount.getBalance());
		bankAccount.credit(50);
		try {
			bankAccount.credit(5000);
			fail();
		} catch (SpecificationsViolationException e) {
			System.out.println("Expected: " + e.getMessage());
			e.printMethodStack();
			e.printStackTrace();
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testLocking() throws Exception {
		BankAccount bankAccount = factory.newInstance(BankAccount.class);
		bankAccount.enableAssertionChecking();
		// bankAccount.toto();
		System.out.println("bankAccount=" + bankAccount);
		System.out.println("balance=" + bankAccount.getBalance());
		bankAccount.credit(50);
		bankAccount.debit(10);
		bankAccount.lockAccount();
		try {
			bankAccount.debit(10);
			fail();
		} catch (SpecificationsViolationException e) {
			System.out.println("Expected: " + e.getMessage());
			e.printMethodStack();
			e.printStackTrace();
		} catch (Exception e) {
			fail();
		}
	}
}
