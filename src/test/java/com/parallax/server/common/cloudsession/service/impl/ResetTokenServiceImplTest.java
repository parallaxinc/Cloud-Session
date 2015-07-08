/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.parallax.server.common.cloudsession.db.dao.ResetTokenDao;
import com.parallax.server.common.cloudsession.db.dao.UserDao;
import com.parallax.server.common.cloudsession.db.generated.tables.records.ResettokenRecord;
import com.parallax.server.common.cloudsession.db.test.tables.records.ResettokenRecordMock;
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

}
