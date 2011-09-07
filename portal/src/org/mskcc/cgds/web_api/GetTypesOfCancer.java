package org.mskcc.cgds.web_api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoTypeOfCancer;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.TypeOfCancer;
import org.mskcc.cgds.model.CancerStudy;

/**
 * Get Types of Cancer and Cancer Studies.
 * Used by the Web API.
 * 
 * @author Ethan Cerami, Arthur Goldberg.
 */
public class GetTypesOfCancer {

    /**
     * Gets all Types of Cancer.
     *
     * @return Tab-Delimited Text.
     * @throws DaoException Database AccessException.
     * @throws ProtocolException Protocol Error.
     */
    public static String getTypesOfCancer() throws DaoException, ProtocolException {
        ArrayList<TypeOfCancer> typeOfCancerList = DaoTypeOfCancer.getAllTypesOfCancer();
        StringBuffer buf = new StringBuffer();
        if (typeOfCancerList == null || typeOfCancerList.size() == 0) {
            throw new ProtocolException ("No Types of Cancer Available.");
        } else {
            buf.append("type_of_cancer_id\tname\n");
            for (TypeOfCancer typeOfCancer : typeOfCancerList) {
                buf.append(typeOfCancer.getTypeOfCancerId() + WebApiUtil.TAB);
                buf.append(typeOfCancer.getName() +  WebApiUtil.NEW_LINE);
            }
            return buf.toString();
        }
    }

    /**
     * Gets all Cancer Studies.
     *
     * @return Tab-Delimited Text.
     * @throws DaoException Database Access Error.
     * @throws ProtocolException Protocol Error.
     */
    public static String getCancerStudies() throws DaoException, ProtocolException {
        ArrayList<CancerStudy> cancerStudyList = DaoCancerStudy.getAllCancerStudies();

        if (cancerStudyList == null || cancerStudyList.size() ==0) {
            throw new ProtocolException ("No Cancer Studies Available.");
        } else {
            //  Before returning the list, sort it alphabetically
            Collections.sort(cancerStudyList, new CancerStudiesComparator());

            StringBuffer buf = new StringBuffer();
            buf.append("cancer_study_id\tname\tdescription\n");

            //  Iterate through all cancer studies
            for (CancerStudy cancerStudy : cancerStudyList) {
                buf.append(cancerStudy.getCancerStudyStableId() +  WebApiUtil.TAB);
                buf.append(cancerStudy.getName() +  WebApiUtil.TAB);
                buf.append(cancerStudy.getDescription() + WebApiUtil.NEW_LINE);
            }
            return buf.toString();
        }
    }
}

/**
 * Compares Cancer Studies, so that we can sort them alphabetically.
 */
class CancerStudiesComparator implements Comparator {

    /**
     * Compare two cancer studies.
     * @param o  First Cancer Study.
     * @param o1 Second Cancer Study.
     * @return int indicating name sort order.
     */
    public int compare(Object o, Object o1) {
        CancerStudy study0 = (CancerStudy) o;
        CancerStudy study1 = (CancerStudy) o1;
        return study0.getName().compareTo(study1.getName());
    }
}