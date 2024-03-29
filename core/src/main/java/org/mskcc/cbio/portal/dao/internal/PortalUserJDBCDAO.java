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
package org.mskcc.cbio.portal.dao.internal;

// imports
import org.mskcc.cbio.cgds.model.User;
import org.mskcc.cbio.cgds.model.UserAuthorities;
import org.mskcc.cbio.portal.dao.PortalUserDAO;

import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.ResultSet;
import javax.sql.DataSource;
import java.sql.SQLException;

import java.util.Collection;

/**
 * JDBC implementation of PortalUserDAO.
 *
 * @author Benjamin Gross
 */
public class PortalUserJDBCDAO implements PortalUserDAO {

	// logger
	private static Log log = LogFactory.getLog(PortalUserJDBCDAO.class);

	// ref to jdbc template
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	/**
	 * Constructor.
     *
     * Takes a datasource reference used to get user data.
     *
     * @param dataSource DataSource
	 */
	public PortalUserJDBCDAO(DataSource dataSource) {
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}


	/**
	 * Implementaiton of {@code PortalUserDAO}
	 */
	public User getPortalUser(final String username) {

        if (log.isDebugEnabled()) {
            log.debug("getPortalUser, username: " + username);
        }

		// sql to execute
		String sql = "select name, enabled from users where email = :username";
		SqlParameterSource namedParameters = new MapSqlParameterSource("username", username);

		ParameterizedRowMapper<User> mapper = new ParameterizedRowMapper<User>() {
			// mapRow override
			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
				User user = new User(username,
                                     rs.getString("name"),
                                     rs.getBoolean("enabled"));
				return user;
			}
		};

		// outta here
		return this.namedParameterJdbcTemplate.queryForObject(sql, namedParameters, mapper);
	}

	/**
	 * Implementation of {@code PortalUserDAO}
	 */
	public UserAuthorities getPortalUserAuthorities(final String username) {
        
        if (log.isDebugEnabled()) {
            log.debug("getPortalUserAuthorities, username: " + username);
        }

        Collection<String> userAuthorities = getUserAuthorities(username); 
        return (userAuthorities.size() > 0) ?
            new UserAuthorities(username, userAuthorities) : null;
    }

    /**
     * Helper function to retrieve user authorities.
     */
    private Collection<String> getUserAuthorities(final String username) {

		// sql to execute
		String sql = "select authority from authorities where email = :username";
		SqlParameterSource namedParameters = new MapSqlParameterSource("username", username);

		// outta here
		return this.namedParameterJdbcTemplate.queryForList(sql, namedParameters, String.class);
	}
}
