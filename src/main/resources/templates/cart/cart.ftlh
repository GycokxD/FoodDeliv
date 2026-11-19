<#import "../layout.ftlh" as layout>
<@layout.layout title="Корзина" active="cart">
<div class="container mt-5">
    <h2 class="mb-4"><i class="bi bi-cart3 me-2"></i>Ваша корзина</h2>

    <#if cartRestaurant??>
        <p class="text-muted">
            Ресторан:
            <a href="/restaurants/${cartRestaurantId}">${cartRestaurant}</a>
        </p>
    </#if>

    <#if mixedCart!false>
        <div class="alert alert-warning">
            <i class="bi bi-exclamation-triangle me-2"></i>
            В корзине блюда из разных ресторанов. Удалите лишние: один заказ можно оформить только из одного заведения.
        </div>
    </#if>

    <#if dishes?has_content>
        <div class="card mb-4 shadow-sm table-card">
            <div class="table-responsive">
                <table class="table align-middle mb-0 wrap-table" style="min-width: 720px">
                    <colgroup>
                        <col style="width: 36%">
                        <col style="width: 16%">
                        <col style="width: 22%">
                        <col style="width: 16%">
                        <col style="width: 10%">
                    </colgroup>
                    <thead>
                        <tr>
                            <th>Блюдо</th>
                            <th class="text-center">Цена</th>
                            <th class="text-center">Количество</th>
                            <th class="text-center">Сумма</th>
                            <th class="text-center">Удалить</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list dishes as dish>
                            <tr>
                                <td class="break-text">
                                    <div class="d-flex align-items-center gap-3">
                                        <img src="${dish.image!'/static/images/dish-placeholder.svg'}"
                                             alt="${dish.name}"
                                             style="width: 72px; height: 72px; object-fit: cover; border-radius: 12px;"
                                             onerror="this.src='/static/images/dish-placeholder.svg'">
                                        <div>
                                    <strong>${dish.name}</strong>
                                    <#if dish.restaurantName??>
                                        <div class="text-muted small">${dish.restaurantName}</div>
                                    </#if>
                                        </div>
                                    </div>
                                </td>
                                <td class="text-center col-nowrap">${dish.price?string["0.00"]} сом</td>
                                <td class="text-center col-actions">
                                    <div class="d-flex justify-content-center align-items-center">
                                        <#if (dish.quantity > 1)>
                                            <form method="post" action="/cart/decrease" class="me-2">
                                                <#if _csrf??>
                                                    <input type="hidden" name="${(_csrf.parameterName)!'csrf-param-name'}"
                                                           value="${(_csrf.token)!'csrf-token'}"/>
                                                </#if>
                                                <input type="hidden" name="dishId" value="${dish.id}">
                                                <button class="btn btn-sm btn-outline-secondary" type="submit">−</button>
                                            </form>
                                        </#if>
                                        <span>${dish.quantity}</span>
                                        <form method="post" action="/cart/increase" class="ms-2">
                                            <#if _csrf??>
                                                <input type="hidden" name="${(_csrf.parameterName)!'csrf-param-name'}"
                                                       value="${(_csrf.token)!'csrf-token'}"/>
                                            </#if>
                                            <input type="hidden" name="dishId" value="${dish.id}">
                                            <button class="btn btn-sm btn-outline-secondary" type="submit">+</button>
                                        </form>
                                    </div>
                                </td>
                                <td class="text-center col-nowrap">${dish.totalPrice?string["0.00"]} сом</td>
                                <td class="text-center col-actions">
                                    <form method="post" action="/cart/remove">
                                        <#if _csrf??>
                                            <input type="hidden" name="${(_csrf.parameterName)!'csrf-param-name'}"
                                                   value="${(_csrf.token)!'csrf-token'}"/>
                                        </#if>
                                        <input type="hidden" name="dishId" value="${dish.id}">
                                        <button class="btn btn-sm btn-outline-danger" type="submit">
                                            <i class="bi bi-trash"></i>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>

        <div class="d-flex justify-content-between align-items-center mb-4">
            <h4 class="mb-0">Итого: <span class="text-success fw-bold">${totalPrice?string["0.00"]} сом</span></h4>
        </div>

        <#if user??>
            <#if mixedCart!false>
            <#else>
                <div class="card shadow-sm mb-4">
                    <div class="card-body">
                        <h4 class="mb-3"><i class="bi bi-truck me-2"></i>Доставка</h4>
                        <#if checkoutError??>
                            <div class="alert alert-danger">${checkoutError}</div>
                        </#if>
                        <form id="checkoutForm" action="/orders/make-order" method="post" data-total="${totalPrice?string["0.00"]}">
                            <#if _csrf??>
                                <input type="hidden" name="${(_csrf.parameterName)!'csrf-param-name'}"
                                       value="${(_csrf.token)!'csrf-token'}"/>
                            </#if>
                            <div class="mb-3">
                                <label for="address" class="form-label">Адрес доставки</label>
                                <textarea class="form-control" id="address" name="address" rows="2" required
                                          placeholder="Город, улица, дом, квартира">${(checkout.address)!''}</textarea>
                            </div>
                            <div class="mb-3">
                                <label for="phone" class="form-label">Телефон</label>
                                <input class="form-control" id="phone" name="phone" required
                                       value="${(checkout.phone)!''}" placeholder="+996555123456">
                            </div>
                            <div class="mb-3">
                                <label for="note" class="form-label">Комментарий курьеру</label>
                                <textarea class="form-control" id="note" name="note" rows="2"
                                          placeholder="Домофон, этаж, пожелания">${(checkout.note)!''}</textarea>
                            </div>
                            <#assign pay = (checkout.paymentMethod!"CASH")?string>
                            <div class="mb-3">
                                <div class="form-label">Способ оплаты</div>
                                <div class="pay-grid">
                                    <label class="pay-option">
                                        <input type="radio" name="paymentMethod" value="CASH" <#if pay == "CASH">checked</#if>>
                                        <strong>Наличные</strong>
                                        <small>Курьер примет деньги у двери</small>
                                    </label>
                                    <label class="pay-option">
                                        <input type="radio" name="paymentMethod" value="CARD_COURIER" <#if pay == "CARD_COURIER">checked</#if>>
                                        <strong>Карта курьеру</strong>
                                        <small>Терминал при получении</small>
                                    </label>
                                    <label class="pay-option">
                                        <input type="radio" name="paymentMethod" value="CARD_ONLINE" <#if pay == "CARD_ONLINE">checked</#if>>
                                        <strong>Карта онлайн</strong>
                                        <small>Списание сразу, чек в заказе</small>
                                    </label>
                                </div>
                            </div>
                            <div id="cardFields" style="<#if pay == 'CARD_ONLINE'>display:block<#else>display:none</#if>">
                                <div class="bank-card" aria-hidden="true">
                                    <div class="bank-card__brand" id="cardBrandPreview">Учебный банк</div>
                                    <div class="bank-card__number" id="cardNumberPreview">•••• •••• •••• ••••</div>
                                    <div class="d-flex justify-content-between">
                                        <span id="cardHolderPreview">ИМЯ НА КАРТЕ</span>
                                        <span id="cardExpiryPreview">ММ/ГГ</span>
                                    </div>
                                </div>
                                <p class="text-muted small">Деньги не уходят в настоящий банк. Успешная карта <strong>4242 4242 4242 4242</strong> или <strong>5555 5555 5555 4444</strong>. Отклонение: <strong>4000 0000 0000 0002</strong>. Недостаточно средств: <strong>4000 0000 0000 9995</strong>. Срок любой будущий, CVV три цифры. CVV <strong>000</strong> банк не примет.</p>
                                <div class="mb-3">
                                    <label for="cardNumber" class="form-label">Номер карты</label>
                                    <input class="form-control" id="cardNumber" name="cardNumber" inputmode="numeric" autocomplete="cc-number" maxlength="19"
                                           value="${(checkout.cardNumber)!''}" placeholder="4242 4242 4242 4242">
                                </div>
                                <div class="mb-3">
                                    <label for="cardHolder" class="form-label">Владелец</label>
                                    <input class="form-control" id="cardHolder" name="cardHolder" autocomplete="cc-name"
                                           value="${(checkout.cardHolder)!''}" placeholder="IVAN IVANOV">
                                </div>
                                <div class="row">
                                    <div class="col-6 mb-3">
                                        <label for="cardExpiry" class="form-label">Срок</label>
                                        <input class="form-control" id="cardExpiry" name="cardExpiry" autocomplete="cc-exp" maxlength="5"
                                               value="${(checkout.cardExpiry)!''}" placeholder="12/28">
                                    </div>
                                    <div class="col-6 mb-3">
                                        <label for="cardCvv" class="form-label">CVV</label>
                                        <input class="form-control" id="cardCvv" name="cardCvv" inputmode="numeric" autocomplete="cc-csc" maxlength="3"
                                               value="${(checkout.cardCvv)!''}" placeholder="123">
                                    </div>
                                </div>
                            </div>
                            <p class="mb-3" id="payChoice">К оплате при получении: ${totalPrice?string["0.00"]} сом</p>
                            <button class="btn btn-primary btn-lg" id="checkoutButton" type="submit">
                                <i class="bi bi-bag-check me-1"></i> <span id="checkoutButtonText"><#if pay == "CARD_ONLINE">Оплатить ${totalPrice?string["0.00"]} сом<#else>Оформить заказ</#if></span>
                            </button>
                        </form>
                        <div id="payOverlay" class="pay-overlay" hidden>
                            <div class="text-center">
                                <div class="spinner-border mb-3" role="status"></div>
                                <div>Банк проверяет карту</div>
                            </div>
                        </div>
                    </div>
                </div>
            </#if>
        <#else>
            <a href="/auth/login?next=/cart" class="btn btn-outline-primary btn-lg mb-4">
                <i class="bi bi-person-check me-1"></i> Войти для оформления заказа
            </a>
        </#if>
    <#else>
        <div class="alert alert-info">
            <i class="bi bi-info-circle me-2"></i>Ваша корзина пуста. Перейдите в <a href="/restaurants" class="alert-link">список заведений</a>, чтобы выбрать блюда.
        </div>
    </#if>
