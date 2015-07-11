/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.parallax.server.common.cloudsession.db.dao.ConfirmTokenDao;
import com.parallax.server.common.cloudsession.db.dao.UserDao;
import com.parallax.server.common.cloudsession.db.generated.tables.records.ConfirmtokenRecord;
import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;
import com.parallax.server.common.cloudsession.db.test.tables.records.ConfirmtokenRecordMock;
import com.parallax.server.common.cloudsession.db.test.tables.records.UserRecordMock;
import com.parallax.server.common.cloudsession.exceptions.InsufficientBucketTokensException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserIdException;
import com.parallax.server.common.cloudsession.service.BucketService;
import com.parallax.server.common.cloudsession.service.MailService;
import java.sql.Timestamp;
import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author Michel
 */
public class ConfirmTokenServiceImplTest {

    private ConfirmTokenServiceImpl confirmTokenService;

    private MailService mailServiceMock;
    private BucketService bucketServiceMock;
    private ConfirmTokenDao confirmTokenDaoMock;
    private UserDao userDaoMock;

    @Before
    public void setUp() {
        mailServiceMock = Mockito.mock(MailService.class);
        bucketServiceMock = Mockito.mock(BucketService.class);
        confirmTokenDaoMock = Mockito.mock(ConfirmTokenDao.class);
        userDaoMock = Mockito.mock(UserDao.class);

        confirmTokenService = new ConfirmTokenServiceImpl();

        confirmTokenService.setMailService(mailServiceMock);
        confirmTokenService.setBucketService(bucketServiceMock);
        confirmTokenService.setConfirmTokenDao(confirmTokenDaoMock);
        confirmTokenService.setUserDao(userDaoMock);
    }

    // Untestable due to delete
    //@Test
    public void isValidTokenTestValid() {
        ConfirmtokenRecord confirmtokenRecord = new ConfirmtokenRecordMock();
        confirmtokenRecord.setValidity(new Timestamp(new Date().getTime() + 1000));

        Mockito.when(confirmTokenDaoMock.getConfirmToken("test")).thenReturn(confirmtokenRecord);

        boolean valid = confirmTokenService.isValidConfirmToken("test");

        if (!valid) {
            Assert.fail("Token should be valid");
        }
    }

    // Untestable due to delete
    //@Test
    public void isValidTokenTestUnknown() {
        ConfirmtokenRecord confirmtokenRecord = new ConfirmtokenRecordMock();

        Mockito.when(confirmTokenDaoMock.getConfirmToken("test2")).thenReturn(confirmtokenRecord);

        boolean valid = confirmTokenService.isValidConfirmToken("test");

        if (valid) {
            Assert.fail("Token should not be known");
        }
    }

    // Untestable due to delete
    //@Test
    public void isValidTokenTestInvalid() {
        ConfirmtokenRecord confirmtokenRecord = new ConfirmtokenRecordMock();
        confirmtokenRecord.setValidity(new Timestamp(new Date().getTime() - 1000));

        Mockito.when(confirmTokenDaoMock.getConfirmToken("test")).thenReturn(confirmtokenRecord);

        boolean valid = confirmTokenService.isValidConfirmToken("test");

        if (valid) {
            Assert.fail("Token should be invalid");
        }
    }

    @Test
    public void createResetTokenTestSuccess() {
        UserRecord userRecord = new UserRecordMock();
        userRecord.setConfirmed(false);
        ConfirmtokenRecord confirmtokenRecord = new ConfirmtokenRecordMock();

        try {
            Mockito.when(userDaoMock.getUser(2L)).thenReturn(userRecord);
        } catch (UnknownUserIdException ex) {
            Assert.fail("Setting mock return value should not throw an exception");
        }
        Mockito.when(confirmTokenDaoMock.createConfirmToken(2L)).thenReturn(confirmtokenRecord);

        try {
            ConfirmtokenRecord returnedToken = confirmTokenService.createConfirmToken("SERVER", 2L);
            Assert.assertSame(confirmtokenRecord, returnedToken);
        } catch (InsufficientBucketTokensException ex) {
            Assert.fail("There should be enough tokens in the bucket");
        } catch (UnknownUserIdException uuie) {
            Assert.fail("The user should be known");
        }
    }

    @Test
    public void createResetTokenTestAlreadyConfirmed() {
        UserRecord userRecord = new UserRecordMock();
        userRecord.setConfirmed(true);
        ConfirmtokenRecord confirmtokenRecord = new ConfirmtokenRecordMock();

        // Set
        try {
            Mockito.when(userDaoMock.getUser(2L)).thenReturn(userRecord);
        } catch (UnknownUserIdException ex) {
            Assert.fail("Setting mock return value should not throw an exception");
        }
        Mockito.when(confirmTokenDaoMock.createConfirmToken(2L)).thenReturn(confirmtokenRecord);

        try {
            // Execute
            ConfirmtokenRecord returnedToken = confirmTokenService.createConfirmToken("SERVER", 2L);

            // Test
            Assert.assertNull(returnedToken);
        } catch (InsufficientBucketTokensException ex) {
            Assert.fail("There should be enough tokens in the bucket");
        } catch (UnknownUserIdException uuie) {
            Assert.fail("The user should be known");
        }
    }

    @Test
    public void createResetTokenTestUnkownUser() {
        ConfirmtokenRecord confirmtokenRecord = new ConfirmtokenRecordMock();

        // Set
        try {
            Mockito.when(userDaoMock.getUser(2L)).thenThrow(new UnknownUserIdException(2L));
        } catch (UnknownUserIdException ex) {
            Assert.fail("Setting mock return value should not throw an exception");
        }
        Mockito.when(confirmTokenDaoMock.createConfirmToken(2L)).thenReturn(confirmtokenRecord);

        try {
            // Execute
            ConfirmtokenRecord returnedToken = confirmTokenService.createConfirmToken("SERVER", 2L);

            // Test
            Assert.fail("User id should not be known");
        } catch (InsufficientBucketTokensException ex) {
            Assert.fail("There should be enough tokens in the bucket");
        } catch (UnknownUserIdException uuie) {
        }
    }

    @Test
    public void createResetTokenTestInsufficientTokens() {
        UserRecord userRecord = new UserRecordMock();
        userRecord.setConfirmed(true);
        ConfirmtokenRecord confirmtokenRecord = new ConfirmtokenRecordMock();

        // Set
        try {
            Mockito.when(userDaoMock.getUser(2L)).thenReturn(userRecord);
        } catch (UnknownUserIdException ex) {
            Assert.fail("Setting mock return value should not throw an exception");
        }
        try {
            Mockito.doThrow(new InsufficientBucketTokensException()).when(bucketServiceMock).consumeTokensInternal(2L, "email-confirm", 1);
        } catch (InsufficientBucketTokensException ex) {
            Assert.fail("Setting mock return value should not throw an exception");
        }
        Mockito.when(confirmTokenDaoMock.createConfirmToken(2L)).thenReturn(confirmtokenRecord);

        try {
            // Execute
            ConfirmtokenRecord returnedToken = confirmTokenService.createConfirmToken("SERVER", 2L);

            // Test
            Assert.fail("There should NOT be enough tokens in the bucket");
        } catch (InsufficientBucketTokensException ex) {

        } catch (UnknownUserIdException uuie) {
            Assert.fail("The user should be known");
        }
    }

}
