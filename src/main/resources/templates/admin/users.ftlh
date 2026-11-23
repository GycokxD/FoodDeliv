<#import "../layout.ftlh" as layout>
<#import "nav.ftlh" as admin>
<@layout.layout title="Пользователи">
<div class="container mt-4 mb-5">
    <h2 class="mb-1">Пользователи</h2>
    <p class="text-muted">Роль, доступ и удаление аккаунта вместе с его заказами</p>
    <@admin.nav "users"/>
    <div class="admin-panel">
        <div class="admin-panel__bar">
            <input class="form-control" data-table-filter type="search" placeholder="Имя, почта или роль" aria-label="Поиск по пользователям">
            <span class="admin-count" data-table-count></span>
        </div>
        <div class="table-responsive">
            <table class="admin-table">
                <thead>
                <tr>
                    <th>Пользователь</th>
                    <th>Роль</th>
                    <th>Доступ</th>
                    <th>Регистрация</th>
                    <th class="text-end">Действия</th>
                </tr>
                </thead>
                <tbody>
                <#list users as account>
                    <#assign active = account.enabled!false>
                    <tr>
                        <td>
                            <div class="stack">
                                <span>${account.name}</span>
                                <span class="sub">${account.email}</span>
                            </div>
                        </td>
                        <td>
                            <form action="/admin/users/${account.id}/role" method="post" class="row-actions justify-content-start">
                                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                                <select class="form-select form-select-sm" name="role" aria-label="Роль ${account.email}">
                                    <#list roles as role>
                                        <option value="${role.role}" <#if account.role?? && account.role.role == role.role>selected</#if>>${admin.roleLabel(role.role)}</option>
                                    </#list>
                                </select>
                                <button class="btn btn-sm btn-outline-primary" type="submit">OK</button>
                            </form>
                        </td>
                        <td>
                            <div class="row-actions justify-content-start">
                                <span class="badge <#if active>bg-success<#else>bg-secondary</#if>"><#if active>Активен<#else>Отключён</#if></span>
                                <form action="/admin/users/${account.id}/enabled" method="post" class="mb-0">
                                    <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                                    <input type="hidden" name="enabled" value="<#if active>false<#else>true</#if>">
                                    <button class="btn btn-sm <#if active>btn-outline-danger<#else>btn-outline-success</#if>" type="submit">
                                        <#if active>Отключить<#else>Включить</#if>
                                    </button>
                                </form>
                            </div>
                        </td>
                        <td><@admin.date account.createdAt/></td>
                        <td>
                            <form action="/admin/users/${account.id}/delete" method="post" class="row-actions" onsubmit="return confirm('Удалить пользователя и все его заказы?');">
                                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                                <button class="btn btn-sm btn-outline-danger" type="submit">Удалить</button>
                            </form>
                        </td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </div>
</div>
</@layout.layout>
