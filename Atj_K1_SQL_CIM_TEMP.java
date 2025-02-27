/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/******************************
 *	Author	: ATK ICS / Amkor AWW CIM
 *	Date		: 2004. 8. 20. 17:46:12
 ******************************/
package com.amkor.cim.atj.client.db_conn;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;

import com.amkor.cim.atj.client.dao.Atj_Validation_QueryDao;

/**
 * This class is used to (Create, Close, Clear, Maintain) DB Connection for all
 * transactions. Atj_CSDBConnection.AUTO_DISCONNECTION_TIME is the time for
 * maintaining connection to DB.
 * 
 * @author ATK ICS / Amkor AWW CIM
 */
public final class Atj_K1_SQL_CIM_TEMP {
	/**
	 * Database connection property
	 */

	// it's test DB
	private static final String URL = "jdbc:sqlserver://10.101.1.102:1433;databaseName=CIM;sendStringParametersAsUnicode=false";
	private static final String USER_ID = "";
	private static final String USER_PWD = "";
	// private static final String URL =
	// "jdbc:sqlserver://10.101.1.86:1433;databaseName=CIM";
	// private static final String USER_ID = "K1STSQSVC";
	// private static final String USER_PWD = "9uaJmsGz";

	public static final int AUTO_DISCONNECTION_TIME = 600;
	private static Connection connection = null;
	private static int g_iTryCount = 0;

	public static Timer timerConnection;

	private static Atj_Validation_QueryDao validationDao = new Atj_Validation_QueryDao();

	/**
	 * Examine a Connection is alive or not.
	 * 
	 * @return if connection is not null, true will be returned
	 */
	public static boolean isConnected() {
		try {
			if (connection != null) {
				if (!connection.isClosed()) return true;
			}
		} catch (Exception ignored) {
		}

		return false;
	}

	/**
	 * Release ResultSet & statement related resources.
	 */
	public static void cleanup(ResultSet rset, Statement stmt) {
		if (rset != null) {
			try {
				rset.close();
			} catch (Exception ignored) {
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (Exception ignored) {
			}
		}
	}

	/**
	 * Release ResultSet & PreparedStatement related resources.
	 */
	public static void cleanup(ResultSet rset, PreparedStatement pstmt) {
		if (rset != null) {
			try {
				rset.close();
			} catch (Exception ignored) {
			}
		}
		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (Exception ignored) {
			}
		}
	}

	/**
	 * Release CallableStatement & PreparedStatement related resources.
	 */
	public static void cleanup(CallableStatement cs, PreparedStatement pstmt) {
		if (cs != null) {
			try {
				cs.close();
			} catch (Exception ignored) {
			}
		}
		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (Exception ignored) {
			}
		}
	}

	public static void cleanup(CallableStatement cs, ResultSet rs) {
		if (cs != null) {
			try {
				cs.close();
			} catch (Exception ignored) {
			}
		}
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception ignored) {
			}
		}
	}

	/**
	 * Release Connection & ResultSet & PreparedStatement related resources.
	 */
	public static void cleanup(Connection conn, ResultSet rset, PreparedStatement pstmt) {
		if (rset != null) {
			try {
				rset.close();
			} catch (Exception ignored) {
			}
		}
		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (Exception ignored) {
			}
		}

		if (conn != null) {
			try {
				conn.close();
			} catch (Exception ignored) {
			}
		}

		try {
			if (timerConnection != null) timerConnection.cancel();
		} catch (Exception e) {
		}
	}

	/**
	 * Close Connection between DB and Connector.
	 */
	public static void closeConnection() {
		if (connection != null) {
			try {
				connection.close();
				connection = null;
			} catch (Exception ignored) {
			}
		}

		try {
			if (timerConnection != null) timerConnection.cancel();
		} catch (Exception e) {
		}
	}

	/**
	 * Create DB Connection based on properties.
	 */
	private static void createConnection() {
		try {
			// Load JDBC Driver
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			connection = DriverManager.getConnection(URL, USER_ID, USER_PWD);

			// Set timer : If time eclipse 600 sec without usage, close
			// connection automatically
			// K1SQLCIMTimerTask connectionTimerTask = new K1SQLCIMTimerTask();
			// timerConnection = new Timer(true);
			// timerConnection.schedule(connectionTimerTask, 100000, 100000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create DB Connection and setting base time(zero) to check connection
	 * time.
	 */
	synchronized public static Connection getConnection() {
		// If connection is null or closed, create Connection----------
		if (connection != null) {
			try {
				if (connection.isClosed()) {
					createConnection();
				} else {
					try {
						validationDao.validateSQL(connection);
					} catch (Exception e) {
						createConnection();
					}
					K1SQLCIMTEMPTimerTask.iEclipse = 0;
				}
			} catch (SQLException e) {
				try {
					connection.close();
				} catch (Exception ignored) {
				}
			}
		} else {
			createConnection();
		}
		return connection;
	}
}

/**
 * Examine how long the connection have to exist. After certain time the
 * connection will be released.
 */
class K1SQLCIMTEMPTimerTask extends TimerTask {
	public static int iEclipse;

	public void run() {
		iEclipse += 10;
		if (iEclipse > Atj_K1_SQL_CIM_TEMP.AUTO_DISCONNECTION_TIME) {
			iEclipse = 0;
			Atj_K1_SQL_CIM_TEMP.closeConnection();
			Atj_K1_SQL_CIM_TEMP.timerConnection.cancel();
		}
	}
}