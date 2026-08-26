const OrdenesPage = {
    async render() {
        const [ordenes, clientes, tipos, tecnicos] = await Promise.all([
            API.get('/ordenes').catch(() => []),
            API.get('/clientes').catch(() => []),
            API.get('/ordenes/tipos').catch(() => []),
            API.get('/ordenes/operarios').catch(() => [])
        ]);
        if (!ordenes) return '';

        const rows = ordenes.map(o => `
            <tr>
                <td>${o.id}</td>
                <td>${o.clienteNombre || ''}</td>
                <td>${o.tipoOrden || ''}</td>
                <td>${o.descripcion || ''}</td>
                <td>${o.operario || ''}</td>
                <td>${o.fechaInicio || ''}</td>
                <td>${o.estado === 'ABIERTA'
                    ? '<span class="badge badge-warning">ABIERTA</span>'
                    : '<span class="badge badge-success">CERRADA</span>'}</td>
                <td>
                    ${o.estado === 'ABIERTA'
                        ? `<button class="btn btn-success btn-sm" onclick="OrdenesPage.mostrarCierre(${o.id}, '${(o.clienteNombre||'').replace(/'/g,"\\'")}', '${(o.descripcion||'').replace(/'/g,"\\'")}')">
                               <i class="fas fa-check"></i> Cerrar
                           </button>`
                        : `<small class="text-muted">${o.comentarioCierre || ''}</small>`}
                </td>
            </tr>`).join('');

        const clienteOptions = (clientes || []).map(c =>
            `<option value="${c.id}">${c.nombres} - ${c.telefono}</option>`).join('');
        const tipoOptions = (tipos || []).map(t =>
            `<option value="${t.id}">${t.descripcion}</option>`).join('');
        const tecnicoOptions = (tecnicos || []).map(t =>
            `<option value="${t.id}">${t.nombres}</option>`).join('');

        return `
        <div class="content-header"><div class="container-fluid">
            <div class="row">
                <div class="col-sm-6"><h1 class="m-0">Órdenes de Trabajo</h1></div>
                <div class="col-sm-6 text-right">
                    <button class="btn btn-danger mr-1" onclick="OrdenesPage.cerrarTodas()">
                        <i class="fas fa-times-circle mr-1"></i>Cerrar Todas
                    </button>
                    <button class="btn btn-primary" onclick="OrdenesPage.mostrarFormNueva()">
                        <i class="fas fa-plus mr-1"></i>Nueva Orden
                    </button>
                </div>
            </div>
        </div></div>
        <section class="content"><div class="container-fluid">

            <!-- Modal Nueva Orden -->
            <div class="modal fade" id="modalNuevaOrden" tabindex="-1">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header bg-primary">
                            <h5 class="modal-title text-white">Nueva Orden de Trabajo</h5>
                            <button type="button" class="close text-white" data-dismiss="modal">&times;</button>
                        </div>
                        <div class="modal-body">
                            <div class="form-row">
                                <div class="form-group col-md-12">
                                    <label>Cliente</label>
                                    <select id="o-cliente" class="form-control" required>
                                        <option value="">-- Seleccione --</option>
                                        ${clienteOptions}
                                    </select>
                                </div>
                            </div>
                            <div class="form-row">
                                <div class="form-group col-md-6">
                                    <label>Tipo de Orden</label>
                                    <select id="o-tipo" class="form-control">${tipoOptions}</select>
                                </div>
                                <div class="form-group col-md-6">
                                    <label>Técnico Responsable</label>
                                    <select id="o-tecnico" class="form-control">${tecnicoOptions}</select>
                                </div>
                            </div>
                            <div class="form-group">
                                <label>Descripción del trabajo a realizar</label>
                                <textarea id="o-descripcion" class="form-control" rows="4"
                                    placeholder="Describa el trabajo a realizar..." required></textarea>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancelar</button>
                            <button type="button" class="btn btn-primary" onclick="OrdenesPage.crear()">
                                <i class="fas fa-save mr-1"></i>Crear y Notificar al Grupo
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Modal Cerrar Orden -->
            <div class="modal fade" id="modalCerrarOrden" tabindex="-1">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header bg-success">
                            <h5 class="modal-title text-white">Cerrar Orden <span id="cierre-titulo"></span></h5>
                            <button type="button" class="close text-white" data-dismiss="modal">&times;</button>
                        </div>
                        <div class="modal-body">
                            <input type="hidden" id="cierre-id">
                            <div class="form-group">
                                <label>Trabajo realizado</label>
                                <textarea id="cierre-comentario" class="form-control" rows="5"
                                    placeholder="Describa el trabajo realizado, materiales usados, solución aplicada..."
                                    required></textarea>
                                <small class="text-muted">Este comentario se enviará al grupo de WhatsApp.</small>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-dismiss="modal">Cancelar</button>
                            <button type="button" class="btn btn-success" onclick="OrdenesPage.cerrar()">
                                <i class="fas fa-check-circle mr-1"></i>Cerrar y Notificar
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="card">
                <div class="card-body">
                    <table id="tbl-ordenes" class="table table-bordered table-striped">
                        <thead><tr>
                            <th>#</th><th>Cliente</th><th>Tipo</th><th>Descripción</th>
                            <th>Técnico</th><th>Fecha</th><th>Estado</th><th>Acciones</th>
                        </tr></thead>
                        <tbody>${rows}</tbody>
                    </table>
                </div>
            </div>
        </div></section>`;
    },

    afterRender() { Utils.initDataTable('#tbl-ordenes'); },

    mostrarFormNueva() {
        $('#modalNuevaOrden').modal('show');
    },

    mostrarCierre(id, cliente, descripcion) {
        document.getElementById('cierre-id').value = id;
        document.getElementById('cierre-titulo').textContent = `#${id} - ${cliente}`;
        document.getElementById('cierre-comentario').value = '';
        $('#modalCerrarOrden').modal('show');
    },

    async cerrarTodas() {
        const res = await Utils.confirmAndRun(
            '¿Cerrar todas las órdenes abiertas?',
            'Se marcarán como cerradas sin enviar notificación.',
            'Cerrando...',
            () => API.post('/ordenes/cerrar-todas')
        );
        if (res) {
            Utils.notify(res.message);
            Router.navigate('#/ordenes');
        }
    },

    async crear() {
        const clienteId = document.getElementById('o-cliente').value;
        const tipoId = document.getElementById('o-tipo').value;
        const tecnicoId = document.getElementById('o-tecnico').value;
        const descripcion = document.getElementById('o-descripcion').value.trim();

        if (!clienteId || !descripcion) {
            Utils.notify('Cliente y descripción son obligatorios', 'error'); return;
        }

        Utils.showLoading('Creando orden...');
        const res = await API.post('/ordenes', { clienteId, tipoOrdenId: tipoId, operarioId: tecnicoId, descripcion });
        Utils.hideLoading();

        if (res && res.status === 'ok') {
            $('#modalNuevaOrden').modal('hide');
            Utils.notify(res.message);
            Router.navigate('#/ordenes');
        } else {
            Utils.notify(res?.message || 'Error al crear orden', 'error');
        }
    },

    async cerrar() {
        const id = document.getElementById('cierre-id').value;
        const comentario = document.getElementById('cierre-comentario').value.trim();

        if (!comentario) { Utils.notify('El comentario es obligatorio', 'error'); return; }

        Utils.showLoading('Cerrando orden...');
        const res = await API.post('/ordenes/' + id + '/cerrar', { comentario });
        Utils.hideLoading();

        if (res && res.status === 'ok') {
            $('#modalCerrarOrden').modal('hide');
            Utils.notify(res.message);
            Router.navigate('#/ordenes');
        } else {
            Utils.notify(res?.message || 'Error al cerrar orden', 'error');
        }
    }
};
