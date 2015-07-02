/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.dao.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.parallax.server.common.cloudsession.db.dao.BucketDao;
import com.parallax.server.common.cloudsession.db.generated.Tables;
import com.parallax.server.common.cloudsession.db.generated.tables.records.BucketRecord;
import org.jooq.DSLContext;

/**
 *
 * @author Michel
 */
@Singleton
public class BucketDaoImpl implements BucketDao {

    private DSLContext create;

    @Inject
    public void setDSLContext(DSLContext dsl) {
        this.create = dsl;
    }

    @Override
    public BucketRecord getBucket(Long idUser, String type) {
        BucketRecord bucket = create.selectFrom(Tables.BUCKET).where(Tables.BUCKET.ID_USER.eq(idUser)).and(Tables.BUCKET.TYPE.eq(type)).fetchOne();
        if (bucket == null) {
            bucket = create.insertInto(Tables.BUCKET).columns(Tables.BUCKET.ID_USER, Tables.BUCKET.TYPE).values(idUser, type).returning().fetchOne();
        }
        return bucket;
    }

}
