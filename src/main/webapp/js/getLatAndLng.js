$(function() {
	configWx();

	setTimeout(function() {
		getLocation()
	}, 500);

	function configWx() {
		var thisPageUrl = location.href.split('#')[0];
		$.ajax({
			url : "/wechatlocation/getticket",
			type : 'POST',
			data : {
				url : thisPageUrl
			},
			success : function(data) {
				if (data != null) {
					configWeiXin(data.appId, data.timestamp, data.nonceStr,
							data.signature);
				}
			}
		});
	}

	function configWeiXin(appId, timestamp, nonceStr, signature) {
		wx.config({
			debug : false,
			appId : appId,
			timestamp : timestamp,
			nonceStr : nonceStr,
			signature : signature,
			jsApiList : [ 'getLocation' ]
		});
	}
	function getLocation() {
		wx.getLocation({
			type : 'gcj02',
			success : function(res) {
				localStorage.setItem("currentlat", res.latitude);
				localStorage.setItem("currentlng", res.longitude);
			}
		});
	}

});