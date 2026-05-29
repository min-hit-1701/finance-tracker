<?php
function request_data(): array
{
    $raw = file_get_contents('php://input');
    $json = json_decode($raw, true);

    if (is_array($json)) {
        return array_merge($_GET, $_POST, $json);
    }

    parse_str($raw, $parsed);
    if (is_array($parsed) && count($parsed) > 0) {
        return array_merge($_GET, $_POST, $parsed);
    }

    return array_merge($_GET, $_POST);
}

function input_string(array $data, string $key, ?string $default = null): ?string
{
    if (!array_key_exists($key, $data) || $data[$key] === null) {
        return $default;
    }

    return trim((string) $data[$key]);
}

function input_int(array $data, string $key, ?int $default = null): ?int
{
    if (!array_key_exists($key, $data) || $data[$key] === '' || $data[$key] === null) {
        return $default;
    }

    return (int) $data[$key];
}

function input_float(array $data, string $key, ?float $default = null): ?float
{
    if (!array_key_exists($key, $data) || $data[$key] === '' || $data[$key] === null) {
        return $default;
    }

    return (float) $data[$key];
}

function input_bool(array $data, string $key, ?bool $default = null): ?bool
{
    if (!array_key_exists($key, $data) || $data[$key] === '' || $data[$key] === null) {
        return $default;
    }

    return filter_var($data[$key], FILTER_VALIDATE_BOOLEAN, FILTER_NULL_ON_FAILURE) ?? ((int) $data[$key] === 1);
}

function require_user_id(array $data): int
{
    $userId = input_int($data, 'userId');
    if (!$userId) {
        json_response(false, 'userId is required', null, 422);
    }

    return $userId;
}
