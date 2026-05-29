<?php
const APP_TOKEN_TTL_SECONDS = 86400;

function app_secret(): string
{
    return getenv('APP_SECRET') ?: 'finance-tracker-local-secret-change-me';
}
