const Utils = {
    formatMoney(val) {
        if (!val) return '$0';
        return new Intl.NumberFormat('es-CO', { style:'currency', currency:'COP', maximumFractionDigits:0 }).format(val);
    },

    notify(msg, type = 'success') {
        Swal.fire({ toast:true, position:'top-end', icon:type, title:msg, showConfirmButton:false, timer:3000 });
    },

    confirm(title, text) {
        return Swal.fire({ title, text, icon:'warning', showCancelButton:true, confirmButtonColor:'#d33', confirmButtonText:'Sí', cancelButtonText:'Cancelar' });
    },

    showLoading(msg = 'Procesando...') {
        Swal.fire({ title: msg, allowOutsideClick: false, allowEscapeKey: false, didOpen: () => Swal.showLoading() });
    },

    hideLoading() {
        Swal.close();
    },

    async confirmAndRun(title, text, loadingMsg, asyncFn) {
        const r = await this.confirm(title, text);
        if (!r.isConfirmed) return null;
        this.showLoading(loadingMsg || 'Procesando...');
        try {
            const res = await asyncFn();
            this.hideLoading();
            return res;
        } catch (e) {
            this.hideLoading();
            this.notify('Error: ' + e.message, 'error');
            return null;
        }
    },

    initDataTable(selector) {
        if ($.fn.DataTable.isDataTable(selector)) { $(selector).DataTable().destroy(); }
        $(selector).DataTable({
            responsive:true, autoWidth:true, ordering:true, paging:true, searching:true, info:true,
            language: {
                sProcessing:"Procesando...", sLengthMenu:"Mostrar _MENU_ registros",
                sZeroRecords:"No se encontraron resultados", sEmptyTable:"Sin datos",
                sInfo:"Mostrando _START_ a _END_ de _TOTAL_", sInfoEmpty:"Sin registros",
                sInfoFiltered:"(filtrado de _MAX_)", sSearch:"Buscar:",
                oPaginate:{ sFirst:"Primero", sLast:"Último", sNext:"Siguiente", sPrevious:"Anterior" }
            }
        });
    },

    estadoBadge(estado) {
        const map = { ACTIVO:'badge bg-success', INACTIVO:'badge bg-warning', SUSPENDIDO:'badge bg-danger' };
        return `<span class="${map[estado] || 'badge bg-secondary'}">${estado}</span>`;
    },

    isAdmin() {
        return window.currentUser && window.currentUser.role === 'ROLE_ADMIN';
    }
};
