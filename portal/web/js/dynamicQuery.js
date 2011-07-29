/******************************************************************************************
* Dynamic Query Javascript, built with JQuery
* @author Ethan Cerami
*
* This code performs the following functions:
*
* 1.  Connects to the portal via AJAX and downloads a JSON document containing information
*     regarding all cancer studies and gene sets stored in the CGDS.
* 2.  Creates event handler for when user selects a cancer study.  This triggers updates
      in the genomic profiles and case lists displayed.
* 3.  Creates event handler for when user selects a gene set.  This triggers updates to the
      gene set text area.
******************************************************************************************/

//  Triggered only when document is ready.
$(document).ready(function(){

    //  Get Portal JSON Meta Data via JQuery AJAX
    jQuery.getJSON("portal_meta_data.json",function(json){
        //  Store JSON Data in global variable for later use
        window.metaDataJson = json;

        //  Add Meta Data to current page
        addMetaDataToPage();
    });  //  end getJSON function


     //  Set up Event Handler for User Selecting Cancer Study from Pull-Down Menu
     $("#select_cancer_type").change(function() {
         cancerStudySelected();
     });

    // Set up Event Handler for User Selecting a Case Set
    $("#select_case_set").change(function() {
        caseSetSelected();
    });

    // Set up Event Handler for User Selecting a Get Set
    $("#select_gene_set").change(function() {
        geneSetSelected();
    });

    //  Set up Event Handler for View/Hide JSON Debug Information
    $("#json_cancer_studies").click(function(event) {
      event.preventDefault();
      $('#cancer_results').toggle();
    });

    //  Set up Event Handler for View/Hide Query Form, when it is on the results page
    $("#toggle_query_form").click(function(event) {
      event.preventDefault();
      $('#query_form_on_results_page').toggle();

      //  Toggle the icons
      $(".query-toggle").toggle();
    });

    //  set toggle Step 5: Optional arguments
    $("#optional_args").hide();
    $("#step5_toggle").click(function(event) {
        event.preventDefault();
        $("#optional_args").toggle( "blind" );
    });

    //  Set up an Event Handler to intercept form submission
    $("#main_form").submit(function() {
       chooseAction();
    });

    //  Set up an Event Handler for the Query / Data Download Tabs
    $("#query_tab").click(function(event) {
       event.preventDefault();
        userClickedMainTab("tab_visualize")
    });
    $("#download_tab").click(function(event) {
       event.preventDefault();
       userClickedMainTab("tab_download");
    });

});  //  end document ready function

//  Triggered when the User Selects one of the Main Query or Download Tabs
function userClickedMainTab(tabAction) {
    console.log("Tab:  " + tabAction);

    //  Change hidden field value
    $("#tab_index").val(tabAction);

    //  Then, submit the form
    $("#main_form").submit();
}

//  When the page is first loaded, the default query will be a cross-cancer type
//  search in which the user will enter ONLY a gene list
function setDefaultQuery() {
    //  Hide form fields not used in cross-study queries
    if ($("#select_cancer_type").val() == 'all'){
        $('#step2').hide();
        $('#step3').hide();
        $('#step5').hide();
    }
}

//  Determine whether to submit a cross-cancer query or
//  a study-specific query
function chooseAction() {
    if ($("#select_cancer_type").val() == 'all'){
       $("#main_form").get(0).setAttribute('action','cross_cancer.do');
    } else {
       $("#main_form").get(0).setAttribute('action','index.do');
    }
}

//  Determine which radio button was clicked and
// automatically select the corresponding checkbox
function selectCheckbox(subgroupClicked) {
    var subgroupClass = subgroupClicked.attr('class');
    var checkboxSelector = "input."+subgroupClass+"[type=checkbox]";
    $(checkboxSelector).attr('checked',true);
}

//  Triggered when a genomic profile is unselected;
//  make sure subrgoups are also unselected
function unselectAllSubgroups(profileUnselected) {
    var profileClass = profileUnselected.attr('class');
    var radioSelector = "input."+profileClass+"[type=radio]";
    $(radioSelector).attr('checked',false);
}

