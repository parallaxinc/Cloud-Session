/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.parallax.server.common.cloudsession.db.dao.UserDao;
import com.parallax.server.common.cloudsession.db.generated.tables.records.UserRecord;
import com.parallax.server.common.cloudsession.db.test.tables.records.UserRecordMock;
import com.parallax.server.common.cloudsession.exceptions.PasswordComplexityException;
import com.parallax.server.common.cloudsession.exceptions.PasswordVerifyException;
import com.parallax.server.common.cloudsession.exceptions.UnknownUserException;
import com.parallax.server.common.cloudsession.service.BucketService;
import com.parallax.server.common.cloudsession.service.ConfirmTokenService;
import com.parallax.server.common.cloudsession.service.ResetTokenService;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.util.ByteSource;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;

/**
 *
 * @author Michel
 */
public class UserServiceImplTest {

    private UserServiceImpl userService;

    private RandomNumberGenerator rngMock;
    private ResetTokenService resetTokenServiceMock;
    private ConfirmTokenService confirmTokenServiceMock;
    private BucketService bucketServiceMock;
    private UserDao userDaoMock;

    @Before
    public void setUp() {
        rngMock = Mockito.mock(RandomNumberGenerator.class);
        resetTokenServiceMock = Mockito.mock(ResetTokenService.class);
        confirmTokenServiceMock = Mockito.mock(ConfirmTokenService.class);
        bucketServiceMock = Mockito.mock(BucketService.class);
        userDaoMock = Mockito.mock(UserDao.class);

        userService = new UserServiceImpl(rngMock);

        userService.setResetTokenSevice(resetTokenServiceMock);
        userService.setConfirmTokenService(confirmTokenServiceMock);
        userService.setBucketService(bucketServiceMock);
        userService.setUserDao(userDaoMock);
    }

//    @Test
//    public void isValidTokenTestValid() {
//        ResettokenRecord resettokenRecord = new ResettokenRecordMock();
//        resettokenRecord.setValidity(new Timestamp(new Date().getTime() + 1000));
//
//        Mockito.when(resetTokenDaoMock.getResetToken("test")).thenReturn(resettokenRecord);
//
//        boolean valid = userService.isValidResetToken("test");
//
//        if (!valid) {
//            Assert.fail("Token should be valid");
//        }
//    }
    //@Test
    public void resetPasswordTestSuccess() {
        UserRecord userRecord = new UserRecordMock();
        byte[] salt = {0x21, 0x72, 0xa, 0xd};
        ByteSource byteSource = ByteSource.Util.bytes(salt);
        String saltString = byteSource.toHex();
        Sha256Hash passwordHash = new Sha256Hash("newpassword", saltString, 1000);

        // Set
        Mockito.when(resetTokenServiceMock.isValidResetToken("token")).thenReturn(true);
        Mockito.when(rngMock.nextBytes()).thenReturn(byteSource);

        try {
            Mockito.when(userDaoMock.getLocalUserByEmail("email")).thenReturn(userRecord);
        } catch (UnknownUserException ex) {
            Assert.fail("Setting mock return value should not throw an exception");
        }

        try {
            // Execute
            UserRecord returned = userService.resetPassword("email", "token", "newpassword", "newpassword");

            // Test
            Assert.assertSame(userRecord, returned);
            Assert.assertEquals(saltString, returned.getSalt());
            Assert.assertEquals(passwordHash.toHex(), returned.getPassword());
        } catch (PasswordVerifyException ex) {
            Assert.fail("Password should match");
        } catch (UnknownUserException ex) {
            Assert.fail("User should be known");
        } catch (PasswordComplexityException ex) {
            Assert.fail("Password should comply with validation rules");
        }
    }
}
