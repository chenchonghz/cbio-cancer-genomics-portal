// threshold for the size of the distinct category set (TODO subject to change)
var CATEGORY_SET_THRESHOLD = 10;

// TODO more categories should be added into this map, do we have complete list of categories?
// map of category names for more readable labels
var CATEGORY_LABEL_MAP = {"TUMORRESIDUALDISEASE" : "Tumor Residual Disease",
		"TUMORGRADE" : "Tumor Grade",
		"VITALSTATUS" : "Vital Status",
		"PlatinumStatus" : "Platinum Status",
		"TUMORSTAGE" : "Tumor Stage",
		"PERSONNEOPLASMCANCERSTATUS" : "Person Neoplasm Cancer Status",
		"ProgressionFreeStatus" : "Progression Free Status",
		"PRIMARYTHERAPYOUTCOMESUCCESS" : "Primary Therapy Outcome Success"};

// map to store user selected category parameters (used for case filtering)
var _customCaseSelection;

// free form data array for the selected cancer study, in other words
// a slice from the clinical_free_form table
var _freeFormData;

// set of all clinical cases (case IDs) for a specific cancer study
var _clinicalCaseSet;

// this (two-dimensional array) filter is used to filter each category set individually,
// the actual filtered case set is the intersection of all arrays (sets) in this filter  
var _caseSetFilter;

// custom case set containing filter (final) result of user selection 
var _customCaseSet;

/**
 * Initializes the Modal Dialog and event handlers for custom case set building.
 */
function initCustomCaseSetUI()
{
	// set up modal dialog box for custom case set building (for step 3)
    $("#custom_case_set_dialog").dialog({autoOpen: false, 
		resizable: false,
		modal: true,
		width: 580});
    
    $("#submit_custom_case_set").click(buildCustomCaseSet);
    
    $("#cancel_custom_case_set").click(function(evt){
    	 $("#custom_case_set_dialog").dialog("close");
    });
}

/**
 * Displays the modal dialog for building custom case set.
 */
function promptCustomCaseSetBuilder()
{
	var cancerStudyId = $("#select_cancer_type").val();
    var cancerStudy = window.metaDataJson.cancer_studies[cancerStudyId];

    // update cancer study name
    $("#case_set_dialog_header #selected_cancer_study").empty();
    $("#case_set_dialog_header #selected_cancer_study").append(cancerStudy.name);
	
    // prepare data to be sent to server
    var data = {studyId: cancerStudyId};
    
	// populate contents of the dialog box
    jQuery.getJSON("ClinicalFreeForm.json", data, function(json){
    	$("#case_set_dialog_header #number_of_cases").empty();
    	$("#case_set_dialog_header #number_of_cases").append(
    			'<span id="current_number_of_cases">' + json.clinicalCaseSet.length + '</span>' +
    			' (out of ' + json.clinicalCaseSet.length + ')');
    	
    	// clear the dialog content
    	$("#case_set_dialog_content").empty();
    	
    	var categorySet = json.categoryMap;
    	
    	for (var category in categorySet)
    	{
    		// skip numeric values if the size of the distinct category set exceeds the threshold value
    		// TODO instead of checking the first element, it may be proper to check first CATEGORY_SET_THRESHOLD elements to avoid incorrect classification of text values such as "Missing".
    		if (categorySet[category].length > 0 &&
    			(categorySet[category].length < CATEGORY_SET_THRESHOLD || isNaN(categorySet[category][0])))
    		{
    			// append selection (multi dropdown) box for the current category (parameter)
    			$("#case_set_dialog_content").append('<tr><td align="right">' + humanReadableCategory(category) + '</td>' +
    				'<td align="left"><select multiple id="select_' + category + '"></select></td></tr>');
    			
    			// append special (select all) checkbox to enable selection/deselection of values
    			$("#case_set_dialog_content #select_" + category).append(
       					'<option selected id="' + category + '_selectAll" ' +
       					'value="' + category + '_selectAll" ' + '>' +
       					'<label>(select all)</label>' +
       					'</option>');
    			
    			// append all other parameter values for the current category    			
	       		for (var i=0; i < categorySet[category].length; i++)
	       		{
	       			$("#case_set_dialog_content #select_" + category).append(
	       					'<option id="' + categorySet[category][i] + '" ' +
	       					'value="' + categorySet[category][i] + '" ' + '>' +
	       					'<label>' + categorySet[category][i] + '</label>' +
	       					'</option>');
	       		}
	       		
	       		// set the dropdown box options
	       		var dropdownOptions = {firstItemChecksAll: true, // enables "select all" button
	       				icon: {placement: 'left'}, // sets the position of the arrow
	       				width: 268,
	       				emptyText: '(none selected)', // text to be displayed when no item is selected
	       				onComplete: refreshCustomCaseSet}; // callback function for the action
	       		
	       		// initialize the dropdown box
	       		$("#case_set_dialog_content #select_" + category).dropdownchecklist(dropdownOptions);
    		}
    	}
    	
    	// store required data as global variables for future reference
    	_freeFormData = json.freeFormData;
    	_clinicalCaseSet = json.clinicalCaseSet;
    	
    	// initialize custom case selection map
    	initCustomCaseSelectionMap(categorySet);
    	
    	// initialize case filter sets
    	initCaseSetFilter(categorySet, json.clinicalCaseSet);
    	
    });
    
	$("#custom_case_set_dialog").dialog("open");
}

