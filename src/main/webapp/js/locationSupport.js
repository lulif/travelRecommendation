$(function(){
var bjStations=null;
var kmStations=null;

$.ajax({
    url:"/locationSupport/km",
    type:"GET",
    success:function(result){
         if(result.code==200){
          kmStations=result.data;
        }
    }});

$.ajax({
     url:"/locationSupport/bj",
     type:"GET",
     success:function(result){
               if(result.code==200){
                bjStations=result.data;
              }
       }});


 $(".kmLocation").on('input',function(){
          jQuery(".kmLocation").autocomplete(kmStations);
       });

 $(".bjLocation").on('input',function(){
          jQuery(".bjLocation").autocomplete(bjStations);
    });

});






