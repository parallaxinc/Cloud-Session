/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.service.impl;

import com.parallax.server.common.cloudsession.db.dao.BucketDao;
import com.parallax.server.common.cloudsession.db.generated.tables.records.BucketRecord;
import com.parallax.server.common.cloudsession.db.test.tables.records.BucketRecordMock;
import com.parallax.server.common.cloudsession.exceptions.InsufficientBucketTokensException;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.commons.configuration.Configuration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author Michel
 */
public class BucketServiceImplMultipleTokensTest {

    private BucketServiceImpl bucketService;
    private Configuration configurationMock;
    private BucketDao bucketDaoMock;

    @Before
    public void setUp() {
        configurationMock = Mockito.mock(Configuration.class);
        bucketDaoMock = Mockito.mock(BucketDao.class);

        bucketService = new BucketServiceImpl();
        bucketService.setConfiguration(configurationMock);
        bucketService.setBucketDao(bucketDaoMock);
    }

    private void setUpConfiguration(String bucket, int size, int input, int freq) {
        Mockito.when(configurationMock.getInt("bucket." + bucket + ".size", 0)).thenReturn(size);
        Mockito.when(configurationMock.getInt("bucket." + bucket + ".input", 0)).thenReturn(input);
        Mockito.when(configurationMock.getInt("bucket." + bucket + ".freq", 1000)).thenReturn(freq);
    }

    @Test
    public void consumeTokenIntenalTestSufficientFull() {
        BucketRecord bucketRecord = new BucketRecordMock();
        bucketRecord.setContent(5);
        bucketRecord.setTimestamp(new Timestamp(new Date().getTime()));

        setUpConfiguration("test", 5, 1, 5000);
        Mockito.when(bucketDaoMock.getBucket(1L, "test")).thenReturn(bucketRecord);

        try {
            bucketService.consumeTokensInternal(1L, "test", 2);
            Assert.assertEquals(new Integer(3), bucketRecord.getContent());
        } catch (InsufficientBucketTokensException ex) {
            Assert.fail("Not enough tokens while there should");
        }
    }

    @Test
    public void consumeTokenIntenalTestSufficientNotFull() {
        BucketRecord bucketRecord = new BucketRecordMock();
        bucketRecord.setContent(4);
        bucketRecord.setTimestamp(new Timestamp(new Date().getTime()));

        setUpConfiguration("test", 5, 1, 5000);
        Mockito.when(bucketDaoMock.getBucket(1L, "test")).thenReturn(bucketRecord);

        try {
            bucketService.consumeTokensInternal(1L, "test", 2);
            Assert.assertEquals(new Integer(2), bucketRecord.getContent());
        } catch (InsufficientBucketTokensException ex) {
            Assert.fail("Not enough tokens while there should");
        }
    }

    @Test
    public void consumeTokenIntenalTestNewBucket() {
        BucketRecord bucketRecord = new BucketRecordMock();
        bucketRecord.setContent(-1);
        bucketRecord.setTimestamp(new Timestamp(new Date().getTime()));

        setUpConfiguration("test", 5, 1, 5000);

        Mockito.when(bucketDaoMock.getBucket(1L, "test")).thenReturn(bucketRecord);

        try {
            bucketService.consumeTokensInternal(1L, "test", 2);
            Assert.assertEquals(new Integer(3), bucketRecord.getContent());
        } catch (InsufficientBucketTokensException ex) {
            Assert.fail("Not enough tokens while there should");
        }
    }

    @Test
    public void consumeTokenIntenalTestInsufficient() {
        BucketRecord bucketRecord = new BucketRecordMock();
        bucketRecord.setContent(1);
        bucketRecord.setTimestamp(new Timestamp(new Date().getTime()));

        setUpConfiguration("test", 5, 1, 5000);

        Mockito.when(bucketDaoMock.getBucket(1L, "test")).thenReturn(bucketRecord);

        try {
            bucketService.consumeTokensInternal(1L, "test", 2);
            Assert.fail("There should not be enough tokens");
        } catch (InsufficientBucketTokensException ex) {
            Assert.assertEquals(new Integer(1), bucketRecord.getContent());
        }
    }

    @Test
    public void consumeTokenIntenalTestSufficientBecauseOfRefilling() {
        BucketRecord bucketRecord = new BucketRecordMock();
        bucketRecord.setContent(1);
        bucketRecord.setTimestamp(new Timestamp(new Date().getTime() - 5001));

        setUpConfiguration("test", 5, 1, 5000);

        Mockito.when(bucketDaoMock.getBucket(1L, "test")).thenReturn(bucketRecord);

        try {
            bucketService.consumeTokensInternal(1L, "test", 2);
            Assert.assertEquals(new Integer(0), bucketRecord.getContent());
        } catch (InsufficientBucketTokensException ex) {
            Assert.fail("Not enough tokens while there should");
        }
    }

}
