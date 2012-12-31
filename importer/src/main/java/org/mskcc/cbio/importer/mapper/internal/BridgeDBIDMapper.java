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

// package
package org.mskcc.cbio.importer.mapper.internal;

// imports
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.model.ReferenceMetadata;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.bridgedb.Xref;
import org.bridgedb.BridgeDb;
import org.bridgedb.IDMapper;
import org.bridgedb.DataSource;

import java.util.Set;
import java.util.Collection;

/**
 * Class which provides bridgedb services.
 */
public class BridgeDBIDMapper implements org.mskcc.cbio.importer.IDMapper {

	// our logger
	private static Log LOG = LogFactory.getLog(BridgeDBIDMapper.class);

	// some statics to gene info column headers
	private static final String ENTREZ_COLUMN_HEADER = "GeneID";
	private static final String SYMBOL_COLUMN_HEADER = "Symbol";
	private static final String FLAT_FILE_CONNECTION_STRING = "idmapper-text:file:";

	// bridge db refs
	IDMapper idMapper;
	DataSource entrezDataSource;
	DataSource symbolDataSource;
	String connectionString;

	/**
	 * Constructor.
	 *
	 * @param humanGeneReferenceType
	 */
	public BridgeDBIDMapper(Config config, String humanGeneReferenceType) {

		if (LOG.isInfoEnabled()) {
			LOG.info("BridgeIDMapper(), human-gene ref-type: " + humanGeneReferenceType);
		}

		// get a ReferenceMetadata object for the given reference type.
		Collection<ReferenceMetadata> referenceMetadata =
			config.getReferenceMetadata(humanGeneReferenceType);

		// sanity check
		if (referenceMetadata.isEmpty()) {
			throw new IllegalArgumentException("cannot instantiate a proper ReferenceMetadata object for: " + humanGeneReferenceType);
		}

		// used when we init mapper (must come after construction)
		this.connectionString = FLAT_FILE_CONNECTION_STRING +
			referenceMetadata.iterator().next().getReferenceFile();
		if (LOG.isInfoEnabled()) {
			LOG.info("BridgeDBIDMapper(), connectionString: " + connectionString);
		}
	}

	/**
	 * For the given symbol, return id.
	 *
	 * @param geneSymbol String
	 * @return String
	 * @throws Exception
	 */
	@Override
	public String symbolToEntrezID(String geneSymbol) throws Exception {

		if (idMapper == null) initMapper();
		Xref ref = new Xref(geneSymbol, symbolDataSource);
		return (processResultSet(idMapper.mapID(ref, entrezDataSource)));
	}

	/**
	 * For the entrezID, return symbol.
	 *
	 * @param entrezID String
	 * @return String
	 * @throws Exception
	 */
	@Override
	public String entrezIDToSymbol(String entrezID) throws Exception {

		if (idMapper == null) initMapper();
		Xref ref = new Xref(entrezID, entrezDataSource);
		return (processResultSet(idMapper.mapID(ref, symbolDataSource)));
	}

	/**
	 * Helper function to process BridgeDB result set.
	 *
	 * @param xrefs Set<Xref>
	 * @return String
	 */
	private String processResultSet(Set<Xref> xrefs) {

		if (xrefs.isEmpty()) {
			return "";
		}
		else {
			if (xrefs.size() > 1) {
				if (LOG.isInfoEnabled()) {
					LOG.info("processResultSet(), WARNING: we have " + xrefs.size() + ", mappings, returning the first.");
				}
			}
		}

		// outta here
		return xrefs.iterator().next().getId();
	}

	/**
	 * Used to initialize the mapper.
	 *
	 * @throws Exception
	 */
	private void initMapper() throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("initMapper(), building data sources");
		}

		Class.forName("org.bridgedb.file.IDMapperText");
		idMapper = BridgeDb.connect(connectionString);
		entrezDataSource = DataSource.getByFullName(ENTREZ_COLUMN_HEADER);
		symbolDataSource = DataSource.getByFullName(SYMBOL_COLUMN_HEADER);

		if (LOG.isInfoEnabled()) {
			LOG.info("initMapper(), building data sources complete");
		}
	}
}
