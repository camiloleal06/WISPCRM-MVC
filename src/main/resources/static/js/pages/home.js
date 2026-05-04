const HomePage = {
    async render() {
        const d = await API.get('/dashboard');
        if (!d) return '';

        const activos = d.clientesActivos || 0;
        const pendiente = d.pendientesMes || 0;
        const recaudado = d.recaudadoMes || 0;
        const totalMes = pendiente + recaudado;
        const pctRecaudo = totalMes > 0 ? Math.round((recaudado / totalMes) * 100) : 0;
        const pctColor = pctRecaudo >= 70 ? 'bg-success' : pctRecaudo >= 40 ? 'bg-warning' : 'bg-danger';

        const conDeuda = d.clientesConDeuda || 0;
        const pctMorosidad = activos > 0 ? Math.round((conDeuda / activos) * 100) : 0;
        const morosidadColor = pctMorosidad <= 20 ? 'text-success' : pctMorosidad <= 50 ? 'text-warning' : 'text-danger';

        const esperado = d.ingresoEsperado || 0;
        const pctEsperado = esperado > 0 ? Math.round((recaudado / esperado) * 100) : 0;

        const morososRows = (d.topMorosos || []).map(f => `
            <tr>
                <td>${f.nombres}</td>
                <td>${f.telefonoCliente}</td>
                <td>${Utils.formatMoney(f.valorFactura)}</td>
                <td><span class="badge ${f.mora > 15 ? 'bg-danger' : f.mora > 7 ? 'bg-warning' : 'bg-info'}">${f.mora} días</span></td>
            </tr>`).join('');

        const deudoresRows = (d.topDeudores || []).map(dd => `
            <tr>
                <td>${dd.nombres}</td>
                <td>${dd.telefono}</td>
                <td><span class="badge bg-danger">${dd.cantidadFacturas}</span></td>
                <td class="text-danger font-weight-bold">${Utils.formatMoney(dd.totalDeuda)}</td>
            </tr>`).join('');

        const pagosRows = (d.ultimosPagos || []).map(p => `
            <tr>
                <td>${p.nombre}</td>
                <td>${Utils.formatMoney(p.pago)}</td>
                <td>${p.fechapago ? new Date(p.fechapago).toLocaleDateString('es-CO') : ''}</td>
            </tr>`).join('');

        const porVencerRows = (d.porVencer || []).map(f => `
            <tr>
                <td>${f.nombres}</td>
                <td>${Utils.formatMoney(f.valorFactura)}</td>
                <td>${f.diapago}</td>
            </tr>`).join('');

        return `
        <div class="content-header"><div class="container-fluid">
            <h1 class="m-0">Dashboard</h1>
        </div></div>
        <section class="content"><div class="container-fluid">

            <!-- Stats Cards -->
            <div class="row">
                <div class="col-lg-3 col-6">
                    <div class="small-box bg-primary stat-card" onclick="location.hash='#/clientes'">
                        <div class="inner"><h3>${activos}</h3><p>Clientes Activos</p></div>
                        <div class="icon"><i class="fas fa-users"></i></div>
                    </div>
                </div>
                <div class="col-lg-3 col-6">
                    <div class="small-box bg-info stat-card" onclick="location.hash='#/facturas'">
                        <div class="inner"><h3>${d.cantidadPendientesMes || 0}</h3><p>Facturas Pendientes</p></div>
                        <div class="icon"><i class="fas fa-file-invoice"></i></div>
                    </div>
                </div>
                <div class="col-lg-3 col-6">
                    <div class="small-box bg-danger stat-card" onclick="location.hash='#/facturas'">
                        <div class="inner"><h3>${Utils.formatMoney(pendiente)}</h3><p>Por Cobrar</p></div>
                        <div class="icon"><i class="fas fa-exclamation-triangle"></i></div>
                    </div>
                </div>
                <div class="col-lg-3 col-6">
                    <div class="small-box bg-success stat-card" onclick="location.hash='#/pagos'">
                        <div class="inner"><h3>${Utils.formatMoney(recaudado)}</h3><p>Recaudado</p></div>
                        <div class="icon"><i class="fas fa-money-bill-wave"></i></div>
                    </div>
                </div>
            </div>

            <!-- KPIs Row -->
            <div class="row">
                <div class="col-lg-3 col-6">
                    <div class="info-box">
                        <span class="info-box-icon bg-warning"><i class="fas fa-percentage"></i></span>
                        <div class="info-box-content">
                            <span class="info-box-text">Tasa de Morosidad</span>
                            <span class="info-box-number ${morosidadColor}">${pctMorosidad}%</span>
                            <small>${conDeuda} de ${activos} clientes</small>
                        </div>
                    </div>
                </div>
                <div class="col-lg-3 col-6">
                    <div class="info-box">
                        <span class="info-box-icon bg-info"><i class="fas fa-balance-scale"></i></span>
                        <div class="info-box-content">
                            <span class="info-box-text">Ingreso Esperado</span>
                            <span class="info-box-number">${Utils.formatMoney(esperado)}</span>
                            <small>Recaudado: ${pctEsperado}%</small>
                        </div>
                    </div>
                </div>
                <div class="col-lg-3 col-6">
                    <div class="info-box">
                        <span class="info-box-icon bg-success"><i class="fas fa-user-plus"></i></span>
                        <div class="info-box-content">
                            <span class="info-box-text">Clientes Nuevos</span>
                            <span class="info-box-number">${d.clientesNuevosMes || 0}</span>
                            <small>Este mes</small>
                        </div>
                    </div>
                </div>
                <div class="col-lg-3 col-6">
                    <div class="info-box">
                        <span class="info-box-icon bg-danger"><i class="fas fa-clock"></i></span>
                        <div class="info-box-content">
                            <span class="info-box-text">Por Vencer (7 días)</span>
                            <span class="info-box-number">${(d.porVencer || []).length}</span>
                            <small>Facturas próximas</small>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Progress Bars -->
            <div class="row">
                <div class="col-lg-6">
                    <div class="card">
                        <div class="card-body py-2">
                            <div class="d-flex justify-content-between mb-1"><span>Recaudo del mes</span><strong>${pctRecaudo}%</strong></div>
                            <div class="progress" style="height:18px;">
                                <div class="progress-bar ${pctColor}" style="width:${pctRecaudo}%">${Utils.formatMoney(recaudado)} de ${Utils.formatMoney(totalMes)}</div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-6">
                    <div class="card">
                        <div class="card-body py-2">
                            <div class="d-flex justify-content-between mb-1"><span>Esperado vs Recaudado</span><strong>${pctEsperado}%</strong></div>
                            <div class="progress" style="height:18px;">
                                <div class="progress-bar bg-info" style="width:${pctEsperado}%">${Utils.formatMoney(recaudado)} de ${Utils.formatMoney(esperado)}</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Charts -->
            <div class="row">
                <div class="col-lg-8">
                    <div class="card">
                        <div class="card-header"><h3 class="card-title"><i class="fas fa-chart-bar mr-1"></i>Recaudo Mensual</h3></div>
                        <div class="card-body"><canvas id="chartPagosMensual" style="height:280px;max-height:280px;"></canvas></div>
                    </div>
                </div>
                <div class="col-lg-4">
                    <div class="card">
                        <div class="card-header"><h3 class="card-title"><i class="fas fa-chart-pie mr-1"></i>Clientes por Estado</h3></div>
                        <div class="card-body"><canvas id="chartClientes" style="height:280px;max-height:280px;"></canvas></div>
                    </div>
                </div>
            </div>

            <!-- Tables -->
            <div class="row">
                <div class="col-lg-6">
                    <div class="card card-danger card-outline">
                        <div class="card-header"><h3 class="card-title"><i class="fas fa-user-clock mr-1"></i>Top 10 Facturas en Mora</h3></div>
                        <div class="card-body p-0">
                            <table class="table table-sm table-striped">
                                <thead><tr><th>Cliente</th><th>Teléfono</th><th>Valor</th><th>Mora</th></tr></thead>
                                <tbody>${morososRows || '<tr><td colspan="4" class="text-center text-muted">Sin morosos</td></tr>'}</tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <div class="col-lg-6">
                    <div class="card card-warning card-outline">
                        <div class="card-header"><h3 class="card-title"><i class="fas fa-exclamation-circle mr-1"></i>Top 10 Más Endeudados</h3></div>
                        <div class="card-body p-0">
                            <table class="table table-sm table-striped">
                                <thead><tr><th>Cliente</th><th>Teléfono</th><th>Facturas</th><th>Deuda</th></tr></thead>
                                <tbody>${deudoresRows || '<tr><td colspan="4" class="text-center text-muted">Sin deudores</td></tr>'}</tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-lg-6">
                    <div class="card card-info card-outline">
                        <div class="card-header"><h3 class="card-title"><i class="fas fa-clock mr-1"></i>Facturas por Vencer (7 días)</h3></div>
                        <div class="card-body p-0">
                            <table class="table table-sm table-striped">
                                <thead><tr><th>Cliente</th><th>Valor</th><th>Día Pago</th></tr></thead>
                                <tbody>${porVencerRows || '<tr><td colspan="3" class="text-center text-muted">Sin facturas próximas</td></tr>'}</tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <div class="col-lg-6">
                    <div class="card card-success card-outline">
                        <div class="card-header"><h3 class="card-title"><i class="fas fa-hand-holding-usd mr-1"></i>Últimos Pagos</h3></div>
                        <div class="card-body p-0">
                            <table class="table table-sm table-striped">
                                <thead><tr><th>Cliente</th><th>Valor</th><th>Fecha</th></tr></thead>
                                <tbody>${pagosRows || '<tr><td colspan="3" class="text-center text-muted">Sin pagos</td></tr>'}</tbody>
                            </table>
                        </div>
                        <div class="card-footer text-center">
                            <a href="#/pagos" class="text-sm">Ver todos los pagos <i class="fas fa-arrow-right"></i></a>
                        </div>
                    </div>
                </div>
            </div>

        </div></section>`;
    },

    async afterRender() {
        const d = await API.get('/dashboard');
        this.renderBarChart();
        this.renderDonutChart(d);
    },

    async renderBarChart() {
        const meses = ['Ene','Feb','Mar','Abr','May','Jun','Jul','Ago','Sep','Oct','Nov','Dic'];
        const data = await API.get('/pagos/mensual');
        if (!data || !data.length) return;
        const ctx = document.getElementById('chartPagosMensual');
        if (!ctx) return;
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.map(d => meses[d.mes - 1] + ' ' + d.anio),
                datasets: [{
                    label: 'Recaudo', data: data.map(d => d.total || 0),
                    backgroundColor: 'rgba(40, 167, 69, 0.6)', borderColor: 'rgba(40, 167, 69, 1)', borderWidth: 1,
                }, {
                    label: 'Tendencia', data: data.map(d => d.total || 0), type: 'line', fill: false,
                    borderColor: 'rgba(0, 123, 255, 1)', borderWidth: 2, pointBackgroundColor: 'rgba(0, 123, 255, 1)', pointRadius: 4, lineTension: 0.3,
                }]
            },
            options: {
                responsive: true, maintainAspectRatio: false,
                legend: { display: true, position: 'top' },
                tooltips: { callbacks: { label: (item, cd) => cd.datasets[item.datasetIndex].label + ': ' + Utils.formatMoney(item.yLabel) } },
                scales: { yAxes: [{ ticks: { beginAtZero: true, callback: v => v >= 1e6 ? '$'+(v/1e6).toFixed(1)+'M' : v >= 1e3 ? '$'+(v/1e3).toFixed(0)+'K' : '$'+v } }] }
            }
        });
    },

    renderDonutChart(d) {
        const ctx = document.getElementById('chartClientes');
        if (!ctx || !d) return;
        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Activos', 'Inactivos', 'Suspendidos'],
                datasets: [{ data: [d.clientesActivos||0, d.clientesInactivos||0, d.clientesSuspendidos||0], backgroundColor: ['#28a745','#ffc107','#dc3545'], borderWidth: 2 }]
            },
            options: { responsive: true, maintainAspectRatio: false, legend: { position: 'bottom' }, cutoutPercentage: 60 }
        });
    }
};
