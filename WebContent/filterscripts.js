function toggleTags(project, category)
{
	jQuery('input[id*=tag-' + project + "-" + category + ']').each(
			function(index, element)
			{
				element.checked=!element.checked; 
				jQuery('input[id*=' + element.id.replace('tag', 'tagnot') + ']').each(
						function(index2, element2)
						{
							element2.disabled = !element.checked;
						}
					);
			}
		); 
	jQuery('div[id*=logicPanel-' + project + "-" + category + '_body]')[0].style.display = '';
}

function toggleNotTags(project, category)
{
	jQuery('input[id*=tagnot-' + project + "-" + category + ']').each(
			function(index, element)
			{
				element.checked=!element.checked
			}
		); 
	jQuery('div[id*=logicPanel-' + category + '_body]')[0].style.display = '';
}

function tagChecked(checkbox, project, category)
{
	try
	{
		// count how many checkboxes are checked
		var count = 0; 
		jQuery('input[id*=tag-' + project + "-" + category + ']').each(
				function(index, element)
				{
					if (element.checked)
						++count;
				}
			); 
	
		// open the logic panel if more than two are checked
		if (count >= 2) 
			jQuery('div[id*=logicPanel-' + project + "-" + category + '_body]')[0].style.display = ''; 
	}
	catch(err)
	{
		console.log("There was an error opening the logic panel.");
		console.log(err.description);
	}

	try
	{
		// enable the not tag if this is checked
		jQuery('input[id*=' + checkbox.id.replace('tag', 'tagnot') + ']').each(
				function(index, element)
				{ 
					element.disabled = !checkbox.checked;
				}
			);
	}
	catch(err)
	{
		console.log("There was an enabling/disabling the not check box");
		console.log(err.description);
	}
}

function tagClicked(tag, excludeArray) 
{
	if (tag.checked)
	{		
		for (i in excludeArray)
		{	
			var excludeTag = jQuery('input[id*=TagID' + excludeArray[i] + ']');
			if (excludeTag.length != 0)
			{
				excludeTag[0].checked = false;
			}
		}
	}
}