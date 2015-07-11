/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.parallax.server.common.cloudsession.db.dao.UserDao;
import com.parallax.server.common.cloudsession.service.BucketService;
import com.parallax.server.common.cloudsession.service.ConfirmTokenService;
import com.parallax.server.common.cloudsession.service.ResetTokenService;
import org.apache.shiro.crypto.RandomNumberGenerator;
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

        userService = new UserServiceImpl();

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
}
