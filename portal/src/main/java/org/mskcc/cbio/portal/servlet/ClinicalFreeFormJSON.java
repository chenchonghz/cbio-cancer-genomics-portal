package org.mskcc.cbio.portal.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.mskcc.cbio.cgds.dao.DaoCancerStudy;
import org.mskcc.cbio.cgds.dao.DaoClinicalFreeForm;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.CancerStudy;
import org.mskcc.cbio.cgds.model.ClinicalFreeForm;
import org.mskcc.cbio.cgds.model.ClinicalParameterMap;
import org.mskcc.cbio.portal.util.CategoryLabelReader;
import org.owasp.validator.html.PolicyException;

public class ClinicalFreeFormJSON extends HttpServlet
{
	public static final String STUDY_ID = "studyId";
	
	private ServletXssUtil servletXssUtil;

    /**
     * Initializes the servlet.
     */
    public void init() throws ServletException {
        super.init();
        try {
            servletXssUtil = ServletXssUtil.getInstance();
        } catch (PolicyException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Handles HTTP GET Request.
     */
    protected void doGet(HttpServletRequest httpServletRequest,
    		HttpServletResponse httpServletResponse)throws ServletException,
            IOException {
        
        String studyId = httpServletRequest.getParameter(STUDY_ID);
        
        try
        {
        	 // final object to be send as JSON
        	 JSONObject jsonObject = new JSONObject();
        	
        	 CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByStableId(studyId);
        	 
        	 // check if cancerStudy exists
        	 if (cancerStudy == null)
        	 {
        		 // just create empty collections if cancers study cannot be found
        		 jsonObject.put("clinicalCaseSet", new JSONArray());
        		 jsonObject.put("categoryMap", new HashMap<String, Object>());
        		 jsonObject.put("freeFormData", new JSONArray());
        	 }
        	 else
        	 {
        		 DaoClinicalFreeForm daoClinicalFreeForm = new DaoClinicalFreeForm();
                 
                 HashSet<String> clinicalCaseSet = 
                		 daoClinicalFreeForm.getAllCases(cancerStudy.getInternalId());
                 
                 HashSet<String> paramSet = 
                		 daoClinicalFreeForm.getDistinctParameters(cancerStudy.getInternalId());
                 
                 List<ClinicalFreeForm> freeFormData = 
                		 daoClinicalFreeForm.getCasesByCancerStudy(cancerStudy.getInternalId());

                 // map of <param, distinctCategorySet> pairs
                 Map<String, Object> categoryMap = new HashMap<String, Object>();
                 
                 // array of clinical case IDs 
                 JSONArray caseIds = new JSONArray();
                 
                 // add the clinical case set
                 for (String caseId : clinicalCaseSet)
                 {
                	 caseIds.add(caseId);
                 }
                 
                 jsonObject.put("clinicalCaseSet", caseIds);
                 
                 // get all distinct categories
                 for (String param : paramSet) {
                     ClinicalParameterMap paramMap = daoClinicalFreeForm.getDataSlice(cancerStudy.getInternalId(), param);
                     HashSet<String> distinctCategorySet = paramMap.getDistinctCategories();
                     JSONArray distinctCategories = new JSONArray();
                     
                     for (String category : distinctCategorySet)
                     {
                    	 if (category.trim().length() > 0)
                    	 {
                    		 distinctCategories.add(category);
                    	 }
                     }
                     
                     categoryMap.put(CategoryLabelReader.safeCategoryName(param),
                    		 distinctCategories);
                 }
                 
                 // add the category map
                 jsonObject.put("categoryMap", categoryMap);
                 
                 // array of free form data
                 JSONArray freeFormArray = new JSONArray();
                 
                 // get all clinical free form data for the specified cancer study
                 for (ClinicalFreeForm data : freeFormData)
                 {
                	 JSONObject freeFormObject = new JSONObject();
                	 
                	 //freeFormObject.put("cancerStudyId", data.getCancerStudyId());
                	 freeFormObject.put("caseId", data.getCaseId());
                	 freeFormObject.put("paramName", CategoryLabelReader.safeCategoryName(data.getParamName()));
                	 freeFormObject.put("paramValue", data.getParamValue());
                	 
                	 freeFormArray.add(freeFormObject);
                 }
                 
                 // add the free form data array
                 jsonObject.put("freeFormData", freeFormArray);
                 
                 // add the map for human readable category names
                 jsonObject.put("categoryLabelMap", CategoryLabelReader.getInstace().getCategoryLabelMap());
        	 }
            
             httpServletResponse.setContentType("application/json");
             PrintWriter out = httpServletResponse.getWriter();
             
             try
             {
            	 JSONValue.writeJSONString(jsonObject, out);
             }
             finally
             {
                 out.close();
             }
        }
        catch (DaoException e) 
        {
            throw new ServletException(e);
        }
    }
}
