/*
 * WWF1000DAO.java
 *
 * Copyright Alico Japan  All Rights Reserved
 *
 */
package jp.co.alico.wwf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import jp.co.alico.wwf.vo.UserInfo;
import jp.co.alico.wwf.vo.WWFListVO;
import jp.co.alico.wwf.utils.Code;
import jp.co.alico.wwf.utils.DBUtil;
import jp.co.alico.wwf.utils.SystemLog;
import jp.co.alico.wwf.utils.Util;
import jp.co.alico.common.wwf.exception.ConnectionInvalidException;

/**
 * <p>�V�X�e�����@�FWWF�V�X�e��</p>
 * <p>�Ɩ����@�@�@�FDB����\�����擾</p>
 * <p>�T�@�@�@�v�@�FDB����\�����擾</p>
 * <p>��@���@���@�F2006/02/13</p>
 * <p>�C�@���@���@�F2008/05/13</p>
 * @version      1.0.5
 * @author       �A���R�W���p�� ���k�}
 * @author       �A���R�W���p�� �v����
 * @author       �A���R�W���p�� �� ��
 * @author       �A���R�W���p�� �� �Q
 * @author       �A���R�W���p�� �j�@
 * @author       �A���R�W���p�� �� �Q
 */
public class WWF1000DAO {
    /**
     * <p>�N���X��</p>
     */
    private static final String CLASS_NAME = "jp.co.alico.wwf.dao.WWF1000DAO";

    /**
     * <p>���[�U�[INFO</p>
     */
     private UserInfo userInfo = new UserInfo();

