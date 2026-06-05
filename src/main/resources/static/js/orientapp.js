/* OrientApp — przełącznik motywu ciemny ⇄ jasny z zapamiętaniem wyboru.
   Skrypt wczytywany w <head> (defer-free), aby ustawić motyw przed renderem
   i uniknąć mignięcia jasnym tłem. */
(function () {
    var KEY = 'orientapp-theme';
    var root = document.documentElement;

    function apply(theme) {
        root.setAttribute('data-theme', theme);
        root.setAttribute('data-bs-theme', theme); // synchronizacja komponentów Bootstrapa
    }

    // Wczytaj zapisany wybór (domyślnie ciemny)
    var saved = null;
    try { saved = localStorage.getItem(KEY); } catch (e) { /* tryb prywatny */ }
    apply(saved === 'light' ? 'light' : 'dark');

    document.addEventListener('DOMContentLoaded', function () {
        // Przełącznik motywu
        document.querySelectorAll('[data-theme-toggle]').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var next = root.getAttribute('data-theme') === 'light' ? 'dark' : 'light';
                apply(next);
                try { localStorage.setItem(KEY, next); } catch (e) { /* ignore */ }
            });
        });

        // Modale -> bezpośrednie dzieci <body>. Bez tego modal osadzony w kontenerze
        // z transformem / position / overflow trafia do innego kontekstu nakładania
        // niż backdrop Bootstrapa (dodawany do <body>) — backdrop zasłania okno i
        // strona wygląda na zablokowaną (nie da się kliknąć ani zamknąć).
        document.querySelectorAll('.modal').forEach(function (m) {
            if (m.parentElement !== document.body) document.body.appendChild(m);
        });

        // Powiadomienia (flash) — automatyczne zamknięcie po 5 s z animacją zanikania.
        document.querySelectorAll('.alert-dismissible').forEach(function (alert) {
            setTimeout(function () {
                if (!alert.isConnected) return;
                if (window.bootstrap && bootstrap.Alert) {
                    bootstrap.Alert.getOrCreateInstance(alert).close();
                } else {
                    alert.classList.remove('show'); // fallback: ręczne wygaszenie
                    setTimeout(function () { alert.remove(); }, 200);
                }
            }, 5000);
        });
    });
})();
