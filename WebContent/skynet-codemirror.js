function initializeCodeMirror(id, readOnly)
{
	var xmlTextArea = document.getElementById(id);
	myCodeMirror = CodeMirror.fromTextArea(
		xmlTextArea, 
		{
			mode: {name: "xml", htmlMode: false}, 
			lineNumbers: true,
			readOnly: readOnly
		}
	);
}

function refreshCodeMirror()
{
	for (var i = 0; i < 10; ++i)
	{
		myCodeMirror.refresh();
	}
	console.log("CodeMirror Refreshed");
}