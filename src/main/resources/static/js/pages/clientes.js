const ClientesPage = {
    async render() {
        const clientes = await API.get('/clientes');
        if (!clientes) return '';
        const rows = clientes.map(c => `
            <tr>
                <td>${c.nombres}</td>
                <td>${c.telefono}</td>
                <td>${Utils.formatMoney(c.valor)}</td>
                <td>${Utils.estadoBadge(c.estado)}</td>
                <td class="text-nowrap">
                    <a href="#/clientes/${c.id}" class="btn btn-info btn-sm" title="Ver"><i class="fas fa-eye"></i></a>
                    ${Utils.isAdmin() ? `<a href="#/clientes/${c.id}/editar" class="btn btn-warning btn-sm" title="Editar"><i class="fas fa-user-edit"></i></a>` : ''}
                    ${c.estado === 'ACTIVO' ? `
                        <a href="/facturar/${c.id}" class="btn btn-success btn-sm" title="Facturar"><i class="fas fa-file-invoice"></i></a>
                        ${Utils.isAdmin() ? `<button class="btn btn-dark btn-sm" onclick="ClientesPage.suspender(${c.id})" title="Suspender"><i class="fas fa-ban"></i></button>` : ''}
                        ${Utils.isAdmin() ? `<button class="btn btn-danger btn-sm" onclick="ClientesPage.eliminar(${c.id})" title="Desactivar"><i class="fas fa-user-minus"></i></button>` : ''}
                    ` : ''}
                    ${c.estado === 'SUSPENDIDO' && Utils.isAdmin() ? `
                        <button class="btn btn-success btn-sm" onclick="ClientesPage.reactivar(${c.id})" title="Reactivar"><i class="fas fa-user-plus"></i></button>
                        <button class="btn btn-outline-success btn-sm" onclick="ClientesPage.reactivarForzado(${c.id})" title="Reactivar sin pago"><i class="fas fa-unlock"></i></button>
                    ` : ''}
                    ${c.estado === 'INACTIVO' && Utils.isAdmin() ? `
                        <button class="btn btn-success btn-sm" onclick="ClientesPage.reactivar(${c.id})" title="Reactivar"><i class="fas fa-user-plus"></i></button>
                    ` : ''}
                </td>
            </tr>`).join('');

        return `
        <div class="content-header"><div class="container-fluid">
            <div class="row">
                <div class="col-sm-6"><h1 class="m-0">Clientes</h1></div>
                <div class="col-sm-6 text-right">
                    <a href="#/clientes/nuevo" class="btn btn-primary"><i class="fas fa-plus mr-1"></i>Nuevo Cliente</a>
                    ${Utils.isAdmin() ? '<a href="#" onclick="ClientesPage.facturarLote(); return false;" class="btn btn-success ml-1"><i class="fas fa-file-invoice-dollar mr-1"></i>Facturar Lote</a>' : ''}
                </div>
            </div>
        </div></div>
        <section class="content"><div class="container-fluid">
            <div class="card">
                <div class="card-body">
                    <table id="tbl-clientes" class="table table-bordered table-striped">
                        <thead><tr><th>Nombre</th><th>Teléfono</th><th>Plan</th><th>Estado</th><th>Acciones</th></tr></thead>
                        <tbody>${rows}</tbody>
                    </table>
                </div>
            </div>
        </div></section>`;
    },

    afterRender() { Utils.initDataTable('#tbl-clientes'); },

    async eliminar(id) {
        const res = await Utils.confirmAndRun('¿Desactivar cliente?', 'El cliente será marcado como inactivo.', 'Desactivando...',
            () => API.delete('/clientes/' + id));
        if (res) { Utils.notify('Cliente desactivado'); Router.navigate('#/clientes'); }
    },

    async reactivar(id) {
        const res = await Utils.confirmAndRun('¿Reactivar cliente?', 'Se reactivará el servicio y se notificará al cliente.', 'Reactivando...',
            () => API.put('/clientes/' + id + '/reactivar'));
        if (res) { Utils.notify(res.message || 'Cliente reactivado'); Router.navigate('#/clientes'); }
    },

    async reactivarForzado(id) {
        const res = await Utils.confirmAndRun('¿Reactivar SIN pago?', 'Se reactivará el servicio sin verificar pago y sin notificar al cliente.', 'Reactivando...',
            () => API.put('/clientes/' + id + '/reactivar-forzado'));
        if (res) { Utils.notify(res.message || 'Cliente reactivado (forzado)'); Router.navigate('#/clientes'); }
    },

    async facturarLote() {
        const res = await Utils.confirmAndRun('¿Facturar en lote?', 'Se generarán facturas para los clientes activos del rango de días actual.', 'Facturando...',
            () => API.post('/facturas/lote'));
        if (res) {
            if (res.status === 'ok') Utils.notify(res.message);
            else Utils.notify(res?.message || 'Error al facturar', 'error');
        }
    },

    async suspender(id) {
        const res = await Utils.confirmAndRun('⚠️ ¿Suspender servicio?', 'Se bloqueará el internet y se notificará por WhatsApp.', 'Suspendiendo servicio...',
            () => API.put('/clientes/' + id + '/suspender'));
        if (res) {
            if (res.status === 'ok') Utils.notify(res.message);
            else Utils.notify(res?.message || 'Error al suspender', 'error');
            Router.navigate('#/clientes');
        }
    }
};

