/*
 * Nextcloud - Android Client
 *
 * SPDX-FileCopyrightText: 2019-2023 Tobias Kaminsky <tobias@kaminsky.me>
 * SPDX-License-Identifier: AGPL-3.0-or-later OR GPL-2.0-only
 */
package com.nextcloud.client.account;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Bundle;

import com.nextcloud.client.preferences.AppPreferences;
import com.nextcloud.client.preferences.AppPreferencesImpl;
import com.owncloud.android.AbstractOnServerIT;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.lib.common.accounts.AccountUtils;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class UserAccountManagerImplTest extends AbstractOnServerIT {

    private AccountManager accountManager;

    @Before
    public void setUp() {
        accountManager = AccountManager.get(targetContext);
    }

    @Test
    public void updateOneAccount() {
        AppPreferences appPreferences = AppPreferencesImpl.fromContext(targetContext);
        UserAccountManagerImpl sut = new UserAccountManagerImpl(targetContext, accountManager);
        assertEquals(1, sut.getAccounts().length);
        assertFalse(appPreferences.isUserIdMigrated());

        Account account = sut.getAccounts()[0];

        // for testing remove userId
        accountManager.setUserData(account, AccountUtils.Constants.KEY_USER_ID, null);
        assertNull(accountManager.getUserData(account, AccountUtils.Constants.KEY_USER_ID));

        boolean success = sut.migrateUserId();
        assertTrue(success);

        Bundle arguments = androidx.test.platform.app.InstrumentationRegistry.getArguments();
        String userId = arguments.getString("TEST_SERVER_USERNAME");

        // assume that userId == loginname (as we manually set it)
        assertEquals(userId, accountManager.getUserData(account, AccountUtils.Constants.KEY_USER_ID));
    }

    @Test
    public void checkName() {
        UserAccountManagerImpl sut = new UserAccountManagerImpl(targetContext, accountManager);

        Account owner = new Account("John@nextcloud.local", "nextcloud");
        Account account1 = new Account("John@nextcloud.local", "nextcloud");
        Account account2 = new Account("john@nextcloud.local", "nextcloud");

        OCFile file1 = new OCFile("/test1.pdf");
        file1.setOwnerId("John");

        assertTrue(sut.accountOwnsFile(file1, owner));
        assertTrue(sut.accountOwnsFile(file1, account1));
        assertTrue(sut.accountOwnsFile(file1, account2));

        file1.setOwnerId("john");
        assertTrue(sut.accountOwnsFile(file1, owner));
        assertTrue(sut.accountOwnsFile(file1, account1));
        assertTrue(sut.accountOwnsFile(file1, account2));
    }
}
