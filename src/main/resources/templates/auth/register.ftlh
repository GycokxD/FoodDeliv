<#import "../layout.ftlh" as layout>
<#import "/spring.ftl" as spring>
<@layout.layout title="Регистрация">
    <div class="auth-container">
        <div class="auth-header">
            <i class="bi bi-egg-fried"></i>
            <h2>Создать аккаунт</h2>
            <p>Зарегистрируйтесь, чтобы заказывать еду</p>
        </div>

        <#if error??>
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i> ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Закрыть"></button>
            </div>
        </#if>

        <form action="/auth/register" method="post">
            <#if _csrf??>
                <input type="hidden" name="${(_csrf.parameterName)!'csrf-param-name'}"
                       value="${(_csrf.token)!'csrf-token'}"/>
            </#if>
            <div class="mb-3">
                <label for="name" class="form-label">Имя</label>
                <div class="input-group">
                    <span class="input-group-text">
                        <i class="bi bi-person"></i>
                    </span>
                    <@spring.formInput "authDto.name" 'id = "name" class = "form-control" required'/>
                </div>
                <@spring.showErrors "<br>" "error text-danger"/>

            </div>

            <div class="mb-3">
                <label for="email" class="form-label">Электронная почта</label>
                <div class="input-group">
                    <span class="input-group-text">
                        <i class="bi bi-envelope"></i>
                    </span>
                    <@spring.formInput "authDto.email" 'id = "email" class = "form-control" required'/>
                </div>
                <@spring.showErrors "<br>" "error text-danger"/>
            </div>
            <div class="mb-3">
                <label for="password" class="form-label">Пароль</label>
                <div class="input-group">
                    <span class="input-group-text">
                        <i class="bi bi-lock"></i>
                    </span>
                    <@spring.formInput "authDto.password" 'id = "password" class = "form-control" required type = "password"'/>
                </div>
                <@spring.showErrors "<br>" "error text-danger"/>
            </div>
            <div class="mb-4">
                <label for="confirmPassword" class="form-label">Повторите пароль</label>
                <div class="input-group">
                    <span class="input-group-text">
                        <i class="bi bi-lock-fill"></i>
                    </span>
                    <@spring.formInput "authDto.confirmPassword" 'id = "confirmPassword" class = "form-control" required type = "password"'/>
                </div>
                <@spring.showErrors "<br>" "error text-danger"/>
            </div>
            <button class="w-100 btn btn-primary btn-lg" type="submit">
                <i class="bi bi-person-plus me-2"></i> Зарегистрироваться
            </button>
        </form>

        <div class="auth-footer">
            <p>Уже есть аккаунт? <a href="/auth/login">Войти</a></p>
        </div>
    </div>
</@layout.layout>