//  Triggered when a cancer study has been selected, either by the user
//  or programatically.
function cancerStudySelected() {
    var cancerStudyId = $("#select_cancer_type").val();
    if (cancerStudyId=='all'){
        setDefaultQuery();
        return;
    }

    var cancer_study = window.metaDataJson.cancer_studies[cancerStudyId];

    //  Update Cancer Study Description
    $("#cancer_study_desc").html("<p> " + cancer_study.description + "</p>");

    //  Iterate through all genomic profiles
    //  Add all non-expression profiles where show_in_analysis_tab = true
    //  First, clear all existing options
    $("#genomic_profiles").html("");

    //  Add Genomic Profiles, in this order
    addGenomicProfiles(cancer_study.genomic_profiles, "MUTATION", "Mutation");
    addGenomicProfiles(cancer_study.genomic_profiles, "MUTATION_EXTENDED", "Mutation");
    addGenomicProfiles(cancer_study.genomic_profiles, "COPY_NUMBER_ALTERATION", "Copy Number");
    addGenomicProfiles(cancer_study.genomic_profiles, "MRNA_EXPRESSION", "mRNA Expression");

    //  Update the Case Set Pull-Down Menu
    //  First, clear all existing pull-down menus
    $("#select_case_set").html("");

    //  Iterate through all case sets
    //  Add each case set as an option, and include description as title, so that it appears
    //  as a tool-tip.
    jQuery.each(cancer_study.case_sets,function(i, case_set) {
        $("#select_case_set").append("<option class='case_set_option' value='"
                + case_set.id + "' title='"
                + case_set.description + "'>" + case_set.name + "</option>");
    }); //  end for each case study loop

    //  Add the user-defined case list option
    $("#select_case_set").append("<option class='case_set_option' value='-1' "
        + "title='Specify you own case list'>User-defined Case List</option>");

    //  Set up Tip-Tip Event Handler for Case Set Pull-Down Menu
    $(".case_set_option").tipTip({defaultPosition: "right", delay:"100", edgeOffset: 25});

    //  Set up Tip-Tip Event Handler for Genomic Profiles help
    $(".profile_help").tipTip({defaultPosition: "right", delay:"100", edgeOffset: 25});

    //  Show any hidden form fields
    if(!$("#step2").is(":visible")){
        $("#step2").show();
        $("#step3").show();
        $("#step5").show();
    }

    //  Set up Event Handler for checking checkboxes associated with radio buttons
    //  This can not be done on page load, because radio buttons are not found when
    //  cancer study selected is "all"
    $("input[type=radio]").click(function(){
        selectCheckbox($(this));
    });

    //  Set up an Event Handler for User unselecting a genomic profile
    $('input[type=checkbox]').click(function(){
        unselectAllSubgroups($(this));
    });
}

//  Triggered when a case set has been selected, either by the user
//  or programatically.
function caseSetSelected() {
    var caseSetId = $("#select_case_set").val();

    //  If user has selected the user-defined option, show the case list div
    //  Otherwise, make sure to hide it.
    if (caseSetId == "-1") {
        $("#custom_case_list_section").show();
    } else {
        $("#custom_case_list_section").hide();
    }
}

//  Triggered when a gene set has been selected, either by the user
//  or programatically.
function geneSetSelected() {
    //  Get the selected ID from the pull-down menu
    var geneSetId = $("#select_gene_set").val();

    //  Get the gene set meta data from global JSON variable
    var gene_set = window.metaDataJson.gene_sets[geneSetId];

    //  Set the gene list text area
    $("#gene_list").html(gene_set.gene_list);
}

