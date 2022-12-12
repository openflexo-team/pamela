/*
 * Copyright (c) 2013-2017, Openflexo
 *
 * This file is part of Flexo-foundation, a component of the software infrastructure
 * developed at Openflexo.
 *
 * Openflexo is dual-licensed under the European Union Public License (EUPL, either
 * version 1.1 of the License, or any later version ), which is available at
 * https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 * and the GNU General Public License (GPL, either version 3 of the License, or any
 * later version), which is available at http://www.gnu.org/licenses/gpl.html .
 *
 * You can redistribute it and/or modify under the terms of either of these licenses
 *
 * If you choose to redistribute it and/or modify under the terms of the GNU GPL, you
 * must include the following additional permission.
 *
 *           Additional permission under GNU GPL version 3 section 7
 *           If you modify this Program, or any covered work, by linking or
 *           combining it with software containing parts covered by the terms
 *           of EPL 1.0, the licensors of this Program grant you additional permission
 *           to convey the resulting work.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.openflexo.org/license.html for details.
 *
 *
 * Please contact Openflexo (openflexo-contacts@openflexo.org)
 * or visit www.openflexo.org if you need additional information.
 *
 */

package org.openflexo.pamela.test.jml;

import org.openflexo.pamela.AccessibleProxyObject;
import org.openflexo.pamela.MonitorableProxyObject;
import org.openflexo.pamela.annotations.Getter;
import org.openflexo.pamela.annotations.ImplementationClass;
import org.openflexo.pamela.annotations.Initializer;
import org.openflexo.pamela.annotations.ModelEntity;
import org.openflexo.pamela.annotations.Setter;
import org.openflexo.pamela.annotations.jml.Assignable;
import org.openflexo.pamela.annotations.jml.Ensures;
import org.openflexo.pamela.annotations.jml.Invariant;
import org.openflexo.pamela.annotations.jml.MethodParameter;
import org.openflexo.pamela.annotations.jml.Requires;
import org.openflexo.pamela.annotations.monitoring.MonitoredEntity;
import org.openflexo.pamela.test.jml.BankAccount.BankAccountImpl;

@ModelEntity
@ImplementationClass(BankAccountImpl.class)
@MonitoredEntity
@Invariant("(balance >= 0) && (balance <= 1000)")
public interface BankAccount extends AccessibleProxyObject, MonitorableProxyObject {

	static final int MAX_BALANCE = 1000;
	static final String BALANCE = "balance";
	static final String LOCKED = "isLocked";

	@Getter(value = BALANCE, defaultValue = "0")
	int getBalance();

	@Setter(BALANCE)
	@Requires("aBalance >= 0")
	@Ensures("balance >= 0")
	public void setBalance(@MethodParameter("aBalance") int aBalance);

	@Getter(value = LOCKED, defaultValue = "false")
	boolean isLocked();

	@Setter(LOCKED)
	public void setLocked(boolean locked);

	@Initializer
	@Assignable(BALANCE)
	@Ensures("balance==0")
	public BankAccount init();

	@Requires("amount>0")
	@Ensures("balance == /old(balance)+amount")
	@Assignable(BALANCE)
	public void credit(@MethodParameter("amount") int amount);

	@Requires("(amount>0) && (amount <= balance) && (!isLocked)")
	@Ensures("balance == /old(balance)-amount")
	@Assignable(BALANCE)
	public void debit(@MethodParameter("amount") int amount);

	@Ensures("(isLocked==true)")
	public void lockAccount();

	public static abstract class BankAccountImpl implements BankAccount {

		@Override
		public void credit(int amount) {
			System.out.println("****** credit with " + amount);
			// Thread.dumpStack();
			setBalance(getBalance() + amount);
		}

		@Override
		public void debit(int amount) {
			// Thread.dumpStack();
			setBalance(getBalance() - amount);
			System.out.println("****** debit with " + amount);
		}

		@Override
		public void lockAccount() {
			setLocked(true);
		}

	}
}
