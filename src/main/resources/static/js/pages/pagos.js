const PagosPage = {
    async render() {
        const pagos = await API.get('/pagos');
        if (!pagos) return '';
        const total = pagos.reduce((sum, p) => sum + p.pago, 0);
        const mesActual = new Date().toLocaleDateString('es-CO', { month: 'long', year: 'numeric' });
        const rows = pagos.map(p => `
            <tr>
                <td>${p.id}</td>
                <td>${p.facturaId}</td>
                <td>${p.nombre}</td>
                <td>${p.fechapago ? new Date(p.fechapago).toLocaleDateString('es-CO') : ''}</td>
                <td>${Utils.formatMoney(p.pago)}</td>
            </tr>`).join('');

        return `
        <div class="content-header"><div class="container-fluid">
            <h1 class="m-0">Pagos — ${mesActual}</h1>
        </div></div>
        <section class="content"><div class="container-fluid">
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">Total Recaudado: <strong class="text-success">${Utils.formatMoney(total)}</strong></h3>
                    <span class="float-right badge bg-info">${pagos.length} pagos</span>
                </div>
                <div class="card-body">
                    <table id="tbl-pagos" class="table table-bordered table-striped">
                        <thead><tr><th>ID</th><th>Factura</th><th>Cliente</th><th>Fecha</th><th>Valor</th></tr></thead>
                        <tbody>${rows}</tbody>
                    </table>
                </div>
            </div>
        </div></section>`;
    },

    afterRender() {
        Utils.initDataTable('#tbl-pagos');
    }
};
