$(document).ready(function() {
	
/*	$.ajax( {
				url : "http://localhost:8082/inventories",
				type : "GET",
				headers: { 
        			'Accept': 'application/json',
        			'Content-Type': 'application/json',
        			'Access-Control-Allow-Origin': '*' 
    				},
    			dataType: "json"
			}
		)
		.done(function(data, textStatus, jqXHR) {
			
			$("#catalogue").DataTable({
				data: data,
				columns: [
					{data: "productId"},
					{data: "quantity"}
				]
			});
		})
		.fail(function(jqXHR, textStatus, errorThrown) {
			alert("Catalogue load failed " + errorThrown);
		});*/
	
	
    $("#createOrder").click(function(){
			$("#statuses").empty();
	        var payload =  $("#orderDetailPayload").val();
	        $.ajax( {
					url : "/order",
					type : "PUT",
					data : payload,
					headers: { 
	        			'Accept': 'application/json',
	        			'Content-Type': 'application/json' 
	    				},
	    			dataType: "text"
				}
			)
			.done(function(data, textStatus, jqXHR) {
					var queryString = $.param({orderId: data});
					var eventSource = new EventSource('order/events?' + queryString);
					
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