var mediasiteLti = JSON.parse('{"source":"","origin":"","editor":"","target":"_self"}');

CKEDITOR.plugins.add('Mediasite',
   {   
       requires: ['iframedialog'],
	   extraAllowedContent: 'iframe',
	   htmlEncodeOutput: 'false',
       init: function (editor) {
		   var pluginName = 'Mediasite';
           var mypath = this.path;
           mediasiteLti.editor = editor;
           editor.ui.addButton(
            'Mediasite',
            {
                label: "Mediasite",
                command: 'Mediasite.cmd',
                icon: mypath + 'images/atom.gif'
            }
         );
           var cmd = editor.addCommand('Mediasite.cmd', { exec: showDialogPlugin });
		   var siteId = "TestSiteId";
		   if (typeof portal != 'undefined' && portal != null) {
			   siteId = portal.siteTitle;
		   }
		   var jsp = 'Mediasite.jsp';
           CKEDITOR.dialog.addIframe(
            'Mediasite.dlg',
            'Mediasite',
			'/direct/Mediasite/postLTI?siteId=' + siteId,
            600,
            600,
            function () {
            	// the frame loaded
            },
            {
            	onOk : function(a, b, c) {
            		// ok was clicked
            		mediasiteLti.source.postMessage('{"messageType":"getData"}', '*');
            	}
            }
         );
       }
   }
);

function showDialogPlugin(e) {
    e.openDialog('Mediasite.dlg');
}

function onDialogEvent(a, b, c) {
	alert('plugins.js onDialogEvent');
}

function receiveMessage(event) {
	var data = JSON.parse(event.data);
	if (data.messageType === 'setup') {
		mediasiteLti.source = event.source;
		mediasiteLti.origin = event.origin;
	} else if (data.messageType === 'content') {
		// take the Json response and Sakaify it with markup that follows Sakai template
		// based on the content type & mode
		// presentation
		//  metadata only
		//  metadata + player
		// catalog
		//  link only
		//  iframe
		// presentation metadata
		var content = JSON.parse(decodeURIComponent(data.content));
		var paste = '';
		if (content.EntityType === 'Presentation') {
			paste = buildPresentationMetadata(content);
			if (content.Mode === 'SakaiMetadataOnly') {
				// nothing else needs to be done
			} else {
				alert(content.Mode + ' not supported.');
			}
		} else if (content.EntityType === 'CatalogFolderDetails') {
			if (content.Mode === 'SakaiBasicLTI') {
				paste = buildCatalogLink(content);
			} else {
				alert(content.Mode + ' not supported.');
			}			
		} else {
			alert(content.EntityType + ' not supported.');
		}
		
		mediasiteLti.editor.insertHtml(paste);
	}
}
function buildPresentationMetadata(data) {
	var title = '<div class="mediasite-title"><a href="/direct/Mediasite/launchContent?mediasiteId=' + data.ResourceId + '" target="' + mediasiteLti.target + '">' + data.Title + '</a></div>';
	var description = '<div class="mediasite-description"><img src="' + data.ThumbnailUrl + '" class="mediasite-thumbnail" />' + data.Description + '</div>';
	var airDate = '<div class="mediasite-airdate">' + data.RecordDateTimeUTC + '</div>';
	var presenters = '';
	for (var i = 0; i < data.Presenters.length; i++) {
		presenters += '<li class="mediasite-presenter">' + data.Presenters[i] + '</li>';
	}
	if (presenters != '') {
		presenters = '<div class="mediasite-presenter-heading">Presenters</div><ul class="mediasite-presenter-list">' + presenters + '</ul>';
	}
	var tags = '';
	for (var i = 0; i < data.Tags.length; i++) {
		tags += '<div class="mediasite-tag">' + data.Tags[i] + '</div>';
	}
	if (tags != '') {
		tags = '<div class="mediasite-tag-heading">Tags</div><ul class="mediasite-tag-list">' + tags + '</ul>';
	}
	
	return '<div>' + title + airDate + description + presenters + tags + '</div>';

}
function buildCatalogLink(data) {
	return '<a href="/direct/Mediasite/launchContent?mediasiteId=' + data.ResourceId + '" target="' + mediasiteLti.target + '">' + data.Title + '</a>';
	
}
window.addEventListener("message", receiveMessage, false);