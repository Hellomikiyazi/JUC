package com.amkor.cim.atj.client.db_conn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import com.amkor.cim.atj.client.dao.Atj_Validation_QueryDao;
import com.amkor.cim.client.util.ErrorLog;

/**
 * This class is used to (Create, Close, Clear, Maintain) DB Connection for all
 * transactions. Atj_K1_SQL_CIM_TEMP.startConnectionTimer is the time for
 * maintaining connection to DB.
 * 
 * @author ATK ICS / Amkor AWW CIM
 * @author Huimei Liang
 * @Date: 2025-02-27
 * @Description: Modified the incorrect logic and startConnectionTimer part
 */
public final class Atj_K1_SQL_CIM_TEMP {

	/**
	 * Database connection property It's test DB private static final String URL =
	 * "jdbc:sqlserver://10.101.1.86:1433;databaseName=CIM"; private static final
	 * String USER_ID = "K1STSQSVC"; private static final String USER_PWD =
	 * "9uaJmsGz";
	 */
	private static final String URL = "jdbc:sqlserver://10.101.1.102:1433;databaseName=CIM;sendStringParametersAsUnicode=false";
	private static final String USER_ID = "";
	private static final String USER_PWD = "";
	public static final int AUTO_DISCONNECTION_TIME = 600;

	private static Connection connection = null;
	protected static Timer timerConnection;
	private static Atj_Validation_QueryDao validationDao = new Atj_Validation_QueryDao();

	public static void cleanup(ResultSet rs, PreparedStatement pstmt) {
		closeResultSet(rs);
		closePreparedStatement(pstmt);
	}

	/**
	 * DAO層のPreparedStatementとDB Connectionのリソースをリリースする。タイマーを閉じる。
	 */
	public static void cleanup(PreparedStatement pstmt, Connection conn) {
		closePreparedStatement(pstmt);
		closeConnection(conn);
		cancelTimer();
	}

	/**
	 * DAO層のResultSet、PreparedStatementとDB Connectionのリソースをリリースする。タイマーを閉じる。
	 */
	public static void cleanup(Connection conn, ResultSet rs, PreparedStatement pstmt) {
		closeResultSet(rs);
		closePreparedStatement(pstmt);
		closeConnection(conn);
		cancelTimer();
	}

	/**
	 * DB Connectionのリソースをリリースする。タイマーを閉じる。
	 */
	public static void closeConnection() {
		closeConnection(connection);
		cancelTimer();
	}

	/**
	 * Create DB Connection and setting base time(zero) to check connection time.
	 */
	public synchronized static Connection getConnection() {
		try {
			// 接続がNullもしくは閉じるの場合は、接続を新しく作る
			if (connection == null || connection.isClosed()) {
				createConnection();
			} else {
				// 接続の状態をチェックする
				validateConnection();
			}
		} catch (SQLException sqlException) {
			ErrorLog.writeLog(ErrorLog.ERR_DATA_SERVER_ACCESS, "Atj_K1_SQL_CIM_TEMP", "getConnection", sqlException
					.getCause() + " " + sqlException.getMessage());
			closeConnection(connection);
		} catch (Exception e) {
			ErrorLog.writeLog(ErrorLog.ERR_DATA_SERVER_ACCESS, "Atj_K1_SQL_CIM_TEMP", "getConnection", e.getCause()
					+ " " + e.getMessage());
			closeConnection(connection);
		}
		return connection;
	}

	private static void createConnection() throws Exception {
		try {
			// Load JDBC Driver
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			connection = DriverManager.getConnection(URL, USER_ID, USER_PWD);
			startConnectionTimer();
		} catch (Exception e) {
			throw e;
		}
	}

	private static void validateConnection() throws Exception {
		validationDao.validateSQL(connection);
		K1SQLCIMTEMPTimerTask.iEclipse = 0;
	}

	private static void startConnectionTimer() {
		K1SQLCIMTEMPTimerTask connectionTimerTask = new K1SQLCIMTEMPTimerTask();
		timerConnection = new Timer(true);
		timerConnection.schedule(connectionTimerTask, 10000, 10000); // Schedule to run every 10 seconds

	}

	private static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (Exception ignored) {
			}
		}
	}

	private static void closePreparedStatement(PreparedStatement pstmt) {
		if (pstmt != null) {
			try {
				pstmt.close();
			} catch (Exception ignored) {
			}
		}
	}

	private static void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception ignored) {
			}
		}
	}

	private static void cancelTimer() {
		if (timerConnection != null) {
			timerConnection.cancel();
		}
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
		}
	}
}
