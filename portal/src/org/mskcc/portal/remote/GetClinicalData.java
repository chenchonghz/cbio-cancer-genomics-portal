package org.mskcc.portal.remote;

import org.mskcc.cgds.dao.DaoClinicalData;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.ClinicalData;
import org.mskcc.portal.util.XDebug;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

/**
 * Gets clinical data for specified cases.
 */
public class GetClinicalData {
    public static final String NA = "NA";

    /**
     * Gets clinical data for specified cases.
     * Note: getClinicalData takes a string of CaseIds. However,
     * the DAO object takes a HashSet of cases, meaning this
     * string must be split and converted to a HashSet.
     * TODO: If string caseIDs is not originally separated by spaces, correct separator.
     *
     * @param caseIds Case IDs.
     * @return an ArrayList of ClinicalData Objects
     * @throws DaoException, as of August 2011 GetClinicalData has direct access to DAO Objects.
     */
    public static ArrayList<ClinicalData> getClinicalData(String caseIds, XDebug xdebug) throws DaoException {
        if (caseIds != null && caseIds.length() > 0){
        try {
            DaoClinicalData daoClinicalData = new DaoClinicalData();
            String caseIdList[] = caseIds.split(" ");
            //check to make sure caseIdList != null, then convert to caseSet, pass through DAO and return
            //an arraylist retrieved from the DAO.
            if (caseIdList.length > 0){
            Set<String> caseSet = new HashSet<String>(Arrays.asList(caseIdList));
            return daoClinicalData.getCases(caseSet);
            }
        } catch (DaoException e) {
            System.err.println("Database Error: " + e.getMessage());
            return null;
        }
        }
    return new ArrayList<ClinicalData>();
    }
}