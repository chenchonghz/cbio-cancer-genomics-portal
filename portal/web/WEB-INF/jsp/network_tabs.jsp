<div id="network_tabs" class="hidden-network-ui">
    <ul>
        <li><a href="#genes_tab"><span>Genes</span></a></li>
        <li><a href="#relations_tab"><span>Interactions</span></a></li>
        <li><a href="#help_tab"><span>Help</span></a></li>
    </ul>
    <div id="genes_tab">
	    <div class="header">
	    	<div id="slider_area">
	    		<label>Filter by Total Alteration (%)</label>
	    		<div id="weight_slider_area">
		    		<span class="slider-value">
		    			<input id="weight_slider_field" type="text" value="0"/>
		    		</span>
		    		<span class="slider-min"><label>0</label></span>
		    		<span class="slider-max"><label>100</label></span>
		    		<div id="weight_slider_bar"></div>
	    		</div>
	    		<label>Affinity</label>
	    		<div id="affinity_slider_area">
	    			<span class="slider-value">
	    				<input id="affinity_slider_field" type="text" value="0.80"/>
	    			</span>
	    			<span class="slider-min"><label>0</label></span>
		    		<span class="slider-max"><label>1.0</label></span>
		    		<div id="affinity_slider_bar"></div>
	    		</div>
    		</div>
    		<div id="control_area">
				
							<input type="button" id="filter_genes" class="image-button" value="" title="Hide Selected"/>
							<input type="button" id="crop_genes" class="image-button" value="" title="Crop Selected"/>
							<input type="button" id="unhide_genes" class="image-button" value="" title="Unhide All"/>
							<input type="text" id="search" value=""/>
							<input type="button" id="search_genes" class="image-button" value="" title="Search"/>
				
			</div>
		</div>
    </div>
    <div id="relations_tab">
		<div>
	        <table id="edge_type_filter">
	        	<tr class="edge-type-header">
	        		<td>
	        			<label class="heading">Type:</label>
	        		</td>
	        	</tr>
	        	<tr class="in-same-component">
		        	<td class="edge-type-checkbox">
		        		<input type="checkbox" checked="checked">
		        		<label>In Same Component</label>
		        	</td>
	        	</tr>
	        	<tr class="in-same-component">
	        		<td>
	        			<div class="percent-bar"></div>	        			
	        		</td>
	        		<td>
	        			<div class="percent-value"></div>
	        		</td>
	        	</tr>
	        	<tr class="reacts-with">
		        	<td class="edge-type-checkbox">
		        		<input type="checkbox" checked="checked">
		        		<label>Reacts With</label>
		        	</td>
	        	</tr>
	        	<tr class="reacts-with">
	        		<td>
	        			<div class="percent-bar"></div>	        			
	        		</td>
	        		<td>
	        			<div class="percent-value"></div>
	        		</td>
	        	</tr>
	        	<tr class="state-change">
		        	<td class="edge-type-checkbox">
		        		<input type="checkbox" checked="checked">
		        		<label>State Change</label>
		        	</td>
	        	</tr>
	        	<tr class="state-change">
	        		<td>
	        			<div class="percent-bar"></div>	        			
	        		</td>
	        		<td>
	        			<div class="percent-value"></div>
	        		</td>
	        	</tr>
	        	<tr class="other">
		        	<td class="edge-type-checkbox">
		        		<input type="checkbox" checked="checked">
		        		<label>Other</label>
		        	</td>
	        	</tr>
	        	<tr class="other">
	        		<td>
	        			<div class="percent-bar"></div>	        			
	        		</td>
	        		<td>
	        			<div class="percent-value"></div>
	        		</td>
	        	</tr>
	        </table>
	        <table id="edge_source_filter">
	        	<tr class="edge-source-header">
	        		<td>
	        			<label class="heading">Source:</label>
	        		</td>
	        	</tr>
	        </table>
	    </div>
        <div class="footer">
        	<label class="heading">Update</label>
			<input type="button" id="update_edges" class="image-button" value="" title="Update"/>
		</div>
    </div>
    <div id="help_tab">
        <jsp:include page="network_help.jsp"></jsp:include>
    </div>
</div>

<div id="node_inspector" class="hidden-network-ui" title="Node Inspector">
	<div id="node_inspector_content" class="content ui-widget-content">
		<table class="data"></table>
		<table class="profile-header"></table>
		<table class="profile"></table>
		<table class="xref"></table>
	</div>
</div>

<div id="node_legend" class="hidden-network-ui" title="Gene Legend">
	<div id="node_legend_content" class="content ui-widget-content">
		<img src="images/network/gene_legend.png"/>
	</div>
</div>

<div id="edge_inspector" class="hidden-network-ui" title="Edge Inspector">
	<div id="edge_inspector_content" class="content ui-widget-content">
		<table class="data"></table>
		<table class="xref"></table>
	</div>
</div>

<div id="edge_legend" class="hidden-network-ui" title="Interaction Legend">
	<div id="edge_legend_content" class="content ui-widget-content">
		<img src="images/network/interaction_legend.png"/>
	</div>
</div>

<% /*
<div id="edge_legend" class="hidden-network-ui" title="Interaction Legend">
	<div id="edge_legend_content" class="content ui-widget-content">
		<table id="edge_type_legend">
			<tr class="edge-type-header">
	        	<td>
	        		<strong>Edge Types:</strong>
	        	</td>
	        </tr>
        	<tr class="in-same-component">
        		<td class="label-cell">
        			<div class="type-label">In Same Component</div>
        		</td>
        		<td class="color-cell">
        			<div class="color-bar"></div>
        		</td>
        	</tr>
        	<tr class="reacts-with">
        		<td class="label-cell">
        			<div class="type-label">Reacts With</div>
        		</td>
        		<td class="color-cell">
        			<div class="color-bar"></div>
        		</td>
        	</tr>
        	<tr class="state-change">
        		<td class="label-cell">
        			<div class="type-label">State Change</div>
        		</td>
        		<td class="color-cell">
        			<div class="color-bar"></div>
        		</td>
        	</tr>
        	<tr class="other">
        		<td class="label-cell">
        			<div class="type-label">Other</div>
        		</td>
        		<td class="color-cell">
        			<div class="color-bar"></div>
        		</td>
        	</tr>
        	<tr class="merged-edge">
        		<td class="label-cell">
        			<div class="type-label">Merged (with different types) </div>
        		</td>
        		<td class="color-cell">
        			<div class="color-bar"></div>
        		</td>
        	</tr>
        </table>
	</div>
</div>
*/ %>
