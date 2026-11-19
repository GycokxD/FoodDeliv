<#import "../layout.ftlh" as layout>
<#import "nav.ftlh" as admin>
<@layout.layout title="Блюда">
<div class="container mt-4 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-1">
        <h2 class="mb-0">Блюда</h2>
        <a class="btn btn-primary" href="/admin/dishes/new">Добавить</a>
    </div>
    <p class="text-muted">Цена указана в сомах</p>
    <@admin.nav "dishes"/>
    <div class="admin-panel">
        <div class="admin-panel__bar">
            <input class="form-control" data-table-filter type="search" placeholder="Блюдо или ресторан" aria-label="Поиск по блюдам">
            <span class="admin-count" data-table-count></span>
        </div>
        <div class="table-responsive">
            <table class="admin-table">
                <thead>
                <tr>
                    <th>Блюдо</th>
                    <th>Ресторан</th>
                    <th class="text-end">Цена</th>
                    <th class="text-end">Действия</th>
                </tr>
                </thead>
                <tbody>
                <#list dishes as dish>
                    <tr>
                        <td>${dish.name}</td>
                        <td>${(dish.restaurant.name)!"—"}</td>
                        <td class="text-end num">${dish.price?string["0.00"]}</td>
                        <td>
                            <div class="row-actions">
                                <a class="btn btn-sm btn-outline-primary" href="/admin/dishes/${dish.id}/edit">Изменить</a>
                                <form action="/admin/dishes/${dish.id}/delete" method="post" class="mb-0" onsubmit="return confirm('Удалить блюдо?');">
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
