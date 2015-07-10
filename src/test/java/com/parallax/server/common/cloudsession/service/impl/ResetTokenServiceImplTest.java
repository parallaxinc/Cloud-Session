/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.parallax.server.common.cloudsession.db.dao.ResetTokenDao;
import com.parallax.server.common.cloudsession.db.dao.UserDao;
import com.parallax.server.common.cloudsession.db.generated.tables.records.ResettokenRecord;
import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;
import com.parallax.server.common.cloudsession.db.test.tables.records.ResettokenRecordMock;
import com.parallax.server.common.cloudsession.db.test.tables.records.UserRecordMock;
import com.parallax.server.common.cloudsession.exceptions.InsufficientBucketTokensException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserException;
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
public class ResetTokenServiceImplTest {

    private ResetTokenServiceImpl resetTokenService;

    private MailService mailServiceMock;
    private BucketService bucketServiceMock;
    private ResetTokenDao resetTokenDaoMock;
    private UserDao userDaoMock;

    @Before
    public void setUp() {
        mailServiceMock = Mockito.mock(MailService.class);
        bucketServiceMock = Mockito.mock(BucketService.class);
        resetTokenDaoMock = Mockito.mock(ResetTokenDao.class);
        userDaoMock = Mockito.mock(UserDao.class);

        resetTokenService = new ResetTokenServiceImpl();

        resetTokenService.setMailService(mailServiceMock);
        resetTokenService.setBucketService(bucketServiceMock);
        resetTokenService.setResetTokenDao(resetTokenDaoMock);
        resetTokenService.setUserDao(userDaoMock);
    }

    @Test
    public void isValidTokenTestValid() {
        ResettokenRecord resettokenRecord = new ResettokenRecordMock();
        resettokenRecord.setValidity(new Timestamp(new Date().getTime() + 1000));

        Mockito.when(resetTokenDaoMock.getResetToken("test")).thenReturn(resettokenRecord);

        boolean valid = resetTokenService.isValidResetToken("test");

        if (!valid) {
            Assert.fail("Token should be valid");
        }
    }

    @Test
    public void isValidTokenTestUnknown() {
        ResettokenRecord resettokenRecord = new ResettokenRecordMock();

        Mockito.when(resetTokenDaoMock.getResetToken("test2")).thenReturn(resettokenRecord);

        boolean valid = resetTokenService.isValidResetToken("test");

        if (valid) {
            Assert.fail("Token should not be known");
        }
    }

    @Test
    public void isValidTokenTestInvalid() {
        ResettokenRecord resettokenRecord = new ResettokenRecordMock();
        resettokenRecord.setValidity(new Timestamp(new Date().getTime() - 1000));

        Mockito.when(resetTokenDaoMock.getResetToken("test")).thenReturn(resettokenRecord);

        boolean valid = resetTokenService.isValidResetToken("test");

        if (valid) {
            Assert.fail("Token should be invalid");
        }
    }

    @Test
    public void createResetTokenTestByIdSuccess() {
        UserRecord userRecord = new UserRecordMock();
        ResettokenRecord resettokenRecord = new ResettokenRecordMock();

        try {
            Mockito.when(userDaoMock.getUser(2L)).thenReturn(userRecord);
        } catch (UnknownUserIdException ex) {
            Assert.fail("Setting mock return value should not throw an exception");
        }
        Mockito.when(resetTokenDaoMock.createResetToken(2L)).thenReturn(resettokenRecord);

        try {
            ResettokenRecord returnedToken = resetTokenService.createResetToken("SERVER", 2L);
            Assert.assertEquals(resettokenRecord, returnedToken);
        } catch (UnknownUserIdException ex) {
            Assert.fail("User id should be known");
        } catch (InsufficientBucketTokensException ex) {
            Assert.fail("There should be enough tokens in the bucket");
        }
    }

    @Test
    public void createResetTokenTestByIdUnkownUser() {
        ResettokenRecord resettokenRecord = new ResettokenRecordMock();

        // Set
        try {
            Mockito.when(userDaoMock.getUser(2L)).thenThrow(new UnknownUserIdException(2L));
        } catch (UnknownUserIdException ex) {
            Assert.fail("Setting mock return value should not throw an exception");
        }
        Mockito.when(resetTokenDaoMock.createResetToken(2L)).thenReturn(resettokenRecord);

        try {
            // Execute
            ResettokenRecord returnedToken = resetTokenService.createResetToken("SERVER", 2L);

            // Test
            Assert.fail("User id should not be known");
        } catch (UnknownUserIdException ex) {

        } catch (InsufficientBucketTokensException ex) {
            Assert.fail("There should be enough tokens in the bucket");
        }
    }

