/**
 * Copyright (c) 2003 Daffodil Software Ltd all rights reserved.
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of version 2 of the GNU General Public License as
 * published by the Free Software Foundation.
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. See the GNU General Public License for more details.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.daffodilwoods.replication.DBHandler;

import java.sql.*;
import java.util.*;

import com.daffodilwoods.replication.*;
import com.daffodilwoods.replication.column.*;
import org.apache.log4j.Logger;

public class DB2Handler
    extends AbstractDataBaseHandler  {
  protected static Logger log = Logger.getLogger(DB2Handler.class.getName());
  public DB2Handler() {}

  public DB2Handler(ConnectionPool connectionPool0) {
    connectionPool = connectionPool0;
    vendorType = Utility.DataBase_DB2;
  }

  protected void createSuperLogTable(String pubName) throws SQLException,
      RepException {
    StringBuffer logTableQuery = new StringBuffer();
    logTableQuery.append(" Create Table ")
        .append(log_Table)
        .append(" ( " + RepConstants.logTable_commonId1 +
                " bigint GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                RepConstants.logTable_tableName2 + " varchar(255) ) ");
    runDDL(pubName, logTableQuery.toString());
    StringBuffer indexQuery = new StringBuffer();
    indexQuery.append("CREATE INDEX ")
        .append(RepConstants.log_Index)
        .append(" ON " + getLogTableName())
        .append("(")
        .append(RepConstants.logTable_commonId1)
        .append(")");
//System.out.println(" Create Index on LogTable : " + indexQuery.toString());
    runDDL(pubName, indexQuery.toString());

  }

  /**
   * Because changes has been made in structure of RepTable
   * by Hisar team.So old method has been commented. After
   * proper testing with all data base it should be deleted.
   */

  /* protected void createRepTable(String pubName) throws SQLException,
       RepException {
     StringBuffer repTableQuery = new StringBuffer();
     repTableQuery.append(" Create Table ").append(getRepTableName())
         .append("(  " + RepConstants.repTable_pubsubName1 +
   " varchar(255) not null ,  " + RepConstants.repTable_tableId2 +
   "  bigint GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), ")
         .append("  " + RepConstants.repTable_tableName2 +
                 "  varchar(255) not null,  " +
                 RepConstants.repTable_filter_clause3 + "  varchar(255), ")
         .append("  " + RepConstants.repTable_conflict_resolver4 +
                 "  varchar(255) , Primary Key ( " +
                 RepConstants.repTable_pubsubName1 + " ,  " +
                 RepConstants.repTable_tableName2 + " ) ) ");
     runDDL(pubName, repTableQuery.toString());
   } */

  protected void createRepTable(String pubName) throws SQLException,
      RepException {
    StringBuffer repTableQuery = new StringBuffer();
    repTableQuery.append(" Create Table ").append(getRepTableName()).append(
        " ( ")
        .append(RepConstants.repTable_pubsubName1).append(
        " varchar(255) not null, ")
        .append(RepConstants.repTable_tableId2).append(
        "  bigint GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1), ")
        .append(RepConstants.repTable_tableName2).append(
        "  varchar(255) not null , ")
        .append(RepConstants.repTable_filter_clause3).append(
        "  varchar(255) , ")
        .append(RepConstants.repTable_createshadowtable6).append(
        "  char(1) Default 'Y', ")
        .append(RepConstants.repTable_cyclicdependency7).append(
        "  char(1) Default 'N', ")
        .append(RepConstants.repTable_conflict_resolver4).append(
        "  varchar(255), ")
        .append("   Primary Key (").append(RepConstants.repTable_pubsubName1).
        append(" , ")
        .append(RepConstants.repTable_tableName2).append(" ) ) ");
    runDDL(pubName, repTableQuery.toString());
  }

  protected void createPublicationTable(String pubName) throws RepException,
      SQLException {
    StringBuffer pubsTableQuery = new StringBuffer();
    pubsTableQuery.append(" Create Table ")
        .append(getPublicationTableName())
        .append(" ( " + RepConstants.publication_pubName1 +
                " varchar(255) not null Primary Key ,  " +
                RepConstants.publication_conflictResolver2 +
                "  varchar(255) , ")
        .append("  " + RepConstants.publication_serverName3 +
                "  varchar (255) ) ");
    runDDL(pubName, pubsTableQuery.toString());
  }

  protected void createBookMarkTable(String pubName) throws SQLException,
      RepException {
    StringBuffer bookmarkTableQuery = new StringBuffer();
    bookmarkTableQuery.append(" Create Table ")
        .append("rep_bookmarktable")
        .append(" (  " + RepConstants.bookmark_LocalName1 +
                "  varchar(255) not null ,  " +
                RepConstants.bookmark_RemoteName2 + " varchar(255) not null, ")
        .append("  " + RepConstants.bookmark_TableName3 +
                " varchar(255) not null,  " + RepConstants.bookmark_lastSyncId4 +
                " bigint , ")
        .append(
        "  " + RepConstants.bookmark_ConisderedId5 + " bigint ,")
        .append(RepConstants.bookmark_IsDeletedTable)
        .append(" char(1) not null default 'n', Primary Key ( " +
                RepConstants.bookmark_LocalName1 + ",  " +
                RepConstants.bookmark_RemoteName2 + ",  " +
                RepConstants.bookmark_TableName3 + ") ) ");
    runDDL(pubName, bookmarkTableQuery.toString());
  }

  public void createShadowTable(String pubsubName, String tableName,
                                String allColseq,String[] primaryColumns) throws RepException {
    StringBuffer shadowTableQuery = new StringBuffer();
    shadowTableQuery.append(" Create Table ")
        .append(RepConstants.shadow_Table(tableName))
        .append(" (  " + RepConstants.shadow_sync_id1 +
                " bigint GENERATED BY DEFAULT AS IDENTITY (START WITH 1, INCREMENT BY 1) , ")
        .append("   " + RepConstants.shadow_common_id2 + "  BIGINT , ")
        .append("   " + RepConstants.shadow_operation3 + "  char(1) , ")
        .append("   " + RepConstants.shadow_status4 + "  char(1) ")
        .append(allColseq)
        .append(" , " + RepConstants.shadow_serverName_n + " varchar(255)  ")
        .append(" ,  " + RepConstants.shadow_PK_Changed + "  char(1) ) ");
    try {
//System.out.println("shadowTableQuery.toString() ="+shadowTableQuery.toString());
      runDDL(pubsubName, shadowTableQuery.toString());
      createIndex(pubsubName, RepConstants.shadow_Table(tableName));
    }
    catch (RepException ex) {
      throw ex;
    }
    catch (SQLException ex) {
//          ex.printStackTrace();
      // Ignore the Exception
    }
    createIndex(pubsubName, RepConstants.shadow_Table(tableName));
  }

  protected void createSubscriptionTable(String pubName) throws RepException,
      SQLException {
    String subsTableQuery = " Create Table  "
        + getSubscriptionTableName()
        + " ( " + RepConstants.subscription_subName1 +
        " varchar(255) NOT NULL Primary Key , "
        + "   " + RepConstants.subscription_pubName2 + " varchar(255)  , "
        + "   " + RepConstants.subscription_conflictResolver3 +
        " varchar(255) , "
        + "   " + RepConstants.subscription_serverName4 + " varchar (255))  ";
    runDDL(pubName, subsTableQuery);
  }

  protected void createScheduleTable(String subName) throws SQLException,
      RepException {
    StringBuffer ScheduleTableQuery = new StringBuffer();

    ScheduleTableQuery.append(" Create Table ")
        .append(getScheduleTableName())
        .append(" ( " + RepConstants.schedule_Name + " varchar(255) not null, " +
                RepConstants.subscription_subName1 +
                " varchar(255) not null unique , ")
        .append("  " + RepConstants.schedule_type + " varchar(255) , ")
        .append(" " + RepConstants.publication_serverName3 + " varchar (255) ," +
                RepConstants.publication_portNo + " varchar(255) ,")
        .append(" " + RepConstants.recurrence_type +
                " varchar(255) , " +
                RepConstants.replication_type +
                " varchar(255) ,")
        .append(" " + RepConstants.schedule_time + " bigint , ")
        .append(" " + RepConstants.schedule_counter +
                " bigint , Primary Key (" +
                RepConstants.schedule_Name +
                " , " + RepConstants.subscription_subName1 +
                ") ) ");
    runDDL(subName, ScheduleTableQuery.toString());
//                        System.out.println(ScheduleTableQuery.toString());
  }

  public void createShadowTableTriggers(String pubsubName, String tableName,
                                        ArrayList colInfoList,
                                        String[] primCols) throws RepException {

    String serverName = getLocalServerName();
//    RepPrinter.print(" Columns are :::::: "  + java.util.Arrays.asList(columnTypeInfoMap.keySet().toArray(new String[0])));
//    String[] colNames = (String[]) columnTypeInfoMap.keySet().toArray(new String[0]);
    int size = colInfoList.size();
    String[] colNames = new String[size];
    for (int i = 0; i < size; i++) {
      colNames[i] = ( (ColumnsInfo) colInfoList.get(i)).getColumnName();
    }
    //RepPrinter.print(" Columns are :::::: "  + java.util.Arrays.asList(colNames));
    String colNameSeq = getColumnNameSequence(colNames, "").toString();
    String colNameSeqPrefixOldRow = getColumnNameSequence(colNames, "oldRow.").
        toString();
    String colNameSeqPrefixNewRow = getColumnNameSequence(colNames, "newRow.").
        toString();
    String shadowTableName = RepConstants.shadow_Table(tableName);
    String primColumnNamesSeq = getColumnNameSequence(primCols, "rep_old_");
    String primColNameSeqPrefixOldRow = getColumnNameSequence(primCols,"oldRow.").toString();
    String primColNameSeqPrefixNewRow = getColumnNameSequence(primCols,"newRow.").toString();
    String[] primOldCols =getColumnNameWithOldOrNewPrefix(primCols,"oldRow.");
    String[] primNewCols =getColumnNameWithOldOrNewPrefix(primCols,"newRow.");

    StringBuffer insertLogTable = new StringBuffer();
    insertLogTable.append(" Insert into ")
        .append(log_Table)
        .append(" ( ").append(RepConstants.logTable_tableName2)
        .append(" ) values ( '")
        .append(tableName).append("'); ");

    StringBuffer insTriggerQuery = new StringBuffer();
    insTriggerQuery.append(" Create trigger ")
        .append(RepConstants.getInsertTriggerName(tableName))
        .append(" after insert on ").append(tableName)
        .append(
        " Referencing new as newRow For each Row MODE DB2SQL begin ATOMIC ")
        .append(insertLogTable).append("  Insert Into  ")
        .append(shadowTableName).append(" ( ")
        .append(RepConstants.shadow_common_id2).append(", ")
        .append(RepConstants.shadow_operation3).append(", ")
        .append(RepConstants.shadow_status4).append(", ")
        .append(colNameSeq).append(primColumnNamesSeq)
        .append(RepConstants.shadow_serverName_n)
        .append(" ) Values ( null , 'I' , null , ")
        .append(colNameSeqPrefixNewRow).append(primColNameSeqPrefixNewRow)
        .append("'").append(serverName).append("') ; end ");

    StringBuffer delTriggerQuery = new StringBuffer();
    delTriggerQuery.append(" Create trigger ")
        .append(RepConstants.getDeleteTriggerName(tableName))
        .append(" after delete on ").append(tableName)
        .append(
        " Referencing old as oldRow For each Row MODE DB2SQL begin ATOMIC ")
        .append(insertLogTable).append(" Insert Into ")
        .append(shadowTableName).append(" ( ")
        .append(RepConstants.shadow_common_id2).append(", ")
        .append(RepConstants.shadow_operation3).append(", ")
        .append(RepConstants.shadow_status4).append(", ")
        .append(colNameSeq).append(primColumnNamesSeq)
        .append(RepConstants.shadow_serverName_n)
        .append(" ) Values ( null , 'D' , null , ")
        .append(colNameSeqPrefixOldRow).append(primColNameSeqPrefixOldRow)
        .append("'").append(serverName).append("') ; end ");

    StringBuffer updTriggerQuery = new StringBuffer();
    updTriggerQuery.append(" Create trigger ")
        .append(RepConstants.getUpdateTriggerName(tableName))
        .append(" after update on ").append(tableName)
        .append(" Referencing new as newRow old as oldRow For each Row MODE DB2SQL begin ATOMIC  ")
        .append(" declare maxlogid bigint; pkchanged char(1); ").append(insertLogTable)
        .append(" set (maxlogid) = (select max(" +
                RepConstants.logTable_commonId1 + ")  from ")
        .append(log_Table).append("); Insert Into ")
        .append(shadowTableName).append(" ( ")
        .append(RepConstants.shadow_common_id2).append(", ")
        .append(RepConstants.shadow_operation3).append(", ")
        .append(RepConstants.shadow_status4).append(", ")
        .append(colNameSeq).append(primColumnNamesSeq)
        .append(RepConstants.shadow_serverName_n)
        .append(" ) Values ( maxlogid , 'U' , 'B' , ")
        .append(colNameSeqPrefixOldRow).append(primColNameSeqPrefixOldRow)
        .append("'").append(serverName).append("') ; ")
        .append(" if( ");
            for (int i = 0; i <primOldCols.length ; i++) {
                  if(i!=0)
                  updTriggerQuery.append(" and ");
                  updTriggerQuery.append(primOldCols[i])
                                         .append("!=")
                                         .append(primNewCols[i]);
             }
         updTriggerQuery.append(" ) then pkchanged='Y'; end if; ")
        .append(" Insert Into ")
        .append(shadowTableName).append(" ( ")
        .append(RepConstants.shadow_common_id2).append(", ")
        .append(RepConstants.shadow_operation3).append(", ")
        .append(RepConstants.shadow_status4).append(", ")
        .append(colNameSeq).append(primColumnNamesSeq)
        .append(RepConstants.shadow_serverName_n).append(" , ")
        .append(RepConstants.shadow_PK_Changed)
        .append(" ) Values ( maxlogid , 'U' , 'A' , ")
        .append(colNameSeqPrefixNewRow).append(primColNameSeqPrefixOldRow)
        .append("'").append(serverName).append("',pkchanged) ; end ");
    try {
      runDDL(pubsubName, insTriggerQuery.toString());
    }
    catch (RepException ex) {
      throw ex;
    }
    catch (SQLException ex) {
//      ex.printStackTrace();
      // Ignore Exception
    } try {
      runDDL(pubsubName, delTriggerQuery.toString());
    }
    catch (RepException ex) {
      throw ex;
    }
    catch (SQLException ex) {
//      ex.printStackTrace();
      // Ignore Exception
    }
    try {
         runDDL(pubsubName, updTriggerQuery.toString());
       }
       catch (RepException ex) {
         throw ex;
       }
       catch (SQLException ex) {
//      ex.printStackTrace();
         // Ignore Exception
       }

  }

  public boolean isDataTypeOptionalSizeSupported(TypeInfo typeInfo) {
    int sqlType = typeInfo.getSqlType();
    String typeName = typeInfo.getTypeName();
    switch (sqlType) {
      case 4:
      case 5:
      case 7:
      case -4:
      case -5:
      case 91:
      case 92:
      case 93:
      case 8:
      case -1:
      case -7:
      case 16:
        return false;
      default:
        return true;
    }
  }

  public void setTypeInfo(TypeInfo typeInfo, ResultSet rs) throws RepException,
      SQLException {
    switch (typeInfo.getSqlType()) {
      case Types.BIT:
        typeInfo.setTypeName("SMALLINT");
        break;
        //-7
      case Types.TINYINT:
        typeInfo.setTypeName("SMALLINT");
        break; //-6;
      case Types.SMALLINT:
        typeInfo.setTypeName("SMALLINT");
        break; // 5;
      case Types.INTEGER:
        typeInfo.setTypeName("INTEGER");
        break; //  4;
      case Types.LONGVARBINARY:
        typeInfo.setTypeName("BLOB");
        break; // -4;
      case Types.BIGINT:
        typeInfo.setTypeName("BIGINT");
        break; // //-5;
      case Types.FLOAT:
        typeInfo.setTypeName("DOUBLE");
        break; // // 6;
      case Types.REAL:
        typeInfo.setTypeName("REAL");
        break; // // 7;
      case Types.DOUBLE:
        typeInfo.setTypeName("DOUBLE");
        break; // // 8;
      case Types.NUMERIC:
        typeInfo.setTypeName("DECIMAL");
        break; // // 2;
      case Types.DECIMAL:
        typeInfo.setTypeName("DECIMAL");
        break; // // 3;
      case Types.CHAR:
        if (typeInfo.getTypeName().equalsIgnoreCase("GRAPHIC")) {
          typeInfo.setTypeName("CLOB");
          typeInfo.setSqlType(Types.CLOB);
          return;
        }
        typeInfo.setTypeName("CHAR");
        break; //  1;
      case Types.VARCHAR:

        //done by nancy to handle postgreSQL to db2
        if (typeInfo.getTypeName().equalsIgnoreCase("text")) {
          typeInfo.setTypeName("CLOB");
          typeInfo.setSqlType(Types.CLOB);
          return;
        }
        typeInfo.setTypeName("VARCHAR");
        break; // //12;
      case Types.LONGVARCHAR:
        if (typeInfo.getTypeName().equalsIgnoreCase("LONG VARGRAPHIC") ||
            typeInfo.getTypeName().equalsIgnoreCase("VARGRAPHIC")) {
          typeInfo.setTypeName("CLOB");
          typeInfo.setSqlType(Types.CLOB);
          return;
        }
        typeInfo.setTypeName("LONG VARCHAR");
        break; // //-1;
      case Types.DATE:
        typeInfo.setTypeName("DATE");
        break; // //91;
      case Types.TIME:
        typeInfo.setTypeName("TIME");
        break; // //92;
      case Types.TIMESTAMP:
        typeInfo.setTypeName("TIMESTAMP");
        break; // 93;
      case Types.BINARY:
        typeInfo.setTypeName("BLOB");
        break; //-2
      case Types.BLOB:
        typeInfo.setTypeName("BLOB");
        break; //2004
      case Types.CLOB:
        if (typeInfo.getTypeName().equalsIgnoreCase("DBCLOB")) {
          typeInfo.setTypeName("CLOB");
          typeInfo.setSqlType(Types.CLOB);
          return;
        }
        typeInfo.setTypeName("CLOB");
        break; //2005
      case Types.VARBINARY:
        typeInfo.setTypeName("BLOB");
        break; //-3
      case Types.REF: //2006;
      case Types.JAVA_OBJECT: //2000
      case Types.NULL: //0
      case Types.DISTINCT: //2001;
      case Types.STRUCT: //2002;
      case Types.ARRAY: //2003;
      case Types.DATALINK: //70;
      case Types.BOOLEAN: //16;
//      case Types.BIT: //-7
      default:
        throw new RepException("REP031", new Object[] {typeInfo.getTypeName()});
    }
  }

  public AbstractColumnObject getColumnObject(TypeInfo typeInfo) throws
      RepException {
    int sqlType = typeInfo.getSqlType();
    switch (sqlType) {
      case 1: // CHAR
      case 12: //VARCHAR
        return new StringObject(sqlType, this);
//      case -4: // LONG VARCHAR FOR BIT DATA
//        return new BytesObject(sqlType,this);
      case 4: // INT
      case 5: // SMALLINT
      case -7:
      case -6: //TINYINT
        return new IntegerObject(sqlType, this);
      case -5: // BIGINT
        return new LongObject(sqlType, this);
      case 3: // DECIMAL
      case 8: // DOUBLE PRECISION
      case 2: // NUMERIC
      case 6: // FLOAT
        return new DoubleObject(sqlType, this);
      case 7: // REAL
        return new FloatObject(sqlType, this);
      case 91: // DATE
        return new DateObject(sqlType, this);
      case 92: // TIME
        return new TimeObject(sqlType, this);
      case 93: // TIMESTAMP
        return new TimeStampObject(sqlType, this);
      case 2004:
      case -3:
      case -4:
      case -2:
        return new BlobObject(sqlType, this);
      case 2005: //clob
      case -1: //LONG VARCHAR

        /*used ClobStreamObject instead of ClobObject as clob was not synchronized with postgreSQl
         --done by nancy*/
        return new ClobStreamObject(sqlType, this);
      default:
        throw new RepException("REP031", new Object[] {typeInfo.getTypeName()});
    }
  }

  public boolean isPrimaryKeyException(SQLException ex) throws SQLException {
    if (ex.getSQLState().equalsIgnoreCase("23505")) {
      return true;
    }
    else {
      return false;
    }
  }

  public int getAppropriatePrecision(int columnSize, String datatypeName) {
//System.out.println(" datatypeName ="+datatypeName+" columnSize ="+columnSize);
    if (datatypeName.equalsIgnoreCase("numeric") && columnSize > 31) {
      columnSize = 31;
    }
    else if (datatypeName.equalsIgnoreCase("DECIMAL") && columnSize > 31) {
      columnSize = 31;
    }
    else if (datatypeName.equalsIgnoreCase("text") && columnSize == -1) {
      columnSize = 3996;
    }
    return columnSize;
  }

  public void makeProvisionForLOBDataTypes(ArrayList dataTypeMap) {
    ArrayList removeKeysList = null;
    for (int i = 0, size = dataTypeMap.size(); i < size; i++) {
      ColumnsInfo ci = (ColumnsInfo) dataTypeMap.get(i);
      String dataType = ci.getDataTypeDeclaration();
      if (dataType.indexOf("BLOB") != -1 ||
          dataType.indexOf("CLOB") != -1) {
        if (removeKeysList == null) {
          removeKeysList = new ArrayList();
        }
        removeKeysList.add(ci);
      }
    }
    if (removeKeysList != null) {
      for (int i = 0, length = removeKeysList.size(); i < length; i++) {
        dataTypeMap.remove(removeKeysList.get(i));
      }
    }
  }

  protected void createIndex(String pubsubName, String tableName) throws
      RepException {
    StringBuffer createIndexQuery = new StringBuffer();
//      create index ind on cmsadm2.R_S_Bank(Rep_sync_id);
    createIndexQuery.append("create index  ")
        .append(RepConstants.Index_Name(tableName))
        .append(" on ")
        .append(tableName)
        .append("(")
        .append(RepConstants.shadow_sync_id1)
        .append(")");
//System.out.println(" createIndexQuery : "+createIndexQuery.toString());
    try {
      runDDL(pubsubName, createIndexQuery.toString());
    }
    catch (RepException ex) {
      // Ignore the Exception
    }
    catch (SQLException ex) {
      // Ignore the Exception
    }
  }

  public int getAppropriateScale(int columnScale) throws RepException {
    if (columnScale < 0) {
      throw new RepException("REP026", new Object[] {"1", "31"});
    }
    else if (columnScale >= 31) {
      columnScale = 31;
    }
    else if (columnScale >= 0 && columnScale < 31)
      columnScale = columnScale;
    return columnScale;
  }

  public PreparedStatement makePrimaryPreperedStatement(String[]
      primaryColumns, String shadowTable, String local_pub_sub_name) throws
      SQLException, RepException {
    StringBuffer query = new StringBuffer();
    query.append(" select * from ");
    query.append(shadowTable);
    query.append(" where ");
    query.append(RepConstants.shadow_sync_id1);
    query.append(" > ");
    query.append("? ");
    for (int i = 0; i < primaryColumns.length; i++) {
      query.append(" and ");
      query.append(primaryColumns[i]);
      query.append("= ? ");
    }
    query.append(" order by " + RepConstants.shadow_sync_id1 +
                 " FETCH FIRST ROW ONLY ");
    Connection pub_sub_Connection = connectionPool.getConnection(
        local_pub_sub_name);
    return pub_sub_Connection.prepareStatement(query.toString());
  }

  public boolean isForiegnKeyException(SQLException ex) throws SQLException {
    if (ex.getErrorCode() == -204)
      return true;
    else
      return false;

  }

  protected void createIgnoredColumnsTable(String pubName) throws SQLException,
      RepException {
    StringBuffer ignoredColumnsQuery = new StringBuffer();
    ignoredColumnsQuery.append(" Create Table ").append(getIgnoredColumns_Table()).
        append(" ( ")
        .append(RepConstants.ignoredColumnsTable_tableId1).append(
        "  bigint not null, ")
        .append(RepConstants.ignoredColumnsTable_ignoredcolumnName2).append(
        "  varchar(255) , ")
        .append("   Primary Key (").append(RepConstants.
                                           ignoredColumnsTable_tableId1).append(
        " , ")
        .append(RepConstants.ignoredColumnsTable_ignoredcolumnName2).append(
        " ) ) ");
    runDDL(pubName, ignoredColumnsQuery.toString());
  }

  protected void createTrackReplicationTablesUpdationTable(String pubSubName) throws
      RepException, SQLException {
    StringBuffer trackRepTablesUpdationQuery = new StringBuffer();
    trackRepTablesUpdationQuery.append(" CREATE  TABLE ").append(
        getTrackReplicationTablesUpdation_Table()).append(" ( " +
        RepConstants.trackUpdation + " SMALLINT  NOT NULL PRIMARY KEY) ");
    runDDL(pubSubName, trackRepTablesUpdationQuery.toString());
    runDDL(pubSubName,
           "Insert into " + getTrackReplicationTablesUpdation_Table() + " values(1)");
  }

  //implement this method for providing provision to stop updations done on shadow table
  protected void createTriggerForTrackReplicationTablesUpdationTable(String
      pubSubName) throws RepException, SQLException {
    /*    StringBuffer trackRepTablesUpdationTriggerQuery = new StringBuffer();
        trackRepTablesUpdationTriggerQuery.append(" CREATE  TRIGGER TRI_")
            .append(getTrackReplicationTablesUpdation_Table()).append(
                " ON " + getTrackReplicationTablesUpdation_Table())
            .append(" AFTER INSERT AS  DELETE FROM " +
                    getTrackReplicationTablesUpdation_Table() + " WHERE ")
     .append(RepConstants.trackUpdation + " NOT IN(SELECT * FROM inserted)");
        runDDL(pubSubName, trackRepTablesUpdationTriggerQuery.toString());*/
  }

  public PreparedStatement makePrimaryPreperedStatementBackwardTraversing(String[] primaryColumns, long lastId, String local_pub_sub_name, String shadowTable) throws SQLException, RepException {
    StringBuffer query = new StringBuffer();
    query.append(" select top 1 * from ")
    .append(shadowTable)
    .append(" where ")
    .append(RepConstants.shadow_sync_id1)
    .append(" < ?  ")
    .append(" and ")
    .append(RepConstants.shadow_sync_id1)
    .append(" > ")
    .append(lastId);
    for (int i = 0; i < primaryColumns.length; i++) {
      query.append(" and ")
      .append(primaryColumns[i])
      .append(" = ?  ");
    }
    query.append(" order by ")
    .append(RepConstants.shadow_sync_id1)
    .append(" FETCH FIRST ROW ONLY ")
    .append(" desc ");
    log.debug(query.toString());
//System.out.println("DB2Handler makePrimaryPreperedStatementDelete  ::  " +query.toString());
    Connection pub_sub_Connection = connectionPool.getConnection(local_pub_sub_name);
    return pub_sub_Connection.prepareStatement(query.toString());
  }

  /**
   * isSchemaSupported
   *
   * @return boolean
   */
  public boolean isSchemaSupported() {
    return true;
  }

}
