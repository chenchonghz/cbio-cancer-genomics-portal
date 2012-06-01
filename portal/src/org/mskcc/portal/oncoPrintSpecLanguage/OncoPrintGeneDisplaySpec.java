package org.mskcc.portal.oncoPrintSpecLanguage;

import java.util.EnumMap;

import org.mskcc.portal.util.Direction;
import org.mskcc.portal.oncoPrintSpecLanguage.DataTypeSpecEnumerations.DataTypeCategory;

/**
 * Stores and implements the filter for a gene's data. 
 * Has a separate filter (ResultDataTypeSpec) for each genetic data type.
 * satisfy() takes a data type, and optional data value, and indicates whether that value satisfies 
 * this filter. 
 * When used by the parser, generated by simplifying a FullDataTypeSpec.
 * 
 * @author Arthur Goldberg
 */
public class OncoPrintGeneDisplaySpec
{

   // may contain a ResultDataTypeSpec for each GeneticDataTypes.values()
    private EnumMap<GeneticDataTypes, ResultDataTypeSpec> finalDataTypeSpecs; 
    
    /**
     * construct a ResultFullDataTypeSpec that selects nothing
     */
    public OncoPrintGeneDisplaySpec() {
        finalDataTypeSpecs = new EnumMap<GeneticDataTypes, ResultDataTypeSpec>(GeneticDataTypes.class);
    }
    
    /**
     * set to default spec; 
     */
    public void setDefault( double zScoreThreshold, double rppaScoreThrehold ){
       this.setDefault( GeneticDataTypes.CopyNumberAlteration );
       this.setDefault( GeneticDataTypes.Mutation );
       this.setDefaultExpression( zScoreThreshold, GeneticDataTypes.Expression );
       this.setDefaultExpression( rppaScoreThrehold, GeneticDataTypes.RPPA );
    }
    
    /**
     * Set default levels for CopyNumberAlteration (AMP and HomDel) and Mutation (Mutated)
     * @param theGeneticDataType
     */
    public void setDefault( GeneticDataTypes theGeneticDataType ){
       ResultDataTypeSpec theResultDataTypeSpec;

       switch( theGeneticDataType ){

          case CopyNumberAlteration:
            theResultDataTypeSpec = new ResultDataTypeSpec( GeneticDataTypes.CopyNumberAlteration );
            theResultDataTypeSpec.setTheDiscreteDataTypeSetSpec( 
                     new DiscreteDataTypeSetSpec( GeneticDataTypes.CopyNumberAlteration,
                              GeneticTypeLevel.HomozygouslyDeleted, GeneticTypeLevel.Amplified ) );
            finalDataTypeSpecs.put( GeneticDataTypes.CopyNumberAlteration, theResultDataTypeSpec );
            break;

         case Expression:
            // TODO: s/b exception
            break;

         case Methylation:
            // TODO: s/b exception
            break;

         case Mutation:
            theResultDataTypeSpec = new ResultDataTypeSpec( GeneticDataTypes.Mutation );
            theResultDataTypeSpec.setTheDiscreteDataTypeSetSpec( 
                     new DiscreteDataTypeSetSpec( GeneticDataTypes.Mutation,
                              GeneticTypeLevel.Mutated ) );
            finalDataTypeSpecs.put( GeneticDataTypes.Mutation, theResultDataTypeSpec );
            break;

       }
    }
    
    /**
     * set the default Expression/RPPA thresholds, <= and >= scoreThreshold
     * @param scoreThreshold
     */
    public void setDefaultExpression( double scoreThreshold, GeneticDataTypes theGeneticDataType ){
       ResultDataTypeSpec theResultDataTypeSpec = new ResultDataTypeSpec( theGeneticDataType );

       theResultDataTypeSpec.setCombinedGreaterContinuousDataTypeSpec( new ContinuousDataTypeSpec( theGeneticDataType,
                ComparisonOp.convertCode(">="), (float)scoreThreshold ) );

       theResultDataTypeSpec.setCombinedLesserContinuousDataTypeSpec( new ContinuousDataTypeSpec( theGeneticDataType,
                ComparisonOp.convertCode("<="), (float) -scoreThreshold ) );
       
       finalDataTypeSpecs.put( theGeneticDataType, theResultDataTypeSpec );
    }
    
    /**
     * get the ResultDataTypeSpec for theGeneticDataType, creating one if necessary.
     * @param theGeneticDataType
     * 
     * @return
     */
    public ResultDataTypeSpec createResultDataTypeSpec( GeneticDataTypes theGeneticDataType ){
       ResultDataTypeSpec theResultDataTypeSpec = finalDataTypeSpecs.get(theGeneticDataType);
       if( null != theResultDataTypeSpec){
           return theResultDataTypeSpec;
       }
       theResultDataTypeSpec = new ResultDataTypeSpec( theGeneticDataType );
       finalDataTypeSpecs.put(theGeneticDataType, theResultDataTypeSpec );
       return theResultDataTypeSpec;
   }

    public ResultDataTypeSpec getResultDataTypeSpec( GeneticDataTypes theGeneticDataType ){
       return finalDataTypeSpecs.get(theGeneticDataType);
   }

    public void setResultDataTypeSpec( GeneticDataTypes theGeneticDataType, ResultDataTypeSpec theResultDataTypeSpec ){
       finalDataTypeSpecs.put(theGeneticDataType, theResultDataTypeSpec );
   }

    /**
     * satisfy for all data types. 
     * 
     * @param theGeneticDataType
     * @return true if the data type defines an alteration for any level
     */
    public boolean satisfy( GeneticDataTypes theGeneticDataType ){
        ResultDataTypeSpec theResultDataTypeSpec = finalDataTypeSpecs.get(theGeneticDataType);
        return (theResultDataTypeSpec != null );
    }
    
