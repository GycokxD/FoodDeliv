<#import "../layout.ftlh" as layout>
<@layout.layout title="Доставки">
<style>
    .courier-card { background: #fff; border: 1px solid var(--line); border-radius: 18px; padding: 1rem 1.1rem; }
    .courier-card + .courier-card { margin-top: 0.75rem; }
    .courier-card__route { display: grid; grid-template-columns: 1.5rem 1fr; gap: 0.15rem 0.5rem; margin: 0.6rem 0; }
    .courier-card__route i { color: var(--primary-color); }
    .courier-card__meta { display: flex; flex-wrap: wrap; gap: 0.4rem 1rem; color: #7a6e66; font-size: 0.9rem; }
</style>
<div class="container mt-4 mb-5" style="max-width: 860px">
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h2 class="mb-0"><i class="bi bi-bicycle me-2"></i>Доставки</h2>
        <a class="btn btn-outline-secondary btn-sm" href="/courier"><i class="bi bi-arrow-clockwise me-1"></i>Обновить</a>
    </div>

    <h5 class="mt-4 mb-2">Мои заказы</h5>
    <#if activeOrders?has_content>
        <#list activeOrders as o>
            <div class="courier-card">
                <div class="d-flex justify-content-between align-items-center gap-2">
                    <strong>Заказ #${o.id}</strong>
                    <span class="badge bg-${o.statusBadge}">${o.statusLabel}</span>
                </div>
                <div class="courier-card__route">
                    <i class="bi bi-shop"></i><span class="break-text">${o.restaurantName}<#if o.restaurantAddress?has_content>, ${o.restaurantAddress}</#if></span>
                    <i class="bi bi-house-door"></i><span class="break-text">${o.address!"—"}</span>
                </div>
                <div class="d-flex flex-wrap justify-content-between align-items-center gap-2">
                    <div class="courier-card__meta">
                        <span>${o.totalPrice?string["0.00"]} сом</span>
                        <span><#if o.paid>Оплачен онлайн<#else>${o.paymentLabel}: получить с клиента</#if></span>
                    </div>
                    <a class="btn btn-primary btn-sm px-3" href="/courier/orders/${o.id}"><i class="bi bi-map me-1"></i>Открыть</a>
                </div>
            </div>
        </#list>
    <#else>
        <p class="text-muted">Сейчас вы ничего не везёте. Возьмите заказ из списка ниже.</p>
    </#if>

    <h5 class="mt-4 mb-2">Свободные заказы</h5>
    <#if availableOrders?has_content>
        <#list availableOrders as o>
            <div class="courier-card">
                <div class="d-flex justify-content-between align-items-center gap-2">
                    <strong>Заказ #${o.id}</strong>
                    <span class="badge bg-${o.statusBadge}">${o.statusLabel}</span>
                </div>
                <div class="courier-card__route">
                    <i class="bi bi-shop"></i><span class="break-text">${o.restaurantName}<#if o.restaurantAddress?has_content>, ${o.restaurantAddress}</#if></span>
                    <i class="bi bi-house-door"></i><span class="break-text">${o.address!"—"}</span>
                </div>
                <div class="d-flex flex-wrap justify-content-between align-items-center gap-2">
                    <div class="courier-card__meta">
                        <span>${o.createdAt!""}</span>
                        <#if o.distanceKm??><span>${o.distanceKm} км по прямой</span></#if>
                        <span>${o.totalPrice?string["0.00"]} сом</span>
                    </div>
                    <form action="/courier/orders/${o.id}/take" method="post" class="mb-0">
                        <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                        <button class="btn btn-primary btn-sm px-3" type="submit"><i class="bi bi-hand-thumbs-up me-1"></i>Взять</button>
                    </form>
                </div>
            </div>
        </#list>
    <#else>
        <p class="text-muted">Свободных заказов нет. Страница обновится сама.</p>
    </#if>
</div>
<script>
    setTimeout(function () { location.reload(); }, 20000);
</script>
</@layout.layout>
