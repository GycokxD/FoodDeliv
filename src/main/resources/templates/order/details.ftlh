<#import "../layout.ftlh" as layout>
<#import "../maps.ftlh" as maps>
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
    .track-progress { height: 8px; background: #f1f2f6; border-radius: 99px; overflow: hidden; }
    .track-progress > div { height: 100%; width: 0; background: linear-gradient(90deg, #ff6b6b, #ffa502); transition: width 1s linear; }
    #trackMap.geo-map { height: 320px; }
    @media (max-width: 576px) {
        .track-step { font-size: 0.72rem; }
        #trackMap.geo-map { height: 250px; }
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
                <div id="trackMap" class="geo-map"></div>
                <p id="mapHint" class="text-muted small mt-2 mb-0">
                    <#if tracking.waitingForCourier>
                        Карта OpenStreetMap. Курьер ещё не взял заказ.
                    <#else>
                        Карта OpenStreetMap: ресторан, вы и курьер по реальному маршруту.
                    </#if>
                </p>
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
                <table class="table align-middle mb-0 wrap-table">
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
<#if tracking.showMap>
    <@maps.assets/>
</#if>
<script>
    (function () {
        var root = document.getElementById("orderTrack");
        if (!root) {
            return;
        }
        var orderId = root.dataset.orderId;
        var badge = document.getElementById("orderStatusBadge");
        var message = document.getElementById("trackMessage");
        var eta = document.getElementById("trackEta");
        var progress = document.getElementById("trackProgress");
        var courierName = document.getElementById("courierName");
        var payment = document.getElementById("paymentState");
        var cancelForm = document.getElementById("cancelOrderForm");
        var mapHint = document.getElementById("mapHint");
        var steps = document.querySelectorAll("#trackSteps .track-step");
        var mapEl = document.getElementById("trackMap");
        var map = null;
        var routeLine = null;
        var restaurantMarker = null;
        var destinationMarker = null;
        var courierMarker = null;
        var finished = root.dataset.live !== "true";

        function point(lat, lng) {
            if (lat == null || lng == null || isNaN(lat) || isNaN(lng)) {
                return null;
            }
            return [lat, lng];
        }

        function fit(bounds) {
            if (!map || !bounds.length) {
                return;
            }
            if (bounds.length === 1) {
                map.setView(bounds[0], 15);
            } else {
                map.fitBounds(bounds, { padding: [36, 36], maxZoom: 16 });
            }
        }

        function ensureMap(data) {
            if (!mapEl || !window.L || !window.EduMap) {
                return;
            }
            if (!map) {
                map = EduMap.create(mapEl, {
                    lat: data.centerLat || 42.8746,
                    lng: data.centerLng || 74.6122
                }, 13);
            }
            var bounds = [];
            if (data.route && data.route.length > 1) {
                if (!routeLine) {
                    routeLine = L.polyline(data.route, { color: "#e23d3d", weight: 5, opacity: 0.75 }).addTo(map);
                } else {
                    routeLine.setLatLngs(data.route);
                }
                bounds = bounds.concat(data.route);
            }
            var restaurant = point(data.restaurantLat, data.restaurantLng);
            if (restaurant) {
                if (!restaurantMarker) {
                    restaurantMarker = L.marker(restaurant, { icon: EduMap.icon("restaurant") })
                        .addTo(map)
                        .bindTooltip(data.restaurantName || "Ресторан");
                } else {
                    restaurantMarker.setLatLng(restaurant);
                }
                bounds.push(restaurant);
            }
            var destination = point(data.destinationLat, data.destinationLng);
            if (destination) {
                if (!destinationMarker) {
                    destinationMarker = L.marker(destination, { icon: EduMap.icon("home") })
                        .addTo(map)
                        .bindTooltip("Вы");
                } else {
                    destinationMarker.setLatLng(destination);
                }
                bounds.push(destination);
            }
            var courier = point(data.courierLat, data.courierLng);
            if (courier) {
                if (!courierMarker) {
                    courierMarker = L.marker(courier, { icon: EduMap.icon("courier"), zIndexOffset: 1000 })
                        .addTo(map);
                } else {
                    courierMarker.setLatLng(courier);
                }
                var label = data.status === "ON_THE_WAY" ? "Едет" : (data.status === "DELIVERED" ? "На месте" : "Ждёт заказ");
                courierMarker.bindTooltip(label);
                bounds.push(courier);
            }
            if (!map._fittedOnce && bounds.length) {
                fit(bounds);
                map._fittedOnce = true;
            } else if (courierMarker && data.status === "ON_THE_WAY") {
                map.panTo(courierMarker.getLatLng(), { animate: true, duration: 0.8 });
            }
        }

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
            if (courierName) {
                courierName.textContent = data.courierName;
            }
            if (payment) {
                var payBadge = data.paymentBadge || (data.paid ? "success" : "secondary");
                payment.textContent = data.paymentState;
                payment.className = "badge ms-1 bg-" + payBadge + (payBadge === "warning" ? " text-dark" : "");
            }
            if (cancelForm) {
                cancelForm.style.display = data.canCancel ? "inline" : "none";
            }
            if (mapHint) {
                mapHint.textContent = data.waitingForCourier
                    ? "Карта OpenStreetMap. Курьер ещё не взял заказ."
                    : "Карта OpenStreetMap: ресторан, вы и курьер по реальному маршруту.";
            }
            if (data.steps) {
                data.steps.forEach(function (step, index) {
                    if (steps[index]) {
                        steps[index].className = "track-step is-" + step.state;
                    }
                });
            }
            if (data.showMap) {
                ensureMap(data);
            }
            finished = data.finished || data.status === "CANCELLED" || !data.live;
            return finished;
        }

        var initial = {
            status: "${tracking.status}",
            statusLabel: "${tracking.statusLabel}",
            badge: "${tracking.badge}",
            message: "${tracking.message?js_string}",
            eta: "${tracking.eta?js_string}",
            progress: ${tracking.progress},
            live: ${tracking.live?c},
            finished: ${tracking.finished?c},
            canCancel: ${tracking.canCancel?c},
            paid: ${tracking.paid?c},
            paymentState: "${tracking.paymentState?js_string}",
            paymentBadge: "${tracking.paymentBadge!'secondary'}",
            courierName: "${tracking.courierName?js_string}",
            restaurantName: "${tracking.restaurantName?js_string}",
            restaurantLat: <#if tracking.restaurantLat??>${tracking.restaurantLat?c}<#else>null</#if>,
            restaurantLng: <#if tracking.restaurantLng??>${tracking.restaurantLng?c}<#else>null</#if>,
            destinationLat: <#if tracking.destinationLat??>${tracking.destinationLat?c}<#else>null</#if>,
            destinationLng: <#if tracking.destinationLng??>${tracking.destinationLng?c}<#else>null</#if>,
            courierLat: <#if tracking.courierLat??>${tracking.courierLat?c}<#else>null</#if>,
            courierLng: <#if tracking.courierLng??>${tracking.courierLng?c}<#else>null</#if>,
            route: [
                <#if tracking.route??>
                    <#list tracking.route as p>[${p[0]?c}, ${p[1]?c}]<#if p_has_next>,</#if></#list>
                </#if>
            ],
            centerLat: ${tracking.centerLat?c},
            centerLng: ${tracking.centerLng?c},
            showMap: ${tracking.showMap?c},
            waitingForCourier: ${tracking.waitingForCourier?c},
            steps: [
                <#list tracking.steps as step>
                { label: "${step.label?js_string}", state: "${step.state}" }<#if step_has_next>,</#if>
                </#list>
            ]
        };
        apply(initial);

        if (finished) {
            return;
        }

        var source = null;
        var pollTimer = null;

        function stopPoll() {
            if (pollTimer) {
                clearTimeout(pollTimer);
                pollTimer = null;
            }
        }

        function poll() {
            stopPoll();
            pollTimer = setTimeout(function () {
                fetch("/orders/" + orderId + "/tracking")
                    .then(function (response) { return response.json(); })
                    .then(function (data) {
                        if (!apply(data)) {
                            poll();
                        }
                    })
                    .catch(function () {
                        poll();
                    });
            }, 3000);
        }

        function connectSse() {
            if (!window.EventSource) {
                poll();
                return;
            }
            source = new EventSource("/orders/" + orderId + "/stream");
            source.addEventListener("tracking", function (event) {
                try {
                    var data = JSON.parse(event.data);
                    if (apply(data) && source) {
                        source.close();
                        source = null;
                    }
                } catch (e) {
                    // ignore malformed chunk
                }
            });
            source.onerror = function () {
                if (source) {
                    source.close();
                    source = null;
                }
                if (!finished) {
                    poll();
                }
            };
        }

        connectSse();
    })();
</script>
</@layout.layout>
