package org.mskcc.cbio.cgds.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoMicroRna;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.MySQLbulkLoader;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;

import java.util.ArrayList;

/**
 * JUnit tests for DaoMicroRna class.
 */
public class TestDaoMicroRna extends TestCase {

    public void testDaoMicroRna() throws DaoException {

        // test with both values of MySQLbulkLoader.isBulkLoad()
        MySQLbulkLoader.bulkLoadOff();
        runTheTest();
        //MySQLbulkLoader.bulkLoadOn();
        //runTheTest();
    }

    private void runTheTest() throws DaoException{
        
        ResetDatabase.resetDatabase();
        DaoMicroRna daoMicroRna = new DaoMicroRna();
        daoMicroRna.addMicroRna("hsa-let-7a", "hsa-let-7a-1");
        daoMicroRna.addMicroRna("hsa-let-7a", "hsa-let-7a-2");

        // if bulkLoading, execute LOAD FILE
        if( MySQLbulkLoader.isBulkLoad()){
            daoMicroRna.flushMicroRna();
        }
        daoMicroRna.addMicroRna("hsa-let-7a", "hsa-let-7a-3");

        // if bulkLoading, execute LOAD FILE
        if( MySQLbulkLoader.isBulkLoad()){
            daoMicroRna.flushMicroRna();
        }
        ArrayList<String> variantIdList = daoMicroRna.getVariantIds("hsa-let-7a");
        assertEquals (3, variantIdList.size());
    }
}
