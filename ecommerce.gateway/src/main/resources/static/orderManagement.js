$(document).ready(function() {
	
	var token = '';
	
	$.ajax( {
					url : "/user/login",
	    			type : "POST",
	    			headers: { 
	        			'Accept': 'application/json',
	        			'Content-Type': 'application/json' 
	    				},
	    			data : JSON.stringify({
    							"userName": "user1",
    							"password": "pwd"
							})
				}
			)
			.done(function(data, textStatus, jqXHR) {
				token = jqXHR.getResponseHeader('Authorization');
			});
	
	$("#searchCatalogue").click(function(){
		
		
		var payload = {
				    "field" : "brand",
				    "input" : {
				            "pattern" : "Pepe",
				            "fuzzyLevel" : 1
				        }
				};
				
		payload = $("#searchDetailPayload").val();
		$.ajax( {
					url : "/catalogue",
					headers: { 
						'Authorization': token,
	        			'Access-Control-Allow-Origin': '*' 
	    				},
	    			data : {'filter' : payload}
				}
			)
			.done(function(data, textStatus, jqXHR) {
				
				if ( $.fn.DataTable.isDataTable('#catalogue') ) {
				  $('#catalogue').DataTable().destroy();
				}
				
				$('#catalogue tbody').empty();
				
				$("#catalogue").DataTable({
					data: data,
					columns: [
						{data: "productId"},
						{data: "category"},
						{data: "brand"},
						{data: "price"},
						{data: "stock"},
						{data: "colour"},
						{data: "type"}
					]
				});
			})
			.fail(function(jqXHR, textStatus, errorThrown) {
				alert("Catalogue load failed " + errorThrown);
			});
		});
	
	
    $("#createOrder").click(function(){
			$("#statuses").empty();
	        var payload =  $("#orderDetailPayload").val();
	        $.ajax( {
					url : "/order",
					type : "PUT",
					data : payload,
					headers: { 
						'Authorization': token,
	        			'Accept': 'application/json',
	        			'Content-Type': 'application/json' 
	    				},
	    			dataType: "text"
				}
			)
			.done(function(data, textStatus, jqXHR) {
					var queryString = $.param({orderId: data});
					var eventSource = new EventSource('order/events?' + queryString, {withCredentials: true})
					
					eventSource.addEventListener('OrderRequested', sse => {
						var now = new Date(Date.now());
						var formatted = now.getHours() + ":" + now.getMinutes() + ":" + now.getSeconds();
  						$("#statuses").append('<li><p>' + formatted + '</p><p>OrderRequested</p></li>');
					});
					
					eventSource.addEventListener('OrderRejected', sse => {
						var now = new Date(Date.now());
						var formatted = now.getHours() + ":" + now.getMinutes() + ":" + now.getSeconds();
						var rejectionReasons = JSON.parse(sse.data);
						var html = '<li><p>' + formatted + '</p><p>OrderRejected</p><p>';
						$.each(rejectionReasons,function(index, value){
    						html = html + '<span>' + value + '</span>';
						});
						html = html + '</p></li>';
  						$("#statuses").append(html);
  						eventSource.close();
  						$.ajax( {
								url : "/order/events?" + queryString,
								type : "DELETE",
								headers: { 
									'Authorization': token,
				        			'Accept': 'application/json',
				        			'Content-Type': 'application/json' 
				    				}
							}
						);
					});
					
					eventSource.addEventListener('OrderApproved', sse => {
						var now = new Date(Date.now());
						var formatted = now.getHours() + ":" + now.getMinutes() + ":" + now.getSeconds();
  						$("#statuses").append('<li><p>' + formatted + '</p><p>OrderApproved</p></li>');
  						eventSource.close();
  						$.ajax( {
								url : "/order/events?" + queryString,
								type : "DELETE",
								headers: { 
									'Authorization': token,
				        			'Accept': 'application/json',
				        			'Content-Type': 'application/json' 
				    				}
							}
						);
					});
					
					/*source.onmessage = function(sse) {
						$("#statuses").append('<li><p>' + formatted + '</p><p>'+ sse.data + '</p></li>');
					}*/
				}
			)
			.fail(function(jqXHR, textStatus, errorThrown) {
					eventSource.close();
					alert("Order creation failed " + errorThrown);
				}
			);
    	}
    ); 
});