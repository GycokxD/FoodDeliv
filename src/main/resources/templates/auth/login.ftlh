<#import "../layout.ftlh" as layout>
<@layout.layout title="Вход">
    <div class="auth-container">
        <div class="auth-header">
            <i class="bi bi-egg-fried"></i>
            <h2>С возвращением</h2>
            <p>Войдите, чтобы заказать еду</p>
        </div>

        <#if error??>
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Закрыть"></button>
            </div>
        </#if>

        <form action="/auth/login" method="post">
            <#if _csrf??>
                <input type="hidden" name="${(_csrf.parameterName)!'csrf-param-name'}"
                       value="${(_csrf.token)!'csrf-token'}"/>
            </#if>
            <#if next?? && next?has_content>
                <input type="hidden" name="next" value="${next}">
            </#if>
            <div class="mb-3">
                <label for="email" class="form-label">Электронная почта</label>
                <div class="input-group">
                    <span class="input-group-text">
                        <i class="bi bi-envelope"></i>
                    </span>
                    <input type="email" class="form-control" id="email" name="email" placeholder="ivan@mail.com" required>
                </div>
            </div>

            <div class="mb-4">
                <label for="password" class="form-label">Пароль</label>
                <div class="input-group">
                    <span class="input-group-text">
                        <i class="bi bi-lock"></i>
                    </span>
                    <input type="password" class="form-control" id="password" name="password" placeholder="Введите пароль" required>
                </div>
            </div>

            <button class="w-100 btn btn-primary btn-lg" type="submit">
                <i class="bi bi-box-arrow-in-right me-2"></i> Войти
            </button>
        </form>

        <div class="auth-footer">
            <p>Нет аккаунта? <a href="/auth/register">Зарегистрироваться</a></p>
        </div>
    </div>
</@layout.layout>
