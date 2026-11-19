<#macro assets>
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
      integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin="">
<style>
    .geo-map {
        height: 320px; border-radius: 16px; overflow: hidden;
        border: 1px solid rgba(47, 53, 66, 0.08); z-index: 0;
    }
    .geo-pin { background: none; border: none; }
    .geo-pin i {
        display: inline-flex; width: 36px; height: 36px; align-items: center; justify-content: center;
        border-radius: 50% 50% 50% 4px; transform: rotate(-45deg); color: #fff; font-size: 1rem;
        box-shadow: 0 6px 14px rgba(47, 53, 66, 0.25);
    }
    .geo-pin i::before { transform: rotate(45deg); }
    .geo-pin--restaurant i { background: #2f3542; }
    .geo-pin--home i { background: #ffa502; }
    .geo-pin--courier i { background: #e23d3d; border-radius: 50%; transform: none; border: 3px solid #fff; }
    .geo-pin--courier i::before { transform: none; }
    .geo-pin--courier.is-stale i { background: #95a5a6; }
    .geo-search { position: relative; }
    .geo-search__results {
        display: none; position: absolute; top: calc(100% + 4px); left: 0; right: 0; z-index: 1000;
        background: #fff; border-radius: 12px; box-shadow: 0 16px 40px rgba(47, 53, 66, 0.18);
        padding: 0.3rem; max-height: 280px; overflow: auto;
    }
    .geo-search__results.is-open { display: block; }
    .geo-search__results button {
        display: block; width: 100%; text-align: left; border: none; background: none;
        padding: 0.5rem 0.65rem; border-radius: 8px;
    }
    .geo-search__results button:hover, .geo-search__results button:focus { background: #fff5f5; outline: none; }
    .geo-search__empty { padding: 0.5rem 0.65rem; color: #7f8c8d; }
</style>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
        integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>
<script src="/static/js/maps.js"></script>
</#macro>
