/**
 * cart.js — Smart cart utility
 *
 * - Logged-in users  → cart saved in DB via /api/cart
 * - Guests           → cart saved in localStorage as 'cart_guest'
 * - On login         → guest cart is merged into DB cart automatically
 *
 * Include on every page: <script src="cart.js"></script>
 */

const Cart = (() => {

    const BASE     = '/api/cart';
    const GUEST_KEY = 'cart_guest';

    function getUser()  { return JSON.parse(localStorage.getItem('user') || 'null'); }
    function getToken() { return localStorage.getItem('token'); }
    function isLoggedIn() { return !!(getUser() && getToken()); }

    // ── Auth header ───────────────────────────────────
    function authHeaders() {
        return {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + getToken()
        };
    }

    // ══════════════════════════════════════════════════
    // GUEST — localStorage only
    // ══════════════════════════════════════════════════
    const Guest = {
        load() { return JSON.parse(localStorage.getItem(GUEST_KEY) || '[]'); },
        save(items) { localStorage.setItem(GUEST_KEY, JSON.stringify(items)); },
        add(id, name, price, qty = 1, imageUrl = '') {
            const items = this.load();
            const ex = items.find(i => i.id === id);
            if (ex) ex.qty += qty;
            else items.push({ id, name, price: parseFloat(price), qty, imageUrl });
            this.save(items);
            return this.toResponse(items);
        },
        updateQty(id, newQty) {
            let items = this.load();
            if (newQty <= 0) items = items.filter(i => i.id !== id);
            else { const ex = items.find(i => i.id === id); if (ex) ex.qty = newQty; }
            this.save(items);
            return this.toResponse(items);
        },
        remove(id) { return this.updateQty(id, 0); },
        clear() { localStorage.removeItem(GUEST_KEY); return this.toResponse([]); },
        toResponse(items) {
            const total = items.reduce((s, i) => s + i.price * i.qty, 0);
            return {
                items: items.map(i => ({
                    productId: i.id, productName: i.name,
                    price: i.price, quantity: i.qty,
                    imageUrl: i.imageUrl || '',
                    subtotal: i.price * i.qty
                })),
                total,
                count: items.reduce((s, i) => s + i.qty, 0)
            };
        }
    };

    // ══════════════════════════════════════════════════
    // API — logged-in users
    // ══════════════════════════════════════════════════
    const API = {
        async get() {
            const res = await fetch(BASE, { headers: authHeaders() });
            return res.json();
        },
        async add(productId, productName, price, quantity = 1, imageUrl = '') {
            const res = await fetch(BASE, {
                method: 'POST', headers: authHeaders(),
                body: JSON.stringify({ productId, productName, price, quantity, imageUrl })
            });
            return res.json();
        },
        async updateQty(productId, quantity) {
            const res = await fetch(BASE, {
                method: 'PUT', headers: authHeaders(),
                body: JSON.stringify({ productId, quantity })
            });
            return res.json();
        },
        async remove(productId) {
            const res = await fetch(BASE, {
                method: 'DELETE', headers: authHeaders(),
                body: JSON.stringify({ productId })
            });
            return res.json();
        },
        async clear() {
            const res = await fetch(BASE, { method: 'DELETE', headers: authHeaders() });
            return res.json();
        },
        async merge(guestItems) {
            const res = await fetch(BASE + '/merge', {
                method: 'POST', headers: authHeaders(),
                body: JSON.stringify({ items: guestItems })
            });
            return res.json();
        }
    };

    // ══════════════════════════════════════════════════
    // PUBLIC API — auto-routes to Guest or API
    // ══════════════════════════════════════════════════

    /** Load cart — returns { items, total, count } */
    async function load() {
        if (isLoggedIn()) return API.get();
        return Guest.toResponse(Guest.load());
    }

    /** Add item to cart */
    async function add(id, name, price, qty = 1, imageUrl = '') {
        if (isLoggedIn()) return API.add(id, name, price, qty, imageUrl);
        return Guest.add(id, name, price, qty, imageUrl);
    }

    /** Update quantity */
    async function updateQty(id, qty) {
        if (isLoggedIn()) return API.updateQty(id, qty);
        return Guest.updateQty(id, qty);
    }

    /** Remove item */
    async function remove(id) {
        if (isLoggedIn()) return API.remove(id);
        return Guest.remove(id);
    }

    /** Clear entire cart */
    async function clear() {
        if (isLoggedIn()) return API.clear();
        return Guest.clear();
    }

    /**
     * Call this right after login.
     * Merges guest cart into DB cart, then clears guest cart.
     */
    async function mergeOnLogin() {
        const guestItems = Guest.load();
        if (guestItems.length > 0) {
            await API.merge(guestItems);
            Guest.clear();
        }
    }

    /**
     * Get total item count — useful for updating badge.
     * Returns a number directly (not a promise) from cache,
     * call refreshBadge() to update from server.
     */
    async function getCount() {
        const data = await load();
        return data.count || 0;
    }

    /** Update a cart count badge element by ID */
    async function refreshBadge(elementId = 'cartCount') {
        try {
            const count = await getCount();
            const el = document.getElementById(elementId);
            if (el) el.textContent = count;
        } catch (e) { /* fail silently */ }
    }

    return { load, add, updateQty, remove, clear, mergeOnLogin, getCount, refreshBadge, Guest, API };
})();