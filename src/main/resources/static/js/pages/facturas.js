const FacturasPage = {
    async render() {
        const facturas = await API.get('/facturas');
        if (!facturas) return '';
        const rows = facturas.map(f => {
            const moraBadge = f.mora > 15
                ? `<span class="badge bg-danger">${f.mora} días</span>`
                : f.mora > 7
                    ? `<span class="badge bg-warning">${f.mora} días</span>`
                    : `<span class="badge bg-success">${f.mora} días</span>`;
            const suspenderBtn = f.mora > 7 && Utils.isAdmin()
                ? `<button class="btn btn-dark btn-sm" onclick="FacturasPage.suspender(${f.clienteId})" title="Suspender"><i class="fas fa-ban"></i></button>`
                : '';
            return `
            <tr>
                <td><input type="checkbox" class="fac-check" data-id="${f.idFactura}" data-cliente="${f.clienteId}"></td>
                <td>${f.idFactura}</td>
                <td>${f.nombres}</td>
                <td>${f.telefonoCliente}</td>
                <td>${Utils.formatMoney(f.valorFactura)}</td>
                <td>${f.diapago}</td>
                <td>${moraBadge}</td>
                <td class="text-nowrap">
                    <button class="btn btn-primary btn-sm" onclick="FacturasPage.pagar(${f.idFactura})"><i class="fas fa-money-bill"></i></button>
                    <a href="/descargarfactura/${f.idFactura}" class="btn btn-success btn-sm" target="_blank"><i class="fas fa-download"></i></a>
                    <button class="btn btn-info btn-sm" onclick="FacturasPage.recordar(${f.idFactura})"><i class="fas fa-bell"></i></button>
                    ${suspenderBtn}
                    ${Utils.isAdmin() ? `<button class="btn btn-danger btn-sm" onclick="FacturasPage.eliminar(${f.idFactura})"><i class="fas fa-trash"></i></button>` : ''}
                </td>
            </tr>`;
        }).join('');

        return `
        <div class="content-header"><div class="container-fluid">
            <div class="row">
                <div class="col-sm-4"><h1 class="m-0">Facturas Pendientes</h1></div>
                <div class="col-sm-8 text-right" id="batch-bar" style="display:none;">
                    <span id="batch-count" class="badge bg-primary mr-2">0 seleccionadas</span>
                    <button class="btn btn-primary btn-sm" onclick="FacturasPage.batchPagar()"><i class="fas fa-money-bill mr-1"></i>Pagar</button>
                    <button class="btn btn-info btn-sm" onclick="FacturasPage.batchRecordar()"><i class="fas fa-bell mr-1"></i>Recordar</button>
                    ${Utils.isAdmin() ? '<button class="btn btn-dark btn-sm" onclick="FacturasPage.batchSuspender()"><i class="fas fa-ban mr-1"></i>Suspender</button>' : ''}
                    ${Utils.isAdmin() ? '<button class="btn btn-danger btn-sm" onclick="FacturasPage.batchEliminar()"><i class="fas fa-trash mr-1"></i>Eliminar</button>' : ''}
                    <button class="btn btn-secondary btn-sm" onclick="FacturasPage.clearSelection()"><i class="fas fa-times"></i></button>
                </div>
            </div>
        </div></div>
        <section class="content"><div class="container-fluid">
            <div class="card">
                <div class="card-body">
                    <table id="tbl-facturas" class="table table-bordered table-striped">
                        <thead><tr>
                            <th><input type="checkbox" id="select-all"></th>
                            <th>ID</th><th>Cliente</th><th>Teléfono</th><th>Valor</th><th>Día Pago</th><th>Mora</th><th>Acciones</th>
                        </tr></thead>
                        <tbody>${rows}</tbody>
                    </table>
                </div>
            </div>
        </div></section>`;
    },

    afterRender() {
        var table = Utils.initDataTable('#tbl-facturas');
        $('#tbl-facturas').on('change', '#select-all', function() {
            var checked = this.checked;
            $('#tbl-facturas tbody .fac-check').prop('checked', checked);
            FacturasPage.updateBatchBar();
        });
        $('#tbl-facturas tbody').on('change', '.fac-check', function() {
            FacturasPage.updateBatchBar();
        });
    },

    getSelectedIds() {
        var ids = [];
        $('#tbl-facturas tbody .fac-check:checked').each(function() { ids.push(parseInt($(this).data('id'))); });
        return ids;
    },

    getSelectedClienteIds() {
        var set = new Set();
        $('#tbl-facturas tbody .fac-check:checked').each(function() { set.add(parseInt($(this).data('cliente'))); });
        return [...set];
    },

    updateBatchBar() {
        var count = $('#tbl-facturas tbody .fac-check:checked').length;
        var bar = document.getElementById('batch-bar');
        var badge = document.getElementById('batch-count');
        bar.style.display = count > 0 ? '' : 'none';
        badge.textContent = count + ' seleccionada' + (count !== 1 ? 's' : '');
    },

    clearSelection() {
        $('#tbl-facturas tbody .fac-check').prop('checked', false);
        $('#select-all').prop('checked', false);
        this.updateBatchBar();
    },

    // ========== ACCIONES INDIVIDUALES ==========

    async pagar(id) {
        const res = await Utils.confirmAndRun('¿Registrar pago?', 'Se marcará la factura como pagada.', 'Registrando pago...',
            () => API.put('/facturas/' + id + '/pagar'));
        if (res) { Utils.notify(res.message || 'Pago registrado'); Router.navigate('#/facturas'); }
    },

    async eliminar(id) {
        const res = await Utils.confirmAndRun('¿Eliminar factura?', 'Esta acción no se puede deshacer.', 'Eliminando...',
            () => API.delete('/facturas/' + id));
        if (res) { Utils.notify('Factura eliminada'); Router.navigate('#/facturas'); }
    },

    async recordar(id) {
        const res = await Utils.confirmAndRun('¿Enviar recordatorio?', 'Se enviará un mensaje por WhatsApp.', 'Enviando...',
            () => API.put('/facturas/' + id + '/recordar'));
        if (res) { Utils.notify(res.message || 'Recordatorio enviado'); Router.navigate('#/facturas'); }
    },

    async suspender(clienteId) {
        const res = await Utils.confirmAndRun('⚠️ ¿Suspender servicio?', 'Se bloqueará el internet y se notificará por WhatsApp.', 'Suspendiendo...',
            () => API.put('/clientes/' + clienteId + '/suspender'));
        if (res) {
            if (res.status === 'ok') Utils.notify(res.message);
            else Utils.notify(res?.message || 'Error', 'error');
            Router.navigate('#/facturas');
        }
    },

    // ========== ACCIONES EN LOTE ==========

    async batchPagar() {
        const ids = this.getSelectedIds();
        const res = await Utils.confirmAndRun(`¿Pagar ${ids.length} facturas?`, 'Se registrarán todos los pagos.', `Pagando ${ids.length} facturas...`,
            () => API.put('/facturas/batch/pagar', ids));
        if (res) { Utils.notify(res.message); Router.navigate('#/facturas'); }
    },

    async batchEliminar() {
        const ids = this.getSelectedIds();
        const res = await Utils.confirmAndRun(`¿Eliminar ${ids.length} facturas?`, 'Esta acción no se puede deshacer.', `Eliminando ${ids.length} facturas...`,
            () => API.put('/facturas/batch/eliminar', ids));
        if (res) { Utils.notify(res.message); Router.navigate('#/facturas'); }
    },

    async batchRecordar() {
        const ids = this.getSelectedIds();
        const res = await Utils.confirmAndRun(`¿Enviar ${ids.length} recordatorios?`, 'Se enviará WhatsApp a cada cliente.', `Enviando ${ids.length} recordatorios...`,
            () => API.put('/facturas/batch/recordar', ids));
        if (res) { Utils.notify(res.message); Router.navigate('#/facturas'); }
    },

    async batchSuspender() {
        const ids = this.getSelectedClienteIds();
        const res = await Utils.confirmAndRun(`⚠️ ¿Suspender ${ids.length} clientes?`, 'Se bloqueará el internet de cada cliente.', `Suspendiendo ${ids.length} clientes...`,
            () => API.put('/clientes/batch/suspender', ids));
        if (res) { Utils.notify(res.message); Router.navigate('#/facturas'); }
    }
};
