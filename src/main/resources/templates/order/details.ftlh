<#import "../layout.ftlh" as layout>
<@layout.layout title="Заказ #${order.id}">
<style>
    .track-steps { display: flex; gap: 0.5rem; margin-bottom: 1rem; }
    .track-step { flex: 1; text-align: center; color: #95a5a6; font-size: 0.85rem; font-weight: 600; }
    .track-step__dot {
        display: block; width: 14px; height: 14px; margin: 0 auto 0.35rem; border-radius: 50%;
        background: #e9ecef; box-shadow: 0 0 0 4px #f8f9fa;
    }
    .track-step.is-done { color: #2f3542; }
    .track-step.is-done .track-step__dot { background: #2ecc71; }
    .track-step.is-current { color: #ff6b6b; }
    .track-step.is-current .track-step__dot { background: #ff6b6b; box-shadow: 0 0 0 4px rgba(255, 107, 107, 0.2); }
    .track-map {
        position: relative; height: 280px; border-radius: 16px; overflow: hidden;
        background: linear-gradient(160deg, #e7f6ef 0%, #d7ecff 55%, #fff4e8 100%);
        border: 1px solid rgba(47, 53, 66, 0.08);
    }
    .track-map__routes { position: absolute; inset: 0; width: 100%; height: 100%; }
    .track-map__routes line { stroke: #ff6b6b; stroke-width: 1.2; stroke-dasharray: 2 1.4; }
    .track-pin {
        position: absolute; transform: translate(-50%, -100%); text-align: center; width: 90px;
    }
    .track-pin i {
        display: inline-flex; width: 36px; height: 36px; align-items: center; justify-content: center;
        border-radius: 50% 50% 50% 4px; color: #fff; font-size: 1rem;
        box-shadow: 0 6px 14px rgba(47, 53, 66, 0.2);
    }
    .track-pin span { display: block; margin-top: 0.2rem; font-size: 0.75rem; font-weight: 700; color: #2f3542; }
    .track-pin--restaurant i { background: #2f3542; }
    .track-pin--home i { background: #ffa502; }
    .track-pin--courier { width: 110px; transition: left 1.2s linear, top 1.2s linear; z-index: 2; }
    .track-pin--courier i { background: #ff6b6b; border-radius: 50%; }
    .track-progress { height: 8px; background: #f1f2f6; border-radius: 99px; overflow: hidden; }
    .track-progress > div { height: 100%; width: 0; background: linear-gradient(90deg, #ff6b6b, #ffa502); transition: width 1s linear; }
    @media (max-width: 576px) {
        .track-step { font-size: 0.72rem; }
        .track-map { height: 230px; }
    }
</style>
<div class="container mt-5" id="orderTrack"
     data-order-id="${order.id}"
     data-live="${tracking.live?c}">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-4">
        <h2 class="mb-0"><i class="bi bi-receipt me-2"></i>Заказ #${order.id}</h2>
        <div class="d-flex flex-wrap gap-2">
            <form action="/orders/${order.id}/repeat" method="post" class="mb-0">
                <#if _csrf??>
                    <input type="hidden" name="${(_csrf.parameterName)!'csrf-param-name'}"
                           value="${(_csrf.token)!'csrf-token'}"/>
                </#if>
                <button class="btn btn-outline-primary" type="submit">
                    <i class="bi bi-arrow-repeat"></i> Повторить заказ
                </button>
            </form>
            <form id="cancelOrderForm" action="/orders/${order.id}/cancel" method="post" class="mb-0"
                  style="<#if tracking.canCancel>display:inline<#else>display:none</#if>"
                  onsubmit="return confirm('Отменить заказ?');">
                <#if _csrf??>
                    <input type="hidden" name="${(_csrf.parameterName)!'csrf-param-name'}"
                           value="${(_csrf.token)!'csrf-token'}"/>
                </#if>
                <button class="btn btn-outline-danger" type="submit">
                    <i class="bi bi-x-circle"></i> Отменить
                </button>
            </form>
            <a href="/profile" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left"></i> Назад в профиль
            </a>
        </div>
    </div>

    <div class="card shadow-sm mb-4">
        <div class="card-body">
            <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
                <div>
                    <span id="orderStatusBadge" class="badge bg-${tracking.badge}">${tracking.statusLabel}</span>
                    <p id="trackMessage" class="mb-0 mt-2">${tracking.message}</p>
                </div>
                <div class="text-md-end">
                    <div class="text-muted small">Курьер</div>
                    <strong id="courierName">${tracking.courierName}</strong>
                    <div id="trackEta" class="text-muted small">${tracking.eta}</div>
                </div>
            </div>
            <div class="track-steps" id="trackSteps">
                <#list tracking.steps as step>
                    <div class="track-step is-${step.state}" data-step="${step?index}">
                        <span class="track-step__dot"></span>
                        <span>${step.label}</span>
                    </div>
                </#list>
            </div>
            <div class="track-progress mb-3"><div id="trackProgress" style="width: ${tracking.progress}%"></div></div>
            <#if tracking.showMap>
                <div class="track-map" id="trackMap">
                    <svg class="track-map__routes" viewBox="0 0 100 100" preserveAspectRatio="none">
                        <line id="routeLine"
                              x1="${tracking.restaurantX}" y1="${tracking.restaurantY}"
                              x2="${tracking.destinationX}" y2="${tracking.destinationY}"></line>
                    </svg>
                    <div class="track-pin track-pin--restaurant" style="left: ${tracking.restaurantX}%; top: ${tracking.restaurantY}%;">
                        <i class="bi bi-shop"></i>
                        <span>${tracking.restaurantName}</span>
                    </div>
                    <div class="track-pin track-pin--home" style="left: ${tracking.destinationX}%; top: ${tracking.destinationY}%;">
                        <i class="bi bi-house-door"></i>
                        <span>Вы</span>
                    </div>
                    <div id="courierPin" class="track-pin track-pin--courier" style="left: ${tracking.courierX}%; top: ${tracking.courierY}%;">
                        <i class="bi bi-bicycle"></i>
                        <span id="courierPinLabel"><#if tracking.status == "ON_THE_WAY">Едет<#elseif tracking.status == "DELIVERED">На месте<#else>Ждёт заказ</#if></span>
                    </div>
                </div>
                <p class="text-muted small mt-2 mb-0">Маршрут учебный: ресторан и адрес поставлены на схему города, это не настоящая геолокация.</p>
            </#if>
        </div>
    </div>

    <div class="card shadow-sm mb-4">
        <div class="card-body">
            <dl class="row mb-0">
                <dt class="col-sm-3">Дата:</dt>
                <dd class="col-sm-9">${createdAtFormatted!"—"}</dd>
                <dt class="col-sm-3">Адрес:</dt>
                <dd class="col-sm-9 break-text">${order.address!"—"}</dd>
                <dt class="col-sm-3">Телефон:</dt>
                <dd class="col-sm-9">${order.phone!"—"}</dd>
                <dt class="col-sm-3">Оплата:</dt>
                <dd class="col-sm-9">
                    <#if order.paymentMethod??>${order.paymentMethod.label}<#else>Не указана</#if>
                    <#if order.cardBrand?? || order.cardLast4??>
                        <span class="text-muted"><#if order.cardBrand??>${order.cardBrand} </#if><#if order.cardLast4??>•••• ${order.cardLast4}</#if></span>
                    </#if>
                    <span id="paymentState" class="badge ms-1 bg-${tracking.paymentBadge!'secondary'}">${tracking.paymentState}</span>
                    <#if order.paymentRef??>
                        <div class="text-muted small">Чек ${order.paymentRef}</div>
                    </#if>
                </dd>
                <#if order.note?? && order.note?has_content>
                    <dt class="col-sm-3">Комментарий:</dt>
                    <dd class="col-sm-9 break-text">${order.note}</dd>
                </#if>
                <dt class="col-sm-3">Сумма:</dt>
                <dd class="col-sm-9"><span class="text-success fw-bold">${order.totalPrice?string["0.00"]} сом</span></dd>
            </dl>
        </div>
    </div>

    <div class="card shadow-sm mb-4">
        <div class="card-body">
            <h4 class="mb-3">Состав заказа</h4>
            <#if order.items?has_content>
                <div class="table-responsive">
                <table class="table align-middle mb-0 wrap-table" style="min-width: 560px">
                    <colgroup>
                        <col style="width: 60%">
                        <col style="width: 18%">
                        <col style="width: 22%">
                    </colgroup>
                    <thead>
                        <tr>
                            <th>Блюдо</th>
                            <th class="text-center">Количество</th>
                            <th class="text-center">Сумма</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list order.items as item>
                            <tr>
                                <td>
                                    <div class="d-flex align-items-center">
                                        <img src="${item.dish.image?starts_with('/')?then(item.dish.image, '/static/' + item.dish.image)}"
                                             alt="${item.dish.name}"
                                             class="rounded me-3"
                                             style="width: 60px; height: 60px; object-fit: cover;"
                                             onerror="this.src='/static/images/dish-placeholder.svg'">
                                        <div>
                                            <strong class="break-text">${item.dish.name}</strong>
                                            <div class="text-muted small break-text">${item.dish.description!''}</div>
                                        </div>
                                    </div>
                                </td>
                                <td class="text-center col-nowrap">${item.quantity}</td>
                                <td class="text-center col-nowrap">${item.price?string["0.00"]} сом</td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
                </div>
            <#else>
                <div class="alert alert-info mb-0">Позиции заказа не найдены</div>
            </#if>
        </div>
    </div>
</div>
<script>
    (function () {
        var root = document.getElementById("orderTrack");
        if (!root || root.dataset.live !== "true") {
            return;
        }
        var orderId = root.dataset.orderId;
        var badge = document.getElementById("orderStatusBadge");
        var message = document.getElementById("trackMessage");
        var eta = document.getElementById("trackEta");
        var progress = document.getElementById("trackProgress");
        var pin = document.getElementById("courierPin");
        var pinLabel = document.getElementById("courierPinLabel");
        var payment = document.getElementById("paymentState");
        var cancelForm = document.getElementById("cancelOrderForm");
        var steps = document.querySelectorAll("#trackSteps .track-step");

        function apply(data) {
            if (badge) {
                badge.className = "badge bg-" + data.badge;
                badge.textContent = data.statusLabel;
            }
            if (message) {
                message.textContent = data.message;
            }
            if (eta) {
                eta.textContent = data.eta;
            }
            if (progress) {
                progress.style.width = data.progress + "%";
            }
            if (pin) {
                pin.style.left = data.courierX + "%";
                pin.style.top = data.courierY + "%";
            }
            if (pinLabel) {
                pinLabel.textContent = data.status === "ON_THE_WAY" ? "Едет" : (data.status === "DELIVERED" ? "На месте" : "Ждёт заказ");
            }
            if (payment) {
                var payBadge = data.paymentBadge || (data.paid ? "success" : "secondary");
                payment.textContent = data.paymentState;
                payment.className = "badge ms-1 bg-" + payBadge + (payBadge === "warning" ? " text-dark" : "");
            }
            if (cancelForm) {
                cancelForm.style.display = data.canCancel ? "inline" : "none";
            }
            if (data.steps) {
                data.steps.forEach(function (step, index) {
                    if (steps[index]) {
                        steps[index].className = "track-step is-" + step.state;
                    }
                });
            }
            return data.finished || data.status === "CANCELLED";
        }

        function tick() {
            fetch("/orders/" + orderId + "/tracking")
                .then(function (response) { return response.json(); })
                .then(function (data) {
                    if (!apply(data)) {
                        setTimeout(tick, 3000);
                    }
                })
                .catch(function () {
                    setTimeout(tick, 5000);
                });
        }
        setTimeout(tick, 3000);
    })();
</script>
</@layout.layout>