    @Test
    public void createResetTokenTestByIdInsufficientTokens() {
        UserRecord userRecord = new UserRecordMock();
        ResettokenRecord resettokenRecord = new ResettokenRecordMock();

        // Set
        try {
            Mockito.when(userDaoMock.getUser(2L)).thenReturn(userRecord);
        } catch (UnknownUserIdException ex) {
            Assert.fail("Setting mock return value should not throw an exception");
        }
        try {
            Mockito.doThrow(new InsufficientBucketTokensException()).when(bucketServiceMock).consumeTokensInternal(2L, "password-reset", 1);
        } catch (InsufficientBucketTokensException ex) {
            Assert.fail("Setting mock return value should not throw an exception");
        }
        Mockito.when(resetTokenDaoMock.createResetToken(2L)).thenReturn(resettokenRecord);

        try {
            // Execute
            ResettokenRecord returnedToken = resetTokenService.createResetToken("SERVER", 2L);

            // Test
            Assert.fail("There should NOT be enough tokens in the bucket");
        } catch (UnknownUserIdException ex) {
            Assert.fail("User id should be known");
        } catch (InsufficientBucketTokensException ex) {

        }
    }

    @Test
    public void createResetTokenTestByEmailSuccess() {
        UserRecord userRecord = new UserRecordMock();
        userRecord.setId(2L);
        ResettokenRecord resettokenRecord = new ResettokenRecordMock();

        try {
            Mockito.when(userDaoMock.getLocalUserByEmail("test@test.com")).thenReturn(userRecord);
        } catch (UnknownUserException ex) {
            Assert.fail("Setting mock return value should not throw an exception");
        }
        Mockito.when(resetTokenDaoMock.createResetToken(2L)).thenReturn(resettokenRecord);

        try {
            ResettokenRecord returnedToken = resetTokenService.createResetToken("SERVER", "test@test.com");
            Assert.assertEquals(resettokenRecord, returnedToken);
        } catch (UnknownUserException ex) {
            Assert.fail("User email should be known");
        } catch (InsufficientBucketTokensException ex) {
            Assert.fail("There should be enough tokens in the bucket");
        }
    }

    @Test
    public void createResetTokenTestByEmailUnkownUser() {
        ResettokenRecord resettokenRecord = new ResettokenRecordMock();

        // Set
        try {
            Mockito.when(userDaoMock.getLocalUserByEmail("test@test.com")).thenThrow(new UnknownUserException("test@test.com"));
        } catch (UnknownUserException ex) {
            Assert.fail("Setting mock return value should not throw an exception");
        }
        Mockito.when(resetTokenDaoMock.createResetToken(2L)).thenReturn(resettokenRecord);

        try {
            // Execute
            ResettokenRecord returnedToken = resetTokenService.createResetToken("SERVER", "test@test.com");

            // Test
            Assert.fail("User email should not be known");
        } catch (UnknownUserException ex) {

        } catch (InsufficientBucketTokensException ex) {
            Assert.fail("There should be enough tokens in the bucket");
        }
    }

    @Test
    public void createResetTokenTestByEmailInsufficientTokens() {
        UserRecord userRecord = new UserRecordMock();
        userRecord.setId(2L);
        ResettokenRecord resettokenRecord = new ResettokenRecordMock();

        // Set
        try {
            Mockito.when(userDaoMock.getLocalUserByEmail("test@test.com")).thenReturn(userRecord);
        } catch (UnknownUserException ex) {
            Assert.fail("Setting mock return value should not throw an exception");
        }
        try {
            Mockito.doThrow(new InsufficientBucketTokensException()).when(bucketServiceMock).consumeTokensInternal(2L, "password-reset", 1);
        } catch (InsufficientBucketTokensException ex) {
            Assert.fail("Setting mock return value should not throw an exception");
        }
        Mockito.when(resetTokenDaoMock.createResetToken(2L)).thenReturn(resettokenRecord);

        try {
            // Execute
            ResettokenRecord returnedToken = resetTokenService.createResetToken("SERVER", "test@test.com");

            // Test
            Assert.fail("There should NOT be enough tokens in the bucket");
        } catch (UnknownUserException ex) {
            Assert.fail("User email should be known");
        } catch (InsufficientBucketTokensException ex) {

        }
    }

}
