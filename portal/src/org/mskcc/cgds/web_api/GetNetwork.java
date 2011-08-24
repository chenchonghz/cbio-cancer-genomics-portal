package org.mskcc.cgds.web_api;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.graph.NetworkOfInterest;

import java.util.ArrayList;

/**
 * Gets a Network of Interest, Based on Input Seed Gene List.
 *
 * @author Ethan Cerami.
 */
public class GetNetwork {
    

    /**
     * Gets Network of Interest, Based on Input Seed Gene List.
     *
     * @param targetGeneList    Target Gene List.
     * @return Tab-Delimited SIF Like Output.
     * @throws DaoException Database Error.
     * @throws ProtocolException Protocol Exception.
     */
    public static String getNetwork(ArrayList<String> targetGeneList) throws DaoException,
        ProtocolException {

        //  Convert Gene Symbols to Canonical Gene List.
        ArrayList<CanonicalGene> canonicalGeneList = new ArrayList<CanonicalGene>();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        for (String geneSymbol:  targetGeneList) {
            CanonicalGene canonicalGene = daoGeneOptimized.getGene(geneSymbol);
            if (canonicalGene != null) {
                canonicalGeneList.add(canonicalGene);
            }
        }
        if (canonicalGeneList.size() ==0) {
            throw new ProtocolException ("You must specify at least one gene.");
        }

        //  Get the Network of Interest
        NetworkOfInterest noi = new NetworkOfInterest(canonicalGeneList);
        return noi.getTabDelim();
    }
}