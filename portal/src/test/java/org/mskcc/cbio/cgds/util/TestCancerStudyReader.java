package org.mskcc.cbio.cgds.test.util;

import java.io.File;

import junit.framework.TestCase;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.util.CancerStudyReader;
import org.mskcc.cgds.util.ProgressMonitor;

/**
 * JUnit test for CancerStudyReader class.
 */
public class TestCancerStudyReader extends TestCase {

   public void testCancerStudyReader() throws Exception {
      ResetDatabase.resetDatabase();
      // load cancers
      ImportTypesOfCancers.load(new ProgressMonitor(), new File("test_data/cancers.txt"));

      File file = new File("test_data/cancer_study.txt");
      CancerStudy cancerStudy = CancerStudyReader.loadCancerStudy( file );
      
      CancerStudy expectedCancerStudy = DaoCancerStudy.getCancerStudyByStableId( "test_brca" );
      assertEquals(expectedCancerStudy, cancerStudy);
      
      file = new File("test_data/cancer_study_bad.txt");
      try {
         cancerStudy = CancerStudyReader.loadCancerStudy( file );
         fail( "Should have thrown DaoException." );
      } catch (DaoException e) {
         assertTrue( e.getMessage().equals( 
                  "cancerStudy.getTypeOfCancerId() 'brcaXXX' does not refer to a TypeOfCancer."));
      }
   }
}
