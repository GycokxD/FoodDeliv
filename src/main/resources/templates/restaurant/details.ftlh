<#import "../layout.ftlh" as layout>
<@layout.layout title="${restaurant.name} - Меню">
<div class="menu-page">
    <#if cartConflict!false>
        <div class="alert alert-warning d-flex flex-wrap justify-content-between align-items-center gap-3">
            <div>
                <i class="bi bi-exclamation-triangle me-2"></i>
                В корзине уже есть блюда из другого ресторана. Чтобы добавить «${conflictDishName!""}», очистите корзину.
            </div>
            <form action="/restaurants/add-dish-to-cart" method="post" class="mb-0">
                <#if _csrf??>
                    <input type="hidden" name="${(_csrf.parameterName)!'csrf-param-name'}"
                           value="${(_csrf.token)!'csrf-token'}"/>
                </#if>
                <input type="hidden" name="dishId" value="${conflictDishId!''}">
                <input type="hidden" name="replace" value="true">
                <button class="btn btn-warning" type="submit">Очистить и добавить</button>
            </form>
        </div>
    </#if>
    <div class="restaurant-hero mb-4">
        <div class="restaurant-hero__media">
            <img src="${restaurant.imageUrl!'/static/images/restaurant-placeholder.svg'}"
                 alt="${restaurant.name}"
                 onerror="this.src='/static/images/restaurant-placeholder.svg'">
            <div class="restaurant-hero__overlay"></div>
        </div>
        <div class="restaurant-hero__content">
            <a href="/restaurants" class="restaurant-back">
                <i class="bi bi-arrow-left"></i> Все рестораны
            </a>
            <h1 class="restaurant-hero__title">${restaurant.name}</h1>
            <p class="restaurant-hero__meta break-text">
                <i class="bi bi-geo-alt-fill"></i> ${restaurant.address}
            </p>
            <p class="restaurant-hero__count">
                <i class="bi bi-egg-fried"></i>
                ${dishes.totalElements} блюд в меню
            </p>
        </div>
    </div>

    <div class="menu-section-head mb-3">
        <h2>Меню</h2>
        <p>Выберите блюда и добавьте их в корзину</p>
    </div>
    <div class="menu-toolbar">
        <div class="menu-search">
            <i class="bi bi-search"></i>
            <input id="menuSearch" type="search" placeholder="Найти блюдо по названию" autocomplete="off" aria-label="Поиск по меню">
        </div>
        <form method="get" class="menu-filters" id="menuFilters">
            <div class="filter-pills" role="radiogroup" aria-label="Сортировка">
                <label><input type="radio" name="sort" value="name" <#if (sort!'name') == 'name'>checked</#if>><span>По названию</span></label>
                <label><input type="radio" name="sort" value="price_asc" <#if (sort!'') == 'price_asc'>checked</#if>><span>Сначала дешевле</span></label>
                <label><input type="radio" name="sort" value="price_desc" <#if (sort!'') == 'price_desc'>checked</#if>><span>Сначала дороже</span></label>
            </div>
            <div class="price-box">
                <input type="number" min="0" step="1" name="minPrice" placeholder="от" value="${minPrice!''}" aria-label="Цена от">
                <span class="text-muted">—</span>
                <input type="number" min="0" step="1" name="maxPrice" placeholder="до" value="${maxPrice!''}" aria-label="Цена до">
                <span class="text-muted">сом</span>
            </div>
            <button class="btn btn-primary" type="submit">Показать</button>
            <#if minPrice?? || maxPrice?? || (sort!'name') != 'name'>
                <a class="btn btn-outline-secondary" href="/restaurants/${restaurant.id}">Сбросить</a>
            </#if>
        </form>
    </div>

    <div class="dish-grid" id="dishGrid"
         data-restaurant-id="${restaurant.id}"
         data-min-price="${minPrice!''}"
         data-max-price="${maxPrice!''}"
         <#if _csrf??>data-csrf-name="${_csrf.parameterName}" data-csrf-value="${_csrf.token}"</#if>>
        <#list dishes.content as dish>
        <article class="dish-card" id="dish-${dish.id}">
            <div class="dish-card__image">
                <img src="${dish.image!'/static/images/dish-placeholder.svg'}"
                     alt="${dish.name}"
                     loading="lazy"
                     onerror="this.src='/static/images/dish-placeholder.svg'">
                <span class="dish-card__price-badge">
                    ${dish.price?string["0"]} <small>сом</small>
                </span>
            </div>
            <div class="dish-card__body">
                <h3 class="dish-card__name">${dish.name}</h3>
                <p class="dish-card__desc">
                    <#if dish.description?? && dish.description?has_content>
                        ${dish.description}
                    <#else>
                        Вкусное блюдо из меню ресторана
                    </#if>
                </p>
                <div class="dish-card__footer">
                    <div class="dish-card__price">
                        <span class="dish-card__price-value">${dish.price?string["0.00"]}</span>
                        <span class="dish-card__price-currency">сом</span>
                    </div>
                    <form action="/restaurants/add-dish-to-cart" method="post" class="dish-card__form">
                        <#if _csrf??>
                            <input type="hidden" name="${(_csrf.parameterName)!'csrf-param-name'}"
                                   value="${(_csrf.token)!'csrf-token'}"/>
                        </#if>
                        <input type="hidden" value="${dish.id}" name="dishId">
                        <input type="hidden" value="${restaurant.id}" name="restaurantId">
                        <button class="dish-card__btn" type="submit" aria-label="Добавить ${dish.name} в корзину">
                            <i class="bi bi-plus-lg"></i>
                            <span>В корзину</span>
                        </button>
                    </form>
                </div>
            </div>
        </article>
        </#list>
    </div>

    <#if !dishes.content?has_content>
        <div class="alert alert-info mt-3">
            <i class="bi bi-info-circle me-2"></i><#if minPrice?? || maxPrice??>Нет блюд в этом диапазоне цен<#else>В этом ресторане пока нет блюд</#if>
        </div>
    </#if>

    <#if (dishes.totalPages > 1)>
    <nav id="dishPagination" aria-label="Страницы меню" class="mt-4">
        <ul class="pagination justify-content-center">
            <li class="page-item ${dishes.first?then('disabled','')}">
                <a class="page-link" href="?page=${dishes.number-1}&${menuQuery!''}" aria-label="Предыдущая">
                    <span aria-hidden="true">&laquo;</span>
                </a>
            </li>
            <#list 1..dishes.totalPages as page>
                <li class="page-item ${(page-1 == dishes.number)?then('active','')}">
                    <a class="page-link" href="?page=${page-1}&${menuQuery!''}">${page}</a>
                </li>
            </#list>
            <li class="page-item ${dishes.last?then('disabled','')}">
                <a class="page-link" href="?page=${dishes.number+1}&${menuQuery!''}" aria-label="Следующая">
                    <span aria-hidden="true">&raquo;</span>
                </a>
            </li>
        </ul>
    </nav>
    </#if>
