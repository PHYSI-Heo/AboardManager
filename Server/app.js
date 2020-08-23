const express = require('express');
const bodyParser = require('body-parser');
const path = require('path');


const db = require('./myDB');
db.init();


var app = express();
app.set('port', process.env.PORT || 3000);
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
  extended: false
}));


app.listen(app.get('port'), function() {
  console.log("# Start Server..");
});


app.post('/register/client', async (req, res) => {
	let code, header;
	try{
		header = await db.query(db.type.insert, "client", req.body, null);
		code = 1001;
	}catch(err){
		console.log(err);
	}
	res.json({
		"resCode" : code,
		"header" : header
	});
});

app.post('/get/client', async (req, res) => {
	let code, rows;
	try{
		if(req.body.no){
			rows = await db.query(db.type.select, "client", null, {"no" : req.body.no});
		}else{
			rows = await db.query(db.type.select, "client", null, null);
		}		
		code = 1001;
	}catch(err){
		console.log(err);
	}
	res.json({
		"resCode" : code,
		"rows" : rows
	});
});

app.post('/get/beacon/list', async (req, res) => {
	let code, rows;
	try{
		rows = await db.query(db.type.select, "client", ["beacon"], null);
		code = 1001;
	}catch(err){
		console.log(err);
	}
	res.json({
		"resCode" : code,
		"rows" : rows
	});
});

app.post('/update/client', async (req, res) => {
	let code, header;
	try{		
		const no = req.body.no;
		delete req.body.no;
		header = await db.query(db.type.update, "client", req.body, {"no" : no});
		code = 1001;
	}catch(err){
		console.log(err);
	}
	res.json({
		"resCode" : code,
		"header" : header
	});
});

app.post('/update/aboard/state', async (req, res) => {
	let code, header;
	try{
		const no = req.body.no;
		delete req.body.no;
		header = await db.query(db.type.update, "client", req.body, {"no" : no});
		code = 1001;
	}catch(err){
		console.log(err);
	}
	res.json({
		"resCode" : code,
		"header" : header
	});
});
