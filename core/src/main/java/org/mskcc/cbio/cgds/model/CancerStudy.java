/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.cgds.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGistic;
import org.mskcc.cbio.cgds.dao.DaoMutSig;
import org.mskcc.cbio.cgds.util.EqualsUtil;
import org.mskcc.cbio.portal.remote.GetGeneticProfiles;

import java.util.ArrayList;
import org.mskcc.cbio.cgds.dao.DaoCaseProfile;
import org.mskcc.cbio.cgds.dao.DaoCopyNumberSegment;

/**
 * This represents a cancer study, with a set of cases and some data sets.
 *
 * @author Ethan Cerami, Arthur Goldberg.
 */
public class CancerStudy {
    /**
     * NO_SUCH_STUDY Internal ID has not been assigned yet.
     */
    public static final int NO_SUCH_STUDY = -1;

    private int studyID; // assigned by dbms auto increment
    private String name;
    private String description;
    private String cancerStudyIdentifier;
    private String typeOfCancerId;  // required
    private boolean publicStudy;  // if true, a public study, otherwise private
    private String pmid;
    private String citation;
    

    /**
     * Constructor.
     * @param name                  Name of Cancer Study.
     * @param description           Description of Cancer Study.
     * @param cancerStudyIdentifier Cancer Study Stable Identifier.
     * @param typeOfCancerId        Type of Cancer.
     * @param publicStudy           Flag to indicate if this is a public study.
     */
    public CancerStudy(String name, String description, String cancerStudyIdentifier,
            String typeOfCancerId, boolean publicStudy) {
        super();
        this.studyID = CancerStudy.NO_SUCH_STUDY;
        this.name = name;
        this.description = description;
        this.cancerStudyIdentifier = cancerStudyIdentifier;
        this.typeOfCancerId = typeOfCancerId;
        this.publicStudy = publicStudy;
    }

    /**
     * Indicates that this is a public study.
     * @return true or false.
     */
    public boolean isPublicStudy() {
        return publicStudy;
    }

    /**
     * Marks this study as public or private.
     * @param publicFlag Public Flag.
     */
    public void setPublicStudy(boolean publicFlag) {
        this.publicStudy = publicFlag;
    }

    /**
     * Gets the Cancer Study Stable Identifier.
     * @return cancer study stable identifier.
     */
    public String getCancerStudyStableId() {
        return cancerStudyIdentifier;
    }

    /**
     * Gets the Type of Cancer.
     * @return type of cancer.
     */
    public String getTypeOfCancerId() {
        return typeOfCancerId;
    }

    /**
     * Sets the Cancer Study Stable Identifier.
     * @param cancerStudyIdentifier Cancer Study Stable Identifier.
     */
    public void setCancerStudyStablId(String cancerStudyIdentifier) {
        this.cancerStudyIdentifier = cancerStudyIdentifier;
    }

    /**
     * Gets the Internal ID associated with this record.
     * @return internal integer ID.
     */
    public int getInternalId() {
        return studyID;
    }

    /**
     * Sets the Internal ID associated with this record.
     * @param studyId internal integer ID.
     */
    public void setInternalId(int studyId) {
        this.studyID = studyId;
    }

    /**
     * Gets the Cancer Study Name.
     * @return cancer study name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the Cancer Study Name.
     * @param name cancer study name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the Cancer Study Description.
     * @return cancer study description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the Cancer Study Description.
     * @param description cancer study description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public String getPmid() {
        return pmid;
    }

    public void setPmid(String pmid) {
        this.pmid = pmid;
    }

    public String getCitation() {
        return citation;
    }

    public void setCitation(String citation) {
        this.citation = citation;
    }

    /**
     * Gets the genetic profiles.
     * @return genetic profiles
     * @throws DaoException database read error
     */
    public ArrayList<GeneticProfile> getGeneticProfiles() throws DaoException {
        return GetGeneticProfiles.getGeneticProfiles(getCancerStudyStableId());
    }

    /* TODO: Add a tag to cancer study in order to get rid of redundant code execution.
        During the talk it was decided not to use an additional tag for each cancer
        study, so we need a rather ugly solution. This won't be hurting us much for now
        but could result in performance issues if the portal ever gets heavy load traffic.
     */
    /**
     * Get mutation profile if any; otherwise, return null.
     *
     * @return mutation profile if there is mutation data; otherwise, null.
     * @param geneticProfiles genetic profiles to search mutations on
     */
    public GeneticProfile getMutationProfile(ArrayList<GeneticProfile> geneticProfiles,
            String caseId) throws DaoException {
        for(GeneticProfile geneticProfile: geneticProfiles) {
            if(geneticProfile.getGeneticAlterationType()
                    .equals(GeneticAlterationType.MUTATION_EXTENDED)
                    && (caseId==null || DaoCaseProfile.caseExistsInGeneticProfile(caseId,geneticProfile.getGeneticProfileId()))) {
                return geneticProfile;
            }
        }

        return null;
    }
    
