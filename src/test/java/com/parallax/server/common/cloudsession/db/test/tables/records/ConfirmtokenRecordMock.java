/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.parallax.server.common.cloudsession.db.test.tables.records;

import com.parallax.server.common.cloudsession.db.generated.tables.records.ConfirmtokenRecord;
import org.jooq.Field;
import org.jooq.exception.DataAccessException;
import org.jooq.exception.DataChangedException;

/**
 *
 * @author Michel
 */
public class ConfirmtokenRecordMock extends ConfirmtokenRecord {

    @Override
    public int update(Field<?>... storeFields) throws DataAccessException, DataChangedException {
        return 1;
    }

}
