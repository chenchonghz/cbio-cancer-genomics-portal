package org.mskcc.cgds.util;

import org.jasypt.util.password.BasicPasswordEncryptor;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoSecretKey;
import org.mskcc.cgds.dao.DaoUserAccessRight;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.SecretKey;
import org.mskcc.cgds.web_api.ProtocolException;

import java.util.ArrayList;

/**
 * Utilities for managing access control.
 *
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class AccessControl {

    /**
     * takes a cleartext key, encrypts it and adds the encrypted key to the dbms.
     *
     * @param userKey
     * @return a SecretKey constructed from the encrypted key
     * @throws DaoException
     */
    public static SecretKey createSecretKey(String userKey) throws DaoException {

        BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
        String encryptedPassword = passwordEncryptor.encryptPassword(userKey);
        SecretKey secretKey = new SecretKey();
        secretKey.setEncryptedKey(encryptedPassword);
        DaoSecretKey.addSecretKey(secretKey);
        return secretKey;
    }

    /**
     * return true if userKey is one of the secret keys; false otherwise.
     * assumes few keys.
     *
     * @param userKey
     * @return
     * @throws DaoException
     */
    public static boolean checkKey(String userKey) throws DaoException {
        BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();

        ArrayList<SecretKey> allKeys = DaoSecretKey.getAllSecretKeys();
        for (SecretKey secretKey : allKeys) {
                if (passwordEncryptor.checkPassword(userKey, secretKey.getEncryptedKey())) {
                    return true;
                }
        }
        return false;
    }

    /**
     * Return true if the user can access the study, false otherwise.
     * Also works properly if no user is specified.
     * <p/>
     * To avoid circumvention of security controls, ALL cancer study access should pass through this function.
     *
     * @param email   the user's email address, or null if no user is specified (no user is logged in).
     * @param stableStudyId
     * @return true if the user can access the study identified by studyId now, false otherwise
     * @throws DaoException
     */
    public static boolean checkAccess(String email, String key, String stableStudyId) throws DaoException {

        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(stableStudyId);
        if (null == cancerStudy) {
            return false;
        }
        if (cancerStudy.isPublicStudy()) {
            return true;
        }

        // is secret key good?
        if (!checkKey(key)) {
            return false;
        }

        // does UserAccessRight contain user, studyId?
        return DaoUserAccessRight.containsUserAccessRightsByEmailAndStudy(email,
                cancerStudy.getStudyId());
    }

    /**
     * Gets Cancer Studies.
     *
     * @param email     Email Identifier.
     * @param key       Key.
     * @return Cancer Studies Table.
     * @throws DaoException         Database Error.
     * @throws ProtocolException    Protocol Error.
     */
    public static String getCancerStudies(String email, String key) throws DaoException, ProtocolException {

        ArrayList<CancerStudy> cancerStudyList = DaoCancerStudy.getAllCancerStudies();
        ArrayList<CancerStudy> accessibleCancerStudies = new ArrayList<CancerStudy>();
        for (CancerStudy cancerStudy : cancerStudyList) {
            if (checkAccess(email, key, cancerStudy.getCancerStudyIdentifier())) {
                accessibleCancerStudies.add(cancerStudy);
            }
        }

        StringBuffer buf = new StringBuffer();
        if (accessibleCancerStudies.size() > 0) {

            buf.append("cancer_study_id\tname\tdescription\n");
            for (CancerStudy cancerStudy : accessibleCancerStudies) {
                
                // changed to output stable identifier, instead of internal integer identifer.
                buf.append(cancerStudy.getCancerStudyIdentifier() + "\t");
                buf.append(cancerStudy.getName() + "\t");
                buf.append(cancerStudy.getDescription() + "\n");
            }
            return buf.toString();
        } else {
            throw new ProtocolException("No cancer studies accessible; either provide credentials to access private studies, " +
                    "or ask administrator to load public ones.\n");
        }
    }

}