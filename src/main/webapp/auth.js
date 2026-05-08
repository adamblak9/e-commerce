/**
 * auth.js — Drop this ONE <script> tag into any page's <head>.
 * It auto-applies the right rule based on the current page.
 *
 * ─── For existing pages ────────────────────────────────────────────────────
 *   Just add ONE line in <head> — no other changes needed:
 *   <script src="auth.js"></script>
 *
 * ─── For any NEW page you create ───────────────────────────────────────────
 *   Option A — add it to the ROUTE_MAP below (zero JS in the page itself)
 *   Option B — call manually after the script tag:
 *
 *     Auth.requireAuth();                        // login required
 *     Auth.requireRole('ADMIN', 'SELLER');       // login + specific roles
 *     Auth.requireGuest();                       // redirect away if logged in
 */

const Auth = (() => {

    // ── Storage keys ─────────────────────────────────────────────────────────
    const TOKEN_KEY    = 'token';
    const USER_KEY     = 'user';
    const INTENDED_KEY = 'auth_intended';

    // ── Route map ─────────────────────────────────────────────────────────────
    // Add any page here — the script handles it automatically.
    //
    // 'auth'         → must be logged in (any role)
    // 'guest'        → must NOT be logged in (login/signup pages)
    // ['ADMIN',...]  → must be logged in AND have one of these roles
    // (omitted)      → public, no check
    const ROUTE_MAP = {
        'login.html'     : 'guest',
        'signup.html'    : 'guest',
        // index.html   → public, remove it
        'dashboard.html' : 'auth',
        'payment.html'   : 'auth',
        'products.html'  : ['ADMIN', 'SELLER'],
        'product.html'   : 'auth',
         'store.html '   : 'guest',
    };

    // ── Helpers ───────────────────────────────────────────────────────────────

    function getToken() { return localStorage.getItem(TOKEN_KEY); }

    function getUser() {
        try { return JSON.parse(localStorage.getItem(USER_KEY) || 'null'); }
        catch { return null; }
    }

    function isLoggedIn() { return !!getToken() && !!getUser(); }

    function currentPage() {
        const parts = window.location.pathname.split('/');
        return parts[parts.length - 1] || 'index.html';
    }

    function saveIntended() {
        sessionStorage.setItem(INTENDED_KEY, window.location.href);
    }

    function popIntended(fallback = 'index.html') {
        const url = sessionStorage.getItem(INTENDED_KEY) || fallback;
        sessionStorage.removeItem(INTENDED_KEY);
        return url;
    }

    // ── Guards (can also be called manually) ─────────────────────────────────

    /** Redirect to login if not logged in. Returns user or null. */
    function requireAuth() {
        if (!isLoggedIn()) {
            saveIntended();
            window.location.replace('login.html');
            return null;
        }
        return getUser();
    }

    /** Redirect away if already logged in (for login/signup pages). */
    function requireGuest() {
        if (isLoggedIn()) {
            // On récupère la page d'où vient l'utilisateur
            const previousPage = document.referrer;

            // Si on a une page précédente et que ce n'est pas la page de login elle-même
            if (previousPage && !previousPage.includes('login.html') && !previousPage.includes('signup.html')) {
                window.location.replace(previousPage);
            } else {
                // Si pas d'historique (accès direct), on va au dashboard par sécurité
                window.location.replace('store.html');
            }
        }
    }

    /** Require login AND one of the given roles. Redirect to 403 if role fails. */
    function requireRole(...roles) {
        const user = requireAuth();   // handles unauthenticated first
        if (!user) return null;       // already redirecting to login

        if (!roles.includes(user.role)) {
            window.location.replace('403.html');
            return null;
        }
        return user;
    }

    /** Call after a successful login — goes to the intended page or fallback. */
    function redirectAfterLogin(fallback = 'dashboard.html') {
        window.location.replace(popIntended(fallback));
    }

    // ── Auto-run based on ROUTE_MAP ───────────────────────────────────────────

    (function autoGuard() {
        const page = currentPage();
        const rule = ROUTE_MAP[page];

        if (!rule) return;
        if (rule === 'guest')    { requireGuest(); return; }
        if (rule === 'auth')     { requireAuth();  return; }
        if (Array.isArray(rule)) { requireRole(...rule); }
    })();

    // ── Public API ────────────────────────────────────────────────────────────
    return { isLoggedIn, getToken, getUser, requireAuth, requireGuest, requireRole, redirectAfterLogin };

})();
