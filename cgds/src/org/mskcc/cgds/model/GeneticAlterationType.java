package org.mskcc.cgds.model;

public class GeneticAlterationType {

    private String type;

    /**
     * Private Constructor. Enumeration Pattern.
     *
     * @param type Alteration Type.
     */
    private GeneticAlterationType(String type) {
        this.type = type;
    }

    /**
     * Gets Type Name.
     *
     * @return Type Name.
     */
    public String toString() {
        return type;
    }
    
    // TODO: convert to Enumeration

    /**
     * Get Type by Type Name.
     *
     * @param type Type Name, e.g. "MUTATION", "COPY_NUMBER_ALERATION" or "MRNA_EXPRESSION", etc.
     * @return correct GeneticAlterationType Object
     * @throws IllegalArgumentException if type is null
     * @throws NullPointerException if type is not a known genetic data type name
     */
    public static GeneticAlterationType getType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        } else if (type.equals(COPY_NUMBER_ALTERATION.toString())) {
            return COPY_NUMBER_ALTERATION;
        } else if (type.equals(MRNA_EXPRESSION.toString())) {
            return MRNA_EXPRESSION;
        } else if (type.equals(MICRO_RNA_EXPRESSION.toString())) {
            return MICRO_RNA_EXPRESSION;
        } else if (type.equals(METHYLATION.toString())) {
            return METHYLATION;
        } else if (type.equals(METHYLATION_BINARY.toString())) {
            return METHYLATION_BINARY;
        } else if (type.equals(MUTATION_EXTENDED.toString())) {
           return MUTATION_EXTENDED;
        } else if (type.equals(PROTEIN_LEVEL.toString())) {
           return PROTEIN_LEVEL;
        } else if (type.equals(PHOSPHORYLATION.toString())) {
           return PHOSPHORYLATION;
        } else {
            throw new NullPointerException("Cannot find: '" + type + "'");
        }
    }

    /**
     * Extended Mutation alteration Type.
     */
    public static final GeneticAlterationType MUTATION_EXTENDED
            = new GeneticAlterationType("MUTATION_EXTENDED");

    /**
     * Copy Number alteration type.
     */
    public static final GeneticAlterationType COPY_NUMBER_ALTERATION
            = new GeneticAlterationType("COPY_NUMBER_ALTERATION");

    /**
     * microRNA expression alteration type.
     */
    public static final GeneticAlterationType MICRO_RNA_EXPRESSION
            = new GeneticAlterationType("MICRO_RNA_EXPRESSION");    

    /**
     * mRNA expression alteration type.
     */
    public static final GeneticAlterationType MRNA_EXPRESSION
            = new GeneticAlterationType("MRNA_EXPRESSION");

    /**
     * Methylation alteration type.
     */
    public static final GeneticAlterationType METHYLATION
            = new GeneticAlterationType("METHYLATION");

    /**
     * Methylation Binary alteration type.
     */
    public static final GeneticAlterationType METHYLATION_BINARY
            = new GeneticAlterationType("METHYLATION_BINARY");

    /**
     * Phosphorylation alteration type.
     */
    public static final GeneticAlterationType PHOSPHORYLATION
            = new GeneticAlterationType("PHOSPHORYLATION");

    /**
     * Protein level alteration type.
     */
    public static final GeneticAlterationType PROTEIN_LEVEL
            = new GeneticAlterationType("PROTEIN_LEVEL");
}