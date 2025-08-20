function eliminar(id) {
	console.log(id);
	swal({
		  title: "Esta seguro de Eliminar?",
		  text: "Se eliminara la factura. Una vez eliminada no se prodra restablecer!",
		  icon: "warning",
		  buttons: true,
		  dangerMode: true,
		})
		.then((OK) => {
		  if (OK) {
			  $.ajax({
				 url:"/eliminarfactura/"+id,
				 success: function(res) {
					console.log(res);
				},			
			  });
		    swal("Poof! Factura eliminada!", {
		      icon: "success",
		    }).then((ok)=>{
		    	if(ok){
		    		location.href="/listarfactura";
		    	}
		    });
		  } 
		});
}

function pagar(id) {
	console.log(id);
	swal({
		  title: "Esta seguro de aplicar pago?",
		  text: "Se registrar pago de la factura. Una vez se registre el pago no se podra anular!",
		  icon: "warning",
		  buttons: true,
		  dangerMode: true,
		})
		.then((OK) => {
		  if (OK) {
			  $.ajax({
				 url:"/pagar/"+id,
				 success: function(res) {
					console.log(res);
				},			
			  });
		    swal("Poof! Se ha registrado el pago con exito!", {
		      icon: "success",
		    }).then((ok)=>{
		    	if(ok){
		    		location.href="/listarfactura";
		    	}
		    });
		  } 
		});
}

