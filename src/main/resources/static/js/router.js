const Router = {
    routes: {
        '':           { page: HomePage },
        '/':          { page: HomePage },
        '/clientes':  { page: ClientesPage },
        '/facturas':  { page: FacturasPage },
        '/pagos':     { page: PagosPage },
        '/planes':    { page: PlanesPage },
    },

    app: null,

    init() {
        this.app = document.getElementById('app');
        window.addEventListener('hashchange', () => this.resolve());
        this.resolve();
    },

    async resolve() {
        const hash = location.hash.replace('#', '') || '/';

        // /clientes/nuevo
        if (hash === '/clientes/nuevo') {
            return this.render(ClienteFormPage);
        }

        // /planes/nuevo
        if (hash === '/planes/nuevo') {
            return this.render(PlanFormPage);
        }

        // /planes/:id/editar
        const planEditMatch = hash.match(/^\/planes\/(\d+)\/editar$/);
        if (planEditMatch) {
            return this.render(PlanFormPage, planEditMatch[1]);
        }

        // /clientes/:id/editar
        const editMatch = hash.match(/^\/clientes\/(\d+)\/editar$/);
        if (editMatch) {
            return this.render(ClienteFormPage, editMatch[1]);
        }

        // /clientes/:id
        const clienteMatch = hash.match(/^\/clientes\/(\d+)$/);
        if (clienteMatch) {
            return this.render(ClienteDetallePage, clienteMatch[1]);
        }

        const route = this.routes[hash];
        if (route) {
            return this.render(route.page);
        }

        this.app.innerHTML = `
            <div class="content-header"><div class="container-fluid">
                <h1>404 - Página no encontrada</h1>
                <a href="#/" class="btn btn-primary mt-3">Ir al Dashboard</a>
            </div></div>`;
    },

    async render(page, param) {
        this.app.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin fa-2x"></i><p>Cargando...</p></div>';
        try {
            this.app.innerHTML = await page.render(param);
            if (page.afterRender) page.afterRender();
            this.updateActiveMenu();
        } catch (e) {
            console.error('Router error:', e);
            window.location.href = '/login';
        }
    },

    navigate(hash) {
        if (location.hash === hash) {
            this.resolve();
        } else {
            location.hash = hash;
        }
    },

    updateActiveMenu() {
        document.querySelectorAll('.nav-sidebar .nav-link').forEach(el => el.classList.remove('active'));
        const hash = location.hash || '#/';
        document.querySelectorAll(`.nav-sidebar a[href="${hash}"]`).forEach(el => {
            el.classList.add('active');
            const parent = el.closest('.has-treeview');
            if (parent) parent.classList.add('menu-open');
        });
    }
};

document.addEventListener('DOMContentLoaded', () => Router.init());
