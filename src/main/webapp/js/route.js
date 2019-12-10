$(function() {
	var param = localStorage.getItem("searchParam");
	var searchParam = JSON.parse(param);
	var getTripLineUrl = '/getTripLine';
	$.ajax({
		url : getTripLineUrl,
		data : searchParam,
		type : "POST",
		success : function(result) {
			if (result.code == 200) {
				var data=result.data;
				console.log(data);
				var totalTime=data.totalTime;
				var totalCost=data.totalCost;
				var totalComfortDegree=data.totalComfortDegree;
				var stepList=data.steps;
				stepList.map(function(item,index){
					var html='';
					html+='<div class="card"><div class="card-header">'
						+item.tempOri+"->"+item.tempTer;
					if(item.isDrive){
						html+="(驾车)";
					}
					html+='<a href="#" class="button button-detail" data-flag="1" style="width:50px;height:30px;">详情</a></div>'
						+'<div class="card-content"><div class="card-content-inner"><div class="route-detail"  style="display:none">';
					if(item.isSubway){
						var lineLen = item.subwayLinePlanning.passSubwayLineList.length;
						var stationLen = item.subwayLinePlanning.passSitesList.length;
						var stationList = item.subwayLinePlanning.passSitesList;
						var index=0;
					for(var tt=0;tt<lineLen;tt++){
						var line=item.subwayLinePlanning.passSubwayLineList[tt];
						if(line==null){
							continue;
						}
						html+='<b>'+line.lineName+'</b>'+'<br>';
						while(stationList[index]!=null&&index<stationLen){
							html+=stationList[index].stationName+'->';
							index++;
						}
						html+='<br>';	
						index++;
					}				
						html+='</div></div></div><div class="card-footer">'
							+"直线距离:"+item.tempDistance+"公里"+"&nbsp;&nbsp;&nbsp;&nbsp;"+"耗时:"+item.tempTime+"&nbsp;&nbsp;&nbsp;&nbsp;"+"花费:"+item.tempCost+"元"
							+'</div></div>';
					}else if(item.isFlight){
					html+='<b>'+item.way.flightName+"&nbsp;&nbsp;&nbsp;&nbsp;"+item.way.aircraftType+"&nbsp;&nbsp;&nbsp;&nbsp;"+item.way.aircraftTypeCode+'<br>'
						+"FROM:"+item.way.departurePlace.cityName+"&nbsp;&nbsp;&nbsp;&nbsp;"+"TO:"+item.way.destinationPlace.cityName+'<br>'
						+"出发时间:"+item.way.departureTime+"&nbsp;&nbsp;&nbsp;&nbsp;"+"到达时间:"+item.way.arriveTime+'<br>'
						+"折扣方式:"+item.way.discountMethod+"&nbsp;&nbsp;&nbsp;&nbsp;"+"准点率:"+item.way.punctualityRate+'%'+'<b>';
					html+='</div></div></div><div class="card-footer">'
							+"距离:"+"—"+"公里"+"&nbsp;&nbsp;&nbsp;&nbsp;"+"耗时:"+item.tempTime+"&nbsp;&nbsp;&nbsp;&nbsp;"+"花费:"+item.tempCost+"元"
							+'</div></div>';
					}else if(item.isRail){
						html+='<b>'+item.way.railCode+"&nbsp;&nbsp;&nbsp;&nbsp;"+"FROM:"+item.way.departurePlace.cityName+"&nbsp;&nbsp;&nbsp;&nbsp;"+"TO:"+item.way.stopoverStation.cityName+'<br>'
						+"出发时间:"+item.way.departureTime+"&nbsp;&nbsp;&nbsp;&nbsp;"+"到达时间:"+item.way.arriveTime+'<br>'
						+"一等座:"+item.way.firstSeatPrice+"&nbsp;&nbsp;&nbsp;&nbsp;"+"二等座:"+item.way.secondSeatPrice+"&nbsp;&nbsp;&nbsp;&nbsp;"+"商务座:"+item.way.businessSeatPrice+'<br><b>';
						html+='</div></div></div><div class="card-footer">'
							+"距离:"+"—"+"公里"+"&nbsp;&nbsp;&nbsp;&nbsp;"+"耗时:"+item.tempTime+"&nbsp;&nbsp;&nbsp;&nbsp;"+"花费:"+item.tempCost+"元"
							+'</div></div>';
					}else{
						item.way.steps.map(function(item1,index1){					
							html+=item1.instruction+'<br>';
						});
						html+='</div></div></div><div class="card-footer">'
							+"距离:"+item.tempDistance+"米"+"&nbsp;&nbsp;&nbsp;&nbsp;"+"耗时:"+item.tempTime+"&nbsp;&nbsp;&nbsp;&nbsp;"+"花费:"+item.tempCost+"元"
							+'</div></div>';
					}
					$(".content").append(html);
				});
				var alertMsg='总耗时:'+totalTime+","+"总费用:"+"￥"+totalCost;
				setTimeout(() => {
					$.alert(alertMsg, '本系统已为您选择最佳路线');
				}, 500); 
			} else {
			console.log('换条路线试试  =￣ω￣=','抱歉,未能找出完整路线！');
				$.alert('换条路线试试  =￣ω￣=','抱歉,未能找出完整路线！');
//				$.alert('早些休息，明日再启程吧  ^-^','夜已深,暂无航班！');
			}
		}
	});
	
	$(".content").on('click','.button-detail',function(e){
		if(e.currentTarget.dataset.flag==1){
			$(e.currentTarget).parent().next().find(".route-detail").css("display","block");
			$(e.currentTarget).text("收起");
			e.currentTarget.dataset.flag=0;
		}else{
			$(e.currentTarget).parent().next().find(".route-detail").css("display","none");
			$(e.currentTarget).text("详情");
			e.currentTarget.dataset.flag=1;
		}	
	});
})