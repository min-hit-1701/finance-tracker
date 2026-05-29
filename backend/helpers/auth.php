<?php
require_once __DIR__ . '/../config/app.php';
require_once __DIR__ . '/response.php';
require_once __DIR__ . '/request.php';

function base64url_encode_string(string $value): string
{
    return rtrim(strtr(base64_encode($value), '+/', '-_'), '=');
}

function base64url_decode_string(string $value): string|false
{
    $padding = strlen($value) % 4;
    if ($padding > 0) {
        $value .= str_repeat('=', 4 - $padding);
    }

    return base64_decode(strtr($value, '-_', '+/'), true);
}

function issue_token(array $user): string
{
    $header = ['alg' => 'HS256', 'typ' => 'JWT'];
    $payload = [
        'id' => (int) $user['id'],
        'email' => $user['email'],
        'iat' => time(),
        'exp' => time() + APP_TOKEN_TTL_SECONDS,
    ];

    $encodedHeader = base64url_encode_string(json_encode($header));
    $encodedPayload = base64url_encode_string(json_encode($payload));
    $signature = hash_hmac('sha256', "$encodedHeader.$encodedPayload", app_secret(), true);

    return "$encodedHeader.$encodedPayload." . base64url_encode_string($signature);
}

function bearer_token(): ?string
{
    $header = $_SERVER['HTTP_AUTHORIZATION'] ?? $_SERVER['REDIRECT_HTTP_AUTHORIZATION'] ?? '';

    if (!$header && function_exists('getallheaders')) {
        $headers = getallheaders();
        $header = $headers['Authorization'] ?? $headers['authorization'] ?? '';
    }

    if (preg_match('/Bearer\s+(.+)/i', $header, $matches)) {
        return trim($matches[1]);
    }

    return null;
}

function verify_token(?string $token): ?array
{
    if (!$token) {
        return null;
    }

    $parts = explode('.', $token);
    if (count($parts) !== 3) {
        return null;
    }

    [$encodedHeader, $encodedPayload, $encodedSignature] = $parts;
    $expectedSignature = base64url_encode_string(hash_hmac('sha256', "$encodedHeader.$encodedPayload", app_secret(), true));

    if (!hash_equals($expectedSignature, $encodedSignature)) {
        return null;
    }

    $payloadJson = base64url_decode_string($encodedPayload);
    $payload = $payloadJson === false ? null : json_decode($payloadJson, true);

    if (!is_array($payload) || empty($payload['id']) || empty($payload['exp']) || time() > (int) $payload['exp']) {
        return null;
    }

    return $payload;
}

function require_auth_user(array $data): array
{
    $payload = verify_token(bearer_token());
    if (!$payload) {
        json_response(false, 'Unauthorized. Please login first', null, 401);
    }

    $requestedUserId = input_int($data, 'userId');
    if ($requestedUserId !== null && $requestedUserId !== (int) $payload['id']) {
        json_response(false, 'Forbidden. userId does not match logged in user', null, 403);
    }

    return [
        'id' => (int) $payload['id'],
        'email' => $payload['email'] ?? null,
    ];
}
