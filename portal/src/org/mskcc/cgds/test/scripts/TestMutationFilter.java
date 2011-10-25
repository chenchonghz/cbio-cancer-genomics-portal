package org.mskcc.cgds.test.scripts;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.cgds.scripts.MutationFilter;
import org.mskcc.cgds.scripts.ResetDatabase;

public class TestMutationFilter extends TestCase {
   
   protected void setUp(){
      try {
         ResetDatabase.resetDatabase();
      } catch (DaoException e) {
         e.printStackTrace();
      }
   }
   
   public void testBadWhiteLists( ){
      try {
         new MutationFilter("no_such_file");
         Assert.fail( "Should throw IllegalArgumentException");
      } catch (IllegalArgumentException e) {
         assertEquals( "Gene list 'no_such_file' not found.", e.getMessage() );
      }
   }
   
   public void testNoWhitelists( ){
      MutationFilter myMutationFilter = new MutationFilter( );
      alwaysRejectTheseMutations( myMutationFilter );
      
      // accept all of these, because a MutationFilter without whitelists
      // accepts all mutations other than Silent, LOH, Intron and Wildtype mutations
      // not valid && somatic
      nowTestAcceptMutation( 
               myMutationFilter,
               true, 
               4L, 
               "Unknown",        // validationStatus,
               "Unknown",        // mutationStatus,
               "Unknown"         // mutationType
            );

      // valid but not somatic
      nowTestAcceptMutation( 
               myMutationFilter,
               true, 
               4L, 
               "Valid",        // validationStatus,
               "Unknown",        // mutationStatus,
               "Unknown"         // mutationType
            );

      // not valid but somatic
      nowTestAcceptMutation( 
               myMutationFilter,
               true, 
               4L, 
               "Unknown",        // validationStatus,
               "Somatic",        // mutationStatus,
               "Unknown"         // mutationType
            );

      // valid && somatic
      nowTestAcceptMutation( 
               myMutationFilter,
               true, 
               4L, 
               "Valid",        // validationStatus,
               "Somatic",        // mutationStatus,
               "Unknown"         // mutationType
            );

      // valid && somatic
      // testing safeStringTest()
      nowTestAcceptMutation( 
               myMutationFilter,
               true, 
               4L, 
               "vALid_as_hell",        // validationStatus,
               "SOMatic_for_sure",        // mutationStatus,
               "Unknown"         // mutationType
            );

   }

   public void testAcceptMutation_germline_white_list() throws DaoException {
      
      // load genes
      loadGene( "FOO", 3L  );
      loadGene( "BAR", 234L  );
      loadGene( "BIG", 234234L  );
                 
      // create MutationFilter
      MutationFilter myMutationFilter = new MutationFilter( 
               "test_data/test_germline_white_list_file.txt");

      alwaysRejectTheseMutations( myMutationFilter );      
      tryGermlineMutations( myMutationFilter );      
   }
   
   private void tryGermlineMutations( MutationFilter myMutationFilter ){

      // a germline on whitelist
      nowTestAcceptMutation( 
            myMutationFilter,
            true, 
            3L, 
            "Unknown",        // validationStatus,
            "GERMLINE",        // mutationStatus,
            "Unknown"         // mutationType
         );

      // a germline on whitelist, but also missense
      nowTestAcceptMutation( 
            myMutationFilter,
            false, 
            3L, 
            "Unknown",        // validationStatus,
            "GERMLINE",        // mutationStatus,
            "Missense_Mutation"         // mutationType
         );

      // a germline NOT on whitelist
      nowTestAcceptMutation( 
            myMutationFilter,
            false, 
            9999L, 
            "Unknown",        // validationStatus,
            "GERMLINE",        // mutationStatus,
            "Unknown"         // mutationType
         );

   }

   private void nowTestAcceptMutation( 
            MutationFilter myMutationFilter,
            boolean expectedResult, 
            long entrezGeneId, 
            String validationStatus, 
            String mutationStatus,
            String mutationType
            ){
      ExtendedMutation anExtendedMutation = new ExtendedMutation(
         entrezGeneId,           // entrezGeneId,
         validationStatus,       // validationStatus,
         mutationStatus,         // mutationStatus,
         mutationType            // mutationType
      );
      if( expectedResult ){
         assertTrue( myMutationFilter.acceptMutation( anExtendedMutation ) );         
      }else{
         assertFalse( myMutationFilter.acceptMutation( anExtendedMutation ) );         
      }
   }
   
   private void alwaysRejectTheseMutations(MutationFilter myMutationFilter){

      // REJECT: Silent, LOH, Intron and Wildtype mutations
      nowTestAcceptMutation( 
               myMutationFilter,
               false, 
               1L, 
               "Unknown",
               "Unknown",
               "Silent"
            );
      nowTestAcceptMutation( 
               myMutationFilter,
               false, 
               1L, 
               "Unknown",
               "Unknown",
               "Intron"
            );
      nowTestAcceptMutation( 
               myMutationFilter,
               false, 
               1L, 
               "Unknown",
               "LOH",
               "Unknown"
            );
      nowTestAcceptMutation( 
               myMutationFilter,
               false, 
               1L, 
               "Unknown",
               "Wildtype",
               "Unknown"
            );
      
   }
   
   private void loadGene( String GeneSymbol, long GeneID ) throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        daoGene.addGene(new CanonicalGene( GeneID, GeneSymbol ));
    }
}