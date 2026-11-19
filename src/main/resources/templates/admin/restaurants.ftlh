<#import "../layout.ftlh" as layout>
<#import "nav.ftlh" as admin>
<@layout.layout title="Рестораны">
<div class="container mt-4 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-1">
        <h2 class="mb-0">Рестораны</h2>
        <a class="btn btn-primary" href="/admin/restaurants/new">Добавить</a>
    </div>
    <p class="text-muted">Карточка ведёт в меню заведения</p>
    <@admin.nav "restaurants"/>
    <div class="admin-panel">
        <div class="admin-panel__bar">
            <input class="form-control" data-table-filter type="search" placeholder="Название или адрес" aria-label="Поиск по ресторанам">
            <span class="admin-count" data-table-count></span>
        </div>
        <div class="table-responsive">
            <table class="admin-table">
                <thead>
                <tr>
                    <th>Заведение</th>
                    <th>Адрес</th>
                    <th class="text-end">Действия</th>
                </tr>
                </thead>
                <tbody>
                <#list restaurants as restaurant>
                    <tr>
                        <td>
                            <div class="d-flex align-items-center gap-3">
                                <img src="${admin.media(restaurant.image, '/static/images/restaurant-placeholder.svg')}" alt="" width="64" height="48" style="width: 72px; height: 52px; object-fit: cover; border-radius: 12px;" onerror="this.src='/static/images/restaurant-placeholder.svg'">
                                <a href="/restaurants/${restaurant.id}">${restaurant.name}</a>
                            </div>
                        </td>
                        <td class="clip">${restaurant.address}</td>
                        <td>
                            <div class="row-actions">
                                <a class="btn btn-sm btn-outline-primary" href="/admin/restaurants/${restaurant.id}/edit">Изменить</a>
                                <form action="/admin/restaurants/${restaurant.id}/delete" method="post" class="mb-0" onsubmit="return confirm('Удалить ресторан?');">
                                    <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                                    <button class="btn btn-sm btn-outline-danger" type="submit">Удалить</button>
                                </form>
                            </div>
                        </td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </div>
</div>
</@layout.layout>
