package org.mskcc.portal.html.special_gene;

import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.portal.mapback.MapBack;
import org.mskcc.portal.mapback.Brca2;
import org.mskcc.portal.mapback.Brca1;
import org.mskcc.portal.html.HtmlUtil;

import java.util.ArrayList;

/**
 * Factory for Special Gene Objects.
 *
 * @author Ethan Cerami.
 */
public class SpecialGeneFactory {

    /**
     * Gets instance of a Special Gene.
     * @param geneSymbol Gene Symbol.
     * @return Special Gene Object.
     */
    public static SpecialGene getInstance(String geneSymbol) {
        if (geneSymbol.equalsIgnoreCase(SpecialGeneBrca1.BRCA1)) {
            return new SpecialGeneBrca1();
        } else if (geneSymbol.equalsIgnoreCase(SpecialGeneBrca2.BRCA2)) {
            return new SpecialGeneBrca2();
        } else {
            return null;
        }
    }
}