const ClienteDetallePage = {
    async render(id) {
        const c = await API.get('/clientes/' + id);
        if (!c) return '<div class="content-header"><div class="container-fluid"><div class="alert alert-danger">Cliente no encontrado</div></div></div>';
        return `
        <div class="content-header"><div class="container-fluid">
            <div class="row">
                <div class="col-sm-6"><h1 class="m-0">Detalle Cliente</h1></div>
                <div class="col-sm-6 text-right">
                    <a href="#/clientes/${id}/editar" class="btn btn-warning"><i class="fas fa-edit mr-1"></i>Editar</a>
                    <a href="#/clientes" class="btn btn-secondary"><i class="fas fa-arrow-left mr-1"></i>Volver</a>
                </div>
            </div>
        </div></div>
        <section class="content"><div class="container-fluid">
            <div class="card card-primary">
                <div class="card-header"><h3 class="card-title">${c.nombres} ${c.apellidos}</h3></div>
                <div class="card-body"><div class="row">
                    <div class="col-md-6"><table class="table table-sm">
                        <tr><td><strong>Identificación</strong></td><td>${c.identificacion || ''}</td></tr>
                        <tr><td><strong>Email</strong></td><td>${c.email || ''}</td></tr>
                        <tr><td><strong>Teléfono</strong></td><td>${c.telefono || ''}</td></tr>
                        <tr><td><strong>Dirección</strong></td><td>${c.direccion || ''}</td></tr>
                    </table></div>
                    <div class="col-md-6"><table class="table table-sm">
                        <tr><td><strong>Día de Pago</strong></td><td>${c.diapago || ''}</td></tr>
                        <tr><td><strong>IP</strong></td><td>${c.ipAddress || ''}</td></tr>
                        <tr><td><strong>PPPoE User</strong></td><td>${c.pppoeUser || ''}</td></tr>
                        <tr><td><strong>PPPoE Pass</strong></td><td>${c.pppoePass || ''}</td></tr>
                    </table></div>
                </div></div>
            </div>
        </div></section>`;
    }
};

