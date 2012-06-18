package org.mskcc.portal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoClinicalFreeForm;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.CaseList;
import org.mskcc.portal.remote.GetCaseSets;

/**
 * Utility class for validation of the user-defined case sets.
 * 
 * @author Selcuk Onur Sumer
 */
public class CaseSetValidator
{
	/**
	 * Checks whether the provided case IDs are valid for a specific
	 * cancer study. This method returns a list of invalid cases if
	 * any, or returns an empty list if all the cases are valid.
	 * 
	 * @param studyId			stable cancer study id
	 * @param caseIds			case IDs as a single string
	 * @return					list of invalid cases
	 * @throws DaoException		if a DB error occurs
	 */
	public static List<String> validateCaseSet(String studyId,
			String caseIds) throws DaoException
	{
		ArrayList<String> invalidCases = new ArrayList<String>();
		DaoClinicalFreeForm daoFreeForm = new DaoClinicalFreeForm();
		
		// get list of all case sets for the given cancer study
		ArrayList<CaseList> caseLists = GetCaseSets.getCaseSets(studyId);
		
		// get cancer study for the given stable id
		CancerStudy study = DaoCancerStudy.getCancerStudyByStableId(studyId);
		
		// get all cases in the clinical free form table for the given cancer study
		Set<String> freeFormCases = daoFreeForm.getAllCases(study.getInternalId());
		
		if (!caseLists.isEmpty() &&
			caseIds != null)
		{
			// validate each case ID
			for(String caseId: caseIds.trim().split("\\s+"))
			{
				boolean valid = false;
				
				// search all lists for the current case
				for (CaseList caseList: caseLists)
				{
					// if the case is found in any of the lists,
					// then it is valid, no need to search further
					if(caseList.getCaseList().contains(caseId))
					{
						valid = true;
						break;
					}
				}
				
				// search also clinical free form table for the current case
				if (freeFormCases.contains(caseId))
				{
					valid = true;
				}
				
				// if the case cannot be found in any of the lists,
				// then it is an invalid case for this cancer study
				if (!valid)
				{
					invalidCases.add(caseId);
				}
			}
		}
		
		return invalidCases;
	}
}
