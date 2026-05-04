const API = {
    base: '/api/v1',

    async get(path) {
        const res = await fetch(this.base + path);
        if (res.status === 401 || res.status === 403) { window.location.href = '/login'; return null; }
        return res.json();
    },

    async post(path, body) {
        const res = await fetch(this.base + path, {
            method: 'POST', headers: {'Content-Type':'application/json'}, body: JSON.stringify(body)
        });
        if (res.status === 401) { window.location.href = '/login'; return null; }
        const data = await res.json();
        data._httpStatus = res.status;
        return data;
    },

    async put(path, body) {
        const res = await fetch(this.base + path, {
            method: 'PUT', headers: {'Content-Type':'application/json'}, body: body ? JSON.stringify(body) : undefined
        });
        if (res.status === 401) { window.location.href = '/login'; return null; }
        const data = await res.json();
        data._httpStatus = res.status;
        return data;
    },

    async delete(path) {
        const res = await fetch(this.base + path, { method: 'DELETE' });
        if (res.status === 401) { window.location.href = '/login'; return null; }
        const data = await res.json();
        data._httpStatus = res.status;
        return data;
    }
};