const ClienteFormPage = {
    async render(id) {
        const planes = await API.get('/planes');
        const profiles = await API.get('/profiles');
        let c = { id:null, identificacion:'', nombres:'', apellidos:'', email:'', telefono:'', diapago:1, direccion:'', ipAddress:'', pppoeUser:'', pppoePass:'', planesId:null, profileId:null };
        const titulo = id ? 'Editar Cliente' : 'Nuevo Cliente';
        if (id) {
            const data = await API.get('/clientes/' + id);
            if (data) c = data;
        }

        const planOptions = (planes || []).map(p =>
            `<option value="${p.id}" ${p.id === c.planesId ? 'selected' : ''}>${p.nombre}</option>`
        ).join('');

        const profileOptions = (profiles || []).map(p =>
            `<option value="${p.id}" ${p.id === c.profileId ? 'selected' : ''}>${p.name}</option>`
        ).join('');

        return `
        <div class="content-header"><div class="container-fluid">
            <div class="row">
                <div class="col-sm-6"><h1 class="m-0">${titulo}</h1></div>
                <div class="col-sm-6 text-right">
                    <a href="#/clientes" class="btn btn-secondary"><i class="fas fa-arrow-left mr-1"></i>Volver</a>
                </div>
            </div>
        </div></div>
        <section class="content"><div class="container-fluid">
            <div class="card card-primary">
                <div class="card-header"><h3 class="card-title">${titulo}</h3></div>
                <form id="formCliente" onsubmit="ClienteFormPage.save(event)">
                    <div class="card-body">
                        <input type="hidden" id="fc-id" value="${c.id || ''}">
                        <div class="row">
                            <div class="form-group col-md-6">
                                <label>Identificación</label>
                                <input type="text" id="fc-identificacion" class="form-control" value="${c.identificacion || ''}" required>
                            </div>
                            <div class="form-group col-md-6">
                                <label>Nombres</label>
                                <input type="text" id="fc-nombres" class="form-control" value="${c.nombres || ''}" required>
                            </div>
                        </div>
                        <div class="row">
                            <div class="form-group col-md-6">
                                <label>Apellidos</label>
                                <input type="text" id="fc-apellidos" class="form-control" value="${c.apellidos || ''}">
                            </div>
                            <div class="form-group col-md-6">
                                <label>Email</label>
                                <input type="email" id="fc-email" class="form-control" value="${c.email || ''}">
                            </div>
                        </div>
                        <div class="row">
                            <div class="form-group col-md-4">
                                <label>Teléfono</label>
                                <input type="text" id="fc-telefono" class="form-control" value="${c.telefono || ''}" required>
                            </div>
                            <div class="form-group col-md-2">
                                <label>Día de Pago</label>
                                <input type="number" id="fc-diapago" class="form-control" min="1" max="30" value="${c.diapago || 1}" required>
                            </div>
                            <div class="form-group col-md-3">
                                <label>Plan</label>
                                <select id="fc-planesId" class="form-control" required>${planOptions}</select>
                            </div>
                            <div class="form-group col-md-3">
                                <label>Dirección</label>
                                <input type="text" id="fc-direccion" class="form-control" value="${c.direccion || ''}">
                            </div>
                        </div>
                        <div class="row">
                            <div class="form-group col-md-3">
                                <label>IP Address</label>
                                <input type="text" id="fc-ipAddress" class="form-control" value="${c.ipAddress || ''}">
                            </div>
                            <div class="form-group col-md-3">
                                <label>PPPoE User</label>
                                <input type="text" id="fc-pppoeUser" class="form-control" value="${c.pppoeUser || ''}">
                            </div>
                            <div class="form-group col-md-3">
                                <label>PPPoE Pass</label>
                                <input type="text" id="fc-pppoePass" class="form-control" value="${c.pppoePass || ''}">
                            </div>
                            <div class="form-group col-md-3">
                                <label>Profile PPPoE</label>
                                <select id="fc-profileId" class="form-control">${profileOptions}</select>
                            </div>
                        </div>
                    </div>
                    <div class="card-footer">
                        <button type="submit" class="btn btn-primary"><i class="fas fa-save mr-1"></i>Guardar</button>
                    </div>
                </form>
            </div>
        </div></section>`;
    },

    async save(e) {
        e.preventDefault();
        const rawId = document.getElementById('fc-id').value;
        const body = {
            id: rawId ? parseInt(rawId) : null,
            identificacion: document.getElementById('fc-identificacion').value,
            nombres: document.getElementById('fc-nombres').value,
            apellidos: document.getElementById('fc-apellidos').value,
            email: document.getElementById('fc-email').value,
            telefono: document.getElementById('fc-telefono').value,
            diapago: parseInt(document.getElementById('fc-diapago').value),
            direccion: document.getElementById('fc-direccion').value,
            ipAddress: document.getElementById('fc-ipAddress').value,
            pppoeUser: document.getElementById('fc-pppoeUser').value,
            pppoePass: document.getElementById('fc-pppoePass').value,
            planesId: parseInt(document.getElementById('fc-planesId').value),
            profileId: parseInt(document.getElementById('fc-profileId').value)
        };

        Utils.showLoading('Guardando...');
        const res = await API.post('/clientes', body);
        Utils.hideLoading();
        if (res && res.status === 'ok') {
            Utils.notify(res.message);
            Router.navigate('#/clientes');
        } else {
            Utils.notify(res?.message || 'Error al guardar', 'error');
        }
    }
};
