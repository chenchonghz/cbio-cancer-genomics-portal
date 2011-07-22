package org.mskcc.cgds.test.scripts;

import junit.framework.TestCase;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoUserAccessRight;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cgds.scripts.ImportUserAccessRights;
import org.mskcc.cgds.scripts.ResetDatabase;

public class TestImportUserAccessRights extends TestCase {
   
   public void testImportUserAccessRights() throws Exception{
      ResetDatabase.resetDatabase();
      // load cancers
      String[] args = { "testData/cancers.txt" };
      ImportTypesOfCancers.main( args );

      CancerStudy cancerStudy = new CancerStudy( "GBM", "GBM Description", "gbm", "brca", false );
      DaoCancerStudy.addCancerStudy(cancerStudy);

      cancerStudy.setName("Breast");
      cancerStudy.setCancerStudyIdentifier( "breast" );
      cancerStudy.setDescription("Breast Description");
      DaoCancerStudy.addCancerStudy(cancerStudy);
      
      String args2[] = { "./testData/test_access_rights.txt" };
      
      System.err.println("Not a problem: Will print two lines starting with: \"Could not add line\"");
      ImportUserAccessRights.main( args2 );
      
      assertTrue( DaoUserAccessRight.containsUserAccessRightsByEmailAndStudy( "goldberg@cbio.mskcc.org", 1 ) );
      assertTrue( DaoUserAccessRight.containsUserAccessRightsByEmailAndStudy( "joe@hotmail.com", 2 ) );
      assertFalse( DaoUserAccessRight.containsUserAccessRightsByEmailAndStudy( "joe@hotmail.com", 1 ) );
      assertFalse( DaoUserAccessRight.containsUserAccessRightsByEmailAndStudy( "joe@hotmail.com", 3 ) );
   }
}
