/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.dao;

import com.parallax.server.common.cloudsession.db.generated.tables.records.BucketRecord;

/**
 *
 * @author Michel
 */
public interface BucketDao {

    BucketRecord getBucket(Long idUser, String type);

}
