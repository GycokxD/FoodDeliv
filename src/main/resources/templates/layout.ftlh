<#macro layout title="" active="">
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>EduFood - ${title!"Food Delivery"}</title>
    <link rel="icon" href="/static/favicon.svg" type="image/svg+xml">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
    <style>
        :root {
            --primary-color: #e23d3d;
            --secondary-color: #f0a202;
            --dark-color: #1c1714;
            --ink: #241c18;
            --paper: #f6f1ea;
            --line: #eadfd4;
            --light-color: #f6f1ea;
        }

        body {
            padding-top: 76px;
            background-color: var(--paper);
            color: var(--ink);
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        a {
            text-decoration: none;
        }

        .container a:not(.btn):not(.page-link):not(.nav-link):not(.navbar-brand):not(.dropdown-item) {
            color: var(--ink);
            text-decoration: none;
        }

        .container a:not(.btn):not(.page-link):not(.nav-link):not(.navbar-brand):not(.dropdown-item):hover {
            color: var(--primary-color);
        }

        .dropdown-item {
            text-decoration: none;
        }

        .container .auth-footer a,
        .container .auth-footer a:hover {
            color: var(--primary-color);
            font-weight: 700;
        }

        .restaurant-hero .restaurant-back,
        .restaurant-hero .restaurant-back:hover {
            color: #fff;
        }

        .navbar {
            background: var(--dark-color);
            box-shadow: 0 8px 24px rgba(28, 23, 20, 0.16);
        }

        .navbar-brand {
            color: #fff !important;
        }

        .navbar-brand i {
            color: var(--primary-color);
        }

            .navbar-brand {
            font-weight: 700;
            font-size: 1.5rem;
        }

        .order-pill {
            background: rgba(255, 255, 255, 0.22);
            border-radius: 999px;
            padding: 0.2rem 0.75rem !important;
        }

        .auth-container {
            max-width: 450px;
            margin: 2rem auto;
            padding: 2.5rem;
            background: white;
            border-radius: 15px;
            box-shadow: 0 5px 25px rgba(0, 0, 0, 0.08);
            border-top: 5px solid var(--primary-color);
        }

        .auth-header {
            text-align: center;
            margin-bottom: 2rem;
            color: var(--dark-color);
        }

        .auth-header h2 {
            font-weight: 700;
            margin-bottom: 0.5rem;
        }

        .auth-header .bi {
            font-size: 2.5rem;
            color: var(--primary-color);
            margin-bottom: 1rem;
        }

        .form-control, .form-select {
            border-radius: 12px;
            padding: 0.7rem 0.9rem;
            border: 1px solid var(--line);
            background: #fff;
        }

        .form-control:focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 0.25rem rgba(255, 107, 107, 0.25);
        }

        .btn-primary {
            background-color: var(--primary-color);
            border: none;
            border-radius: 8px;
            padding: 0.75rem;
            font-weight: 600;
            transition: all 0.3s;
        }

        .btn-primary:hover {
            background-color: #ff5252;
            transform: translateY(-2px);
        }

        .auth-footer {
            text-align: center;
            margin-top: 1.5rem;
            color: #666;
        }

        .auth-footer a {
            color: var(--primary-color);
            text-decoration: none;
            font-weight: 600;
        }

        footer {
            background: var(--dark-color);
            color: white;
            padding: 2rem 0;
            margin-top: 3rem;
        }

        .alert {
            border-radius: 8px;
        }

            .card {
                border-radius: 18px;
                border: 1px solid rgba(234, 223, 212, 0.9);
                box-shadow: 0 10px 28px rgba(36, 28, 24, 0.05);
            }

            .hover-lift {
                transition: transform 0.25s, box-shadow 0.25s;
            }

            .hover-lift:hover {
                transform: translateY(-4px);
                box-shadow: 0 16px 32px rgba(36, 28, 24, 0.1);
            }

            .card-img-top {
                border-top-left-radius: 12px;
                border-top-right-radius: 12px;
            }

            .pagination .page-item.active .page-link {
                background-color: var(--primary-color);
                border-color: var(--primary-color);
            }

            .pagination .page-link {
                color: var(--primary-color);
            }

            .pagination .page-item.active .page-link {
                color: white;
            }

            .add-to-cart {
                transition: all 0.2s;
            }

            .add-to-cart:hover {
                background-color: var(--primary-color) !important;
                color: white !important;
            }

            .menu-page {
                padding-bottom: 2rem;
            }

            .restaurant-hero {
                position: relative;
                border-radius: 20px;
                overflow: hidden;
                min-height: 220px;
                box-shadow: 0 12px 30px rgba(47, 53, 66, 0.12);
            }

            .restaurant-hero__media {
                position: absolute;
                inset: 0;
            }

            .restaurant-hero__media img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }

            .restaurant-hero__overlay {
                position: absolute;
                inset: 0;
                background: linear-gradient(120deg, rgba(47, 53, 66, 0.88) 0%, rgba(255, 107, 107, 0.55) 100%);
            }

            .restaurant-hero__content {
                position: relative;
                z-index: 1;
                padding: 1.75rem 2rem 2rem;
                color: #fff;
            }

            .restaurant-back {
                display: inline-flex;
                align-items: center;
                gap: 0.4rem;
                color: rgba(255, 255, 255, 0.85);
                text-decoration: none;
                font-size: 0.9rem;
                margin-bottom: 0.75rem;
                transition: color 0.2s;
            }

            .restaurant-back:hover {
                color: #fff;
            }

            .restaurant-hero__title {
                font-weight: 800;
                font-size: clamp(1.6rem, 3vw, 2.2rem);
                margin-bottom: 0.4rem;
                letter-spacing: -0.02em;
            }

            .restaurant-hero__meta,
            .restaurant-hero__count {
                margin: 0;
                opacity: 0.92;
                font-size: 0.95rem;
            }

            .restaurant-hero__count {
                margin-top: 0.5rem;
                display: inline-flex;
                align-items: center;
                gap: 0.4rem;
                background: rgba(255, 255, 255, 0.15);
                padding: 0.3rem 0.75rem;
                border-radius: 999px;
                backdrop-filter: blur(4px);
            }

            .menu-section-head h2 {
                font-weight: 700;
                color: var(--dark-color);
                margin-bottom: 0.25rem;
            }

            .menu-section-head p {
                color: #7f8c8d;
                margin: 0;
            }

            .dish-grid {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
                gap: 1.25rem;
            }

            .dish-card {
                background: #fff;
                border-radius: 18px;
                overflow: hidden;
                box-shadow: 0 6px 18px rgba(47, 53, 66, 0.06);
                border: 1px solid rgba(0, 0, 0, 0.04);
                display: flex;
                flex-direction: column;
                transition: transform 0.25s ease, box-shadow 0.25s ease;
                scroll-margin-top: 90px;
            }

            .dish-card:hover {
                transform: translateY(-4px);
                box-shadow: 0 14px 28px rgba(47, 53, 66, 0.12);
            }

            .dish-card__image {
                position: relative;
                height: 190px;
                overflow: hidden;
                background: #f5f5f5;
            }

            .dish-card__image img {
                width: 100%;
                height: 100%;
                object-fit: cover;
                transition: transform 0.4s ease;
            }

            .dish-card:hover .dish-card__image img {
                transform: scale(1.05);
            }

            .dish-card__price-badge {
                position: absolute;
                top: 12px;
                right: 12px;
                background: rgba(255, 255, 255, 0.95);
                color: var(--primary-color);
                font-weight: 800;
                font-size: 1rem;
                padding: 0.35rem 0.7rem;
                border-radius: 999px;
                box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
                line-height: 1.1;
            }

            .dish-card__price-badge small {
                font-weight: 600;
                font-size: 0.7rem;
                opacity: 0.85;
            }

            .dish-card__body {
                padding: 1.1rem 1.15rem 1.2rem;
                display: flex;
                flex-direction: column;
                flex: 1;
            }

            .dish-card__name {
                font-size: 1.1rem;
                font-weight: 700;
                color: var(--dark-color);
                margin: 0 0 0.45rem;
                line-height: 1.3;
            }

            .dish-card__desc {
                color: #6c757d;
                font-size: 0.9rem;
                line-height: 1.45;
                margin: 0 0 1rem;
                display: -webkit-box;
                -webkit-line-clamp: 2;
                -webkit-box-orient: vertical;
                overflow: hidden;
                min-height: 2.6em;
                flex: 1;
            }

            .dish-card__footer {
                display: flex;
                align-items: center;
                justify-content: space-between;
                gap: 0.75rem;
                padding-top: 0.85rem;
                border-top: 1px solid #f0f0f0;
            }

            .dish-card__price {
                display: flex;
                align-items: baseline;
                gap: 0.3rem;
            }

            .dish-card__price-value {
                font-size: 1.35rem;
                font-weight: 800;
                color: var(--dark-color);
                letter-spacing: -0.02em;
            }

            .dish-card__price-currency {
                font-size: 0.8rem;
                font-weight: 600;
                color: #95a5a6;
                text-transform: lowercase;
            }

            .dish-card__btn {
                display: inline-flex;
                align-items: center;
                gap: 0.4rem;
                background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
                color: #fff;
                border: none;
                border-radius: 999px;
                padding: 0.55rem 1rem;
                font-weight: 600;
                font-size: 0.88rem;
                cursor: pointer;
                transition: transform 0.2s, box-shadow 0.2s, filter 0.2s;
                white-space: nowrap;
            }

            .dish-card__btn:hover {
                transform: translateY(-1px);
                box-shadow: 0 6px 14px rgba(255, 107, 107, 0.35);
                filter: brightness(1.05);
            }

            .dish-card__btn:active {
                transform: translateY(0);
            }

            .live-search {
                position: relative;
                width: min(440px, 100%);
            }

            .live-search__icon {
                position: absolute;
                left: 0.9rem;
                top: 50%;
                transform: translateY(-50%);
                color: #ff6b6b;
                pointer-events: none;
            }

            .live-search input {
                width: 100%;
                border: none;
                border-radius: 999px;
                padding: 0.5rem 1rem 0.5rem 2.4rem;
                background: #fff;
                color: var(--dark-color);
                outline: none;
            }

            .live-search input:focus {
                box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.35);
            }

            .live-search-results {
                display: none;
                position: absolute;
                top: calc(100% + 8px);
                left: 0;
                right: 0;
                background: #fff;
                color: var(--dark-color);
                border-radius: 14px;
                box-shadow: 0 16px 40px rgba(47, 53, 66, 0.18);
                max-height: 440px;
                overflow: auto;
                z-index: 2000;
                padding: 0.35rem;
            }

            .live-search-results.is-open {
                display: block;
            }

            .live-search-results a,
            .live-search-empty {
                display: flex;
                gap: 0.75rem;
                align-items: center;
                padding: 0.55rem 0.65rem;
                border-radius: 10px;
                text-decoration: none;
                color: inherit;
            }

            .live-search-results a:hover {
                background: #fff5f5;
            }

            .live-search-results img {
                width: 48px;
                height: 48px;
                object-fit: cover;
                border-radius: 8px;
                flex-shrink: 0;
                background: #f3f3f3;
            }

            .live-search-results .hit-title {
                font-weight: 650;
                line-height: 1.2;
            }

            .live-search-results .hit-sub {
                color: #7f8c8d;
                font-size: 0.82rem;
            }

            .live-search-results .hit-type {
                display: inline-block;
                font-size: 0.68rem;
                font-weight: 700;
                letter-spacing: 0.02em;
                text-transform: uppercase;
                color: var(--primary-color);
            }

            .live-search-empty {
                color: #7f8c8d;
                justify-content: center;
            }

            .menu-search {
                position: relative;
                max-width: 420px;
            }

            .menu-search i {
                position: absolute;
                left: 0.9rem;
                top: 50%;
                transform: translateY(-50%);
                color: #ff6b6b;
            }

            .menu-search input {
                width: 100%;
                border: 1px solid #f0d0d0;
                border-radius: 999px;
                padding: 0.7rem 1rem 0.7rem 2.5rem;
                background: #fff;
                outline: none;
            }

            .menu-search input:focus {
                border-color: var(--primary-color);
                box-shadow: 0 0 0 0.2rem rgba(226, 61, 61, 0.15);
            }

            .menu-toolbar .menu-search {
                max-width: none;
            }

            .menu-toolbar {
                background: #fff;
                border: 1px solid var(--line);
                border-radius: 20px;
                padding: 1rem;
                box-shadow: 0 10px 28px rgba(36, 28, 24, 0.05);
                margin-bottom: 1.25rem;
            }

            .menu-filters {
                display: flex;
                flex-wrap: wrap;
                gap: 0.75rem 1rem;
                align-items: center;
                margin-top: 0.85rem;
            }

            .filter-pills {
                display: flex;
                flex-wrap: wrap;
                gap: 0.4rem;
            }

            .filter-pills label {
                margin: 0;
            }

            .filter-pills input {
                position: absolute;
                opacity: 0;
                pointer-events: none;
            }

            .filter-pills span {
                display: inline-flex;
                align-items: center;
                border: 1px solid var(--line);
                background: #fbf8f4;
                color: var(--ink);
                border-radius: 999px;
                padding: 0.4rem 0.8rem;
                font-size: 0.92rem;
                cursor: pointer;
            }

            .filter-pills input:checked + span,
            .filter-pills input:focus-visible + span {
                background: var(--dark-color);
                border-color: var(--dark-color);
                color: #fff;
            }

            .price-box {
                display: flex;
                align-items: center;
                gap: 0.4rem;
                background: #fbf8f4;
                border: 1px solid var(--line);
                border-radius: 999px;
                padding: 0.25rem 0.35rem 0.25rem 0.85rem;
            }

            .price-box input {
                width: 4.6rem;
                border: none;
                background: transparent;
                padding: 0.35rem 0;
                outline: none;
            }

            .pay-grid {
                display: grid;
                grid-template-columns: repeat(3, minmax(0, 1fr));
                gap: 0.6rem;
            }

            .pay-option {
                border: 1px solid var(--line);
                border-radius: 16px;
                padding: 0.85rem;
                background: #fff;
                cursor: pointer;
                height: 100%;
            }

            .pay-option input {
                margin-right: 0.4rem;
            }

            .pay-option strong {
                display: block;
            }

            .pay-option small {
                color: #7a6e66;
            }

            .pay-option:has(input:checked) {
                border-color: var(--primary-color);
                background: #fff6f4;
                box-shadow: inset 0 0 0 1px var(--primary-color);
            }

            .bank-card {
                background: linear-gradient(135deg, #2a211c, #5c2a24);
                color: #fff;
                border-radius: 18px;
                padding: 1.1rem 1.2rem;
                min-height: 148px;
                margin-bottom: 1rem;
            }

            .bank-card__brand {
                letter-spacing: 0.08em;
                font-size: 0.78rem;
                text-transform: uppercase;
                opacity: 0.8;
            }

            .bank-card__number {
                font-size: 1.25rem;
                letter-spacing: 0.12em;
                margin: 1.1rem 0 0.8rem;
                font-variant-numeric: tabular-nums;
            }

            .pay-overlay {
                position: fixed;
                inset: 0;
                background: rgba(28, 23, 20, 0.55);
                z-index: 1080;
                display: flex;
                align-items: center;
                justify-content: center;
                color: #fff;
            }

            .pay-overlay[hidden] {
                display: none !important;
            }

            @media (max-width: 720px) {
                .pay-grid {
                    grid-template-columns: 1fr;
                }
            }

            .profile-sheet {
                background: #fff;
                border: 1px solid var(--line);
                border-radius: 24px;
                padding: 1.4rem;
                box-shadow: 0 12px 32px rgba(36, 28, 24, 0.05);
            }

            .profile-head {
                display: flex;
                gap: 1.25rem;
                align-items: center;
                margin-bottom: 1.25rem;
            }

            .avatar-pick {
                position: relative;
                width: 112px;
                height: 112px;
                flex: 0 0 112px;
                cursor: pointer;
            }

            .avatar-pick img,
            .avatar-fallback {
                width: 112px;
                height: 112px;
                border-radius: 50%;
                object-fit: cover;
                display: grid;
                place-items: center;
                background: #f3e7df;
                color: var(--primary-color);
                font-size: 2.4rem;
                font-weight: 800;
            }

            .avatar-pick__cam {
                position: absolute;
                right: 0;
                bottom: 0;
                width: 34px;
                height: 34px;
                border-radius: 50%;
                background: var(--dark-color);
                color: #fff;
                display: grid;
                place-items: center;
                border: 3px solid #fff;
            }

            .profile-head h1 {
                font-size: 1.8rem;
                font-weight: 800;
                margin: 0 0 0.2rem;
            }

            .profile-meta {
                color: #7a6e66;
                margin: 0;
            }

            .profile-grid {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 0.8rem 1rem;
            }

            .profile-grid label,
            .password-fold label {
                display: block;
                font-size: 0.82rem;
                color: #7a6e66;
                margin-bottom: 0.3rem;
            }

            .password-fold {
                margin-top: 0.9rem;
                border-top: 1px solid var(--line);
                padding-top: 0.8rem;
            }

            .password-fold summary {
                cursor: pointer;
                font-weight: 700;
                list-style: none;
            }

            .password-fold summary::-webkit-details-marker {
                display: none;
            }

            .order-list {
                display: flex;
                flex-direction: column;
                gap: 0.7rem;
            }

            .order-row {
                display: flex;
                align-items: center;
                gap: 0.75rem;
                background: #fff;
                border: 1px solid var(--line);
                border-radius: 16px;
                padding: 0.35rem 0.35rem 0.35rem 0.9rem;
            }

            .order-row__main {
                flex: 1;
                display: grid;
                grid-template-columns: 5.5rem 1fr auto auto;
                gap: 0.6rem;
                align-items: center;
                padding: 0.55rem 0.4rem;
            }

            .container .order-row__main:hover {
                color: var(--ink);
            }

            .order-row__id {
                font-weight: 800;
            }

            @media (max-width: 720px) {
                .profile-head,
                .profile-grid,
                .order-row {
                    display: block;
                }

                .order-row__main {
                    grid-template-columns: 1fr 1fr;
                }

                .avatar-pick {
                    margin-bottom: 0.8rem;
                }
            }

            .stat-card {
                border: none;
                border-radius: 16px;
                box-shadow: 0 6px 18px rgba(47, 53, 66, 0.06);
            }

            .stat-card .value {
                font-size: 1.7rem;
                font-weight: 800;
                color: var(--dark-color);
            }

            .admin-nav {
                gap: 0.35rem;
                background: #fff;
                border: 1px solid var(--line);
                border-radius: 16px;
                padding: 0.4rem;
            }

            .admin-nav .nav-link {
                color: var(--dark-color);
                font-weight: 600;
                border-radius: 999px;
                white-space: nowrap;
            }

            .admin-nav .nav-link.active,
            .admin-nav .nav-link.active:hover {
                background: var(--dark-color);
                color: #fff;
            }

            .admin-panel {
                background: #fff;
                border: 1px solid var(--line);
                border-radius: 20px;
                overflow: hidden;
                box-shadow: 0 10px 28px rgba(36, 28, 24, 0.05);
            }

            .admin-panel__bar {
                display: flex;
                justify-content: space-between;
                align-items: center;
                gap: 1rem;
                padding: 0.85rem 1rem;
                background: #fbf8f4;
                border-bottom: 1px solid var(--line);
            }

            .admin-panel__bar input {
                max-width: 360px;
            }

            .admin-count {
                color: #7a6e66;
                font-size: 0.9rem;
                white-space: nowrap;
            }

            .admin-table {
                width: 100%;
                border-collapse: separate;
                border-spacing: 0;
                margin: 0;
            }

            .admin-table th {
                position: sticky;
                top: 0;
                z-index: 1;
                background: #f6f1ea;
                color: #7a6e66;
                font-size: 0.75rem;
                font-weight: 700;
                letter-spacing: 0.04em;
                text-transform: uppercase;
                padding: 0.8rem 0.95rem;
                border-bottom: 1px solid var(--line);
                white-space: nowrap;
            }

            .admin-table td {
                padding: 0.9rem 0.95rem;
                border-bottom: 1px solid #f0e6dc;
                vertical-align: middle;
                background: #fff;
            }

            .admin-table tbody tr:last-child td {
                border-bottom: none;
            }

            .admin-table tbody tr:hover td {
                background: #fff8f5;
            }

            .admin-table .stack {
                display: flex;
                flex-direction: column;
                gap: 0.1rem;
                min-width: 0;
            }

            .admin-table .sub {
                color: #7a6e66;
                font-size: 0.82rem;
            }

            .admin-table .clip {
                max-width: 280px;
            }

            .row-actions {
                display: flex;
                align-items: center;
                justify-content: flex-end;
                gap: 0.4rem;
                flex-wrap: nowrap;
            }

            .row-actions .form-select {
                width: auto;
                min-width: 8.5rem;
            }

            .admin-table .num {
                font-variant-numeric: tabular-nums;
                font-weight: 700;
                white-space: nowrap;
            }

            .table-card,
            .table-card .table-responsive {
                max-width: 100%;
            }

            .wrap-table {
                width: 100%;
                table-layout: fixed;
            }

            .table-responsive > .wrap-table {
                min-width: 720px;
            }

            .wrap-table th,
            .wrap-table td,
            .break-text,
            dd,
            .card-text,
            .dish-card__name,
            .dish-card__desc {
                overflow-wrap: anywhere;
                word-break: break-word;
            }

            .wrap-table .col-actions,
            .wrap-table .col-nowrap,
            .wrap-table .col-actions *,
            .wrap-table .col-nowrap * {
                overflow-wrap: normal;
                word-break: normal;
                white-space: nowrap;
            }

            .wrap-table .cell-controls {
                display: flex;
                align-items: center;
                gap: 0.5rem;
                flex-wrap: nowrap;
            }

            .wrap-table .cell-controls .form-select {
                width: auto;
                min-width: 7.5rem;
                flex: 1 1 auto;
            }

            .wrap-table .cell-controls .btn,
            .wrap-table .col-actions .btn {
                flex: 0 0 auto;
            }

            @media (max-width: 576px) {
                .restaurant-hero__content {
                    padding: 1.25rem;
                }

                .dish-card__footer {
                    flex-direction: column;
                    align-items: stretch;
                }

                .dish-card__btn {
                    justify-content: center;
                    width: 100%;
                }
            }
    </style>
</head>
<body class="d-flex flex-column min-vh-100">
    <nav class="navbar navbar-expand-lg navbar-dark fixed-top">
        <div class="container">
            <a class="navbar-brand" href="/">
                <i class="bi bi-egg-fried me-2"></i>EduFood
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarNav">
                <ul class="navbar-nav">
                    <li class="nav-item">
                        <a class="nav-link" href="/restaurants">
                            <i class="bi bi-shop me-1"></i> Рестораны
                        </a>
                    </li>
                    <#if isAdmin!false>
                        <li class="nav-item">
                            <a class="nav-link" href="/admin">
                                <i class="bi bi-speedometer2 me-1"></i> Панель
                            </a>
                        </li>
                    </#if>
                    <#if activeOrder??>
                        <li class="nav-item">
                            <a class="nav-link order-pill" href="/orders/${activeOrder.id}">
                                <i class="bi bi-bicycle me-1"></i> Заказ · ${activeOrder.label}
                            </a>
                        </li>
                    </#if>
                </ul>
                <div class="live-search mx-lg-auto my-2 my-lg-0">
                    <i class="bi bi-search live-search__icon"></i>
                    <input id="liveSearchInput" type="search" placeholder="Ресторан или блюдо" autocomplete="off" aria-label="Поиск ресторанов и блюд">
                    <div id="liveSearchResults" class="live-search-results" role="listbox"></div>
                </div>
                <ul class="navbar-nav">
                    <#if currentUser??>
                        <li class="nav-item dropdown">
                                                    <a class="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown">
                                                        <#if currentUser.avatar?? && currentUser.avatar != "">
                                                            <img src="/uploads/avatars/${currentUser.avatar}"
                                                                 class="rounded-circle"
                                                                 style="width: 30px; height: 30px; object-fit: cover;">
                                                        <#else>
                                                            <i class="bi bi-person-circle"></i>
                                                        </#if>
                                                    </a>
                                                    <ul class="dropdown-menu dropdown-menu-end">
                                                        <#if isAdmin!false>
                                                            <li><a class="dropdown-item" href="/admin"><i class="bi bi-speedometer2 me-2"></i>Панель</a></li>
                                                        </#if>
                                                        <li><a class="dropdown-item" href="/profile"><i class="bi bi-person me-2"></i>Профиль</a></li>
                                                        <li><hr class="dropdown-divider"></li>
                                                        <li>
                                                            <form action="/logout" method="post" class="mb-0">
                                                                <#if _csrf??>
                                                                    <input type="hidden" name="${(_csrf.parameterName)!'csrf-param-name'}"
                                                                           value="${(_csrf.token)!'csrf-token'}"/>
                                                                </#if>
                                                                <button type="submit" class="dropdown-item">
                                                                    <i class="bi bi-box-arrow-right me-2"></i>Выйти
                                                                </button>
                                                            </form>
                                                        </li>
                                                    </ul>
                                                </li>
                    <#else>
                        <li class="nav-item">
                            <a class="nav-link" href="/auth/login?next=${loginNext?url}">
                                <i class="bi bi-box-arrow-in-right me-1"></i> Войти
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="/auth/register">
                                <i class="bi bi-person-plus me-1"></i> Регистрация
                            </a>
                        </li>
                    </#if>
                    <li class="nav-item">
                        <a class="nav-link" href="/cart">
                            <i class="bi bi-cart3 me-1"></i> Корзина
                            <span class="badge bg-primary">${cartCount!0}</span>
                        </a>
                    </li>
                </ul>
            </div>
        </div>
    </nav>

    <div class="container flex-grow-1">
        <#if message??>
            <div class="alert alert-${messageType!"info"} alert-dismissible fade show mt-3" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Закрыть"></button>
            </div>
        </#if>

        <#nested>
    </div>

    <footer>
        <div class="container">
            <div class="row">
                <div class="col-md-6">
                    <h5><i class="bi bi-egg-fried me-2"></i>EduFood</h5>
                    <p>Заказывайте еду из лучших ресторанов и кафе <#if appSettings?? && appSettings.city?? && appSettings.city?has_content>${appSettings.city}<#else>вашего города</#if>.</p>
                    <#if appSettings?? && appSettings.supportPhone?? && appSettings.supportPhone?has_content>
                        <p class="mb-0">Поддержка: ${appSettings.supportPhone}</p>
                    </#if>
                </div>
                <div class="col-md-6 text-md-end">
                    <p>&copy; 2026 EduFood. Все права защищены.</p>
                </div>
            </div>
        </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.querySelectorAll("[data-table-filter]").forEach(function (input) {
            var panel = input.closest(".admin-panel");
            if (!panel) return;
            var rows = panel.querySelectorAll("tbody tr");
            var count = panel.querySelector("[data-table-count]");
            function apply() {
                var query = input.value.trim().toLowerCase();
                var shown = 0;
                rows.forEach(function (row) {
                    var match = !query || row.innerText.toLowerCase().indexOf(query) !== -1;
                    row.hidden = !match;
                    if (match) shown++;
                });
                if (count) count.textContent = shown + " из " + rows.length;
            }
            input.addEventListener("input", apply);
            apply();
        });
    </script>
    <script>
        (function () {
            var input = document.getElementById("liveSearchInput");
            var box = document.getElementById("liveSearchResults");
            if (!input || !box) {
                return;
            }
            var timer = null;
            var requestId = 0;

            function esc(value) {
                return String(value == null ? "" : value).replace(/[&<>"']/g, function (ch) {
                    return ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" })[ch];
                });
            }

            function closeResults() {
                box.classList.remove("is-open");
                box.innerHTML = "";
            }

            function render(hits) {
                if (!hits.length) {
                    box.innerHTML = '<div class="live-search-empty">Ничего не найдено</div>';
                    box.classList.add("is-open");
                    return;
                }
                box.innerHTML = hits.map(function (hit) {
                    var kind = hit.type === "dish" ? "Блюдо" : "Ресторан";
                    return '<a href="' + esc(hit.url) + '">' +
                        '<img src="' + esc(hit.image) + '" alt="">' +
                        '<span><span class="hit-type">' + kind + '</span>' +
                        '<span class="hit-title d-block">' + esc(hit.title) + '</span>' +
                        '<span class="hit-sub d-block">' + esc(hit.subtitle) + '</span></span></a>';
                }).join("");
                box.classList.add("is-open");
            }

            input.addEventListener("input", function () {
                var q = input.value.trim();
                clearTimeout(timer);
                if (q.length < 2) {
                    closeResults();
                    return;
                }
                timer = setTimeout(function () {
                    var current = ++requestId;
                    fetch("/restaurants/search?q=" + encodeURIComponent(q))
                        .then(function (response) { return response.json(); })
                        .then(function (hits) {
                            if (current !== requestId) {
                                return;
                            }
                            render(hits);
                        })
                        .catch(function () {
                            if (current === requestId) {
                                closeResults();
                            }
                        });
                }, 220);
            });

            input.addEventListener("keydown", function (event) {
                if (event.key === "Escape") {
                    closeResults();
                }
            });

            document.addEventListener("click", function (event) {
                if (!event.target.closest(".live-search")) {
                    closeResults();
                }
            });
        })();
    </script>
</body>
</html>
</#macro>