const mysql = require('mysql');

const MYSQLIP = 'localhost';
const MYSQLID = 'root';
const MYSQLPWD = '1234';
const DBNAME = 'AMS';

const dbInfo = {
  host : MYSQLIP,
  port : 3306,
  user : MYSQLID,
  password : MYSQLPWD,
  database : DBNAME,
  connectionLimit:100,
  waitForConnections:true
};

var dbPool;

module.exports.createPool = () => {
  dbPool = mysql.createPool(dbInfo);
  console.log("# Create MySQL ThreadPool..");
  if(dbPool){
    initTable();
  }
}

function query(sql, values, callback){
  var result = {};
  dbPool.getConnection(function (con_Err, con) {
    if(con_Err){
      // DB Connect Err
      result.resCode = 1002;
      console.log('\x1b[35m%s\x1b[0m', "## DB Connect Err : " + con_Err.message);
      callback(result);
    }else{
      con.query(sql, values, function (query_Err, rows) {
        if(query_Err){
          // Query Error
          result.resCode = 1003;
          console.log('\x1b[35m%s\x1b[0m', "## DB Query Err : " + query_Err.message);
        }else{
          // Query Result
          result.resCode = 1001;
          result.rows = rows;
        }
        con.release();
        callback(result);
      });
    }
  });
}
module.exports.query = query;


const SQL_CT_CLIENT = "CREATE TABLE client ( " +
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

function initTable() {
    query(SQL_CT_CLIENT, [], (res)=>{
      console.log("# Create Table : " + res.resCode);
    });
};  