    public GeneticProfile getMutationProfile(String caseId) throws DaoException {
        return getMutationProfile(getGeneticProfiles(),caseId);
    }
    
    public GeneticProfile getMutationProfile() throws DaoException {
        return getMutationProfile(null);
    }
    
    /**
     * Checks if there is any mutation data associated with this cancer study.
     *
     * @return true if there is mutation data
     * @param geneticProfiles genetic profiles to search mutations on
     */
    public boolean hasMutationData(ArrayList<GeneticProfile> geneticProfiles) throws DaoException {
        return null != getMutationProfile(geneticProfiles,null);
    }
    
    /**
     * Get copy number alteration profile if any; otherwise, return null.
     *
     * @return cn profile if there is mutation data; otherwise, null. If 
     *         showInAnalysisOnly is true, return cn profile shown in analysis tab only.
     * @param geneticProfiles genetic profiles to search mutations on
     */
    public GeneticProfile getCopyNumberAlterationProfile(boolean showInAnalysisOnly)
            throws DaoException {
        return getCopyNumberAlterationProfile(null,showInAnalysisOnly);
    }

    public boolean hasCnaData() throws DaoException {
        GeneticProfile copyNumberAlterationProfile = getCopyNumberAlterationProfile(true);
        return copyNumberAlterationProfile != null;
    }

    /**
     * Get copy number alteration profile if any; otherwise, return null.
     *
     * @return cn profile if there is mutation data; otherwise, null. If 
     *         showInAnalysisOnly is true, return cn profile shown in analysis tab only.
     * @param geneticProfiles genetic profiles to search mutations on
     */
    public GeneticProfile getCopyNumberAlterationProfile(String caseId, boolean showInAnalysisOnly)
            throws DaoException {
        for(GeneticProfile geneticProfile: getGeneticProfiles()) {
            if(geneticProfile.getGeneticAlterationType()
                    .equals(GeneticAlterationType.COPY_NUMBER_ALTERATION)
                    && (!showInAnalysisOnly || geneticProfile.showProfileInAnalysisTab())
                    && (caseId==null || DaoCaseProfile.caseExistsInGeneticProfile(caseId,geneticProfile.getGeneticProfileId()))) {
                return geneticProfile;
            }
        }

        return null;
    }

    /**
     * Similar to:
     * @see #hasMutationData(java.util.ArrayList)
     * but this method grabs all the genetic profiles associated to the cancer study
     * Utilizes @link #getGeneticProfiles()
     *
     * @return true if there is mutation data
     * @throws DaoException database read error
     */
    public boolean hasMutationData() throws DaoException {
        return hasMutationData(getGeneticProfiles());
    }
    
    /**
     * 
     * @return true if copy number segment data exist for this study; false, otherwise.
     * @throws DaoException 
     */
    public boolean hasCnaSegmentData() throws DaoException {
        return DaoCopyNumberSegment.segmentDataExistForCancerStudy(studyID);
    }

    /**
     * Equals.
     * @param otherCancerStudy Other Cancer Study.
     * @return true of false.
     */
    @Override
    public boolean equals(Object otherCancerStudy) {
        if (this == otherCancerStudy) {
            return true;
        }
        
        if (!(otherCancerStudy instanceof CancerStudy)) {
            return false;
        }
        
        CancerStudy that = (CancerStudy) otherCancerStudy;
        return
                EqualsUtil.areEqual(this.publicStudy, that.publicStudy) &&
                        EqualsUtil.areEqual(this.cancerStudyIdentifier,
                                that.cancerStudyIdentifier) &&
                        EqualsUtil.areEqual(this.description, that.description) &&
                        EqualsUtil.areEqual(this.name, that.name) &&
                        EqualsUtil.areEqual(this.typeOfCancerId, that.typeOfCancerId) &&
                        EqualsUtil.areEqual(this.studyID, that.studyID);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + this.studyID;
        hash = 11 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 11 * hash + (this.description != null ? this.description.hashCode() : 0);
        hash = 11 * hash + (this.cancerStudyIdentifier != null ? this.cancerStudyIdentifier.hashCode() : 0);
        hash = 11 * hash + (this.typeOfCancerId != null ? this.typeOfCancerId.hashCode() : 0);
        hash = 11 * hash + (this.publicStudy ? 1 : 0);
        return hash;
    }

    /**
     * toString() Override.
     * @return string summary of cancer study.
     */
    @Override
    public String toString() {
        return "CancerStudy [studyID=" + studyID + ", name=" + name + ", description="
                + description + ", cancerStudyIdentifier=" + cancerStudyIdentifier
                + ", typeOfCancerId=" + typeOfCancerId + ", publicStudy=" + publicStudy + "]";
    }

    public boolean hasMutSigData() throws DaoException {
        return !DaoMutSig.getInstance().hasMutsig(this);
    }

    public boolean hasGisticData() throws DaoException {
        return DaoGistic.hasGistic(this);
    }

}