/**
 * Initializes custom case selection map for the given category set.
 * 
 * @param categorySet	category set containing parameters (categories)
 */
function initCustomCaseSelectionMap(categorySet)
{
	_customCaseSelection = new Array();
	
	for (var category in categorySet)
	{
		_customCaseSelection["select_" + category] = new Array();
		
		for (var i=0; i < categorySet[category].length; i++)
		{
			_customCaseSelection["select_" + category][categorySet[category][i]] = true;
		}
	}
}

/**
 * Initializes the case set filter.
 */
function initCaseSetFilter(categorySet, clinicalCaseSet)
{
	_caseSetFilter = new Array();
	
	// initially all individual filter set will contain all case IDs by default
	for (var category in categorySet)
	{
		_caseSetFilter[category] = clinicalCaseSet.slice();
	}
}

/**
 * Updates the custom case set according to the new user selection.
 *  
 * @param selector	target selection box modified by the user
 */
function refreshCustomCaseSet(selector)
{
	var selectAll = false;
	
	// update custom case selection map
	for(var i = 0; i < selector.options.length; i++)
	{
		// special "select all" option
		if (selector.options[i].value.indexOf("_selectAll") != -1)
		{
			// if select all is selected, no need to process other selections
			if (selector.options[i].selected)
			{
				selectAll = true;
				break;
			}
		}
		// all options other than _selectAll
		else
		{
			// update selection map
			_customCaseSelection[selector.id][selector.options[i].value] =
				selector.options[i].selected;
		}
		
		//console.log("[" + i + "] " + selector.id + "," + selector.options[i].value);
    }
	
	// extract category id from selector id
	var category = selector.id.substring(selector.id.indexOf('_') + 1);
	
	// reset & update corresponding (individual) filter set
	
	if (selectAll)
	{
		// add all cases without filtering
		_caseSetFilter[category] = _clinicalCaseSet.slice();
	}
	else
	{
		_caseSetFilter[category] = new Array();
		
		// since free form data contains a single parameter (category) and
		// value pair per row, we should iterate all the table to filter cases
		for(var i = 0; i < _freeFormData.length; i++)
		{
			if (_freeFormData[i].paramName != category)
			{
				// skip parameters other than the selected category
				continue;
			}
			
			// get the category map corresponding to the current parameter name
			var categoryMap = _customCaseSelection["select_" + category];
			
			// check if parameter value (corresponding to the current case) is
			// selected by the user
			if (categoryMap != null &&
				categoryMap[_freeFormData[i].paramValue])
			{	
				// add the case (patient) to the set
				_caseSetFilter[category].push(_freeFormData[i].caseId);
			}
		}
	}
	
	// TODO debug (remove before commit)
	console.log(_caseSetFilter);
	
	// update the case set by taking intersection of all individual parameter sets
	_customCaseSet = intersectAllCaseSets(_caseSetFilter);
	
	// update current number of included cases
	$("#case_set_dialog_header #current_number_of_cases").text(_customCaseSet.length);
}

/**
 * Intersects all sets, each of which is filtered individually for
 * a specific category, in the given caseSetFilter array, and returns
 * the resulting set as an array.
 *
 * @param caseSetFiler	array of sets containing case IDs
 * @returns				intersection of all sets as an array
 */
function intersectAllCaseSets(caseSetFilter)
{
	var intersection = _clinicalCaseSet.slice();
	
	for (var key in caseSetFilter)
	{
		intersection = intersectNArrays(intersection, caseSetFilter[key]);
	}
	
	return intersection;
}

/**
 * Creates a custom case set with respect to the input from the user
 */
function buildCustomCaseSet()
{
	// final update on the custom case set
	_customCaseSet = intersectAllCaseSets(_caseSetFilter);
	
	// close custom case set builder dialog
	$("#custom_case_set_dialog").dialog("close");
	
	//select "User-defined Case List"
	$("#select_case_set .case_set_option").each(function(index){		
		if ($(this).val() == -1)
		{
			$(this).attr("selected", "selected");
		}
		else
		{
			$(this).removeAttr("selected");
		}
	})
	
	//enter case ids into the text field of custom case set ids
	
	var caseIds = "";
	
	$("#custom_case_set_ids").empty();
	
	for (var i = 0; i < _customCaseSet.length; i++)
	{
		caseIds += _customCaseSet[i] + " ";
	}
	
	$("#custom_case_set_ids").append(jQuery.trim(caseIds));
	
	// this is necessary to show the custom case list
	caseSetSelected();
}

/**
 * Converts the given category name to a more readable text.
 * 
 * @param category	category name from the data
 * @returns			nicely formatted label for the given category
 */
function humanReadableCategory(category)
{
	var label = CATEGORY_LABEL_MAP[category];
	
	if (label == null)
	{
		label = category;
	}
	
	return label;
}