    /**
     * satisfy for continuous data types.
     * 
     * @param theGeneticDataType
     * @return true if the value is in one of the inequalities passed by the spec
     */
    public boolean satisfy( GeneticDataTypes theGeneticDataType, float value )
        throws IllegalArgumentException{
            if( theGeneticDataType.getTheDataTypeCategory().equals(DataTypeCategory.Discrete)){
                throw new IllegalArgumentException("satisfy for continuous data types only takes continuous GeneticDataTypes");
            }
        ResultDataTypeSpec theResultDataTypeSpec = finalDataTypeSpecs.get(theGeneticDataType);
        if( null == theResultDataTypeSpec ){
           return false;
        }
        if( theResultDataTypeSpec.acceptAll ) {
            return true;
        }
        if( theResultDataTypeSpec.combinedLesserContinuousDataTypeSpec != null &&
                finalDataTypeSpecs.get(theGeneticDataType).combinedLesserContinuousDataTypeSpec.satisfy(value) ){
                return true;
            }
        if( theResultDataTypeSpec.combinedGreaterContinuousDataTypeSpec != null &&
                finalDataTypeSpecs.get(theGeneticDataType).combinedGreaterContinuousDataTypeSpec.satisfy(value) ){
                return true;
            }
        return false;
    }
    
    /**
     * Another satisfy for continuous data types.
     * Indicates whether a particular value is higher or lower (as specified by theDirection) than the upper 
     * or lower threshold.
     * <p>
     * @param theGeneticDataType
     * @return true if the value is satisfied by the specified inequalities in the spec
     */
    public boolean satisfy( GeneticDataTypes theGeneticDataType, float value, Direction theDirection )
        throws IllegalArgumentException{
            if( !theGeneticDataType.getTheDataTypeCategory().equals(DataTypeCategory.Continuous)){
                throw new IllegalArgumentException("satisfy for continuous data types only takes continuous GeneticDataTypes");
            }
        ResultDataTypeSpec theResultDataTypeSpec = finalDataTypeSpecs.get(theGeneticDataType);
        if( null == theResultDataTypeSpec ){
           return false;
        }
        if( theResultDataTypeSpec.acceptAll ) {
            return true;
        }
        switch(theDirection){
         case higher:
            return( theResultDataTypeSpec.combinedGreaterContinuousDataTypeSpec != null &&
                  finalDataTypeSpecs.get(theGeneticDataType).combinedGreaterContinuousDataTypeSpec.satisfy(value) );
         case lower:
            return( theResultDataTypeSpec.combinedLesserContinuousDataTypeSpec != null &&
                  finalDataTypeSpecs.get(theGeneticDataType).combinedLesserContinuousDataTypeSpec.satisfy(value) );
        }
        // unreachable code; keep compiler happy
        // TODO: throw an exception
        return false;
    }
    
    /**
     * satisfy for discrete data types.
     * 
     * @param theGeneticDataType
     * @param theGeneticTypeLevel
     * @return indicates whether this display spec classifies the given data types' level as an alteration
     * @throws IllegalArgumentException
     */
    public boolean satisfy( GeneticDataTypes theGeneticDataType, Object level)//GeneticTypeLevel theGeneticTypeLevel )
                throws IllegalArgumentException {
        if( !theGeneticDataType.getTheDataTypeCategory().equals(DataTypeCategory.Discrete)){
            throw new IllegalArgumentException("satisfy for discrete data types only takes Discrete GeneticDataTypes");
        }
        
        ResultDataTypeSpec theResultDataTypeSpec = finalDataTypeSpecs.get(theGeneticDataType);
        if( null == theResultDataTypeSpec ){
           return false;
        }
        
        if( theResultDataTypeSpec.acceptAll ) {
            return true;
        }

        if( theResultDataTypeSpec.theDiscreteDataTypeSetSpec != null &&
                 theResultDataTypeSpec.theDiscreteDataTypeSetSpec.satisfy(level) ){
            return true;
        }
        
        return false;
    }
    
    /**
     * Returns true if this OncoPrintGeneDisplaySpec specifies the given type and the other one does not, 
     * i.e., if the difference is positive.
     * 
     * @param otherOncoPrintGeneDisplaySpec
     * @param aGeneticDataType
     * @return
     */
    public boolean typeDifference( OncoPrintGeneDisplaySpec otherOncoPrintGeneDisplaySpec, 
             GeneticDataTypes aGeneticDataType ){
       
       return (null != this.getResultDataTypeSpec(aGeneticDataType)) && (null == otherOncoPrintGeneDisplaySpec.getResultDataTypeSpec(aGeneticDataType));
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(); 
        for( GeneticDataTypes aGeneticDataTypes: GeneticDataTypes.values()){
            //out.format("checking %s%n", aGeneticDataTypes);
            ResultDataTypeSpec theResultDataTypeSpec = this.finalDataTypeSpecs.get(aGeneticDataTypes);
            if( null != theResultDataTypeSpec ){
                //out.format(" for gt %s%n", aGeneticDataTypes);
                sb.append(theResultDataTypeSpec.toString()).append(" ");
            }
        }
        return sb.append(";").toString(); 
    }

    /*
     * TODO: fix
    protected ResultFullDataTypeSpec clone(){
        ResultFullDataTypeSpec theResultFullDataTypeSpec = new ResultFullDataTypeSpec();
        theResultFullDataTypeSpec.defaultDataTypeSpec = this.defaultDataTypeSpec;
        theResultFullDataTypeSpec.finalDataTypeSpecs = this.finalDataTypeSpecs.clone();
        return theResultFullDataTypeSpec;
    }
     */
    // TODO: COPY
    // TODO: equals

}
