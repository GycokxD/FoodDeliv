<#import "../layout.ftlh" as layout>
<#import "nav.ftlh" as admin>
<@layout.layout title="Заказы">
<div class="container mt-4 mb-5">
    <h2 class="mb-1">Заказы</h2>
    <p class="text-muted">Смена статуса останавливает автоматическую имитацию доставки</p>
    <@admin.nav "orders"/>
    <div class="admin-panel">
        <div class="admin-panel__bar">
            <input class="form-control" data-table-filter type="search" placeholder="Номер, клиент, адрес, статус" aria-label="Поиск по заказам">
            <span class="admin-count" data-table-count></span>
        </div>
        <div class="table-responsive">
            <table class="admin-table">
                <thead>
                <tr>
                    <th>№</th>
                    <th>Дата</th>
                    <th>Клиент</th>
                    <th>Адрес</th>
                    <th>Оплата</th>
                    <th class="text-end">Сумма</th>
                    <th class="text-end">Статус</th>
                </tr>
                </thead>
                <tbody>
                <#list orders as order>
                    <tr>
                        <td class="num"><a href="/orders/${order.id}">#${order.id}</a></td>
                        <td class="text-nowrap"><@admin.date order.createdAt/></td>
                        <td>
                            <div class="stack">
                                <span>${(order.user.name)!"—"}</span>
                                <span class="sub">${(order.user.email)!""}</span>
                            </div>
                        </td>
                        <td class="clip">${order.address!"—"}</td>
                        <td>
                            <div class="stack">
                                <span><#if order.paymentMethod??>${order.paymentMethod.label}<#else>—</#if></span>
                                <span class="sub"><#if order.paymentStatus??>${order.paymentStatus.label}<#elseif order.paid!false>Оплачено</#if></span>
                            </div>
                        </td>
                        <td class="text-end num">${order.totalPrice?string["0.00"]}</td>
                        <td>
                            <form action="/admin/orders/${order.id}/status" method="post" class="row-actions">
                                <#if _csrf??>
                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                </#if>
                                <select class="form-select form-select-sm" name="status" aria-label="Статус заказа ${order.id}">
                                    <#list statuses as status>
                                        <option value="${status}" <#if order.status?? && order.status?string == status?string>selected</#if>>${status.label}</option>
                                    </#list>
                                </select>
                                <button class="btn btn-sm btn-primary" type="submit">OK</button>
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