</div>
<script>
    (function () {
        var input = document.getElementById("menuSearch");
        var grid = document.getElementById("dishGrid");
        if (!input || !grid) {
            return;
        }
        var original = grid.innerHTML;
        var pagination = document.getElementById("dishPagination");
        var timer = null;
        var requestId = 0;
        var filters = document.getElementById("menuFilters");
        if (filters) {
            filters.querySelectorAll('input[name="sort"]').forEach(function (input) {
                input.addEventListener("change", function () { filters.requestSubmit(); });
            });
        }
        var restaurantId = grid.getAttribute("data-restaurant-id");
        var minPrice = grid.getAttribute("data-min-price") || "";
        var maxPrice = grid.getAttribute("data-max-price") || "";
        var csrfName = grid.getAttribute("data-csrf-name") || "";
        var csrfValue = grid.getAttribute("data-csrf-value") || "";

        function esc(value) {
            return String(value == null ? "" : value).replace(/[&<>"']/g, function (ch) {
                return ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[ch];
            });
        }

        function card(dish) {
            var csrf = "";
            if (csrfName) {
                csrf = '<input type="hidden" name="' + esc(csrfName) + '" value="' + esc(csrfValue) + '">';
            }
            var description = dish.description ? esc(dish.description) : "Вкусное блюдо из меню ресторана";
            var price = Number(dish.price || 0);
            return '<article class="dish-card" id="dish-' + dish.id + '">' +
                '<div class="dish-card__image">' +
                '<img src="' + esc(dish.image || "/static/images/dish-placeholder.svg") + '" alt="' + esc(dish.name) + '" onerror="this.src=\'/static/images/dish-placeholder.svg\'">' +
                '<span class="dish-card__price-badge">' + Math.round(price) + ' <small>сом</small></span></div>' +
                '<div class="dish-card__body"><h3 class="dish-card__name">' + esc(dish.name) + '</h3>' +
                '<p class="dish-card__desc">' + description + '</p>' +
                '<div class="dish-card__footer"><div class="dish-card__price">' +
                '<span class="dish-card__price-value">' + price.toFixed(2) + '</span>' +
                '<span class="dish-card__price-currency">сом</span></div>' +
                '<form action="/restaurants/add-dish-to-cart" method="post" class="dish-card__form">' + csrf +
                '<input type="hidden" value="' + dish.id + '" name="dishId">' +
                '<input type="hidden" value="' + restaurantId + '" name="restaurantId">' +
                '<button class="dish-card__btn" type="submit"><i class="bi bi-plus-lg"></i><span>В корзину</span></button>' +
                '</form></div></div></article>';
        }

        input.addEventListener("input", function () {
            var q = input.value.trim();
            clearTimeout(timer);
            if (!q) {
                grid.innerHTML = original;
                if (pagination) {
                    pagination.style.display = "";
                }
                return;
            }
            timer = setTimeout(function () {
                var current = ++requestId;
                var searchUrl = "/restaurants/" + restaurantId + "/dishes/search?q=" + encodeURIComponent(q);
                if (minPrice) searchUrl += "&minPrice=" + encodeURIComponent(minPrice);
                if (maxPrice) searchUrl += "&maxPrice=" + encodeURIComponent(maxPrice);
                fetch(searchUrl)
                    .then(function (response) { return response.json(); })
                    .then(function (dishes) {
                        if (current !== requestId) {
                            return;
                        }
                        if (pagination) {
                            pagination.style.display = "none";
                        }
                        if (!dishes.length) {
                            grid.innerHTML = '<div class="alert alert-info mb-0">По запросу ничего не найдено</div>';
                            return;
                        }
                        grid.innerHTML = dishes.map(card).join("");
                    });
            }, 200);
        });
    })();
</script>
</@layout.layout>
