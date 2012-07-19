package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.mskcc.cbio.cgds.model.GeneticAlterationType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Genetic Profile Util Class.
 *
 */
public class GeneticProfileUtil {

    /**
     * Gets the GeneticProfile with the Specified GeneticProfile ID.
     * @param profileId GeneticProfile ID.
     * @param profileList List of Genetic Profiles.
     * @return GeneticProfile or null.
     */
    public static GeneticProfile getProfile(String profileId,
            ArrayList<GeneticProfile> profileList) {
        for (GeneticProfile profile : profileList) {
            if (profile.getStableId().equals(profileId)) {
                return profile;
            }
        }
        return null;
    }

    /**
     * Returns true if Any of the Profiles Selected by the User Refer to mRNA Expression
     * outlier profiles.
     *
     * @param geneticProfileIdSet   Set of Chosen Profiles IDs.
     * @param profileList           List of Genetic Profiles.
     * @return true or false.
     */
    public static boolean outlierExpressionSelected(HashSet<String> geneticProfileIdSet,
            ArrayList<GeneticProfile> profileList) {
        Iterator<String> geneticProfileIdIterator = geneticProfileIdSet.iterator();
        while (geneticProfileIdIterator.hasNext()) {
            String geneticProfileId = geneticProfileIdIterator.next();
            GeneticProfile geneticProfile = getProfile (geneticProfileId, profileList);
            if (geneticProfile != null && geneticProfile.getGeneticAlterationType().
                    equals(GeneticAlterationType.MRNA_EXPRESSION)) {
                String profileName = geneticProfile.getProfileName();
                if (profileName != null) {
                    if (profileName.toLowerCase().contains("outlier")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