    /**
     * <p>�\���̃��\�b�h</p>
     */
    public WWF1000DAO(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    /**
     * <p>�Č����X�g�擾</p>
     * 
     * @return mnList List�i�ꗗ�\���p�Č����X�g�j
     * @exception ConnectionInvalidException ���\�[�X��⑌n�ُ�
     * @exception Exception �V�X�e���ُ�
     */
    public  List getTtlMnList(String flg) throws ConnectionInvalidException, Exception {
         // method start
        final String METHOD_NAME = "getTtlMnList";
        SystemLog.printLog(userInfo, CLASS_NAME, METHOD_NAME, SystemLog.METHOD_START);
        
        Connection conn = null;
        ResultSet rs = null;
        /* Start:AVT project fix applied for SQL Injection issue reported by the veracode*/
        PreparedStatement ps = null;
        ArrayList<String> listOfValues = new ArrayList<String>();
        // ���[�U�[ID�擾
        String userId = userInfo.getUserId();

        List mnList = new ArrayList();

        // �擾����
        String strSel = "SELECT TWF.seq_no AS seq_no,";
            strSel += " TWF.rec_no  AS rec_no,";
            strSel += " TMA.ap_nm   AS ap_nm,";
            strSel += " TWF.ak_nm   AS ak_nm,";
            strSel += " ISNULL(CASE WHEN (SELECT COUNT(ope_id) FROM V_USER_PROFILE VUP1 WHERE TWF.created_by = VUP1.ope_id) = 0 THEN (SELECT TDP1.dp_nm FROM t_dept_name TDP1 WHERE SUBSTRING(TWF.created_by,1,4) = TDP1.dp_cd) ELSE (SELECT TDP1.dp_nm FROM t_dept_name TDP1 WHERE SUBSTRING(TWF.created_by,1,4) = TDP1.dp_cd) + '�@' + (SELECT ope_nm FROM V_USER_PROFILE VUP1 WHERE TWF.created_by = VUP1.ope_id) END, '�|') AS created_user,";
            strSel += " TWF.updated_by AS updated_by,";
            strSel += " ISNULL(CASE WHEN LEN(TWF.updated_by) = 4 THEN (SELECT dp_nm FROM t_dept_name TDP WHERE TWF.updated_by = TDP.dp_cd) ELSE CASE WHEN (SELECT COUNT(ope_id) FROM V_USER_PROFILE VUP2 WHERE TWF.updated_by = VUP2.ope_id) = 0 THEN (SELECT TDP.dp_nm FROM t_dept_name TDP WHERE SUBSTRING(TWF.updated_by,1,4) = TDP.dp_cd) ELSE (SELECT TDP.dp_nm FROM t_dept_name TDP WHERE SUBSTRING(TWF.updated_by,1,4) = TDP.dp_cd) + '�@' + (SELECT ope_nm FROM V_USER_PROFILE VUP2 WHERE TWF.updated_by = VUP2.ope_id) END END, '�|') AS updated_user,";
            strSel += " CONVERT(VARCHAR(10), TWF.updated_date, 111) AS updated_date,";
            strSel += " TWF.status AS status_cd, TMS.status_nm AS status_nm,";
            strSel += " TWF.modify_date AS modify_date";

        // �擾���e�[�u�� (use ANSI LEFT JOINs for SQL Server)
        String strFrm = " FROM t_work_flow TWF LEFT JOIN t_mst_application TMA ON TWF.dp_kubun = TMA.dp_kubun AND TWF.ap_kubun = TMA.ap_kubun LEFT JOIN t_mst_status TMS ON TWF.status = TMS.status";

        // �擾����
        String strWhr = " WHERE (seq_no ,rec_no) IN (";
        if (flg != null && !flg.equals(Code.SAKUSEI_FLG)) {
            strWhr +="SELECT seq_no,rec_no ";        
            strWhr +="FROM   t_work_flow A ";
            // �I�t�B�X�������Č�
            if (Code.OFFICE_MISYORI_FLG.equals(flg)) {
                strWhr += " WHERE UPDATED_BY IN ( " ;
                strWhr += " SELECT SUBSTR(UPPER(?),1,4) ";
                listOfValues.add(userId);
                strWhr += " DEPT_CD FROM DUAL UNION " ;
                strWhr += " SELECT DEPT.DP_CD DEPT_CD " ;
                strWhr += " FROM " ;
                strWhr += " T_USR_PAGT_CTG PAGT, " ;
                strWhr += " T_DEPT_NAME DEPT " ;
                strWhr += " WHERE SUBSTR(PAGT.PAGT_CD,3) = DEPT.SHARE_CD " ;

                strWhr += " AND PAGT.OPE_ID = UPPER(?) ";
                listOfValues.add(userId);
                strWhr += " UNION " ;
                strWhr += " SELECT DEPT.DP_CD DEPT_CD " ;
                strWhr += " FROM " ;
                strWhr += " T_MST_PAIR_OFFICE_USER PAIR, " ;
                strWhr += " T_DEPT_NAME DEPT " ;
                strWhr += " WHERE PAIR.OFFICE_CD = DEPT.SHARE_CD " ;

                strWhr += " AND PAIR.OPE_ID = UPPER(?))";
                listOfValues.add(userId);
            // ���[�U�[�������Č�
            } else {
                strWhr += " WHERE  UPDATED_BY  =UPPER(?) ";
                listOfValues.add(userId);
            }
            strWhr +="AND    status  = '" + Code.STATUS_MISYORI + "' "; 
            strWhr +="AND    updated_date IS NULL "; 
            strWhr +="AND   (SELECT count(*)  FROM  t_work_flow B WHERE A.seq_no  = B.seq_no AND updated_date  IS NULL) = 1 )";   
        } else {
            strWhr += " SELECT seq_no, MIN(rec_no) FROM t_work_flow WHERE created_by = UPPER(?) AND updated_date IS NULL GROUP BY seq_no";
            strWhr += " UNION SELECT seq_no,rec_no FROM t_work_flow WHERE updated_by = UPPER(?) AND status = '" + Code.STATUS_MISYOUNIN+")";
            listOfValues.add(userId);
            listOfValues.add(userId);
        }
        // joins handled in FROM clause for SQL Server
        strWhr += " ORDER BY TWF.seq_no DESC";

        // �擾�pSQL
        String strSql = strSel + strFrm + strWhr;
        SystemLog.printSql(userInfo, CLASS_NAME, METHOD_NAME, strSql);
        try {
            conn = DBUtil.getPooledConnection();
            ps = conn.prepareStatement(strSql);
            for (int value = 0; value < listOfValues.size(); value++) {
			ps.setString((value+1), listOfValues.get(value));
		}
            rs = ps.executeQuery();
            /*End: AVT project*/
            
            while (rs.next()) {
                WWFListVO vo = new WWFListVO();
                // �Č�No.
                vo.setSeqNO(rs.getString("seq_no"));

                // ����ʔ�
                vo.setRecNo(rs.getString("rec_no"));

                // ���[��(�OCode.MAX_STRING_LENGTH����)
                String ap_nm = Util.getString(rs.getString("ap_nm"));
                if (ap_nm.length() > Code.MAX_STRING_LENGTH) {
                    ap_nm = ap_nm.substring(0, Code.MAX_STRING_LENGTH);
                }
                vo.setApName(ap_nm);

                // �Č���(�OCode.MAX_STRING_LENGTH����)
                String ak_nm = Util.getString(rs.getString("ak_nm"));
                if (ak_nm.length() > Code.MAX_STRING_LENGTH) {
                    ak_nm = ak_nm.substring(0, Code.MAX_STRING_LENGTH);
                }
                vo.setAkName(ak_nm);

                // �쐬��(�OCode.MAX_STRING_LENGTH����)
                String created_user = Util.
                    trimString(rs.getString("created_user"));
                if (created_user.length() > Code.MAX_STRING_LENGTH) {
                    created_user = created_user.substring(0, 
                        Code.MAX_STRING_LENGTH);
                }
                vo.setCreateBy(created_user);

                // ������(�OCode.MAX_STRING_LENGTH����)
                String updated_user = "";
                updated_user = Util.trimString(rs.getString("updated_user"));
                if (updated_user.length() > Code.MAX_STRING_LENGTH) {
                    updated_user = updated_user.substring(0, 
                        Code.MAX_STRING_LENGTH);
                }
                vo.setUpdatedBy(updated_user);

                // �ŏI������
                String update_date = Util.
                    getString(rs.getString("updated_date"));
                vo.setUpdateDate(update_date);

                // �X�e�[�^�X
                vo.setStatus(Util.getString(rs.getString("status_nm")));

                // �X�e�[�^�X�R�[�h
                vo.setStatusCode(Util.getString(rs.getString("status_cd")));

                // �X�V��
                vo.setModifyDate(Util.getString(rs.getString("modify_date")));

                // �����t���O
                if (rs.getString("status_cd").equals(Code.STATUS_MISYOUNIN) &&
                     rs.getString("updated_by").equals(userId)) {
                      vo.setFlg("APR");
                } else if (rs.getString("status_cd").equals(Code.STATUS_SAKUSEI)) {
                      vo.setFlg("INS");
                } else {
                      vo.setFlg("UPD");
                }
                mnList.add(vo);
            }
            SystemLog.printSqlResult(userInfo, CLASS_NAME, METHOD_NAME, mnList.size());
        } finally {
            DBUtil.closePooledConnection(conn, ps);
            SystemLog.printLog(userInfo, CLASS_NAME, METHOD_NAME, SystemLog.METHOD_END);
        }
        return mnList;
    }

}