</div>
<script>
    (function () {
        var form = document.getElementById("checkoutForm");
        var fields = document.getElementById("cardFields");
        var label = document.getElementById("checkoutButtonText");
        var radios = document.querySelectorAll('input[name="paymentMethod"]');
        var number = document.getElementById("cardNumber");
        var holder = document.getElementById("cardHolder");
        var expiry = document.getElementById("cardExpiry");
        var overlay = document.getElementById("payOverlay");
        if (!fields || !radios.length || !form) {
            return;
        }
        var total = form.getAttribute("data-total") || "";
        var payChoice = document.getElementById("payChoice");
        function brandOf(digits) {
            if (digits.indexOf("4") === 0) return "Visa";
            if (digits.indexOf("5") === 0) return "Mastercard";
            if (/^220[0-4]/.test(digits)) return "Мир";
            return "Учебный банк";
        }
        function paintCard() {
            var digits = (number && number.value || "").replace(/\D/g, "").slice(0, 16);
            if (number && number.value.replace(/\D/g, "") !== digits) {
                number.value = digits.replace(/(.{4})/g, "$1 ").trim();
            } else if (number) {
                number.value = digits.replace(/(.{4})/g, "$1 ").trim();
            }
            var preview = document.getElementById("cardNumberPreview");
            var brand = document.getElementById("cardBrandPreview");
            var holderPreview = document.getElementById("cardHolderPreview");
            var expiryPreview = document.getElementById("cardExpiryPreview");
            if (preview) {
                var shown = (digits + "••••••••••••••••").slice(0, 16);
                preview.textContent = shown.replace(/(.{4})/g, "$1 ").trim();
            }
            if (brand) brand.textContent = digits ? brandOf(digits) : "Учебный банк";
            if (holderPreview) holderPreview.textContent = (holder && holder.value.trim() || "ИМЯ НА КАРТЕ").toUpperCase();
            if (expiryPreview) expiryPreview.textContent = (expiry && expiry.value.trim() || "ММ/ГГ");
        }
        function sync() {
            var selected = document.querySelector('input[name="paymentMethod"]:checked');
            var online = selected && selected.value === "CARD_ONLINE";
            fields.style.display = online ? "block" : "none";
            if (label) {
                label.textContent = online ? ("Оплатить " + total + " сом") : "Оформить заказ";
            }
            if (payChoice && selected) {
                payChoice.textContent = online
                    ? ("Списание сейчас: " + total + " сом")
                    : ("К оплате при получении: " + total + " сом");
            }
        }
        radios.forEach(function (radio) {
            radio.addEventListener("change", sync);
        });
        if (number) number.addEventListener("input", paintCard);
        if (holder) holder.addEventListener("input", paintCard);
        if (expiry) expiry.addEventListener("input", function () {
            var digits = expiry.value.replace(/\D/g, "").slice(0, 4);
            expiry.value = digits.length > 2 ? digits.slice(0, 2) + "/" + digits.slice(2) : digits;
            paintCard();
        });
        form.addEventListener("submit", function (event) {
            var selected = document.querySelector('input[name="paymentMethod"]:checked');
            if (!selected || selected.value !== "CARD_ONLINE" || form.dataset.sending === "1") {
                return;
            }
            event.preventDefault();
            form.dataset.sending = "1";
            if (overlay) overlay.hidden = false;
            window.setTimeout(function () { form.submit(); }, 900);
        });
        sync();
        paintCard();
    })();
</script>
</@layout.layout>
