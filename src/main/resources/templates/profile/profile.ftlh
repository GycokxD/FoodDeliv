<#import "../layout.ftlh" as layout>
<#assign rawName = (user.name!"?")?trim>
<#assign initial = rawName?has_content?then(rawName?substring(0,1)?upper_case, "?")>
<@layout.layout title="Профиль">
<div class="profile-sheet mt-4">
    <#if profileError??>
        <div class="alert alert-danger">${profileError}</div>
    </#if>
    <div class="profile-head">
        <form method="post" action="/profile/avatar" enctype="multipart/form-data">
            <#if _csrf??>
                <input type="hidden" name="${(_csrf.parameterName)!'csrf-param-name'}" value="${(_csrf.token)!'csrf-token'}"/>
            </#if>
            <label class="avatar-pick">
                <#if user.avatar?? && user.avatar != "">
                    <img src="/uploads/avatars/${user.avatar}" alt="${user.name!''}">
                <#else>
                    <span class="avatar-fallback">${initial}</span>
                </#if>
                <span class="avatar-pick__cam" title="Сменить фото"><i class="bi bi-camera"></i></span>
                <input type="file" name="file" accept="image/*" hidden onchange="this.form.submit()">
            </label>
        </form>
        <div>
            <h1>${user.name!''}</h1>
            <p class="profile-meta">${user.email}</p>
            <p class="profile-meta">С нами с ${createdAtFormatted!"—"}</p>
        </div>
    </div>

    <form method="post" action="/profile">
        <#if _csrf??>
            <input type="hidden" name="${(_csrf.parameterName)!'csrf-param-name'}" value="${(_csrf.token)!'csrf-token'}"/>
        </#if>
        <div class="profile-grid">
            <div>
                <label for="name">Имя</label>
                <input class="form-control" id="name" name="name" required value="${user.name!''}">
            </div>
            <div>
                <label for="phone">Телефон</label>
                <input class="form-control" id="phone" name="phone" value="${user.phone!''}" placeholder="+996555123456">
            </div>
        </div>
        <details class="password-fold">
            <summary>Сменить пароль</summary>
            <div class="profile-grid mt-3">
                <div>
                    <label for="currentPassword">Текущий пароль</label>
                    <input class="form-control" id="currentPassword" name="currentPassword" type="password" autocomplete="current-password">
                </div>
                <div>
                    <label for="newPassword">Новый пароль</label>
                    <input class="form-control" id="newPassword" name="newPassword" type="password" autocomplete="new-password">
                </div>
                <div>
                    <label for="confirmPassword">Повторите пароль</label>
                    <input class="form-control" id="confirmPassword" name="confirmPassword" type="password" autocomplete="new-password">
                </div>
            </div>
        </details>
        <button class="btn btn-primary mt-3" type="submit">Сохранить</button>
    </form>
</div>

<h2 class="mt-4 mb-3">Заказы</h2>
<#if orders?has_content>
    <div class="order-list mb-4">
        <#list orders as order>
            <article class="order-row">
                <a class="order-row__main" href="/orders/${order.id}">
                    <span class="order-row__id">#${order.id}</span>
                    <span>${order.totalPrice?string["0.00"]} сом<#if order.paymentState??><span class="d-block text-muted small">${order.paymentState}</span></#if></span>
                    <span class="text-muted">${order.createdAtFormatted!""}</span>
                    <span class="badge bg-${order.statusBadge}">${order.statusLabel}</span>
                </a>
                <form action="/orders/${order.id}/repeat" method="post" class="mb-0">
                    <#if _csrf??>
                        <input type="hidden" name="${(_csrf.parameterName)!'csrf-param-name'}" value="${(_csrf.token)!'csrf-token'}"/>
                    </#if>
                    <button class="btn btn-sm btn-outline-primary" type="submit">Повторить</button>
                </form>
            </article>
        </#list>
    </div>
<#else>
    <div class="alert alert-info mb-4">Заказов пока нет. Выберите ресторан и соберите корзину.</div>
</#if>
</@layout.layout>
