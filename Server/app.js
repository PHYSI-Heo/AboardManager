const express = require('express');
const bodyParser = require('body-parser');
const path = require('path');

const db = require('./db');
db.createPool();

var app = express();
app.set('port', process.env.PORT || 3000);
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
  extended: false
}));


app.listen(app.get('port'), function() {
  console.log("# Start Server..");
});



app.post('/register/client', (req, res) => {
	var sql = "INSERT INTO client(name, address, phone, beacon, token) VALUES (?, ?, ?, ?, ?)";
	db.query(sql, [req.body.name, req.body.address, req.body.phone, req.body.beacon, req.body.token], (result) => {   
		if(result.resCode == 1001){
			sql = "SELECT max(no) FROM client";
			db.query(sql, [], (max) => {
				res.json(max);
			});
		}else{
			res.json(result);
		}
	});
});


app.post('/get/client', (req, res) => {
	var sql = "SELECT * FROM client WHERE no =?";
	db.query(sql, [req.body.no], (result) => {   
		res.json(result);
	});
});

app.post('/get/beacon/list', (req, res) => {
	var sql = "SELECT beacon FROM client";
	db.query(sql, [], (result) => {   
		res.json(result);
	});
});

app.post('/get/clients', (req, res) => {
	var sql = "SELECT * FROM client";
	db.query(sql, [], (result) => {   
		res.json(result);
	});
});


app.post('/update/client', (req, res) => {
	var sql = "UPDATE client SET name =?, address =?, phone =?, beacon =?, token =? WHERE no =?";
	db.query(sql, [req.body.name, req.body.address, req.body.phone, req.body.beacon, req.body.token, req.body.no], (result) => {   
		res.json(result);
	});
});


app.post('/update/aboard/state', (req, res) => {
	var sql = "UPDATE client SET status =?, time =?, lat =?, lon =? WHERE no =?";
	db.query(sql, [req.body.status, req.body.time, req.body.lat, req.body.lon, req.body.no], (result) => {   
		res.json(result);
	});
});
