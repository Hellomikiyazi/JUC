/**
 * @author nksung(nksung@amkor.co.kr)
 * package name: com.amkor.cim.atj.client.dao
 * file name : Atj_Validation_QueryDao.java
 * created time : 2015. 10. 30. ���� 9:13:40
 * AWW eCIM Project
 */
package com.amkor.cim.atj.client.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.amkor.cim.atj.client.db_conn.Atj_Oracle_QROC_Connection;
import com.amkor.cim.atj.client.db_conn.Atj_MSSQL_CIM_Connection;

public class Atj_Validation_QueryDao {

	public void validateDB2(Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			String sql = "select 1 from sysibm.sysdummy1";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
		} catch (Exception e) {
			throw e;
		} finally {
			Atj_Oracle_QROC_Connection.cleanup(conn, rs, pstmt);
		}
	}

	public void validateSQL(Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql = "SELECT 1";
		try {
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

		} catch (SQLException e) {
			throw e;
		}
	}
}
