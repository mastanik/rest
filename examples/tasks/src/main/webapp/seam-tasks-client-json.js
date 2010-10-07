$(document).ready(function() {
	$.ajaxSetup({
		cache : false
	}); // workaround for IE
})

function ajax(type, url, callback, data) {
	$.ajax({
		type : type,
		url : url,
		data : data,
		complete : function(response) {
			handleResponse(response, callback);
		},
		dataType : "json",
		contentType : "application/json"
	});
}

function getCategories(start, limit, callback) {
	var url = "/seam-tasks/category?start" + start + "&limit=" + limit;
	ajax("GET", url, callback);
}

function putCategory(categoryName, success) {
	var url = "/seam-tasks/category/" + escape(categoryName);
	ajax("PUT", url, success);
}

function deleteCategory(categoryName, success) {
	var url = "/seam-tasks/category/" + escape(categoryName);
	ajax("DELETE", url, success);
}

function getTasksForCategory(categoryName, callback) {
	var URI = escape("/seam-tasks/category/" + categoryName + "/task");
	ajax("GET", URI, callback);
}

function getTask(URI, callback) {
	ajax("GET", URI, callback)
}

function getTasks(status, start, limit, callback) {
	var URI = "/seam-tasks/task?status=" + escape(status) + "&start="
			+ escape(start) + "&limit=" + escape(limit);
	ajax("GET", URI, callback);
}

function postTask(categoryName, taskName, success) {
	var URI = escape("/seam-tasks/category/" + categoryName + "/task");
	ajax("POST", URI, success, JSON.stringify({
		"task" : {
			"name" : taskName
		}
	}))
}

function putTask(task, success) {
	var URI = escape("/seam-tasks/task/" + task.id);
	ajax("PUT", URI, success, JSON.stringify({
		"task" : task
	}));
}

function moveTask(task, newCategory, success) {
	var URI = "/seam-tasks/task/" + task.id + "/move?category="
			+ newCategory.name;
	ajax("POST", URI, success);
}

function deleteTask(task, success) {
	var URI = escape("/seam-tasks/task/" + task.id);
	ajax("DELETE", URI, success);
}

function handleResponse(response, success) {
	if (response.status >= 400) {
		error(response.responseText);
	} else if (response.responseText) {
		success(JSON.parse(response.responseText));
	} else // successful response with no body
	{
		success(response);
	}
}

function error(response) {
	var jsonError;
	try {
		var jsonError = JSON.parse(response);
		if (typeof jsonError.message != "undefined") {
			alert(jsonError.message);
		} else if (typeof jsonError.messages != "undefined") {
			var message = "";
			for (key in jsonError.messages) {
				message = message + jsonError.messages[key] + "\n";
			}
			alert(message);
		} else {
			throw "Error";
		}
	} catch (e) {
		alert("Unknown error");
	}
}

// needed for functional tests
function initialized() {
	$('body').attr('initialized', 'true');
}