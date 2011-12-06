package org.mskcc.portal.mutation.diagram;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneOptimized;
import org.mskcc.cgds.dao.DaoMutation;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.ExtendedMutation;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import com.google.inject.Inject;

/**
 * Implementation of MutationService that reads from the CGDS data source.
 */
public final class CgdsMutationService implements MutationService {
    private static final Logger logger = Logger.getLogger(CacheDomainService.class);
    private final DaoGeneOptimized geneDao;
    private final DaoMutation mutationDao;

    /**
     * Create a new CGDS mutation service with the specified gene DAO and mutation DAO.
     *
     * @param geneDao gene data access object (DAO), must not be null
     * @param mutationDao mutation data access object (DAO), must not be null
     */
    @Inject
    public CgdsMutationService(final DaoGeneOptimized geneDao, final DaoMutation mutationDao) {
        checkNotNull(geneDao);
        checkNotNull(mutationDao);
        this.geneDao = geneDao;
        this.mutationDao = mutationDao;
    }

    /** {@inheritDoc} */
    public List<Mutation> getMutations(final String hugoGeneSymbol) {
        checkNotNull(hugoGeneSymbol);
        List<Mutation> mutations = new LinkedList<Mutation>();
        try {
            CanonicalGene gene = geneDao.getGene(hugoGeneSymbol);
            // todo:  pass in genetic_profile_id, or cancer_study_id, or target case list
            List<ExtendedMutation> extendedMutations = mutationDao.getMutations(1, gene.getEntrezGeneId());

            Multiset<Integer> locations = HashMultiset.create();
            SetMultimap<Integer, String> labels = HashMultimap.create();
            for (ExtendedMutation extendedMutation : extendedMutations) {
                String label = extendedMutation.getAminoAcidChange();
                try {
                    int location = Integer.valueOf(label.replaceAll("[A-Z*]+", ""));
                    locations.add(location);
                    labels.put(location, label);
                }
                catch (NumberFormatException e) {
                    logger.warn("ignoring extended mutation " + label + ", no location information");
                }
            }
            for (Multiset.Entry<Integer> entry : locations.entrySet()) {
                int location = entry.getElement();
                int count = entry.getCount();
                String label = Joiner.on("/").join(labels.get(location));
                Mutation mutation = new Mutation(label, location, count);
                mutations.add(mutation);
            }
        }
        catch (DaoException e) {
            logger.error("could not find mutations for HUGO gene symbol " + hugoGeneSymbol, e);
        }
        return ImmutableList.copyOf(mutations);
    }
}
