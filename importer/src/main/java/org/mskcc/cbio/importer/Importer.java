// package
package org.mskcc.cbio.importer;

// imports

/**
 * Interface used to import portal data.
 */
public interface Importer {

	/**
	 * Imports data into the given database.
	 *
	 * @param database String
	 * @throws Exception
	 */
	void importData(final String database) throws Exception;
}