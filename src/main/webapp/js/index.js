$(function() {
	var searchUrl='/checkSearchMsg';
	var userMsgUrl='/getUserMsg';
	
	Date.prototype.Format = function(fmt) {
		var o = {
			"M+" : this.getMonth() + 1, // 月份
			"d+" : this.getDate(), // 日
			"h+" : this.getHours(), // 小时
			"m+" : this.getMinutes(), // 分
			"s+" : this.getSeconds(), // 秒
			"q+" : Math.floor((this.getMonth() + 3) / 3), // 季度
			"S" : this.getMilliseconds()
		// 毫秒
		};
		if (/(y+)/.test(fmt))
			fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "")
					.substr(4 - RegExp.$1.length));
		for ( var k in o)
			if (new RegExp("(" + k + ")").test(fmt))
				fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k])
						: (("00" + o[k]).substr(("" + o[k]).length)));
		return fmt;
	}
	 var time = new Date().Format("yyyy-MM-dd hh:mm");	
	 var t=time.split(" ");
	 var t1=t[0].split("-");
	 var t2=t[1].split(":");
	 var tt=t1.concat(t2);
	 $("#datetime-picker").datetimePicker({	
		    value: tt
		  });
	
	$("#search").click(function(){
		var searchParam={};
		searchParam.origin=$("#origin").val();
		searchParam.terminal=$("#terminal").val();
		searchParam.departWay=$("#depart-way").val();
		searchParam.departTime=$("#datetime-picker").val()+":00";
		searchParam.economicScore=$(".text_ec").text();
		searchParam.timeScore=$(".text_tt").text();
		searchParam.loadBearingScore=$(".text_ss").text();
		searchParam.currentLongitude=localStorage.getItem("currentlng");
		searchParam.currentLatitude=localStorage.getItem("currentlat");
		$.ajax({url:searchUrl,
		data:searchParam,
		type:"POST",
		success:function(result){
			switch(result.code){
			case 200: 
				var data=result.data;
				localStorage.setItem("searchParam",JSON.stringify(searchParam));
				window.location.href="/linkToRoute";			
				break;
			case 40005:
				$.toast("请完整填写搜索信息！");break;
			case 40008:
				$.toast("未匹配到相关地点！");break;	
			case 40009:
				$.toast("请评价你的出行状态！");break;
			case 40007:
				$.toast("定位失败！");break;
			default: 
				$.toast("系统错误");	
		}}
		});
	});
	
	 $('#user').click(function () {
		 $.getJSON(userMsgUrl,function(result){
			 if(result.code==200){
				 $("#profileImg").attr("src",result.data.profileImg);
				 $("#nickName").text(result.data.nickName);
			 }
			 
		 })
	        $.openPanel('#panel-left-demo');
	    });

})