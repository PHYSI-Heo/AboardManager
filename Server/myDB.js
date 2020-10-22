
const mysql = require('mysql2');

const MYSQLIP = 'localhost';
const MYSQLID = 'root';
const MYSQLPWD = '1234';
const DBNAME = 'ams';

var dbConfig = {
  host : MYSQLIP,
  port : 3306,
  user : MYSQLID,
  password : MYSQLPWD,
  connectionLimit:100,
  waitForConnections:true
};

const SQL_CD = "CREATE DATABASE " + DBNAME;

const SQL_CT_CLIENT = "CREATE TABLE IF NOT EXISTS client ( " +
"no INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
"name VARCHAR(20) NOT NULL, " +
"address VARCHAR(100) NOT NULL, " +
"phone VARCHAR(20) NOT NULL, " +
"beacon VARCHAR(20) NOT NULL, " +
"token VARCHAR(255) NOT NULL, " +
"time VARCHAR(20), " +
"status CHAR(1) NOT NULL DEFAULT '0', " +
"lat VARCHAR(20), " +
"lon VARCHAR(20));";


let dbPool = mysql.createPool(dbConfig);

module.exports.init = () => {
  initDB().then(async()=>{
    const db = dbPool.promise();
    await db.query(SQL_CT_CLIENT);    
  });
}

function initDB() {
  return new Promise((res, rej) => {
    dbPool.query(SQL_CD, (err, rows, fields) => {
      dbConfig.database = DBNAME;
      dbPool = mysql.createPool(dbConfig);
      res();
    });
  });
}

module.exports.type = {
  "insert" : 1,
  "select" : 2,
  "delete" : 3,
  "update" : 4,
}

// Type List
// 1 - Insert, 2 - Select, 3 - Delete
module.exports.query = (type, table, params, target) => {
  return new Promise(async(resolve, reject) => {
    var sql, val;
    var cnt = 0;
    if(type == 1){
      var columns = Object.keys(params);
      sql = "INSERT INTO " + table + "(";
      val = "VALUES ("
      for await (const column of columns) {
        sql += column;
        val += "'" + params[column] + "'";
        if(columns.length -1 != cnt++){
          sql += ", ";
          val += ", ";
        } else {
         sql += ") ";
         val += ")";
       }
     }
     sql += val;
   }else if(type == 2){
    sql = "SELECT ";
    if(params){
      for await (const column of params) {
        sql += column;
        if(params.length -1 != cnt++){
          sql += ", ";
        } 
      }
      
    }else{
      sql += "*";
    }
    sql += " FROM " + table;    
  }else if(type == 3){
    sql = "DELETE FROM " + table;
  }else if(type == 4){
    sql = "UPDATE " + table + " SET ";
    var columns = Object.keys(params);
    if(params){
      for await (const column of columns) {
        sql += column + "='" + params[column] + "'";      
        if(columns.length -1 != cnt++){
          sql += ", ";
        } 
      }
    }
  }

  if(target){
    const name = Object.keys(target)[0];
    sql += " WHERE " + name + " = '"  + target[name] + "'";
  }

  console.log("Create SQL : " + sql);

  dbPool.query(sql, (err, rows, fields) => {
    if(err)
      reject(err);    
    if(rows)
      resolve(rows);
  });
});
}
