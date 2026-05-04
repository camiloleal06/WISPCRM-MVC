const PlanesPage = {
    async render() {
        const planes = await API.get('/planes');
        if (!planes) return '';
        const rows = planes.map(p => `
            <tr>
                <td>${p.id}</td>
                <td>${p.nombre}</td>
                <td>${p.subida || ''}</td>
                <td>${p.descarga || ''}</td>
                <td>${Utils.formatMoney(p.precio)}</td>
                <td>
                    ${Utils.isAdmin() ? `<a href="#/planes/${p.id}/editar" class="btn btn-warning btn-sm"><i class="fas fa-edit"></i></a>` : ''}
                </td>
            </tr>`).join('');

        return `
        <div class="content-header"><div class="container-fluid">
            <div class="row">
                <div class="col-sm-6"><h1 class="m-0">Planes</h1></div>
                <div class="col-sm-6 text-right">
                    ${Utils.isAdmin() ? '<a href="#/planes/nuevo" class="btn btn-primary"><i class="fas fa-plus mr-1"></i>Nuevo Plan</a>' : ''}
                </div>
            </div>
        </div></div>
        <section class="content"><div class="container-fluid">
            <div class="card">
                <div class="card-body">
                    <table id="tbl-planes" class="table table-bordered table-striped">
                        <thead><tr><th>ID</th><th>Nombre</th><th>Subida</th><th>Descarga</th><th>Precio</th><th>Acciones</th></tr></thead>
                        <tbody>${rows}</tbody>
                    </table>
                </div>
            </div>
        </div></section>`;
    },

    afterRender() {
        Utils.initDataTable('#tbl-planes');
    }
};

const PlanFormPage = {
    async render(id) {
        let p = { id: null, nombre: '', subida: '', descarga: '', precio: '' };
        const titulo = id ? 'Editar Plan' : 'Nuevo Plan';
        if (id) {
            const planes = await API.get('/planes');
            const found = (planes || []).find(pl => pl.id == id);
            if (found) p = found;
        }

        return `
        <div class="content-header"><div class="container-fluid">
            <div class="row">
                <div class="col-sm-6"><h1 class="m-0">${titulo}</h1></div>
                <div class="col-sm-6 text-right">
                    <a href="#/planes" class="btn btn-secondary"><i class="fas fa-arrow-left mr-1"></i>Volver</a>
                </div>
            </div>
        </div></div>
        <section class="content"><div class="container-fluid">
            <div class="card card-primary">
                <div class="card-header"><h3 class="card-title">${titulo}</h3></div>
                <form id="formPlan" onsubmit="PlanFormPage.save(event)">
                    <div class="card-body">
                        <input type="hidden" id="fp-id" value="${p.id || ''}">
                        <div class="row">
                            <div class="form-group col-md-6">
                                <label>Nombre del Plan</label>
                                <input type="text" id="fp-nombre" class="form-control" value="${p.nombre || ''}" required>
                            </div>
                            <div class="form-group col-md-6">
                                <label>Precio</label>
                                <input type="number" id="fp-precio" class="form-control" value="${p.precio || ''}" required>
                            </div>
                        </div>
                        <div class="row">
                            <div class="form-group col-md-6">
                                <label>Velocidad Subida</label>
                                <input type="text" id="fp-subida" class="form-control" value="${p.subida || ''}">
                            </div>
                            <div class="form-group col-md-6">
                                <label>Velocidad Descarga</label>
                                <input type="text" id="fp-descarga" class="form-control" value="${p.descarga || ''}">
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
        const id = document.getElementById('fp-id').value;
        const body = {
            nombre: document.getElementById('fp-nombre').value,
            subida: document.getElementById('fp-subida').value,
            descarga: document.getElementById('fp-descarga').value,
            precio: parseFloat(document.getElementById('fp-precio').value)
        };
        if (id) body.id = parseInt(id);

        Utils.showLoading('Guardando plan...');
        const res = await API.post('/planes', body);
        Utils.hideLoading();
        if (res && res.status === 'ok') {
            Utils.notify(res.message);
            Router.navigate('#/planes');
        } else {
            Utils.notify(res?.message || 'Error al guardar', 'error');
        }
    }
};
