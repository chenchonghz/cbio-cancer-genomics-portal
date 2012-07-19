package org.mskcc.cbio.cgds.web_api;

import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoMutationFrequency;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.model.CanonicalGene;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.text.DecimalFormat;

/**
 * class to get mutation frequencies
 * @author jgao
 */
public class GetMutationFrequencies {
    public static final String TAB = "\t";

    public static String getMutationFrequencies( int cancerStudyId,
            HttpServletRequest httpServletRequest) throws DaoException, ProtocolException {
        StringBuffer buf = new StringBuffer();
        DaoMutationFrequency daoMutationFrequency = new DaoMutationFrequency();
        DecimalFormat formatter = new DecimalFormat("#,###,###.#####");
        String gene = httpServletRequest.getParameter("gene");
        if (gene != null) {
            DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
            CanonicalGene canonicalGene = daoGene.getGene(gene);
            if (canonicalGene == null) {
                throw new ProtocolException ("Don't know gene:  " + gene);
            }
            appendHeader(buf);
            canonicalGene = daoMutationFrequency.getSomaticMutationFrequency(canonicalGene.getEntrezGeneId());
            buf.append(canonicalGene.getEntrezGeneId()).append(TAB)
                    .append(canonicalGene.getHugoGeneSymbolAllCaps()).append(TAB)
                    .append(formatter.format(canonicalGene.getSomaticMutationFrequency())).append ("\n");
        } else {
            appendHeader(buf);
            ArrayList <CanonicalGene> geneList = daoMutationFrequency.getTop100SomaticMutatedGenes(cancerStudyId);
            for (CanonicalGene canonicalGene :  geneList) {
                buf.append(canonicalGene.getEntrezGeneId()).append(TAB)
                        .append(canonicalGene.getHugoGeneSymbolAllCaps()).append(TAB)
                        .append(formatter.format(canonicalGene.getSomaticMutationFrequency())).append ("\n");
            }
        }
        return buf.toString();
    }

    private static void appendHeader(StringBuffer buf) {
        buf.append("entrez_gene_id\tgene_symbol\tsomatic_mutation_rate\n");
    }
}