//  Adds Meta Data to the Page.
//  Tiggered at the end of successful AJAX/JSON request.
function addMetaDataToPage() {

    json = window.metaDataJson;

    //  Iterate through all cancer studies
    jQuery.each(json.cancer_studies,function(key,cancer_study){

        //  Append to Cancer Study Pull-Down Menu
        var addCancerStudy = true;

        //  If the tab index is selected, and this is the all cancer studies option, do not show
        if (window.tab_index == "tab_download" && key == "all") {
            addCancerStudy = false;
        }
        if (addCancerStudy) {
            $("#select_cancer_type").append("<option value='" + key + "'>" + cancer_study.name + "</option>");
        }
        
    });  //  end 1st for each cancer study loop

    //  Add Gene Sets to Pull-down Menu
    jQuery.each(json.gene_sets,function(key,gene_set){
        $("#select_gene_set").append("<option value='" + key + "'>"
                + gene_set.name + "</option>");
    });  //  end for each gene set loop

    //  Set things up, based on currently selected cancer type
    jQuery.each(json.cancer_studies,function(key,cancer_study){
        // Set Selected Cancer Type, Based on User Parameter
        if (key == window.cancer_study_id_selected) {
            $("#select_cancer_type").val(key);
            cancerStudySelected();
        } 
    });  //  end 2nd for each cancer study loop

    //   Set things up, based on currently selected case set id
    if (window.case_set_id_selected != null && window.case_set_id_selected != "") {
        $("#select_case_set").val(window.case_set_id_selected);
    }
    caseSetSelected();

    //  Set things up, based on currently selected gene set id
    if (window.gene_set_id_selected != null && window.gene_set_id_selected != "") {
        $("#select_gene_set").val(window.gene_set_id_selected);    
    } else {
        $("#select_gene_set").val("user-defined-list");
    }

    //  Set things up, based on all currently selected genomic profiles
    //  To do so, we iterate through all input elements with the name = 'genetic_profile_ids'
    $("input:[name=genetic_profile_ids]").each(function(index) {
        //  val() is the value that or stable ID of the genetic profile ID
        var currentValue = $(this).val();

        //  if the user has this stable ID already selected, mark it as checked
        if (window.genomic_profile_id_selected[currentValue] == 1) {
            $(this).attr('checked','checked');
        }
    });  //  end for each genomic profile option

    setDefaultQuery();
}

// Adds the specified genomic profiles to the page.
// Code checks for three possibilities:
// 1.  0 profiles of targetType --> show nothing
// 2.  1 profile of targetType --> show as checkbox
// 3.  >1 profiles of targetType --> show group checkbox plus radio buttons
function addGenomicProfiles (genomic_profiles, targetAlterationType, targetTitle) {
    var numProfiles = 0;
    var profileHtml = "";
    var downloadTab = false;

    //  Determine whether we are in the download tab
    if (window.tab_index == "tab_download") {
        downloadTab = true;
    }

    //  First count how many profiles match the targetAltertion type
    jQuery.each(genomic_profiles,function(key, genomic_profile) {
        if (genomic_profile.alteration_type == targetAlterationType) {
            if (downloadTab || genomic_profile.show_in_analysis_tab == true)
            numProfiles++;
        }
    }); //  end for each genomic profile loop

    if (numProfiles == 0) {
        return;
    } else if(numProfiles >1 && downloadTab == false) {
        //  If we have more than 1 profile, output group checkbox
        //  assign a class to associate the checkbox with any subgroups (radio buttons)
        profileHtml += "<input type='checkbox' class='" + targetAlterationType + "'>"
         + targetTitle + " data."
            + " Select one of the profiles below:";
        profileHtml += "<div class='genomic_profiles_subgroup'>";
    }

    //  First count how many profiles match the targetAltertion type
    jQuery.each(genomic_profiles,function(key, genomic_profile) {
        if (genomic_profile.alteration_type == targetAlterationType) {
            if (downloadTab || genomic_profile.show_in_analysis_tab == true) {
                //  Branch depending on number of profiles
                var optionType = "checkbox";
                if (downloadTab) {
                    optionType = "radio";
                } else {
                    if (numProfiles == 1) {
                        optionType = "checkbox";
                    } else if (numProfiles > 1) {
                        optionType = "radio";
                    }
                }
                profileHtml += outputGenomicProfileOption (optionType, targetAlterationType,
                        genomic_profile.id, genomic_profile.name, genomic_profile.description);                
            }
        }
    }); //  end for each genomic profile loop

    if(numProfiles >1) {
        //  If we have more than 1 profile, output the end div tag
        profileHtml += "</div>";
    }
    $("#genomic_profiles").append(profileHtml);
}

// Outputs a Single Genomic Profile Options
function outputGenomicProfileOption (optionType, targetAlterationType, id, name, description) {
    var html =  "<input type='" + optionType + "' class='" + targetAlterationType + "' "
        + "name='genetic_profile_ids' group='" + targetAlterationType + "'"
        + "value='" + id +"'>" + name + "</input>"
        + "  <img class='profile_help' src='images/help.png' title='"
        + description + "'><br/>";
    return html;
}
