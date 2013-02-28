/* ===========================================================
 * TradeManager : a application to trade strategies for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Project Info:  org.trade
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Oracle, Inc.
 * in the United States and other countries.]
 *
 * (C) Copyright 2011-2011, by Simon Allen and Contributors.
 *
 * Original Author:  Simon Allen;
 * Contributor(s):   -;
 *
 * Changes
 * -------
 *
 */
package org.trade.broker.request;

import junit.framework.TestCase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.dao.Aspect;
import org.trade.core.dao.Aspects;
import org.trade.core.factory.ClassFactory;
import org.trade.core.util.CoreUtils;
import org.trade.dictionary.valuetype.AccountType;
import org.trade.dictionary.valuetype.Currency;
import org.trade.persistent.PersistentModel;
import org.trade.persistent.dao.AccountAllocation;
import org.trade.persistent.dao.FinancialAccount;
import org.trade.persistent.dao.Account;
import org.trade.ui.TradeAppLoadConfig;

/**
 */
public class TWSFinancialAccountRequestTest extends TestCase {

	private final static Logger _log = LoggerFactory
			.getLogger(TWSFinancialAccountRequestTest.class);
	private PersistentModel tradePersistentModel = null;

	/**
	 * Method setUp.
	 * 
	 * @throws Exception
	 */
	protected void setUp() throws Exception {
		TradeAppLoadConfig.loadAppProperties();
		tradePersistentModel = (PersistentModel) ClassFactory
				.getServiceForInterface(PersistentModel._persistentModel, this);
	}

	/**
	 * Method tearDown.
	 * 
	 * @throws Exception
	 */
	protected void tearDown() throws Exception {
		Aspects items = tradePersistentModel
				.findAspectsByClassName(FinancialAccount.class.getName());
		for (Aspect aspect : items.getAspect()) {
			tradePersistentModel.removeAspect(aspect);
		}
	}

	@Test
	public void testAliasRequest() {

		try {

			final TWSAccountAliasRequest request = new TWSAccountAliasRequest();
			final Aspects aspects = (Aspects) request
					.fromXML(Thread
							.currentThread()
							.getContextClassLoader()
							.getResourceAsStream(
									"org/trade/broker/request/aliases.xml"));

			for (Aspect aspect : aspects.getAspect()) {
				Account account = (Account) aspect;
				Account ta = tradePersistentModel.findAccountByNumber(account
						.getAccountNumber());
				if (null != ta) {
					account.setAlias(account.getAlias());
					tradePersistentModel.persistAspect(ta);
				} else {
					account.setAccountType(AccountType.INDIVIDUAL);
					account.setCurrency(Currency.USD);
					account.setName(account.getAccountNumber());
					tradePersistentModel.persistAspect(account);
				}
				_log.info("Aspect: \n" + CoreUtils.toFormattedXMLString(aspect));
			}

		} catch (Exception e) {
			TestCase.fail("Error :" + e.getMessage());
		}
	}

	@Test
	public void testGroupRequest() {

		try {

			final TWSGroupRequest request = new TWSGroupRequest();
			final Aspects aspects = (Aspects) request
					.fromXML(Thread
							.currentThread()
							.getContextClassLoader()
							.getResourceAsStream(
									"org/trade/broker/request/groups.xml"));

			for (Aspect aspect : aspects.getAspect()) {
				FinancialAccount account = (FinancialAccount) aspect;
				for (AccountAllocation item : account.getAccountAllocation()) {
					FinancialAccount parent = tradePersistentModel
							.findFinancialAccountByGroupName(item
									.getFinancialAccount().getGroupName());
					if (null == parent) {
						parent = (FinancialAccount) tradePersistentModel
								.persistAspect(item.getFinancialAccount());
					} else {
						if (!parent.getMethod().equals(
								item.getFinancialAccount().getMethod())) {
							parent.setMethod(item.getFinancialAccount()
									.getMethod());
							parent = (FinancialAccount) tradePersistentModel
									.persistAspect(parent);
						}

					}
					item.setFinancialAccount(parent);
					tradePersistentModel.persistAspect(item);
				}

				_log.info("Aspect: \n" + CoreUtils.toFormattedXMLString(aspect));
			}

		} catch (Exception e) {
			TestCase.fail("Error :" + e.getMessage());
		}
	}

	@Test
	public void testAllocationRequest() {

		try {

			final TWSAllocationRequest request = new TWSAllocationRequest();
			final Aspects aspects = (Aspects) request.fromXML(Thread
					.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(
							"org/trade/broker/request/allocation.xml"));

			for (Aspect aspect : aspects.getAspect()) {
				FinancialAccount account = (FinancialAccount) aspect;
				for (AccountAllocation item : account.getAccountAllocation()) {
					FinancialAccount parent = tradePersistentModel
							.findFinancialAccountByProfileName(item
									.getFinancialAccount().getProfileName());
					if (null == parent) {
						parent = (FinancialAccount) tradePersistentModel
								.persistAspect(item.getFinancialAccount());
					} else {
						if (!parent.getType().equals(
								item.getFinancialAccount().getType())) {
							parent.setType(item.getFinancialAccount().getType());
							parent = (FinancialAccount) tradePersistentModel
									.persistAspect(parent);
						}

					}
					item.setFinancialAccount(parent);
					tradePersistentModel.persistAspect(item);
				}
				_log.info("Aspect: \n" + CoreUtils.toFormattedXMLString(aspect));
			}

		} catch (Exception e) {
			TestCase.fail("Error :" + e.getMessage());
		}
	}
}
