<#import "layout.ftlh" as layout>
<@layout.layout title="Доставка еды">
<div class="home-hero mb-4">
    <h1>Еда из любимых ресторанов</h1>
    <p>Выберите заведение, соберите корзину и следите за курьером.</p>
    <div class="home-search">
        <i class="bi bi-search"></i>
        <input id="homeSearch" type="search" placeholder="Ресторан или блюдо" autocomplete="off" aria-label="Поиск на главной">
        <div id="homeSearchResults" class="live-search-results"></div>
    </div>
</div>

<div class="row g-3 mb-5">
    <div class="col-md-4">
        <div class="card stat-card h-100"><div class="card-body">
            <h5><i class="bi bi-shop me-2"></i>1. Ресторан</h5>
            <p class="mb-0 text-muted">Откройте меню и добавьте блюда одного заведения.</p>
        </div></div>
    </div>
    <div class="col-md-4">
        <div class="card stat-card h-100"><div class="card-body">
            <h5><i class="bi bi-bag-check me-2"></i>2. Корзина</h5>
            <p class="mb-0 text-muted">Укажите адрес и выберите, как оплатить заказ.</p>
        </div></div>
    </div>
    <div class="col-md-4">
        <div class="card stat-card h-100"><div class="card-body">
            <h5><i class="bi bi-bicycle me-2"></i>3. Доставка</h5>
            <p class="mb-0 text-muted">Смотрите, как заказ готовят и везут к вам.</p>
        </div></div>
    </div>
</div>

<div class="d-flex justify-content-between align-items-center mb-3">
    <h2 class="mb-0">Заведения</h2>
    <a href="/restaurants" class="btn btn-outline-primary">Все рестораны</a>
</div>
<div class="row">
    <#list featured as restaurant>
        <div class="col-md-4 mb-4">
            <div class="card hover-lift h-100 shadow-sm">
                <img src="${restaurant.imageUrl!'/static/images/restaurant-placeholder.svg'}"
                     onerror="this.src='/static/images/restaurant-placeholder.svg'"
                     class="card-img-top" alt="${restaurant.name}" style="height: 180px; object-fit: cover;">
                <div class="card-body">
                    <h5 class="card-title">${restaurant.name}</h5>
                    <p class="card-text text-muted break-text"><i class="bi bi-geo-alt"></i> ${restaurant.address}</p>
                    <a href="/restaurants/${restaurant.id}" class="btn btn-primary">Меню</a>
                </div>
            </div>
        </div>
    </#list>
</div>
<style>
    .home-hero {
        background: linear-gradient(135deg, #1c1714 0%, #5c2a24 62%, #e23d3d 100%);
        color: #fff;
        border-radius: 20px;
        padding: 2.2rem 1.6rem;
    }
    .home-hero h1 { font-weight: 800; margin-bottom: 0.4rem; }
    .home-search { position: relative; max-width: 520px; margin-top: 1.2rem; }
    .home-search i { position: absolute; left: 0.9rem; top: 50%; transform: translateY(-50%); color: #ff6b6b; }
    .home-search input {
        width: 100%; border: none; border-radius: 999px;
        padding: 0.8rem 1rem 0.8rem 2.5rem;
    }
    .home-search .live-search-results { color: #2f3542; }
</style>
<script>
    (function () {
        var input = document.getElementById("homeSearch");
        var box = document.getElementById("homeSearchResults");
        if (!input || !box) return;
        var timer = null;
        function esc(value) {
            return String(value == null ? "" : value).replace(/[&<>"']/g, function (ch) {
                return ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[ch];
            });
        }
        input.addEventListener("input", function () {
            var q = input.value.trim();
            clearTimeout(timer);
            if (q.length < 2) { box.classList.remove("is-open"); box.innerHTML = ""; return; }
            timer = setTimeout(function () {
                fetch("/restaurants/search?q=" + encodeURIComponent(q))
                    .then(function (r) { return r.json(); })
                    .then(function (hits) {
                        if (!hits.length) {
                            box.innerHTML = '<div class="live-search-empty">Ничего не найдено</div>';
                        } else {
                            box.innerHTML = hits.map(function (hit) {
                                var kind = hit.type === "dish" ? "Блюдо" : "Ресторан";
                                return '<a href="' + esc(hit.url) + '"><img src="' + esc(hit.image) + '" alt="">' +
                                    '<span><span class="hit-type">' + kind + '</span>' +
                                    '<span class="hit-title d-block">' + esc(hit.title) + '</span>' +
                                    '<span class="hit-sub d-block">' + esc(hit.subtitle) + '</span></span></a>';
                            }).join("");
                        }
                        box.classList.add("is-open");
                    });
            }, 220);
        });
    })();
</script>
</@layout.layout>
