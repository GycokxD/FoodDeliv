<#import "../layout.ftlh" as layout>
<@layout.layout title="Рестораны и кафе">
<div class="container mt-5">
    <h2 class="mb-4"><i class="bi bi-shop me-2"></i>Наши заведения</h2>

    <div class="row">
        <#list restaurants.content as restaurant>
        <div class="col-md-4 mb-4">
            <div class="card hover-lift h-100 shadow-sm">
                <img src="${restaurant.imageUrl!'/static/images/restaurant-placeholder.svg'}"
                     onerror="this.src='/static/images/restaurant-placeholder.svg'"
                     class="card-img-top"
                     alt="${restaurant.name}"
                     style="height: 200px; object-fit: cover;">
                <div class="card-body">
                    <h5 class="card-title">${restaurant.name}</h5>
                    <p class="card-text text-muted break-text">
                        <i class="bi bi-geo-alt"></i> ${restaurant.address}
                    </p>
                    <a href="/restaurants/${restaurant.id}" class="btn btn-primary">
                        <i class="bi bi-menu-up me-1"></i> Посмотреть меню
                    </a>
                </div>
            </div>
        </div>
        </#list>
    </div>

    <nav aria-label="Страницы">
        <ul class="pagination justify-content-center">
            <li class="page-item ${restaurants.first?then('disabled','')}">
                <a class="page-link" href="?page=${restaurants.number-1}" aria-label="Предыдущая">
                    <span aria-hidden="true">&laquo;</span>
                </a>
            </li>

            <#if (restaurants.totalPages > 0)>
            <#list 1..restaurants.totalPages as page>
                <li class="page-item ${(page-1 == restaurants.number)?then('active','')}">
                    <a class="page-link" href="?page=${page-1}">${page}</a>
                </li>
            </#list>
            </#if>

            <li class="page-item ${restaurants.last?then('disabled','')}">
                <a class="page-link" href="?page=${restaurants.number+1}" aria-label="Следующая">
                    <span aria-hidden="true">&raquo;</span>
                </a>
            </li>
        </ul>
    </nav>
</div>
</@layout.layout>